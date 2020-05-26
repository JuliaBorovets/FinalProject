package ua.training.converters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.training.dto.*;
import ua.training.entity.order.*;
import ua.training.entity.user.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DtoToOrderConverterTest {

    @Mock
    DtoToOrderTypeConverter dtoToOrderTypeConverter;

    @Mock
    DtoToUserConverter dtoToUserConverter;

    @Mock
    DtoToDestinationConverter dtoToDestinationConverter;

    @Mock
    DtoToServiceConverter dtoToServiceConverter;

    @Mock
    DtoToCheckConverter dtoToCheckConverter;

    @InjectMocks
    DtoToOrderConverter converter;

    final Long ORDER_ID = 11L;
    final Long USER_ID = 1L;
    final Long SERVICE_ID = 2L;
    final Long CHECK_ID = 3L;
    final Long DESTINATION_ID = 4L;
    final Long ORDER_TYPE_ID = 5L;

    @Test
    void convert() {

        Service service = Service.builder().id(SERVICE_ID).build();
        User user = User.builder().id(USER_ID).build();
        OrderCheck check = OrderCheck.builder().id(CHECK_ID).build();
        Destination destination = Destination.builder().id(DESTINATION_ID).build();
        OrderType orderType = OrderType.builder().id(ORDER_TYPE_ID).build();

        OrderDto orderDto = new OrderDto();
        orderDto.setId(ORDER_ID);
        orderDto.setOwner(UserDto.builder().id(USER_ID).build());
        orderDto.getServices().add(ServiceDto.builder().id(SERVICE_ID).build());
        orderDto.setCheck(OrderCheckDto.builder().id(CHECK_ID).build());
        orderDto.setDestination(DestinationDto.builder().id(DESTINATION_ID).build());
        orderDto.setOrderType(OrderTypeDto.builder().id(ORDER_TYPE_ID).build());

        when(dtoToServiceConverter.convert(any(ServiceDto.class))).thenReturn(service);
        when(dtoToUserConverter.convert(any(UserDto.class))).thenReturn(user);
        when(dtoToCheckConverter.convert(any(OrderCheckDto.class))).thenReturn(check);
        when(dtoToDestinationConverter.convert(any(DestinationDto.class))).thenReturn(destination);
        when(dtoToOrderTypeConverter.convert(any(OrderTypeDto.class))).thenReturn(orderType);

        Order order = converter.convert(orderDto);

        assert order != null;
        assertEquals(orderDto.getId(), order.getId());
        assertEquals(orderDto.getServices().size(), order.getServices().size());
        assertEquals(orderDto.getOrderType().getId(), order.getOrderType().getId());
        assertEquals(orderDto.getOwner().getId(), order.getOwner().getId());
        assertEquals(orderDto.getDestination().getId(), order.getDestination().getId());
        assertEquals(orderDto.getCheck().getId(), order.getCheck().getId());
    }
}