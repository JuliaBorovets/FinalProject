package ua.training.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.training.entity.user.BankCard;
import ua.training.entity.user.User;

import java.util.List;

@Repository
public interface BankCardRepository extends CrudRepository<BankCard, Long> {

    List<BankCard> findBankCardByUsers(User user);

}
