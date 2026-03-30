package model;

import java.time.LocalDateTime;
import model.Enum.Role;

public class User {
	private Integer id;
	private String username;
	private String password;
	private String fullName;
	private String phone;
	private String email;
	private Role role;
	private LocalDateTime createdAt;

	public User() {
	}

	public User(Integer id, String username, String password, String fullName, String phone, String email, Role role, LocalDateTime createdAt) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.fullName = fullName;
		this.phone = phone;
		this.email = email;
		this.role = role;
		this.createdAt = createdAt;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", username='" + username + '\'' +
				", fullName='" + fullName + '\'' +
				", phone='" + phone + '\'' +
				", email='" + email + '\'' +
				", role=" + role +
				", createdAt=" + createdAt +
				'}';
	}
}
