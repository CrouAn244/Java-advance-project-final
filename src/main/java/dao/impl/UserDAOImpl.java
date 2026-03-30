package dao.impl;

import dao.IUserDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Enum.Role;
import model.User;
import util.DBConnection;

public class UserDAOImpl implements IUserDAO {
	@Override
	public Optional<User> findByUsername(String username) {
		String sql = "SELECT id, username, password, full_name, phone, email, role, created_at FROM users WHERE username = ?";

		try (Connection connection = DBConnection.openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, username);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapRow(rs));
				}
				return Optional.empty();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the tim user theo username.", e);
		}
	}

	@Override
	public Optional<User> findByEmail(String email) {
		String sql = "SELECT id, username, password, full_name, phone, email, role, created_at FROM users WHERE email = ?";

		try (Connection connection = DBConnection.openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, email);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapRow(rs));
				}
				return Optional.empty();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the tim user theo email.", e);
		}
	}

	@Override
	public Optional<User> findByUsernameOrEmail(String identifier) {
		String sql = "SELECT id, username, password, full_name, phone, email, role, created_at FROM users WHERE username = ? OR email = ?";

		try (Connection connection = DBConnection.openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, identifier);
			statement.setString(2, identifier);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapRow(rs));
				}
				return Optional.empty();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the tim user.", e);
		}
	}

	@Override
	public List<User> findByRole(Role role) {
		String sql = "SELECT id, username, password, full_name, phone, email, role, created_at FROM users WHERE role = ? ORDER BY id";
		List<User> users = new ArrayList<>();

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, role.name());
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					users.add(mapRow(rs));
				}
			}
			return users;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the lay danh sach user theo vai tro.", e);
		}
	}

	@Override
	public User save(User user) {
		String sql = "INSERT INTO users (username, password, full_name, phone, email, role) VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection connection = DBConnection.openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, user.getUsername());
			statement.setString(2, user.getPassword());
			statement.setString(3, user.getFullName());
			statement.setString(4, user.getPhone());
			statement.setString(5, user.getEmail());
			statement.setString(6, user.getRole().name());
			statement.executeUpdate();

			try (ResultSet keys = statement.getGeneratedKeys()) {
				if (keys.next()) {
					user.setId(keys.getInt(1));
				}
			}
			return findByUsername(user.getUsername()).orElse(user);
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the tao user moi.", e);
		}
	}

	private User mapRow(ResultSet rs) throws SQLException {
		Timestamp createdAt = rs.getTimestamp("created_at");
		LocalDateTime createdDateTime = createdAt != null ? createdAt.toLocalDateTime() : null;
		return new User(
				rs.getInt("id"),
				rs.getString("username"),
				rs.getString("password"),
				rs.getString("full_name"),
				rs.getString("phone"),
				rs.getString("email"),
				Role.valueOf(rs.getString("role")),
				createdDateTime
		);
	}
}
