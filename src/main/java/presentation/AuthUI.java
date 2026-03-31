package presentation;

import model.enums.Role;
import model.User;
import service.AuthService;
import util.ConsoleHelper;
import util.Validator;

public class AuthUI {
	private final AuthService authService;
	private final AdminUI adminUI;
	private final EmployeeUI employeeUI;
	private final SupportUI supportUI;

	public AuthUI() {
		this.authService = new AuthService();
		this.adminUI = new AdminUI();
		this.employeeUI = new EmployeeUI();
		this.supportUI = new SupportUI();
	}

	public void run() {
		boolean running = true;
		while (running) {
			System.out.println();
			System.out.println(ConsoleHelper.ANSI_CYAN + "================= MENU XAC THUC =================" + ConsoleHelper.ANSI_RESET);
			System.out.println("1. Dang ky (Nhan vien)");
			System.out.println("2. Dang nhap");
			System.out.println("0. Thoat");
			System.out.println(ConsoleHelper.ANSI_CYAN + "==================================================" + ConsoleHelper.ANSI_RESET);

			int choice = askChoice(0, 2);
			switch (choice) {
				case 1:
					handleRegister();
					break;
				case 2:
					handleLogin();
					break;
				case 0:
					running = false;
					break;
				default:
					break;
			}
		}
		System.out.println("Tam biet.");
	}

	private void handleRegister() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= DANG KY NHAN VIEN =================" + ConsoleHelper.ANSI_RESET);
		String username = ConsoleHelper.promptWithValidation("Ten dang nhap: ", value -> {
			String validated = Validator.validateUsername(value);
			if (!authService.isUsernameAvailable(validated)) {
				throw new IllegalArgumentException("Ten dang nhap da ton tai.");
			}
			return validated;
		});
		String password = promptValidatedPassword("Mat khau: ");
		String fullName = ConsoleHelper.promptWithValidation("Ho ten: ", Validator::validateFullName);
		String phone = ConsoleHelper.promptWithValidation("So dien thoai: ", Validator::validatePhone);
		String email = ConsoleHelper.promptWithValidation("Email: ", value -> {
			String validated = Validator.validateEmail(value);
			if (!authService.isEmailAvailable(validated)) {
				throw new IllegalArgumentException("Email da ton tai.");
			}
			return validated;
		});

		try {
			User user = authService.registerEmployee(username, password, fullName, phone, email);
			System.out.println("Dang ky thanh cong. Ma tai khoan: " + user.getId());
		} catch (IllegalArgumentException e) {
			System.out.println("Dang ky that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void handleLogin() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= DANG NHAP =================" + ConsoleHelper.ANSI_RESET);
		String identifier = ConsoleHelper.promptWithValidation(
				"Ten dang nhap hoac email: ",
				value -> Validator.requireNotBlank(value, "Ten dang nhap hoac email")
		);
		String password = Validator.requireNotBlank(ConsoleHelper.promptPassword("Mat khau: "), "Mat khau");

		try {
			User user = authService.login(identifier, password);
			System.out.println("Dang nhap thanh cong. Vai tro: " + mapRoleLabel(user.getRole()));
			routeByRole(user);
		} catch (IllegalArgumentException e) {
			System.out.println("Dang nhap that bai: " + e.getMessage());
			ConsoleHelper.waitForEnter();
		}
	}

	private void routeByRole(User user) {
		Role role = user.getRole();
		if (role == null) {
			System.out.println("Tai khoan nay chua duoc gan vai tro.");
			ConsoleHelper.waitForEnter();
			return;
		}

		switch (role) {
			case ADMIN:
				adminUI.run(user);
				break;
			case SUPPORT:
				supportUI.run(user);
				break;
			case EMPLOYEE:
				employeeUI.run(user);
				break;
			default:
				System.out.println("Vai tro khong duoc ho tro: " + role);
				ConsoleHelper.waitForEnter();
				break;
		}
	}

	private String mapRoleLabel(Role role) {
		switch (role) {
			case ADMIN:
				return "Quan tri";
			case SUPPORT:
				return "Ho tro";
			case EMPLOYEE:
				return "Nhan vien";
			default:
				return role.name();
		}
	}

	private String promptValidatedPassword(String label) {
		while (true) {
			try {
				return Validator.validatePassword(ConsoleHelper.promptPassword(label));
			} catch (IllegalArgumentException e) {
				System.out.println("Canh bao: " + e.getMessage());
			}
		}
	}

	private int askChoice(int min, int max) {
		return ConsoleHelper.promptIntInRange("Chon chuc nang: ", min, max);
	}
}
