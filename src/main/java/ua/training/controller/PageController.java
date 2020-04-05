package ua.training.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ua.training.controller.exception.OrderCreateException;
import ua.training.controller.exception.OrderNotFoundException;
import ua.training.controller.exception.RegException;
import ua.training.dto.*;
import ua.training.entity.user.User;
import ua.training.service.CalculatorService;
import ua.training.service.OrderService;
import ua.training.service.UserService;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Controller
public class PageController implements WebMvcConfigurer {

    private final UserService userService;
    private final OrderService orderService;

    @Value("${pagination.user.shipments.size}")
    Integer USER_SHIPMENTS_SIZE;

    @Value("${pagination.admin.size}")
    Integer ADMIN_SHIPMENTS_SIZE;

    @Value("${first.page.number}")
    Integer FIRST_PAGE;

    @Autowired
    public PageController(UserService userService, OrderService orderService, CalculatorService calculatorService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @RequestMapping("/")
    public String mainPage() {
        return "index";
    }


    @RequestMapping("/login")
    public String loginPage() {
        return "login";
    }


    @RequestMapping("/account_page")
    public String accountPage(Model model, @AuthenticationPrincipal User user) {
        insertBalanceInfo(user, model);
        return "account_page";
    }

    @RequestMapping("/success")
    public String localRedirect() {
        return "redirect:/account_page";
    }


    @GetMapping("/reg")
    public String registerUser(@ModelAttribute("newUser") UserDTO user, Model model) {

        model.addAttribute("newOrder", user == null ? new UserDTO() : user);
        return "registration";
    }

    @PostMapping("/reg")
    public String newUser(@ModelAttribute("newUser") @Valid UserDTO modelUser) throws RegException {
        userService.saveNewUser(modelUser);
        log.info("new user registration");
        return "redirect:/login";
    }


    @GetMapping("/my_shipments/page/{page}")
    public String shipmentsPage(Model model, @AuthenticationPrincipal User user,
                                @PathVariable("page") int page) {

        insertBalanceInfo(user, model);

        List<OrderDTO> orders = orderService.findAllUserOrder(user.getId());

        insertPaginatedOrders(page, USER_SHIPMENTS_SIZE, model, orders);

        return "my_shipments";
    }

    @GetMapping(value = "/adding_money")
    public String addMoneyPage(@AuthenticationPrincipal User user, Model model) {
        insertBalanceInfo(user, model);
        return "adding_money";
    }


    @GetMapping("/admin_page/page/{page}")
    public String calculatePage(@AuthenticationPrincipal User user, Model model,
                                @PathVariable("page") int page) {

        if (!user.getRole().name().equals("ROLE_ADMIN")) {
            return "redirect:/account_page";
        }


        insertBalanceInfo(user, model);
        List<OrderDTO> orders = orderService.findAllPaidOrdersDTO();

        insertPaginatedOrders(page, ADMIN_SHIPMENTS_SIZE, model, orders);

        return "admin_page";
    }

    @GetMapping("/calculator")
    public String calculatePage(@ModelAttribute OrderDTO modelOrder) {
        return "calculator";
    }

    @GetMapping("/create")
    public String createOrder(@ModelAttribute("newOrder") OrderDTO order, @AuthenticationPrincipal User user,
                              Model model) {
        insertBalanceInfo(user, model);

        model.addAttribute("newOrder", order == null ? new OrderDTO() : order);

        return "new_order";
    }

    @PostMapping("/create")
    public String newOrder(@ModelAttribute OrderDTO modelOrder, @AuthenticationPrincipal User user) throws OrderCreateException {

        orderService.createOrder(modelOrder, user);
        log.info("new order creation");

        return "redirect:/my_shipments/page/1";

    }

    @PostMapping(value = "/to_ship")
    public String adminPage(@AuthenticationPrincipal User user, Model model,
                            @PageableDefault Pageable pageable) throws OrderNotFoundException {
        orderService.orderToShip();

        log.info("shipping orders");
        return "redirect:/admin_page/page/1";

    }


    private void insertBalanceInfo(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("info", userService.listBankAccountInfo(user.getId()));
    }


    private void insertPaginatedOrders(int page, int size, Model model, List<OrderDTO> orders) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<OrderDTO> articlePage = orderService.findPaginated(pageable, orders);

        int totalPages = articlePage.getTotalPages();
        List<Integer> pageNumbers = IntStream.rangeClosed(FIRST_PAGE, totalPages).boxed().collect(Collectors.toList());
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("orders", articlePage.getContent());
    }

}


