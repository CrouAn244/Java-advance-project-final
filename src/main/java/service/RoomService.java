package service;

import dao.IRoomDAO;
import dao.impl.RoomDAOImpl;
import java.util.List;
import model.Room;
import util.Validator;

public class RoomService {
	private final IRoomDAO roomDAO;

	public RoomService() {
		this.roomDAO = new RoomDAOImpl();
	}

	public List<Room> getAllRooms() {
		return roomDAO.findAll();
	}

	public Room createRoom(String name, int capacity, String location, String description) {
		String validatedName = Validator.requireNotBlank(name, "Ten phong");
		String validatedLocation = Validator.requireNotBlank(location, "Vi tri");
		String validatedDescription = Validator.requireNotBlank(description, "Mo ta");
		if (capacity <= 0) {
			throw new IllegalArgumentException("Suc chua phong phai lon hon 0.");
		}

		Room room = new Room();
		room.setName(validatedName);
		room.setCapacity(capacity);
		room.setLocation(validatedLocation);
		room.setDescription(validatedDescription);
		return roomDAO.save(room);
	}

	public Room updateRoom(int id, String name, int capacity, String location, String description) {
		Room existing = roomDAO.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong hop voi ID " + id + "."));

		String validatedName = Validator.requireNotBlank(name, "Ten phong");
		String validatedLocation = Validator.requireNotBlank(location, "Vi tri");
		String validatedDescription = Validator.requireNotBlank(description, "Mo ta");
		if (capacity <= 0) {
			throw new IllegalArgumentException("Suc chua phong phai lon hon 0.");
		}

		existing.setName(validatedName);
		existing.setCapacity(capacity);
		existing.setLocation(validatedLocation);
		existing.setDescription(validatedDescription);

		boolean updated = roomDAO.update(existing);
		if (!updated) {
			throw new IllegalArgumentException("Cap nhat phong hop that bai.");
		}
		return existing;
	}

	public void deleteRoom(int id) {
		boolean deleted = roomDAO.deleteById(id);
		if (!deleted) {
			throw new IllegalArgumentException("Khong tim thay phong hop voi ID " + id + " de xoa.");
		}
	}
}
