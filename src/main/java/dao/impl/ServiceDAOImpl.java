package dao.impl;

import dao.IServiceDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Service;
import util.DBConnection;

public class ServiceDAOImpl implements IServiceDAO {
	@Override
	public List<Service> findAll() {
		String sql = "SELECT id, name, price FROM services ORDER BY id";
		List<Service> services = new ArrayList<>();

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql);
				 ResultSet rs = statement.executeQuery()) {
			while (rs.next()) {
				services.add(mapRow(rs));
			}
			return services;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the lay danh sach dich vu.", e);
		}
	}

	@Override
	public Optional<Service> findById(int id) {
		String sql = "SELECT id, name, price FROM services WHERE id = ?";

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
			throw new IllegalStateException("Khong the tim dich vu theo ID.", e);
		}
	}

	@Override
	public Optional<Service> findByName(String name) {
		String sql = "SELECT id, name, price FROM services WHERE LOWER(name) = LOWER(?)";

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
			throw new IllegalStateException("Khong the tim dich vu theo ten.", e);
		}
	}

	@Override
	public Service save(Service service) {
		String sql = "INSERT INTO services (name, price) VALUES (?, ?)";

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, service.getName());
			statement.setBigDecimal(2, service.getPrice());
			statement.executeUpdate();

			try (ResultSet keys = statement.getGeneratedKeys()) {
				if (keys.next()) {
					service.setId(keys.getInt(1));
				}
			}
			return service;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the them dich vu moi.", e);
		}
	}

	@Override
	public boolean update(Service service) {
		String sql = "UPDATE services SET name = ?, price = ? WHERE id = ?";

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, service.getName());
			statement.setBigDecimal(2, service.getPrice());
			statement.setInt(3, service.getId());
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the cap nhat dich vu.", e);
		}
	}

	@Override
	public boolean deleteById(int id) {
		String sql = "DELETE FROM services WHERE id = ?";

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, id);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the xoa dich vu. Co the da duoc gan vao booking.", e);
		}
	}

	private Service mapRow(ResultSet rs) throws SQLException {
		return new Service(
				rs.getInt("id"),
				rs.getString("name"),
				rs.getBigDecimal("price")
		);
	}
}
