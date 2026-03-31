package dao.impl;

import dao.IBookingDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Booking;
import model.BookingDetail;
import model.enums.BookingStatus;
import model.enums.PreparationStatus;
import util.DBConnection;

public class BookingDAOImpl implements IBookingDAO {
	@Override
	public boolean existsTimeConflict(int roomId, LocalDateTime requestedStart, LocalDateTime requestedEnd) {
		String sql = "SELECT 1 FROM bookings "
				+ "WHERE room_id = ? "
				+ "AND status IN ('PENDING', 'APPROVED') "
				+ "AND start_time < ? "
				+ "AND end_time > ? "
				+ "LIMIT 1";

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, roomId);
			statement.setTimestamp(2, Timestamp.valueOf(requestedEnd));
			statement.setTimestamp(3, Timestamp.valueOf(requestedStart));
			try (ResultSet rs = statement.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the kiem tra xung dot lich dat phong.", e);
		}
	}

	@Override
	public Booking save(Booking booking, List<BookingDetail> details) {
		String insertBooking = "INSERT INTO bookings (user_id, room_id, start_time, end_time, status, support_staff_id, preparation_status) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		String insertDetail = "INSERT INTO booking_details (booking_id, equipment_id, service_id, quantity) VALUES (?, ?, ?, ?)";

		try (Connection connection = DBConnection.openConnection()) {
			connection.setAutoCommit(false);
			try {
				try (PreparedStatement bookingStatement = connection.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS)) {
					bookingStatement.setInt(1, booking.getUserId());
					bookingStatement.setInt(2, booking.getRoomId());
					bookingStatement.setTimestamp(3, Timestamp.valueOf(booking.getStartTime()));
					bookingStatement.setTimestamp(4, Timestamp.valueOf(booking.getEndTime()));
					bookingStatement.setString(5, booking.getStatus().name());
					if (booking.getSupportStaffId() != null) {
						bookingStatement.setInt(6, booking.getSupportStaffId());
					} else {
						bookingStatement.setNull(6, Types.INTEGER);
					}
					bookingStatement.setString(7, booking.getPreparationStatus().name());
					bookingStatement.executeUpdate();

					try (ResultSet keys = bookingStatement.getGeneratedKeys()) {
						if (keys.next()) {
							booking.setId(keys.getInt(1));
						}
					}
				}

				if (details != null && !details.isEmpty()) {
					try (PreparedStatement detailStatement = connection.prepareStatement(insertDetail)) {
						for (BookingDetail detail : details) {
							detailStatement.setInt(1, booking.getId());
							if (detail.getEquipmentId() != null) {
								detailStatement.setInt(2, detail.getEquipmentId());
							} else {
								detailStatement.setNull(2, Types.INTEGER);
							}
							if (detail.getServiceId() != null) {
								detailStatement.setInt(3, detail.getServiceId());
							} else {
								detailStatement.setNull(3, Types.INTEGER);
							}
							detailStatement.setInt(4, detail.getQuantity());
							detailStatement.addBatch();
						}
						detailStatement.executeBatch();
					}
				}

				connection.commit();
				return booking;
			} catch (SQLException e) {
				connection.rollback();
				throw new IllegalStateException("Khong the tao yeu cau dat phong.", e);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the luu du lieu dat phong.", e);
		}
	}

	@Override
	public List<Booking> findAll() {
		String sql = "SELECT id, user_id, room_id, start_time, end_time, status, support_staff_id, preparation_status, created_at "
				+ "FROM bookings ORDER BY created_at DESC";
		List<Booking> bookings = new ArrayList<>();

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql);
				 ResultSet rs = statement.executeQuery()) {
			while (rs.next()) {
				bookings.add(mapRow(rs));
			}
			return bookings;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the lay danh sach booking.", e);
		}
	}

	@Override
	public List<Booking> findByUserId(int userId) {
		String sql = "SELECT id, user_id, room_id, start_time, end_time, status, support_staff_id, preparation_status, created_at "
				+ "FROM bookings WHERE user_id = ? ORDER BY start_time ASC";
		List<Booking> bookings = new ArrayList<>();

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, userId);
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					bookings.add(mapRow(rs));
				}
			}
			return bookings;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the lay danh sach lich dat phong.", e);
		}
	}

	@Override
	public Optional<Booking> findById(int bookingId) {
		String sql = "SELECT id, user_id, room_id, start_time, end_time, status, support_staff_id, preparation_status, created_at "
				+ "FROM bookings WHERE id = ?";

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, bookingId);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return Optional.of(mapRow(rs));
				}
				return Optional.empty();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the tim booking theo ID.", e);
		}
	}

	@Override
	public List<Booking> findBySupportStaffId(int supportStaffId) {
		String sql = "SELECT id, user_id, room_id, start_time, end_time, status, support_staff_id, preparation_status, created_at "
				+ "FROM bookings WHERE support_staff_id = ? AND status = 'APPROVED' ORDER BY start_time ASC";
		List<Booking> bookings = new ArrayList<>();

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, supportStaffId);
			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					bookings.add(mapRow(rs));
				}
			}
			return bookings;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the lay cong viec da phan cong cho Support.", e);
		}
	}

	@Override
	public boolean updateApproval(int bookingId, BookingStatus status, Integer supportStaffId, PreparationStatus preparationStatus) {
		String sql = "UPDATE bookings SET status = ?, support_staff_id = ?, preparation_status = ? WHERE id = ?";

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, status.name());
			if (supportStaffId != null) {
				statement.setInt(2, supportStaffId);
			} else {
				statement.setNull(2, Types.INTEGER);
			}
			statement.setString(3, preparationStatus.name());
			statement.setInt(4, bookingId);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the cap nhat phe duyet booking.", e);
		}
	}

	@Override
	public boolean updatePreparationStatus(int bookingId, int supportStaffId, PreparationStatus preparationStatus) {
		String sql = "UPDATE bookings SET preparation_status = ? "
				+ "WHERE id = ? AND support_staff_id = ? AND status = 'APPROVED'";

		try (Connection connection = DBConnection.openConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, preparationStatus.name());
			statement.setInt(2, bookingId);
			statement.setInt(3, supportStaffId);
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new IllegalStateException("Khong the cap nhat trang thai chuan bi.", e);
		}
	}

	@Override
	public boolean deletePendingByUser(int bookingId, int userId) {
		String deleteDetails = "DELETE FROM booking_details WHERE booking_id = ?";
		String deleteBooking = "DELETE FROM bookings WHERE id = ? AND user_id = ? AND status = 'PENDING'";

		try (Connection connection = DBConnection.openConnection()) {
			connection.setAutoCommit(false);
			try {
				try (PreparedStatement detailStatement = connection.prepareStatement(deleteDetails)) {
					detailStatement.setInt(1, bookingId);
					detailStatement.executeUpdate();
				}

				int affectedRows;
				try (PreparedStatement bookingStatement = connection.prepareStatement(deleteBooking)) {
					bookingStatement.setInt(1, bookingId);
					bookingStatement.setInt(2, userId);
					affectedRows = bookingStatement.executeUpdate();
				}

				if (affectedRows == 0) {
					connection.rollback();
					return false;
				}

				connection.commit();
				return true;
			} catch (SQLException e) {
				connection.rollback();
				throw new IllegalStateException("Khong the huy booking dang cho duyet.", e);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Loi SQL khi huy booking.", e);
		}
	}

	private Booking mapRow(ResultSet rs) throws SQLException {
		Timestamp createdAt = rs.getTimestamp("created_at");
		return new Booking(
				rs.getInt("id"),
				rs.getInt("user_id"),
				rs.getInt("room_id"),
				rs.getTimestamp("start_time").toLocalDateTime(),
				rs.getTimestamp("end_time").toLocalDateTime(),
				BookingStatus.valueOf(rs.getString("status")),
				(Integer) rs.getObject("support_staff_id"),
				PreparationStatus.valueOf(rs.getString("preparation_status")),
				createdAt != null ? createdAt.toLocalDateTime() : null
		);
	}
}
