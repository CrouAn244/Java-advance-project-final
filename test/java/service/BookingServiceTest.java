package service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class BookingServiceTest {
	private final BookingService bookingService = new BookingService();

	@Test
	void isTimeConflict_shouldReturnTrue_whenTimeRangesOverlap() {
		LocalDateTime existingStart = LocalDateTime.of(2026, 4, 1, 9, 0);
		LocalDateTime existingEnd = LocalDateTime.of(2026, 4, 1, 11, 0);
		LocalDateTime newStart = LocalDateTime.of(2026, 4, 1, 10, 0);
		LocalDateTime newEnd = LocalDateTime.of(2026, 4, 1, 12, 0);

		assertTrue(bookingService.isTimeConflict(existingStart, existingEnd, newStart, newEnd));
	}

	@Test
	void isTimeConflict_shouldReturnFalse_whenRangesTouchAtBoundary() {
		LocalDateTime existingStart = LocalDateTime.of(2026, 4, 1, 9, 0);
		LocalDateTime existingEnd = LocalDateTime.of(2026, 4, 1, 11, 0);
		LocalDateTime newStart = LocalDateTime.of(2026, 4, 1, 11, 0);
		LocalDateTime newEnd = LocalDateTime.of(2026, 4, 1, 12, 0);

		assertFalse(bookingService.isTimeConflict(existingStart, existingEnd, newStart, newEnd));
	}
}
