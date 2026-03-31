package presentation;

import java.util.ArrayList;
import java.util.List;
import model.Booking;
import model.enums.BookingStatus;
import model.User;
import presentation.admin.AdminEquipmentManagementUI;
import presentation.admin.AdminRoomManagementUI;
import presentation.admin.AdminServiceManagementUI;
import service.AdminService;
import util.ConsoleHelper;
import util.DateTimeUtil;
import util.TablePrinter;
import util.Validator;

public class AdminUI {
	private final AdminService adminService;
	private final AdminRoomManagementUI roomManagementUI;
	private final AdminEquipmentManagementUI equipmentManagementUI;
	private final AdminServiceManagementUI serviceManagementUI;

	public AdminUI() {
		this.adminService = new AdminService();
		this.roomManagementUI = new AdminRoomManagementUI(adminService);
		this.equipmentManagementUI = new AdminEquipmentManagementUI(adminService);
		this.serviceManagementUI = new AdminServiceManagementUI(adminService);
	}

	// Menu tong cho tai khoan quan tri.
	public void run(User user) {
		boolean running = true;
		while (running) {
			System.out.println();
			System.out.println(ConsoleHelper.ANSI_CYAN + "================= MENU QUAN TRI - " + user.getFullName() + " =================" + ConsoleHelper.ANSI_RESET);
			System.out.println("1. Quan ly phong hop");
			System.out.println("2. Quan ly thiet bi di dong");
			System.out.println("3. Quan ly nguoi dung (tao Support)");
			System.out.println("4. Duyet/Tu choi yeu cau dat phong");
			System.out.println("5. Quan ly dich vu di kem");
			System.out.println("6. Xem danh sach nguoi dung");
			System.out.println("0. Dang xuat");
			System.out.println(ConsoleHelper.ANSI_CYAN + "==================================================" + ConsoleHelper.ANSI_RESET);

			int choice = ConsoleHelper.promptIntInRange("Chon chuc nang: ", 0, 6);
			switch (choice) {
				case 1:
					roomManagementUI.run();
					break;
				case 2:
					equipmentManagementUI.run();
					break;
				case 3:
					handleSupportAccountCreation();
					break;
				case 4:
					handleBookingApproval();
					break;
				case 5:
					serviceManagementUI.run();
					break;
				case 6:
					showUsers();
					break;
				case 0:
					running = false;
					break;
				default:
					break;
			}
		}
	}

	// Chuc nang 3: Tao tai khoan support.
	private void handleSupportAccountCreation() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= TAO TAI KHOAN SUPPORT =================" + ConsoleHelper.ANSI_RESET);
		String username = ConsoleHelper.promptWithValidation("Ten dang nhap: ", value -> {
			String validated = Validator.validateUsername(value);
			if (!adminService.isUsernameAvailable(validated)) {
				throw new IllegalArgumentException("Ten dang nhap da ton tai.");
			}
			return validated;
		});
		String password = promptValidatedPassword("Mat khau: ");
		String fullName = ConsoleHelper.promptWithValidation("Ho ten: ", Validator::validateFullName);
		String phone = ConsoleHelper.promptWithValidation("So dien thoai: ", Validator::validatePhone);
		String email = ConsoleHelper.promptWithValidation("Email: ", value -> {
			String validated = Validator.validateEmail(value);
			if (!adminService.isEmailAvailable(validated)) {
				throw new IllegalArgumentException("Email da ton tai.");
			}
			return validated;
		});

