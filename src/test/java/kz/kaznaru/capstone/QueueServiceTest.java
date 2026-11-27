package kz.kaznaru.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.ClinicException;

class QueueServiceTest {
    private final Fixture f = new Fixture();

    @Test
    void ticketsAreSequential() {
        assertEquals("A-001", f.queue.checkIn(f.book(1, 1, 0).id()).ticket());
        assertEquals("A-002", f.queue.checkIn(f.book(2, 2, 0).id()).ticket());
    }

    @Test
    void emptyQueueReturnsNothing() {
        assertTrue(f.queue.callNext(1).isEmpty());
    }

    @Test
    void doctorMustFinishCurrentPatientFirst() {
        Appointment a = f.book(1, 1, 0);
        Appointment b = f.book(2, 1, 1);
        f.queue.checkIn(a.id());
        f.queue.checkIn(b.id());
        f.queue.callNext(1);
        assertThrows(ClinicException.class, () -> f.queue.callNext(1));
        f.queue.complete(a.id());
        assertEquals(b.id(), f.queue.callNext(1).orElseThrow().id());
    }

    @Test
    void cannotCheckInTwice() {
        Appointment a = f.book(1, 1, 0);
        f.queue.checkIn(a.id());
        assertThrows(IllegalStateException.class, () -> f.queue.checkIn(a.id()));
        assertThrows(ClinicException.class, () -> f.queue.checkIn(404));
    }
}
