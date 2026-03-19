package sk.tuke.gamestudio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sk.tuke.gamestudio.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByName(String userName);
    boolean existsByEmail(String email);
    void deleteByName(String name);
    Optional<User> getUserByName(String name);
    Optional<User> getUserById(Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.passwordHash = :password WHERE u.id = :id")
    void updatePassword(@Param("id") int id, @Param("password") String password);
}
