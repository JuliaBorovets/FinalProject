package ua.training.entity.order;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString

@Entity
@Table(name = "destination")
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cityFrom;

    private String cityTo;

    private Long daysToDeliver;

    private BigDecimal kilometers;

    private BigDecimal priceOnCentsForKilometer;

    @OneToMany(mappedBy = "destination")
    private List<Order> orders;
}