package presentation;

import java.util.ArrayList;
import java.util.List;
import model.Booking;
import model.Equipment;
import model.Enum.BookingStatus;
import model.Room;
import model.User;
import util.DateTimeUtil;
import service.AdminService;
import util.ConsoleHelper;
import util.Validator;

public class AdminUI {
	private final AdminService adminService;

	public AdminUI() {
		this.adminService = new AdminService();
	}

	public void run(User user) {
		boolean running = true;
		while (running) {
			MenuHelper.printHeader("MENU QUAN TRI - " + user.getFullName());
			MenuHelper.printOptions(
					"1. Quan ly phong hop",
					"2. Quan ly thiet bi di dong",
					"3. Quan ly nguoi dung (tao Support)",
					"4. Duyet/Tu choi yeu cau dat phong",
					"0. Dang xuat"
			);

			int choice = MenuHelper.askChoice(0, 4);
			switch (choice) {
				case 1:
					handleRoomManagement();
					break;
				case 2:
					handleEquipmentManagement();
					break;
				case 3:
					handleSupportAccountCreation();
					break;
				case 4:
					handleBookingApproval();
					break;
				case 0:
					running = false;
					break;
				default:
					break;
			}
		}
	}

	private void handleRoomManagement() {
		boolean running = true;
		while (running) {
			MenuHelper.printHeader("QUAN LY PHONG HOP");
			MenuHelper.printOptions(
					"1. Xem danh sach phong",
					"2. Them phong",
					"3. Sua phong",
					"4. Xoa phong",
					"0. Quay lai"
			);

			int choice = MenuHelper.askChoice(0, 4);
			switch (choice) {
				case 1:
					showRooms();
					break;
				case 2:
					createRoom();
					break;
				case 3:
					updateRoom();
					break;
				case 4:
					deleteRoom();
					break;
				case 0:
					running = false;
					break;
				default:
					break;
			}
		}
	}

	private void handleEquipmentManagement() {
		boolean running = true;
		while (running) {
			MenuHelper.printHeader("QUAN LY THIET BI DI DONG");
			MenuHelper.printOptions(
					"1. Xem danh sach thiet bi",
					"2. Cap nhat so luong kha dung",
					"0. Quay lai"
			);

			int choice = MenuHelper.askChoice(0, 2);
			switch (choice) {
				case 1:
					showEquipments();
					break;
				case 2:
					updateEquipmentAvailableQuantity();
					break;
				case 0:
					running = false;
					break;
				default:
					break;
			}
		}
	}

