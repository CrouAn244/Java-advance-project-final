package presentation;

import java.util.ArrayList;
import java.util.List;
import model.Booking;
import model.enums.PreparationStatus;
import model.User;
import service.SupportService;
import util.ConsoleHelper;
import util.DateTimeUtil;
import util.TablePrinter;

public class SupportUI {
	private final SupportService supportService;

	public SupportUI() {
		this.supportService = new SupportService();
	}

	public void run(User user) {
		boolean running = true;
		while (running) {
			System.out.println();
			System.out.println(ConsoleHelper.ANSI_CYAN + "================= MENU HO TRO - " + user.getFullName() + " =================" + ConsoleHelper.ANSI_RESET);
			System.out.println("1. Xem cong viec duoc phan cong");
			System.out.println("2. Cap nhat trang thai chuan bi");
			System.out.println("0. Dang xuat");
			System.out.println(ConsoleHelper.ANSI_CYAN + "==================================================" + ConsoleHelper.ANSI_RESET);

			int choice = askChoice(0, 2);
			switch (choice) {
				case 1:
					handleViewAssignedBookings(user);
					break;
				case 2:
					handleUpdatePreparationStatus(user);
					break;
				case 0:
					running = false;
					break;
				default:
					break;
			}
		}
	}

	private void handleViewAssignedBookings(User user) {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= CONG VIEC DUOC PHAN CONG =================" + ConsoleHelper.ANSI_RESET);
		List<Booking> bookings = supportService.getAssignedBookings(user.getId());
		if (bookings.isEmpty()) {
			System.out.println("Ban chua duoc phan cong cong viec nao.");
			ConsoleHelper.waitForEnter();
			return;
		}

		showAssignedBookings(bookings);
		ConsoleHelper.waitForEnter();
	}

	private void handleUpdatePreparationStatus(User user) {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= CAP NHAT TRANG THAI CHUAN BI =================" + ConsoleHelper.ANSI_RESET);
		List<Booking> bookings = supportService.getAssignedBookings(user.getId());
		if (bookings.isEmpty()) {
			System.out.println("Ban chua duoc phan cong cong viec nao.");
			ConsoleHelper.waitForEnter();
			return;
		}

		showAssignedBookings(bookings);
		int bookingId = ConsoleHelper.promptPositiveInt("Nhap ID booking can cap nhat: ");
		if (!containsBooking(bookings, bookingId)) {
			System.out.println("Booking nay khong nam trong danh sach ban duoc phan cong.");
			ConsoleHelper.waitForEnter();
			return;
		}

		System.out.println("1. PREPARING");
		System.out.println("2. READY");
		System.out.println("3. MISSING_EQUIPMENT");
		int statusChoice = askChoice(1, 3);
		PreparationStatus status = mapPreparationStatus(statusChoice);

		try {
			supportService.updatePreparationStatus(user.getId(), bookingId, status);
			System.out.println("Cap nhat trang thai chuan bi thanh cong.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Cap nhat that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}

	private void showAssignedBookings(List<Booking> bookings) {
		List<String[]> rows = new ArrayList<>();
		for (Booking booking : bookings) {
			rows.add(new String[]{
					String.valueOf(booking.getId()),
					String.valueOf(booking.getRoomId()),
					DateTimeUtil.formatUserDateTime(booking.getStartTime()),
					DateTimeUtil.formatUserDateTime(booking.getEndTime()),
					booking.getPreparationStatus().name()
			});
		}

		TablePrinter.printTable(
				new String[]{"Booking", "Phong", "Bat dau", "Ket thuc", "Preparation"},
				new int[]{8, 6, 16, 16, 12},
				rows
		);
	}

	private boolean containsBooking(List<Booking> bookings, int bookingId) {
		for (Booking booking : bookings) {
			if (booking.getId() != null && booking.getId() == bookingId) {
				return true;
			}
		}
		return false;
	}

	private PreparationStatus mapPreparationStatus(int choice) {
		switch (choice) {
			case 1:
				return PreparationStatus.PREPARING;
			case 2:
				return PreparationStatus.READY;
			case 3:
				return PreparationStatus.MISSING_EQUIPMENT;
			default:
				throw new IllegalArgumentException("Lua chon trang thai khong hop le.");
		}
	}

	private int askChoice(int min, int max) {
		return ConsoleHelper.promptIntInRange("Chon chuc nang: ", min, max);
	}

}
