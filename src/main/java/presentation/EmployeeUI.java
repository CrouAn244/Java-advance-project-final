package presentation;

import java.util.ArrayList;
import java.util.List;
import model.Booking;
import model.enums.BookingStatus;
import model.enums.PreparationStatus;
import model.User;
import presentation.employee.EmployeeBookingCreationUI;
import service.BookingService;
import util.ConsoleHelper;
import util.DateTimeUtil;
import util.TablePrinter;

public class EmployeeUI {
	private final BookingService bookingService;
	private final EmployeeBookingCreationUI bookingCreationUI;

	public EmployeeUI() {
		this.bookingService = new BookingService();
		this.bookingCreationUI = new EmployeeBookingCreationUI(bookingService);
	}

	// Chuc nang menu chinh cho nhan vien.
	public void run(User user) {
		boolean running = true;
		while (running) {
			System.out.println();
			System.out.println(ConsoleHelper.ANSI_CYAN + "================= MENU NHAN VIEN - " + user.getFullName() + " =================" + ConsoleHelper.ANSI_RESET);
			System.out.println("1. Dat phong");
			System.out.println("2. Xem lich dat phong cua toi");
			System.out.println("3. Huy booking PENDING");
			System.out.println("0. Dang xuat");
			System.out.println(ConsoleHelper.ANSI_CYAN + "================================================================" + ConsoleHelper.ANSI_RESET);

			int choice = ConsoleHelper.promptIntInRange("Chon chuc nang: ", 0, 3);
			switch (choice) {
				case 1:
					bookingCreationUI.handleCreateBooking(user);
					break;
				case 2:
					handleViewMyBookings(user);
					break;
				case 3:
					handleCancelPendingBooking(user);
					break;
				case 0:
					running = false;
					break;
				default:
					break;
			}
		}
	}

	// Chuc nang 2: Xem lich dat phong cua nhan vien hien tai.
	private void handleViewMyBookings(User user) {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "======================================== LICH DAT PHONG CUA TOI ========================================" + ConsoleHelper.ANSI_RESET);
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

		TablePrinter.printTable(
				new String[]{"Ma dat", "Phong", "Bat dau", "Ket thuc", "Booking", "Preparation", "Co the hop"},
				new int[]{10, 6, 16, 16, 8, 12, 13},
				rows
		);
		ConsoleHelper.waitForEnter();
	}

	// Chuc nang 3: Huy booking o trang thai PENDING.
	private void handleCancelPendingBooking(User user) {
		System.out.println();
		System.out.println(ConsoleHelper.ANSI_CYAN + "================= HUY BOOKING PENDING =================" + ConsoleHelper.ANSI_RESET);
		int bookingId = ConsoleHelper.promptPositiveInt("Nhap ID booking muon huy: ");

		try {
			bookingService.cancelPendingBooking(user.getId(), bookingId);
			System.out.println("Huy booking thanh cong.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("Huy booking that bai: " + e.getMessage());
		}
		ConsoleHelper.waitForEnter();
	}
}