	private void handleSupportAccountCreation() {
		MenuHelper.printHeader("TAO TAI KHOAN SUPPORT");
		String username = ConsoleHelper.promptWithValidation("Ten dang nhap: ", value -> {
			String validated = Validator.validateUsername(value);
			if (!adminService.isUsernameAvailable(validated)) {
				throw new IllegalArgumentException("Ten dang nhap da ton tai.");
			}
			return validated;
		});
		String password = ConsoleHelper.promptWithValidation("Mat khau: ", Validator::validatePassword);
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

	private void handleBookingApproval() {
		MenuHelper.printHeader("DUYET / TU CHOI YEU CAU DAT PHONG");
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
		int action = MenuHelper.askChoice(1, 2);

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

	private void showRooms() {
		MenuHelper.printHeader("DANH SACH PHONG HOP");
		List<Room> rooms = adminService.getAllRooms();
		if (rooms.isEmpty()) {
			System.out.println("Chua co phong hop nao.");
		} else {
			List<String[]> rows = new ArrayList<>();
			for (Room room : rooms) {
				rows.add(new String[]{
						String.valueOf(room.getId()),
						room.getName(),
						String.valueOf(room.getCapacity()),
						room.getLocation(),
						room.getDescription()
				});
			}
			MenuHelper.printTable(
					new String[]{"ID", "Ten phong", "Suc chua", "Vi tri", "Mo ta"},
					rows
			);
		}
		ConsoleHelper.waitForEnter();
	}

	private void createRoom() {
		MenuHelper.printHeader("THEM PHONG HOP");
		String name = ConsoleHelper.promptWithValidation("Ten phong: ", value -> Validator.requireNotBlank(value, "Ten phong"));
		int capacity = ConsoleHelper.promptPositiveInt("Suc chua: ");
		String location = ConsoleHelper.promptWithValidation("Vi tri: ", value -> Validator.requireNotBlank(value, "Vi tri"));
		String description = ConsoleHelper.promptWithValidation("Mo ta: ", value -> Validator.requireNotBlank(value, "Mo ta"));

		try {
			Room room = adminService.createRoom(name, capacity, location, description);
			System.out.println("Them phong hop thanh cong. ID phong: " + room.getId());
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Them phong hop that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void updateRoom() {
		MenuHelper.printHeader("SUA PHONG HOP");
		int id = promptExistingRoomId("Nhap ID phong can sua: ");
		String name = ConsoleHelper.promptWithValidation("Ten phong moi: ", value -> Validator.requireNotBlank(value, "Ten phong"));
		int capacity = ConsoleHelper.promptPositiveInt("Suc chua moi: ");
		String location = ConsoleHelper.promptWithValidation("Vi tri moi: ", value -> Validator.requireNotBlank(value, "Vi tri"));
		String description = ConsoleHelper.promptWithValidation("Mo ta moi: ", value -> Validator.requireNotBlank(value, "Mo ta"));

		try {
			adminService.updateRoom(id, name, capacity, location, description);
			System.out.println("Cap nhat phong hop thanh cong.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Cap nhat phong hop that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void deleteRoom() {
		MenuHelper.printHeader("XOA PHONG HOP");
		int id = promptExistingRoomId("Nhap ID phong can xoa: ");

		try {
			adminService.deleteRoom(id);
			System.out.println("Xoa phong hop thanh cong.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Xoa phong hop that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void showEquipments() {
		MenuHelper.printHeader("DANH SACH THIET BI");
		List<Equipment> equipments = adminService.getAllEquipments();
		if (equipments.isEmpty()) {
			System.out.println("Chua co thiet bi nao.");
		} else {
			List<String[]> rows = new ArrayList<>();
			for (Equipment equipment : equipments) {
				rows.add(new String[]{
						String.valueOf(equipment.getId()),
						equipment.getName(),
						String.valueOf(equipment.getTotalQuantity()),
						String.valueOf(equipment.getAvailableQuantity()),
						equipment.getStatus()
				});
			}
			MenuHelper.printTable(
					new String[]{"ID", "Ten thiet bi", "Tong so luong", "So luong kha dung", "Trang thai"},
					rows
			);
		}
		ConsoleHelper.waitForEnter();
	}

	private void updateEquipmentAvailableQuantity() {
		MenuHelper.printHeader("CAP NHAT SO LUONG KHA DUNG");
		int equipmentId = promptExistingEquipmentId("Nhap ID thiet bi: ");
		Equipment target = findEquipmentById(equipmentId);
		String availableRaw = ConsoleHelper.promptWithValidation("Nhap so luong kha dung moi: ", value -> {
			int parsed;
			try {
				parsed = Integer.parseInt(value.trim());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("So luong kha dung phai la so nguyen.");
			}
			if (parsed < 0) {
				throw new IllegalArgumentException("So luong kha dung khong duoc am.");
			}
			if (target != null && parsed > target.getTotalQuantity()) {
				throw new IllegalArgumentException("So luong kha dung khong duoc lon hon tong so luong la " + target.getTotalQuantity() + ".");
			}
			return String.valueOf(parsed);
		});
		int availableQuantity = Integer.parseInt(availableRaw);

		try {
			Equipment equipment = adminService.updateEquipmentAvailableQuantity(equipmentId, availableQuantity);
			System.out.println("Cap nhat thanh cong. Thiet bi " + equipment.getName() + " co so luong kha dung moi la "
					+ equipment.getAvailableQuantity() + ".");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Cap nhat thiet bi that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private int promptExistingRoomId(String label) {
		while (true) {
			int id = ConsoleHelper.promptPositiveInt(label);
			if (findRoomById(id) != null) {
				return id;
			}
			System.out.println("Canh bao: Khong tim thay phong hop voi ID " + id + ". Vui long nhap lai.");
		}
	}

	private int promptExistingEquipmentId(String label) {
		while (true) {
			int id = ConsoleHelper.promptPositiveInt(label);
			if (findEquipmentById(id) != null) {
				return id;
			}
			System.out.println("Canh bao: Khong tim thay thiet bi voi ID " + id + ". Vui long nhap lai.");
		}
	}

	private Room findRoomById(int id) {
		List<Room> rooms = adminService.getAllRooms();
		for (Room room : rooms) {
			if (room.getId() != null && room.getId() == id) {
				return room;
			}
		}
		return null;
	}

	private Equipment findEquipmentById(int id) {
		List<Equipment> equipments = adminService.getAllEquipments();
		for (Equipment equipment : equipments) {
			if (equipment.getId() != null && equipment.getId() == id) {
				return equipment;
			}
		}
		return null;
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

		MenuHelper.printTable(
				new String[]{"Booking", "Employee", "Phong", "Bat dau", "Ket thuc", "Trang thai", "Support", "Preparation"},
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
		for (User user : supportUsers) {
			rows.add(new String[]{
					String.valueOf(user.getId()),
					user.getFullName(),
					user.getUsername(),
					user.getEmail() == null ? "" : user.getEmail()
			});
		}

		MenuHelper.printTable(new String[]{"ID", "Ho ten", "Username", "Email"}, rows);
	}
}
