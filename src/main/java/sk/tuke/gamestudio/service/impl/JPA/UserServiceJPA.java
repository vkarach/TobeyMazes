package sk.tuke.gamestudio.service.impl.JPA;

import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.repository.UserRepository;
import sk.tuke.gamestudio.service.UserService;

import java.util.Optional;

@Service
public class UserServiceJPA implements UserService {
    private final UserRepository userRepository;

    public UserServiceJPA(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean userExists(String userName) {
        return userRepository.existsByName(userName);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public int createUser(String userName, String password, String email) {
        User user = new User(userName, password, email);
        userRepository.save(user);
        return user.getId();
    }

    @Override
    public void deleteUserByName(String userName) {
        userRepository.deleteByName(userName);
    }

    @Override
    public Integer getUserIdByName(String userName) {
        Optional<User> user = userRepository.getUserByName(userName);
        return user.map(User::getId).orElse(null);
    }

    @Override
    public String getUserNameById(int userId) {
        Optional<User> user = userRepository.getUserById(userId);
        return user.map(User::getName).orElse(null);
    }

    @Override
    public String getPasswordByUserId(int userId) {
        Optional<User> user = userRepository.getUserById(userId);
        return user.map(User::getPasswordHash).orElse(null);
    }

    @Override
    public String getEmailByUserId(int userId) {
        Optional<User> user = userRepository.getUserById(userId);
        return user.map(User::getEmail).orElse(null);
    }

    @Override
    public void changePassword(int userId, String newPassword) {
        userRepository.updatePassword(userId, newPassword);
    }
}
