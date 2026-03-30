package service;

import dao.IBookingDAO;
import dao.impl.BookingDAOImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Booking;
import model.BookingDetail;
import model.Equipment;
import model.Enum.BookingStatus;
import model.Enum.PreparationStatus;
import model.Room;
import model.Service;
import util.Validator;

public class BookingService {
	private final IBookingDAO bookingDAO;
	private final RoomService roomService;
	private final EquipmentService equipmentService;
	private final ServiceService serviceService;

	public BookingService() {
		this.bookingDAO = new BookingDAOImpl();
		this.roomService = new RoomService();
		this.equipmentService = new EquipmentService();
		this.serviceService = new ServiceService();
	}

	public List<Room> getAvailableRooms(LocalDateTime startTime, LocalDateTime endTime) {
		validateTimeRange(startTime, endTime);

		List<Room> availableRooms = new ArrayList<>();
		for (Room room : roomService.getAllRooms()) {
			if (room.getId() != null && !bookingDAO.existsTimeConflict(room.getId(), startTime, endTime)) {
				availableRooms.add(room);
			}
		}
		return availableRooms;
	}

	public List<Room> getAllRooms() {
		return roomService.getAllRooms();
	}

	public List<Equipment> getAllEquipments() {
		return equipmentService.getAllEquipments();
	}

	public List<Service> getAllServices() {
		return serviceService.getAllServices();
	}

	public List<Booking> getBookingsByUser(int userId) {
		if (userId <= 0) {
			throw new IllegalArgumentException("Ma nguoi dung khong hop le.");
		}
		return bookingDAO.findByUserId(userId);
	}

	public List<Booking> getAllBookings() {
		return bookingDAO.findAll();
	}

	public List<Booking> getAssignedBookings(int supportStaffId) {
		if (supportStaffId <= 0) {
			throw new IllegalArgumentException("Ma nhan vien ho tro khong hop le.");
		}
		return bookingDAO.findBySupportStaffId(supportStaffId);
	}

	public void approveBooking(int bookingId, int supportStaffId) {
		Booking booking = bookingDAO.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Khong tim thay booking voi ID " + bookingId + "."));
		if (booking.getStatus() != BookingStatus.PENDING) {
			throw new IllegalArgumentException("Chi duoc duyet booking dang PENDING.");
		}

		boolean updated = bookingDAO.updateApproval(
				bookingId,
				BookingStatus.APPROVED,
				supportStaffId,
				PreparationStatus.PREPARING
		);
		if (!updated) {
			throw new IllegalStateException("Khong the duyet booking.");
		}
	}

	public void rejectBooking(int bookingId) {
		Booking booking = bookingDAO.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Khong tim thay booking voi ID " + bookingId + "."));
		if (booking.getStatus() != BookingStatus.PENDING) {
			throw new IllegalArgumentException("Chi duoc tu choi booking dang PENDING.");
		}

		boolean updated = bookingDAO.updateApproval(
				bookingId,
				BookingStatus.REJECTED,
				null,
				PreparationStatus.PREPARING
		);
		if (!updated) {
			throw new IllegalStateException("Khong the tu choi booking.");
		}
	}

	public void updatePreparationStatus(int supportStaffId, int bookingId, PreparationStatus preparationStatus) {
		if (supportStaffId <= 0) {
			throw new IllegalArgumentException("Ma nhan vien ho tro khong hop le.");
		}
		if (preparationStatus == null) {
			throw new IllegalArgumentException("Trang thai chuan bi khong hop le.");
		}

		boolean updated = bookingDAO.updatePreparationStatus(bookingId, supportStaffId, preparationStatus);
		if (!updated) {
			throw new IllegalArgumentException("Khong tim thay booking da duoc phan cong cho ban de cap nhat.");
		}
	}

	public void cancelPendingBooking(int userId, int bookingId) {
		if (userId <= 0 || bookingId <= 0) {
			throw new IllegalArgumentException("Thong tin huy booking khong hop le.");
		}

		boolean deleted = bookingDAO.deletePendingByUser(bookingId, userId);
		if (!deleted) {
			throw new IllegalArgumentException("Chi co the huy booking PENDING cua chinh ban.");
		}
	}

