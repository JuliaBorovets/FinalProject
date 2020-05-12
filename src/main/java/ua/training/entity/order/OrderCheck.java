package ua.training.entity.order;

import lombok.*;
import ua.training.entity.user.BankCard;
import ua.training.entity.user.User;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString

@Entity
@Table(name = "order_check")
public class OrderCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "check")
    private Order order;

    private BigDecimal priceInCents;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private Status status;

    @ManyToOne
    private User user;

    @ManyToOne
    private BankCard bankCard;


}
