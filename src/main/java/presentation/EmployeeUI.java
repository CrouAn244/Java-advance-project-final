package presentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Booking;
import model.Equipment;
import model.Enum.BookingStatus;
import model.Enum.PreparationStatus;
import model.Room;
import model.User;
import service.BookingService;
import util.ConsoleHelper;
import util.DateTimeUtil;

public class EmployeeUI {
	private final BookingService bookingService;

	public EmployeeUI() {
		this.bookingService = new BookingService();
	}

	public void run(User user) {
		boolean running = true;
		while (running) {
			MenuHelper.printHeader("MENU NHAN VIEN - " + user.getFullName());
			MenuHelper.printOptions(
					"1. Dat phong",
					"2. Xem lich dat phong cua toi",
					"0. Dang xuat"
			);

			int choice = MenuHelper.askChoice(0, 2);
			switch (choice) {
				case 1:
					handleCreateBooking(user);
					break;
				case 2:
					handleViewMyBookings(user);
					break;
				case 0:
					running = false;
					break;
				default:
					break;
			}
		}
	}

	private void handleCreateBooking(User user) {
		MenuHelper.printHeader("DAT PHONG");
		List<Room> allRooms = bookingService.getAllRooms();
		if (allRooms.isEmpty()) {
			System.out.println("Không có phòng họp");
			ConsoleHelper.waitForEnter();
			return;
		}

		showAllRooms(allRooms);

		System.out.println("Nhap thoi gian theo dinh dang yyyy-MM-dd HH:mm");
		LocalDateTime startTime = promptDateTime("Thoi gian bat dau: ");
		LocalDateTime endTime = promptDateTime("Thoi gian ket thuc: ");

		List<Room> availableRooms;
		try {
			availableRooms = bookingService.getAvailableRooms(startTime, endTime);
		} catch (IllegalArgumentException e) {
			System.out.println("Dat phong that bai: " + e.getMessage());
			ConsoleHelper.waitForEnter();
			return;
		}

		if (availableRooms.isEmpty()) {
			System.out.println("Khong co phong trong trong khoang thoi gian nay (cac phong da co lich trung).");
			ConsoleHelper.waitForEnter();
			return;
		}

		showAvailableRooms(availableRooms);
		int roomId = promptAvailableRoomId(availableRooms);
		Map<Integer, Integer> equipmentRequests = promptEquipmentRequests();

		try {
			Booking booking = bookingService.createBookingRequest(user.getId(), roomId, startTime, endTime, equipmentRequests);
			System.out.println("Tao yeu cau dat phong thanh cong. Ma dat phong: " + booking.getId() + ", trang thai: " + booking.getStatus());
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Dat phong that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void handleViewMyBookings(User user) {
		MenuHelper.printHeader("LICH DAT PHONG CUA TOI");
		List<Booking> bookings;
		try {
			bookings = bookingService.getBookingsByUser(user.getId());
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Khong the tai lich dat phong: " + e.getMessage());
			ConsoleHelper.waitForEnter();
			return;
		}

		if (bookings.isEmpty()) {
			System.out.println("Ban chua co lich dat phong nao.");
			ConsoleHelper.waitForEnter();
			return;
		}

		List<String[]> rows = new ArrayList<>();
		for (Booking booking : bookings) {
			boolean isReadyToStart = booking.getStatus() == BookingStatus.APPROVED
					&& booking.getPreparationStatus() == PreparationStatus.READY;
			rows.add(new String[]{
					String.valueOf(booking.getId()),
					String.valueOf(booking.getRoomId()),
					DateTimeUtil.formatUserDateTime(booking.getStartTime()),
					DateTimeUtil.formatUserDateTime(booking.getEndTime()),
					booking.getStatus().name(),
					booking.getPreparationStatus().name(),
					isReadyToStart ? "Da san sang" : "Chua san sang"
			});
		}

		MenuHelper.printTable(
				new String[]{"Ma dat phong", "Phong", "Bat dau", "Ket thuc", "Booking", "Preparation", "Co the hop"},
				rows
		);
		ConsoleHelper.waitForEnter();
	}

	private LocalDateTime promptDateTime(String label) {
		while (true) {
			String raw = ConsoleHelper.prompt(label);
			try {
				return DateTimeUtil.parseUserDateTime(raw);
			} catch (IllegalArgumentException e) {
				System.out.println("Canh bao: " + e.getMessage());
			}
		}
	}

	private void showAvailableRooms(List<Room> availableRooms) {
		List<String[]> rows = new ArrayList<>();
		for (Room room : availableRooms) {
			rows.add(new String[]{
					String.valueOf(room.getId()),
					room.getName(),
					String.valueOf(room.getCapacity()),
					room.getLocation(),
					room.getDescription()
			});
		}

		System.out.println("Danh sach phong trong:");
		MenuHelper.printTable(new String[]{"ID", "Ten phong", "Suc chua", "Vi tri", "Mo ta"}, rows);
	}

	private void showAllRooms(List<Room> rooms) {
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

		System.out.println("Danh sach phong hop:");
		MenuHelper.printTable(new String[]{"ID", "Ten phong", "Suc chua", "Vi tri", "Mo ta"}, rows);
	}

	private int promptAvailableRoomId(List<Room> availableRooms) {
		while (true) {
			int roomId = ConsoleHelper.promptPositiveInt("Nhap ID phong muon dat: ");
			for (Room room : availableRooms) {
				if (room.getId() != null && room.getId() == roomId) {
					return roomId;
				}
			}
			System.out.println("Canh bao: Phong nay khong nam trong danh sach phong trong. Vui long chon lai.");
		}
	}

	private Map<Integer, Integer> promptEquipmentRequests() {
		Map<Integer, Integer> requests = new HashMap<>();

		String choice = ConsoleHelper.prompt("Muon them thiet bi? (y/n): ").trim().toLowerCase();
		if (!"y".equals(choice)) {
			return requests;
		}

		List<Equipment> equipments = bookingService.getAllEquipments();
		if (equipments.isEmpty()) {
			System.out.println("Hien khong co thiet bi de muon them.");
			return requests;
		}

		showEquipments(equipments);
		while (true) {
			int equipmentId = ConsoleHelper.promptNonNegativeInt("Nhap ID thiet bi (0 de dung): ");
			if (equipmentId == 0) {
				break;
			}

			Equipment equipment = findEquipmentById(equipments, equipmentId);
			if (equipment == null) {
				System.out.println("Canh bao: Khong tim thay thiet bi voi ID " + equipmentId + ".");
				continue;
			}

			int quantity = ConsoleHelper.promptPositiveInt("So luong muon: ");
			requests.put(equipmentId, requests.getOrDefault(equipmentId, 0) + quantity);
		}

		return requests;
	}

	private void showEquipments(List<Equipment> equipments) {
		List<String[]> rows = new ArrayList<>();
		for (Equipment equipment : equipments) {
			rows.add(new String[]{
					String.valueOf(equipment.getId()),
					equipment.getName(),
					String.valueOf(equipment.getAvailableQuantity()),
					equipment.getStatus()
			});
		}

		System.out.println("Danh sach thiet bi co the muon:");
		MenuHelper.printTable(new String[]{"ID", "Ten thiet bi", "So luong kha dung", "Trang thai"}, rows);
	}

	private Equipment findEquipmentById(List<Equipment> equipments, int equipmentId) {
		for (Equipment equipment : equipments) {
			if (equipment.getId() != null && equipment.getId() == equipmentId) {
				return equipment;
			}
		}
		return null;
	}
}