		try {
			User support = adminService.createSupportAccount(username, password, fullName, phone, email);
			System.out.println("Tao tai khoan Support thanh cong. Ma tai khoan: " + support.getId());
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Tao tai khoan Support that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	// Chuc nang 4: Duyet/tu choi yeu cau dat phong va phan cong support.
	private void handleBookingApproval() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= DUYET / TU CHOI YEU CAU DAT PHONG =================" + ConsoleHelper.ANSI_RESET);
		List<Booking> bookings = adminService.getAllBookings();
		if (bookings.isEmpty()) {
			System.out.println("Chua co yeu cau dat phong nao.");
			ConsoleHelper.waitForEnter();
			return;
		}

		showBookings(bookings);
		int bookingId = ConsoleHelper.promptPositiveInt("Nhap ID booking can xu ly: ");
		Booking target = findBookingById(bookings, bookingId);
		if (target == null) {
			System.out.println("Khong tim thay booking voi ID " + bookingId + ".");
			ConsoleHelper.waitForEnter();
			return;
		}
		if (target.getStatus() != BookingStatus.PENDING) {
			System.out.println("Booking nay khong con o trang thai PENDING de xu ly.");
			ConsoleHelper.waitForEnter();
			return;
		}

		System.out.println("1. Duyet va phan cong Support");
		System.out.println("2. Tu choi");
		int action = ConsoleHelper.promptIntInRange("Chon chuc nang: ", 1, 2);

		try {
			if (action == 1) {
				List<User> supportUsers = adminService.getSupportUsers();
				if (supportUsers.isEmpty()) {
					System.out.println("Khong co tai khoan Support nao de phan cong.");
					ConsoleHelper.waitForEnter();
					return;
				}

				showSupportUsers(supportUsers);
				int supportId = ConsoleHelper.promptPositiveInt("Nhap ID Support de phan cong: ");
				adminService.approveBooking(bookingId, supportId);
				System.out.println("Da duyet booking va phan cong Support thanh cong.");
			} else {
				adminService.rejectBooking(bookingId);
				System.out.println("Da tu choi booking thanh cong.");
			}
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Xu ly booking that bai: " + e.getMessage());
		}

		ConsoleHelper.waitForEnter();
	}

	// Chuc nang 6: Hien thi danh sach nguoi dung he thong.
	private void showUsers() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= DANH SACH NGUOI DUNG =================" + ConsoleHelper.ANSI_RESET);
		List<User> users = adminService.getAllUsers();
		if (users.isEmpty()) {
			System.out.println("Chua co nguoi dung nao.");
		} else {
			List<String[]> rows = new ArrayList<>();
			for (User item : users) {
				rows.add(new String[]{
						String.valueOf(item.getId()),
						item.getUsername(),
						item.getRole() == null ? "" : item.getRole().name(),
						item.getFullName() == null ? "" : item.getFullName()
				});
			}
			TablePrinter.printTable(
					new String[]{"ID", "Username", "Role", "Ho ten"},
					new int[]{4, 20, 9, 24},
					rows
			);
		}
		ConsoleHelper.waitForEnter();
	}

	private void showBookings(List<Booking> bookings) {
		List<String[]> rows = new ArrayList<>();
		for (Booking booking : bookings) {
			rows.add(new String[]{
					String.valueOf(booking.getId()),
					String.valueOf(booking.getUserId()),
					String.valueOf(booking.getRoomId()),
					DateTimeUtil.formatUserDateTime(booking.getStartTime()),
					DateTimeUtil.formatUserDateTime(booking.getEndTime()),
					booking.getStatus().name(),
					booking.getSupportStaffId() == null ? "" : String.valueOf(booking.getSupportStaffId()),
					booking.getPreparationStatus().name()
			});
		}

		TablePrinter.printTable(
				new String[]{"Booking", "Employee", "Phong", "Bat dau", "Ket thuc", "Trang thai", "Support", "Preparation"},
				new int[]{8, 8, 6, 16, 16, 10, 7, 12},
				rows
		);
	}

	private Booking findBookingById(List<Booking> bookings, int bookingId) {
		for (Booking booking : bookings) {
			if (booking.getId() != null && booking.getId() == bookingId) {
				return booking;
			}
		}
		return null;
	}

	private void showSupportUsers(List<User> supportUsers) {
		List<String[]> rows = new ArrayList<>();
		for (User support : supportUsers) {
			rows.add(new String[]{
					String.valueOf(support.getId()),
					support.getFullName(),
					support.getUsername(),
					support.getEmail() == null ? "" : support.getEmail()
			});
		}

		TablePrinter.printTable(
				new String[]{"ID", "Ho ten", "Username", "Email"},
				new int[]{4, 24, 20, 28},
				rows
		);
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
}