	public Booking createBookingRequest(
			int userId,
			int roomId,
			int participantCount,
			LocalDateTime startTime,
			LocalDateTime endTime,
			Map<Integer, Integer> equipmentRequests,
			Map<Integer, Integer> serviceRequests
	) {
		if (userId <= 0) {
			throw new IllegalArgumentException("Ma nguoi dung khong hop le.");
		}
		int validatedParticipants = Validator.validateParticipantCount(participantCount);
		validateTimeRange(startTime, endTime);
		Room room = findRoomById(roomId);
		Validator.validateRoomCapacityVsParticipants(room.getCapacity(), validatedParticipants);

		if (bookingDAO.existsTimeConflict(roomId, startTime, endTime)) {
			throw new IllegalArgumentException("Phong da co lich trung trong khoang thoi gian nay.");
		}

		List<BookingDetail> details = new ArrayList<>();
		details.addAll(buildEquipmentDetails(equipmentRequests));
		details.addAll(buildServiceDetails(serviceRequests));

		Booking booking = new Booking();
		booking.setUserId(userId);
		booking.setRoomId(roomId);
		booking.setStartTime(startTime);
		booking.setEndTime(endTime);
		booking.setStatus(BookingStatus.PENDING);
		booking.setPreparationStatus(PreparationStatus.PREPARING);
		booking.setCreatedAt(LocalDateTime.now());

		return bookingDAO.save(booking, details);
	}

	// Xung dot khi khoang moi giao nhau voi khoang da ton tai: start < existingEnd va end > existingStart.
	public boolean isTimeConflict(LocalDateTime existingStart, LocalDateTime existingEnd, LocalDateTime newStart, LocalDateTime newEnd) {
		return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
	}

	private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
		Validator.validateNotPastDateTime(startTime, "Thoi gian bat dau");
		Validator.validateNotPastDateTime(endTime, "Thoi gian ket thuc");
		if (!startTime.isBefore(endTime)) {
			throw new IllegalArgumentException("Thoi gian bat dau phai nho hon thoi gian ket thuc.");
		}
	}

	private Room findRoomById(int roomId) {
		for (Room room : roomService.getAllRooms()) {
			if (room.getId() != null && room.getId() == roomId) {
				return room;
			}
		}
		throw new IllegalArgumentException("Khong tim thay phong hop voi ID " + roomId + ".");
	}

	private List<BookingDetail> buildEquipmentDetails(Map<Integer, Integer> equipmentRequests) {
		List<BookingDetail> details = new ArrayList<>();
		if (equipmentRequests == null || equipmentRequests.isEmpty()) {
			return details;
		}

		Map<Integer, Equipment> equipmentMap = new HashMap<>();
		for (Equipment equipment : equipmentService.getAllEquipments()) {
			if (equipment.getId() != null) {
				equipmentMap.put(equipment.getId(), equipment);
			}
		}

		for (Map.Entry<Integer, Integer> entry : equipmentRequests.entrySet()) {
			Integer equipmentId = entry.getKey();
			Integer quantity = entry.getValue();
			if (equipmentId == null || quantity == null) {
				continue;
			}
			if (quantity <= 0) {
				throw new IllegalArgumentException("So luong thiet bi muon them phai lon hon 0.");
			}

			Equipment equipment = equipmentMap.get(equipmentId);
			if (equipment == null) {
				throw new IllegalArgumentException("Khong tim thay thiet bi voi ID " + equipmentId + ".");
			}
			if (equipment.getAvailableQuantity() == null || quantity > equipment.getAvailableQuantity()) {
				throw new IllegalArgumentException("Thiet bi " + equipment.getName() + " chi con "
						+ equipment.getAvailableQuantity() + " cho phep muon.");
			}

			BookingDetail detail = new BookingDetail();
			detail.setEquipmentId(equipmentId);
			detail.setQuantity(quantity);
			details.add(detail);
		}

		return details;
	}

	private List<BookingDetail> buildServiceDetails(Map<Integer, Integer> serviceRequests) {
		List<BookingDetail> details = new ArrayList<>();
		if (serviceRequests == null || serviceRequests.isEmpty()) {
			return details;
		}

		Map<Integer, Service> serviceMap = new HashMap<>();
		for (Service service : serviceService.getAllServices()) {
			if (service.getId() != null) {
				serviceMap.put(service.getId(), service);
			}
		}

		for (Map.Entry<Integer, Integer> entry : serviceRequests.entrySet()) {
			Integer serviceId = entry.getKey();
			Integer quantity = entry.getValue();
			if (serviceId == null || quantity == null) {
				continue;
			}
			if (quantity <= 0) {
				throw new IllegalArgumentException("So luong dich vu phai lon hon 0.");
			}

			Service service = serviceMap.get(serviceId);
			if (service == null) {
				throw new IllegalArgumentException("Khong tim thay dich vu voi ID " + serviceId + ".");
			}

			BookingDetail detail = new BookingDetail();
			detail.setServiceId(serviceId);
			detail.setQuantity(quantity);
			details.add(detail);
		}

		return details;
	}
}
