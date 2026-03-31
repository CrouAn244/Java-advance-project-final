package util;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class Validator {
	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,30}$");
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{9,15}$");

	private Validator() {
	}

	public static String requireNotBlank(String value, String fieldName) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " khong duoc de trong.");
		}
		return value.trim();
	}

	public static String validateUsername(String username) {
		String normalized = requireNotBlank(username, "Ten dang nhap");
		if (!USERNAME_PATTERN.matcher(normalized).matches()) {
			throw new IllegalArgumentException("Ten dang nhap phai dai 4-30 ky tu va chi gom chu cai, so hoac dau gach duoi.");
		}
		return normalized;
	}

	public static String validatePassword(String password) {
		String normalized = requireNotBlank(password, "Mat khau");
		if (normalized.length() < 8) {
			throw new IllegalArgumentException("Mat khau phai co it nhat 8 ky tu.");
		}

		boolean hasUpper = false;
		boolean hasLower = false;
		boolean hasDigit = false;
		for (char c : normalized.toCharArray()) {
			if (Character.isUpperCase(c)) {
				hasUpper = true;
			} else if (Character.isLowerCase(c)) {
				hasLower = true;
			} else if (Character.isDigit(c)) {
				hasDigit = true;
			}
		}
		if (!hasUpper || !hasLower || !hasDigit) {
			throw new IllegalArgumentException("Mat khau phai co chu hoa, chu thuong va chu so.");
		}
		return normalized;
	}

	public static String validateFullName(String fullName) {
		String normalized = requireNotBlank(fullName, "Ho ten");
		if (normalized.length() < 2 || normalized.length() > 100) {
			throw new IllegalArgumentException("Ho ten phai dai tu 2 den 100 ky tu.");
		}
		return normalized;
	}

	public static String validateEmail(String email) {
		String normalized = requireNotBlank(email, "Email");
		if (!EMAIL_PATTERN.matcher(normalized).matches()) {
			throw new IllegalArgumentException("Dinh dang email khong hop le.");
		}
		return normalized;
	}

	public static String validatePhone(String phone) {
		String normalized = requireNotBlank(phone, "So dien thoai").replace(" ", "");
		if (!PHONE_PATTERN.matcher(normalized).matches()) {
			throw new IllegalArgumentException("So dien thoai phai co 9-15 chu so va co the bat dau bang +.");
		}
		return normalized;
	}

	public static LocalDateTime validateNotPastDateTime(LocalDateTime dateTime, String fieldName) {
		if (dateTime == null) {
			throw new IllegalArgumentException(fieldName + " khong duoc de trong.");
		}
		if (dateTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException(fieldName + " khong duoc o qua khu.");
		}
		return dateTime;
	}

	public static int validateParticipantCount(int participantCount) {
		if (participantCount <= 0) {
			throw new IllegalArgumentException("So nguoi tham gia phai lon hon 0.");
		}
		return participantCount;
	}

	public static void validateRoomCapacityVsParticipants(int roomCapacity, int participantCount) {
		if (roomCapacity <= participantCount) {
			throw new IllegalArgumentException("So nguoi tham gia phai be hon suc chua phong.");
		}
	}
}
