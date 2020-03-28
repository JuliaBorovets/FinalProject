package ua.training.dto;

import lombok.*;
import ua.training.entity.user.RoleType;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {

    Long id;
    @NotNull
    String firstName;

    @NotNull
    String firstNameCyr;

    @NotNull
    String lastName;

    @NotNull
    String lastNameCyr;

    @NotNull
    String login;

    @Email
    String email;

    @NotNull
    String password;

    @NotNull
    RoleType roleType;

    BigDecimal balance;

}