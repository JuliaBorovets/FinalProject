package ua.training.service.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ua.training.controller.exception.RegException;
import ua.training.controller.utility.ProjectPasswordEncoder;
import ua.training.dto.UserDto;
import ua.training.entity.user.RoleType;
import ua.training.entity.user.User;
import ua.training.mappers.UserMapper;
import ua.training.repository.UserRepository;
import ua.training.service.UserService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService, UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(login));
    }


    @Override
    public User findUserById(Long id) {
        return userRepository
                .findUserById(id)
                .orElseThrow(() -> new UsernameNotFoundException("user with id " + id + " not found"));
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW,
            rollbackFor = RegException.class)
    @Override
    public void saveNewUserDto(UserDto userDto) throws RegException {

        User user = userMapper.userDtoToUser(userDto);
        ProjectPasswordEncoder encoder = new ProjectPasswordEncoder();

        user.setPassword(encoder.encode(userDto.getPassword()));

        try {
            userRepository.save(Objects.requireNonNull(user));
        } catch (DataIntegrityViolationException e) {
            throw new RegException("scan not save user with id = " + user.getId());
        }

    }


    @Override
    public UserDto findUserDTOById(Long id) {

        return userMapper.userToUserDto(findUserById(id));
    }

    @Override
    public List<UserDto> findAllByLoginLike(String login) {
        return userRepository.findAllByLoginLike("%" + login + "%").stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> findAllUserDto() {
        return userRepository.findAll().stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void changeRole(Long userId) {
        User user = findUserById(userId);

        if (user.getRole().equals(RoleType.ROLE_ADMIN)){
            user.setRole(RoleType.ROLE_USER);
        } else {
            user.setRole(RoleType.ROLE_ADMIN);
        }
        userRepository.save(user);
    }
}
