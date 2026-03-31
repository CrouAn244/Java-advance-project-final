package dao;

import java.util.List;
import java.util.Optional;
import model.enums.Role;
import model.User;

public interface IUserDAO {
	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByUsernameOrEmail(String identifier);

	List<User> findAll();

	List<User> findByRole(Role role);

	User save(User user);
}
