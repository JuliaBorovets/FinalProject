package ua.training.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ua.training.controller.exception.BankTransactionException;
import ua.training.controller.exception.OrderCreateException;
import ua.training.controller.exception.OrderNotFoundException;
import ua.training.dto.OrderDTO;
import ua.training.entity.order.*;
import ua.training.entity.user.User;
import ua.training.repository.OrderRepository;
import ua.training.repository.UserRepository;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Service
@PropertySource("classpath:constants.properties")
public class OrderService {

    @Autowired
    private EntityManager entityManager;

    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private UserService userService;
    private ConverterService converterService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, UserService userService,
                        ConverterService converterService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.converterService = converterService;
    }

    @Value("${constants.BASE.PRICE}")
    Integer BASE_PRICE;

    @Value("${constants.COEFFICIENT}")
    Double COEFFICIENT;

    public List<OrderDTO> findAllUserOrder(Long userId) {
        return orderRepository.findOrderByOwnerId(userId)
                .stream()
                .map(order -> OrderDTO.builder()
                        .dtoId(order.getId())
                        .dtoOrderType(order.getOrderType())
                        .dtoDestination(order.getDestination())
                        .dtoOrderStatus(order.getOrderStatus())
                        .dtoOwner(order.getOwner())
                        .dtoWeight(order.getWeight())
                        .shippingDate(order.getShippingDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                                .withLocale(LocaleContextHolder.getLocale())))
                        .deliveryDate(isShipped(order) ? order.getDeliveryDate().
                                format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                                        .withLocale(LocaleContextHolder.getLocale())) : " ")
                        .dtoShippingPrice(isLocaleUa() ? order.getShippingPriceUkr() : order.getShippingPriceEn())
                        .build())
                .collect(Collectors.toList());
    }

    public List<OrderDTO> findAllPaidOrdersDTO() {

        return orderRepository
                .findOrderByOrderStatus(OrderStatus.PAID)
                .stream()
                .map(order -> OrderDTO.builder()
                        .dtoId(order.getId())
                        .dtoOrderType(order.getOrderType())
                        .dtoDestination(order.getDestination())
                        .dtoOrderStatus(order.getOrderStatus())
                        .dtoOwner(order.getOwner())
                        .dtoWeight(order.getWeight())
                        .shippingDate(order.getShippingDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                                .withLocale(LocaleContextHolder.getLocale())))
                        .dtoShippingPrice(isLocaleUa() ? order.getShippingPriceUkr() : order.getShippingPriceEn())
                        .build())
                .collect(Collectors.toList());

    }


    private boolean isLocaleUa() {
        return LocaleContextHolder.getLocale().equals(new Locale("uk"));
    }


    public void createOrder(OrderDTO orderDTO, User user) throws OrderCreateException {

        Order order = Order.builder()
                .destination(orderDTO.getDtoDestination())
                .orderType(orderDTO.getDtoOrderType())
                .orderStatus(OrderStatus.NOT_PAID)
                .shippingDate(LocalDate.now())
                .weight(orderDTO.getDtoWeight())
                .owner(user)
                .shippingPriceUkr(calculatePrice(orderDTO))
                .shippingPriceEn(converterService.convertPriceToLocale(calculatePrice(orderDTO), "en"))
                .build();
        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            throw new OrderCreateException("Can not create order");
        }
    }


    public BigDecimal calculatePrice(OrderDTO orderDTO) {
        return BigDecimal.valueOf(BASE_PRICE + (orderDTO.getDtoDestination().getPriceForDestination()
                + orderDTO.getDtoOrderType().getPriceForType()) * COEFFICIENT);
    }

    private Long getAdminAccount() {
        User admin = userRepository.findByLogin("admin")
                .orElseThrow(() -> new UsernameNotFoundException("no admin"));

        return admin.getId();
    }


    public Order getOrderById(Long id) throws OrderNotFoundException {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("order " + id + " not found"));
    }

    public boolean isPaid(Order order) {

        return order.getOrderStatus().equals(OrderStatus.PAID);
    }

    public boolean isShipped(Order order) {

        return order.getOrderStatus().equals(OrderStatus.SHIPPED);
    }


    private void orderSetShippedStatus(Long id) throws OrderNotFoundException {
        Order order = getOrderById(id);
        if (isPaid(order)) {
            order.setOrderStatus(OrderStatus.SHIPPED);
            order.setDeliveryDate(LocalDate.now().plusDays(order.getDestination().getDay()));
            orderRepository.save(order);
        }

    }

    public void orderToShip() throws OrderNotFoundException {
        List<OrderDTO> orders = findAllPaidOrdersDTO();

        for (OrderDTO o : orders) {
            orderSetShippedStatus(o.getDtoId());
        }

    }

    public Page<OrderDTO> findPaginated(Pageable pageable, List<OrderDTO> orders) {

        List<OrderDTO> sortedOrders = orders
                .stream()
                .sorted(Comparator.comparing(OrderDTO::getDtoId)
                        .reversed())
                .collect(Collectors.toList());

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<OrderDTO> list;

        if (sortedOrders.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, orders.size());
            list = sortedOrders.subList(startItem, toIndex);
        }

        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), orders.size());
    }


    public void addAmount(Long id, BigDecimal amount) throws BankTransactionException {
        User account = userService.findUserById(id);
        BigDecimal newBalance = account.getBalance().add(converterService.convertPriceFromLocale(amount, localeName()));
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BankTransactionException("no money");
        }
        account.setBalance(newBalance);
        userRepository.save(account);
    }


    public void payForOrder(Order order) throws BankTransactionException {
        if (!isShipped(order) && !isPaid(order)) {
            BigDecimal amount = isLocaleUa() ? order.getShippingPriceUkr() : order.getShippingPriceEn();
            sendMoney(order.getOwner().getId(), getAdminAccount(), amount);
            order.setOrderStatus(OrderStatus.PAID);
            orderRepository.save(order);
        } else throw new BankTransactionException("order is already paid");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
            rollbackFor = BankTransactionException.class)
    void sendMoney(Long fromAccountId, Long toAccountId, BigDecimal amount) throws BankTransactionException {
        addAmount(fromAccountId, amount.negate());
        addAmount(toAccountId, amount);

    }

    private String localeName() {
        return LocaleContextHolder.getLocale().toString();
    }

}

