package kz.kaznaru.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.ClinicException;
import kz.kaznaru.capstone.domain.TimeSlot;
import kz.kaznaru.capstone.service.BookingRequest;
import org.junit.jupiter.api.Test;

class BookingServiceTest {
    private final Fixture f = new Fixture();

    @Test
    void freeSlotsAreFutureAndSorted() {
        assertEquals(12, f.booking.freeSlots(1).size());
        TimeSlot past = new TimeSlot(f.slots.nextId(), 1, Fixture.NOW.minusHours(1), Duration.ofMinutes(20));
        f.slots.save(past);
        assertEquals(12, f.booking.freeSlots(1).size(), "past slot must be hidden");
        assertTrue(f.booking.freeSlots(1).get(0).start().isBefore(f.booking.freeSlots(1).get(1).start()));
    }

    @Test
    void bookingOccupiesSlotAndHidesItFromFreeList() {
        TimeSlot slot = f.freeSlot(1, 0);
        Appointment a = f.booking.book(new BookingRequest(1, slot.id(), "бас ауруы"));
        assertFalse(f.slots.findById(slot.id()).orElseThrow().isFree());
        assertEquals(11, f.booking.freeSlots(1).size());
        assertEquals("бас ауруы", a.complaint());
    }

    @Test
    void takenSlotCannotBeBookedTwice() {
        TimeSlot slot = f.freeSlot(1, 0);
        f.booking.book(new BookingRequest(1, slot.id(), ""));
        assertThrows(ClinicException.class, () -> f.booking.book(new BookingRequest(2, slot.id(), "")));
    }

    @Test
    void pastSlotCannotBeBooked() {
        TimeSlot past = new TimeSlot(f.slots.nextId(), 1, Fixture.NOW.minusHours(1), Duration.ofMinutes(20));
        f.slots.save(past);
        assertThrows(ClinicException.class, () -> f.booking.book(new BookingRequest(1, past.id(), "")));
    }

    @Test
    void patientCannotBeInTwoPlacesAtOnce() {
        f.book(1, 1, 0);
        TimeSlot sameTimeOtherDoctor = f.freeSlot(2, 0);
        assertThrows(ClinicException.class,
            () -> f.booking.book(new BookingRequest(1, sameTimeOtherDoctor.id(), "")));
    }

    @Test
    void unknownPatientOrSlotIsRejected() {
        assertThrows(ClinicException.class, () -> f.booking.book(new BookingRequest(99, 1, "")));
        assertThrows(ClinicException.class, () -> f.booking.book(new BookingRequest(1, 999, "")));
        assertThrows(ClinicException.class, () -> f.booking.get(42));
        assertThrows(ClinicException.class, () -> f.booking.doctor(42));
    }

    @Test
    void cancelFreesSlot() {
        Appointment a = f.book(1, 1, 0);
        f.booking.cancel(a.id());
        assertTrue(f.slots.findById(a.slotId()).orElseThrow().isFree());
        assertEquals(1, f.booking.appointmentsOf(1).size());
    }
}
