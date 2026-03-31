package presentation.admin;

import java.util.ArrayList;
import java.util.List;
import model.Room;
import service.AdminService;
import util.ConsoleHelper;
import util.TablePrinter;
import util.Validator;

public class AdminRoomManagementUI {
	private final AdminService adminService;

	public AdminRoomManagementUI(AdminService adminService) {
		this.adminService = adminService;
	}

	public void run() {
		boolean running = true;
		while (running) {
			System.out.println();
			System.out.println(ConsoleHelper.ANSI_CYAN + "================= QUAN LY PHONG HOP =================" + ConsoleHelper.ANSI_RESET);
			System.out.println("1. Xem danh sach phong");
			System.out.println("2. Them phong");
			System.out.println("3. Sua phong");
			System.out.println("4. Xoa phong");
			System.out.println("5. Tim kiem phong theo ten");
			System.out.println("0. Quay lai");
			System.out.println(ConsoleHelper.ANSI_CYAN + "==================================================" + ConsoleHelper.ANSI_RESET);

			int choice = ConsoleHelper.promptIntInRange("Chon chuc nang: ", 0, 5);
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
				case 5:
					searchRoomByName();
					break;
				case 0:
					running = false;
					break;
				default:
					break;
			}
		}
	}

	private void showRooms() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= DANH SACH PHONG HOP =================" + ConsoleHelper.ANSI_RESET);
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
			TablePrinter.printTable(
					new String[]{"ID", "Ten phong", "Suc chua", "Vi tri", "Mo ta"},
					new int[]{4, 20, 8, 18, 24},
					rows
			);
		}
		ConsoleHelper.waitForEnter();
	}

	private void createRoom() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= THEM PHONG HOP =================" + ConsoleHelper.ANSI_RESET);
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

	private void searchRoomByName() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= TIM KIEM PHONG THEO TEN =================" + ConsoleHelper.ANSI_RESET);
		String keyword = ConsoleHelper.promptWithValidation("Nhap tu khoa: ", value -> Validator.requireNotBlank(value, "Tu khoa"));

		try {
			List<Room> rooms = adminService.searchRoomsByName(keyword);
			if (rooms.isEmpty()) {
				System.out.println("Khong tim thay phong nao phu hop.");
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
				TablePrinter.printTable(
						new String[]{"ID", "Ten phong", "Suc chua", "Vi tri", "Mo ta"},
						new int[]{4, 20, 8, 18, 24},
						rows
				);
			}
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Tim kiem that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void updateRoom() {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= SUA PHONG HOP =================" + ConsoleHelper.ANSI_RESET);
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
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= XOA PHONG HOP =================" + ConsoleHelper.ANSI_RESET);
		int id = promptExistingRoomId("Nhap ID phong can xoa: ");

		try {
			adminService.deleteRoom(id);
			System.out.println("Xoa phong hop thanh cong.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Xoa phong hop that bai: " + e.getMessage());
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

	private Room findRoomById(int id) {
		List<Room> rooms = adminService.getAllRooms();
		for (Room room : rooms) {
			if (room.getId() != null && room.getId() == id) {
				return room;
			}
		}
		return null;
	}
}
