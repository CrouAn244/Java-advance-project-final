package model;

import java.time.LocalDateTime;
import model.Enum.BookingStatus;
import model.Enum.PreparationStatus;

public class Booking {
	private Integer bookingId;
	private Integer userId;
	private Integer roomId;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private BookingStatus status;
	private Integer supportStaffId;
	private PreparationStatus preparationStatus;
	private LocalDateTime createdAt;

	public Booking() {
	}

	public Booking(Integer id, Integer userId, Integer roomId, LocalDateTime startTime, LocalDateTime endTime, BookingStatus status,
				   Integer supportStaffId, PreparationStatus preparationStatus, LocalDateTime createdAt) {
		this.bookingId = id;
		this.userId = userId;
		this.roomId = roomId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.status = status;
		this.supportStaffId = supportStaffId;
		this.preparationStatus = preparationStatus;
		this.createdAt = createdAt;
	}

	public Integer getId() {
		return bookingId;
	}

	public void setId(Integer id) {
		this.bookingId = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public BookingStatus getStatus() {
		return status;
	}

	public void setStatus(BookingStatus status) {
		this.status = status;
	}

	public Integer getSupportStaffId() {
		return supportStaffId;
	}

	public void setSupportStaffId(Integer supportStaffId) {
		this.supportStaffId = supportStaffId;
	}

	public PreparationStatus getPreparationStatus() {
		return preparationStatus;
	}

	public void setPreparationStatus(PreparationStatus preparationStatus) {
		this.preparationStatus = preparationStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "Booking{" +
				"bookingId=" + bookingId +
				", userId=" + userId +
				", roomId=" + roomId +
				", startTime=" + startTime +
				", endTime=" + endTime +
				", status=" + status +
				", supportStaffId=" + supportStaffId +
				", preparationStatus=" + preparationStatus +
				", createdAt=" + createdAt +
				'}';
	}
}
