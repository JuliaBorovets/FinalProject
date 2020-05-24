package ua.training.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ua.training.dto.OrderCheckDto;
import ua.training.entity.order.OrderCheck;

@Component
public class DtoToCheckConverter implements Converter<OrderCheckDto, OrderCheck> {
    @Override
    public OrderCheck convert(OrderCheckDto orderCheckDto) {
        return null;
    }
}
