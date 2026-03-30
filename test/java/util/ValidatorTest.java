package util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ValidatorTest {
	@Test
	void validateNotPastDateTime_shouldThrow_whenDateTimeInPast() {
		LocalDateTime past = LocalDateTime.now().minusHours(1);

		assertThrows(IllegalArgumentException.class, () -> Validator.validateNotPastDateTime(past, "Thoi gian bat dau"));
	}

	@Test
	void validateRoomCapacityVsParticipants_shouldThrow_whenCapacityNotGreaterThanParticipants() {
		assertThrows(IllegalArgumentException.class, () -> Validator.validateRoomCapacityVsParticipants(10, 10));
		assertThrows(IllegalArgumentException.class, () -> Validator.validateRoomCapacityVsParticipants(10, 12));
	}

	@Test
	void validateRoomCapacityVsParticipants_shouldPass_whenCapacityGreaterThanParticipants() {
		assertDoesNotThrow(() -> Validator.validateRoomCapacityVsParticipants(10, 9));
	}
}
