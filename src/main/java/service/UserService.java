package service;

import dao.IUserDAO;
import dao.impl.UserDAOImpl;
import java.util.List;
import java.util.Optional;
import model.Enum.Role;
import model.User;

public class UserService {
	private final IUserDAO userDAO;

	public UserService() {
		this.userDAO = new UserDAOImpl();
	}

	public Optional<User> findByIdentifier(String identifier) {
		return userDAO.findByUsernameOrEmail(identifier);
	}

	public boolean existsUsername(String username) {
		return userDAO.findByUsername(username).isPresent();
	}

	public boolean existsEmail(String email) {
		return userDAO.findByEmail(email).isPresent();
	}

	public List<User> getAllUsers() {
		return userDAO.findAll();
	}

	public List<User> findByRole(Role role) {
		return userDAO.findByRole(role);
	}

	public User createUser(User user) {
		return userDAO.save(user);
	}
}
