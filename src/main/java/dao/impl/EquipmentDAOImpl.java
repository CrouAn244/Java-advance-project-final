package dao.impl;

import dao.IEquipmentDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
