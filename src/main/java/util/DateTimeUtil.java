package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtil {
	private static final DateTimeFormatter USER_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private DateTimeUtil() {
	}

	public static LocalDateTime parseUserDateTime(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Thoi gian khong duoc de trong.");
		}
		try {
			return LocalDateTime.parse(value.trim(), USER_FORMATTER);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Sai dinh dang thoi gian. Dung mau yyyy-MM-dd HH:mm (vd: 2026-03-30 09:00).");
		}
	}

	public static String formatUserDateTime(LocalDateTime value) {
		if (value == null) {
			return "";
		}
		return value.format(USER_FORMATTER);
	}
}
