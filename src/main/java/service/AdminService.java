package service;

import java.util.List;
import model.Booking;
import model.Equipment;
import model.Enum.Role;
import model.Room;
import model.User;
import util.PasswordHash;
import util.Validator;

public class AdminService {
	private final RoomService roomService;
	private final EquipmentService equipmentService;
	private final UserService userService;
	private final BookingService bookingService;

	public AdminService() {
		this.roomService = new RoomService();
		this.equipmentService = new EquipmentService();
		this.userService = new UserService();
		this.bookingService = new BookingService();
	}

	public List<Room> getAllRooms() {
		return roomService.getAllRooms();
	}

	public Room createRoom(String name, int capacity, String location, String description) {
		return roomService.createRoom(name, capacity, location, description);
	}

	public Room updateRoom(int id, String name, int capacity, String location, String description) {
		return roomService.updateRoom(id, name, capacity, location, description);
	}

	public void deleteRoom(int id) {
		roomService.deleteRoom(id);
	}

	public List<Equipment> getAllEquipments() {
		return equipmentService.getAllEquipments();
	}

	public Equipment updateEquipmentAvailableQuantity(int equipmentId, int availableQuantity) {
		return equipmentService.updateAvailableQuantity(equipmentId, availableQuantity);
	}

	public User createSupportAccount(String username, String password, String fullName, String phone, String email) {
		String validatedUsername = Validator.validateUsername(username);
		String validatedPassword = Validator.validatePassword(password);
		String validatedFullName = Validator.validateFullName(fullName);
		String validatedPhone = Validator.validatePhone(phone);
		String validatedEmail = Validator.validateEmail(email);

		if (userService.existsUsername(validatedUsername)) {
			throw new IllegalArgumentException("Ten dang nhap da ton tai.");
		}
		if (userService.existsEmail(validatedEmail)) {
			throw new IllegalArgumentException("Email da ton tai.");
		}

		User user = new User();
		user.setUsername(validatedUsername);
		user.setPassword(PasswordHash.hashPassword(validatedPassword));
		user.setFullName(validatedFullName);
		user.setPhone(validatedPhone);
		user.setEmail(validatedEmail);
		user.setRole(Role.SUPPORT);
		return userService.createUser(user);
	}

	public boolean isUsernameAvailable(String username) {
		String validatedUsername = Validator.validateUsername(username);
		return !userService.existsUsername(validatedUsername);
	}

	public boolean isEmailAvailable(String email) {
		String validatedEmail = Validator.validateEmail(email);
		return !userService.existsEmail(validatedEmail);
	}

	public List<Booking> getAllBookings() {
		return bookingService.getAllBookings();
	}

	public List<User> getSupportUsers() {
		return userService.findByRole(Role.SUPPORT);
	}

	public void approveBooking(int bookingId, int supportStaffId) {
		boolean supportExists = false;
		for (User supportUser : getSupportUsers()) {
			if (supportUser.getId() != null && supportUser.getId() == supportStaffId) {
				supportExists = true;
				break;
			}
		}
		if (!supportExists) {
			throw new IllegalArgumentException("Khong tim thay nhan vien Support voi ID " + supportStaffId + ".");
		}

		bookingService.approveBooking(bookingId, supportStaffId);
	}

	public void rejectBooking(int bookingId) {
		bookingService.rejectBooking(bookingId);
	}
}
