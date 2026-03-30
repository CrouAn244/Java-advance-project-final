package dao.impl;

import dao.IRoomDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Room;
import util.DBConnection;

public class RoomDAOImpl implements IRoomDAO {
	@Override
	public List<Room> findAll() {
		String sql = "SELECT id, name, capacity, location, description FROM rooms ORDER BY id";
		List<Room> rooms = new ArrayList<>();

		try (Connection connection = DBConnection.openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql);
			 ResultSet rs = statement.executeQuery()) {
			while (rs.next()) {
				rooms.add(mapRow(rs));
			}
			return rooms;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the lay danh sach phong hop.", e);
		}
	}

	@Override
	public Optional<Room> findById(int id) {
		String sql = "SELECT id, name, capacity, location, description FROM rooms WHERE id = ?";

		try (Connection connection = DBConnection.openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, id);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapRow(rs));
				}
				return Optional.empty();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the tim phong hop theo ID.", e);
		}
	}

	@Override
	public Room save(Room room) {
		String sql = "INSERT INTO rooms (name, capacity, location, description) VALUES (?, ?, ?, ?)";

		try (Connection connection = DBConnection.openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, room.getName());
			statement.setInt(2, room.getCapacity());
			statement.setString(3, room.getLocation());
			statement.setString(4, room.getDescription());
			statement.executeUpdate();

			try (ResultSet keys = statement.getGeneratedKeys()) {
				if (keys.next()) {
					room.setId(keys.getInt(1));
				}
			}
			return room;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the them phong hop moi.", e);
		}
	}

	@Override
	public boolean update(Room room) {
		String sql = "UPDATE rooms SET name = ?, capacity = ?, location = ?, description = ? WHERE id = ?";

		try (Connection connection = DBConnection.openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, room.getName());
			statement.setInt(2, room.getCapacity());
			statement.setString(3, room.getLocation());
			statement.setString(4, room.getDescription());
			statement.setInt(5, room.getId());
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the cap nhat phong hop.", e);
		}
	}

	@Override
	public boolean deleteById(int id) {
		String sql = "DELETE FROM rooms WHERE id = ?";

		try (Connection connection = DBConnection.openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, id);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the xoa phong hop. Co the phong da duoc dat lich.", e);
		}
	}

	private Room mapRow(ResultSet rs) throws SQLException {
		return new Room(
				rs.getInt("id"),
				rs.getString("name"),
				rs.getInt("capacity"),
				rs.getString("location"),
				rs.getString("description")
		);
	}
}
