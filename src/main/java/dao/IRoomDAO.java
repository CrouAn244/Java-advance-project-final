package dao;

import java.util.List;
import java.util.Optional;
import model.Room;

public interface IRoomDAO {
	List<Room> findAll();

	Optional<Room> findById(int id);

	Room save(Room room);

	boolean update(Room room);

	boolean deleteById(int id);
}
