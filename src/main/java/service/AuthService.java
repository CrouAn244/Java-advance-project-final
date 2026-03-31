package service;

import model.enums.Role;
import model.User;
import util.PasswordHash;
import util.Validator;

public class AuthService {
	private final UserService userService;

	public AuthService() {
		this(new UserService());
	}

	public AuthService(UserService userService) {
		if (userService == null) {
			throw new IllegalArgumentException("UserService khong duoc null.");
		}
		this.userService = userService;
		seedDefaultAccounts();
	}

	public User registerEmployee(String username, String password, String fullName, String phone, String email) {
		String validatedUsername = Validator.validateUsername(username);
		String validatedPassword = Validator.validatePassword(password);
		String validatedFullName = Validator.validateFullName(fullName);
		String validatedPhone = Validator.validatePhone(phone);
		String validatedEmail = Validator.validateEmail(email);

		if (userService.existsUsername(validatedUsername)) {
			throw new IllegalArgumentException("Ten dang nhap da ton tai.");
		}
		if (userService.existsEmail(validatedEmail)) {
			throw new IllegalArgumentException("Email da ton tai.");
		}

		User user = new User();
		user.setUsername(validatedUsername);
		user.setPassword(PasswordHash.hashPassword(validatedPassword));
		user.setFullName(validatedFullName);
		user.setPhone(validatedPhone);
		user.setEmail(validatedEmail);
		user.setRole(Role.EMPLOYEE);
		return userService.createUser(user);
	}

	public User login(String usernameOrEmail, String password) {
		String identifier = Validator.requireNotBlank(usernameOrEmail, "Ten dang nhap hoac email");
		String rawPassword = Validator.requireNotBlank(password, "Mat khau");

		User user = userService.findByIdentifier(identifier)
				.orElseThrow(() -> new IllegalArgumentException("Ten dang nhap, email hoac mat khau khong dung."));

		if (!PasswordHash.verifyPassword(rawPassword, user.getPassword())) {
			throw new IllegalArgumentException("Ten dang nhap, email hoac mat khau khong dung.");
		}
		return user;
	}

	public boolean isUsernameAvailable(String username) {
		String validatedUsername = Validator.validateUsername(username);
		return !userService.existsUsername(validatedUsername);
	}

	public boolean isEmailAvailable(String email) {
		String validatedEmail = Validator.validateEmail(email);
		return !userService.existsEmail(validatedEmail);
	}

	private void seedDefaultAccounts() {
		createDefaultUserIfMissing(
				"admin",
				"Admin@123",
				"System Admin",
				"+84900000001",
				"admin@company.local",
				Role.ADMIN
		);

		createDefaultUserIfMissing(
				"support",
				"Support@123",
				"Support Team",
				"+84900000002",
				"support@company.local",
				Role.SUPPORT
		);
	}

	private void createDefaultUserIfMissing(
			String username,
			String password,
			String fullName,
			String phone,
			String email,
			Role role
	) {
		if (userService.existsUsername(username)) {
			return;
		}

		User user = new User();
		user.setUsername(username);
		user.setPassword(PasswordHash.hashPassword(password));
		user.setFullName(fullName);
		user.setPhone(phone);
		user.setEmail(email);
		user.setRole(role);
		userService.createUser(user);
	}
}
