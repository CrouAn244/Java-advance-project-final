package service;

import java.util.List;
import model.Booking;
import model.Enum.PreparationStatus;

public class SupportService {
	private final BookingService bookingService;

	public SupportService() {
		this.bookingService = new BookingService();
	}

	public List<Booking> getAssignedBookings(int supportStaffId) {
		return bookingService.getAssignedBookings(supportStaffId);
	}

	public void updatePreparationStatus(int supportStaffId, int bookingId, PreparationStatus preparationStatus) {
		bookingService.updatePreparationStatus(supportStaffId, bookingId, preparationStatus);
	}
}
