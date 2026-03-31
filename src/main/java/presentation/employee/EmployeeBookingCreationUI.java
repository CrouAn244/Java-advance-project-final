package presentation.employee;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Booking;
import model.Equipment;
import model.Room;
import model.Service;
import model.User;
import service.BookingService;
import util.ConsoleHelper;
import util.DateTimeUtil;
import util.TablePrinter;
import util.Validator;

public class EmployeeBookingCreationUI {
	private final BookingService bookingService;

	public EmployeeBookingCreationUI(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	public void handleCreateBooking(User user) {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================================================== DAT PHONG ==================================================" + ConsoleHelper.ANSI_RESET);
		List<Room> allRooms = bookingService.getAllRooms();
		if (allRooms.isEmpty()) {
			System.out.println("Khong co phong hop");
			ConsoleHelper.waitForEnter();
			return;
		}

		showAllRooms(allRooms);

		System.out.println("Nhap thoi gian theo dinh dang yyyy-MM-dd HH:mm");
		LocalDateTime startTime = promptDateTime("Thoi gian bat dau: ");
		LocalDateTime endTime = promptEndDateTime(startTime);

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
		Room selectedRoom = promptAvailableRoom(availableRooms);
		int participantCount = promptParticipantCountByRoomCapacity(selectedRoom);
		Map<Integer, Integer> equipmentRequests = promptEquipmentRequests();
		Map<Integer, Integer> serviceRequests = promptServiceRequests();

		try {
			Booking booking = bookingService.createBookingRequest(
					user.getId(),
					selectedRoom.getId(),
					participantCount,
					startTime,
					endTime,
					equipmentRequests,
					serviceRequests
			);
			System.out.println("Tao yeu cau dat phong thanh cong. Ma dat phong: " + booking.getId() + ", trang thai: " + booking.getStatus());
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Dat phong that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private LocalDateTime promptDateTime(String label) {
		while (true) {
			String raw = ConsoleHelper.prompt(label);
			try {
				LocalDateTime value = DateTimeUtil.parseUserDateTime(raw);
				return Validator.validateNotPastDateTime(value, label.replace(":", ""));
			} catch (IllegalArgumentException e) {
				System.out.println("Canh bao: " + e.getMessage());
			}
		}
	}

	private LocalDateTime promptEndDateTime(LocalDateTime startTime) {
		while (true) {
			LocalDateTime endTime = promptDateTime("Thoi gian ket thuc: ");
			if (endTime.isAfter(startTime)) {
				return endTime;
			}
			System.out.println("Canh bao: Thoi gian ket thuc phai lon hon thoi gian bat dau. Vui long nhap lai.");
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
		TablePrinter.printTable(
				new String[]{"ID", "Ten phong", "Suc chua", "Vi tri", "Mo ta"},
				new int[]{4, 25, 8, 18, 40},
				rows
		);
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
		TablePrinter.printTable(
				new String[]{"ID", "Ten phong", "Suc chua", "Vi tri", "Mo ta"},
				new int[]{4, 25, 8, 18, 40},
				rows
		);
	}

	private Room promptAvailableRoom(List<Room> availableRooms) {
		while (true) {
			int roomId = ConsoleHelper.promptPositiveInt("Nhap ID phong muon dat: ");
			for (Room room : availableRooms) {
				if (room.getId() != null && room.getId() == roomId) {
					return room;
				}
			}
			System.out.println("Canh bao: Phong nay khong nam trong danh sach phong trong. Vui long chon lai.");
		}
	}

	private int promptParticipantCountByRoomCapacity(Room room) {
		int capacity = room.getCapacity() == null ? 0 : room.getCapacity();
		while (true) {
			int participantCount = ConsoleHelper.promptPositiveInt("So nguoi tham gia: ");
			if (participantCount < capacity) {
				return participantCount;
			}
			System.out.println("Canh bao: So nguoi tham gia phai be hon suc chua phong (" + capacity + "). Vui long nhap lai.");
		}
	}

	private Map<Integer, Integer> promptEquipmentRequests() {
		Map<Integer, Integer> requests = new HashMap<>();

		if (!promptYesNo("Muon them thiet bi? (y/n): ")) {
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

			int available = equipment.getAvailableQuantity() == null ? 0 : equipment.getAvailableQuantity();
			int alreadyRequested = requests.getOrDefault(equipmentId, 0);
			int remaining = available - alreadyRequested;

			if (remaining <= 0) {
				System.out.println("Canh bao: Thiet bi " + equipment.getName() + " khong con so luong kha dung de chon them.");
				continue;
			}

			int quantity = ConsoleHelper.promptPositiveInt("So luong muon (toi da " + remaining + "): ");
			if (quantity > remaining) {
				System.out.println("Canh bao: Chi con toi da " + remaining + " thiet bi " + equipment.getName() + ". Vui long nhap lai.");
				continue;
			}

			requests.put(equipmentId, alreadyRequested + quantity);
		}

		return requests;
	}

	private Map<Integer, Integer> promptServiceRequests() {
		Map<Integer, Integer> requests = new HashMap<>();

		if (!promptYesNo("Dung them dich vu? (y/n): ")) {
			return requests;
		}

		List<Service> services = bookingService.getAllServices();
		if (services.isEmpty()) {
			System.out.println("Hien khong co dich vu de chon.");
			return requests;
		}

		showServices(services);
		while (true) {
			int serviceId = ConsoleHelper.promptNonNegativeInt("Nhap ID dich vu (0 de dung): ");
			if (serviceId == 0) {
				break;
			}

			Service service = findServiceById(services, serviceId);
			if (service == null) {
				System.out.println("Canh bao: Khong tim thay dich vu voi ID " + serviceId + ".");
				continue;
			}

			int quantity = ConsoleHelper.promptPositiveInt("So luong su dung: ");
			requests.put(serviceId, requests.getOrDefault(serviceId, 0) + quantity);
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
		TablePrinter.printTable(
				new String[]{"ID", "Ten thiet bi", "So luong kha dung", "Trang thai"},
				new int[]{4, 20, 18, 12},
				rows
		);
	}

	private void showServices(List<Service> services) {
		List<String[]> rows = new ArrayList<>();
		for (Service service : services) {
			rows.add(new String[]{
					String.valueOf(service.getId()),
					service.getName(),
					service.getPrice().toPlainString()
			});
		}

		System.out.println("Danh sach dich vu co the chon:");
		TablePrinter.printTable(
				new String[]{"ID", "Ten dich vu", "Gia"},
				new int[]{4, 28, 13},
				rows
		);
	}

	private Equipment findEquipmentById(List<Equipment> equipments, int equipmentId) {
		for (Equipment equipment : equipments) {
			if (equipment.getId() != null && equipment.getId() == equipmentId) {
				return equipment;
			}
		}
		return null;
	}

	private Service findServiceById(List<Service> services, int serviceId) {
		for (Service service : services) {
			if (service.getId() != null && service.getId() == serviceId) {
				return service;
			}
		}
		return null;
	}

	private boolean promptYesNo(String label) {
		while (true) {
			String answer = ConsoleHelper.prompt(label).trim().toLowerCase();
			if ("y".equals(answer)) {
				return true;
			}
			if ("n".equals(answer)) {
				return false;
			}
			System.out.println("Canh bao: Vui long chi nhap y hoac n.");
		}
	}
}
