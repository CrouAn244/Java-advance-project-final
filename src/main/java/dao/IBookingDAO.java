package dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import model.Booking;
import model.BookingDetail;
import model.Enum.BookingStatus;
import model.Enum.PreparationStatus;

public interface IBookingDAO {
	boolean existsTimeConflict(int roomId, LocalDateTime requestedStart, LocalDateTime requestedEnd);

	Booking save(Booking booking, List<BookingDetail> details);

	List<Booking> findAll();

	List<Booking> findByUserId(int userId);

	Optional<Booking> findById(int bookingId);

	List<Booking> findBySupportStaffId(int supportStaffId);

	boolean updateApproval(int bookingId, BookingStatus status, Integer supportStaffId, PreparationStatus preparationStatus);

	boolean updatePreparationStatus(int bookingId, int supportStaffId, PreparationStatus preparationStatus);
}
