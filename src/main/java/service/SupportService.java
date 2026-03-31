package service;

import java.util.List;
import model.Booking;
import model.enums.PreparationStatus;

public class SupportService {
	private final BookingService bookingService;

	public SupportService() {
		this(new BookingService());
	}

	public SupportService(BookingService bookingService) {
		if (bookingService == null) {
			throw new IllegalArgumentException("BookingService khong duoc null.");
		}
		this.bookingService = bookingService;
	}

	public List<Booking> getAssignedBookings(int supportStaffId) {
		return bookingService.getAssignedBookings(supportStaffId);
	}

	public void updatePreparationStatus(int supportStaffId, int bookingId, PreparationStatus preparationStatus) {
		bookingService.updatePreparationStatus(supportStaffId, bookingId, preparationStatus);
	}
}
