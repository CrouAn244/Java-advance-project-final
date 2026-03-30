package dao.impl;

import dao.IEquipmentDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Equipment;
import util.DBConnection;

public class EquipmentDAOImpl implements IEquipmentDAO {
	@Override
	public List<Equipment> findAll() {
		String sql = "SELECT id, name, total_quantity, available_quantity, status FROM equipments ORDER BY id";
		List<Equipment> equipments = new ArrayList<>();

		try (Connection connection = DBConnection.openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql);
			 ResultSet rs = statement.executeQuery()) {
			while (rs.next()) {
				equipments.add(mapRow(rs));
			}
			return equipments;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the lay danh sach thiet bi.", e);
		}
	}

	@Override
	public Optional<Equipment> findById(int id) {
		String sql = "SELECT id, name, total_quantity, available_quantity, status FROM equipments WHERE id = ?";

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
			throw new IllegalStateException("Khong the tim thiet bi theo ID.", e);
		}
	}

	@Override
	public Optional<Equipment> findByName(String name) {
		String sql = "SELECT id, name, total_quantity, available_quantity, status FROM equipments WHERE LOWER(name) = LOWER(?)";

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, name);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapRow(rs));
				}
				return Optional.empty();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the tim thiet bi theo ten.", e);
		}
	}

	@Override
	public Equipment save(Equipment equipment) {
		String sql = "INSERT INTO equipments (name, total_quantity, available_quantity, status) VALUES (?, ?, ?, ?)";

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, equipment.getName());
			statement.setInt(2, equipment.getTotalQuantity());
			statement.setInt(3, equipment.getAvailableQuantity());
			statement.setString(4, equipment.getStatus());
			statement.executeUpdate();

			try (ResultSet keys = statement.getGeneratedKeys()) {
				if (keys.next()) {
					equipment.setId(keys.getInt(1));
				}
			}
			return equipment;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the them thiet bi moi.", e);
		}
	}

	@Override
	public boolean update(Equipment equipment) {
		String sql = "UPDATE equipments SET name = ?, total_quantity = ?, available_quantity = ?, status = ? WHERE id = ?";

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, equipment.getName());
			statement.setInt(2, equipment.getTotalQuantity());
			statement.setInt(3, equipment.getAvailableQuantity());
			statement.setString(4, equipment.getStatus());
			statement.setInt(5, equipment.getId());
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the cap nhat thiet bi.", e);
		}
	}

	@Override
	public boolean deleteById(int id) {
		String sql = "DELETE FROM equipments WHERE id = ?";

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, id);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the xoa thiet bi. Co the da duoc gan vao booking.", e);
		}
	}

	@Override
	public boolean updateAvailableQuantity(int id, int availableQuantity) {
		String sql = "UPDATE equipments SET available_quantity = ?, status = ? WHERE id = ?";

		try (Connection connection = DBConnection.openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, availableQuantity);
			statement.setString(2, availableQuantity > 0 ? "SAN_SANG" : "HET_HANG");
			statement.setInt(3, id);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the cap nhat so luong kha dung cua thiet bi.", e);
		}
	}

	private Equipment mapRow(ResultSet rs) throws SQLException {
		return new Equipment(
				rs.getInt("id"),
				rs.getString("name"),
				rs.getInt("total_quantity"),
				rs.getInt("available_quantity"),
				rs.getString("status")
		);
	}
}
