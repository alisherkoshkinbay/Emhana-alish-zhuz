package kz.kaznaru.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.AppointmentStatus;
import org.junit.jupiter.api.Test;

class AppointmentTest {
    private final Appointment a = new Appointment(1, 1, 1, 1, LocalDateTime.of(2026, 11, 18, 9, 0), null);

    @Test
    void happyPathBookedWaitingInProgressCompleted() {
        a.checkIn("A-001", LocalDateTime.of(2026, 11, 18, 8, 50));
        assertEquals(AppointmentStatus.WAITING, a.status());
        a.call();
        assertEquals(AppointmentStatus.IN_PROGRESS, a.status());
        a.complete();
        assertEquals(AppointmentStatus.COMPLETED, a.status());
        assertFalse(a.isActive());
    }

    @Test
    void cannotCancelCompleted() {
        a.checkIn("A-001", LocalDateTime.now());
        a.call();
        a.complete();
        assertThrows(IllegalStateException.class, a::cancel);
    }

    @Test
    void cancelledIsNotActive() {
        assertTrue(a.isActive());
        a.cancel();
        assertFalse(a.isActive());
        assertThrows(IllegalStateException.class, a::call);
    }
}
