package kz.kaznaru.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.TimeSlot;
import kz.kaznaru.capstone.service.BookingRequest;
import org.junit.jupiter.api.Test;

class ReminderTest {
    private final Fixture f = new Fixture();

    @Test
    void remindsOnlyUpcomingBookedOnce() {
        f.book(1, 1, 0);                 // бүгін 09:00 → SMS
        Appointment b = f.book(2, 2, 0); // бүгін 09:00 → EMAIL
        Appointment c = f.book(3, 3, 0);
        f.queue.checkIn(c.id());          // кезекте — еске салу қажет емес
        TimeSlot far = new TimeSlot(f.slots.nextId(), 1, Fixture.NOW.plusDays(3), Duration.ofMinutes(20));
        f.slots.save(far);
        f.booking.book(new BookingRequest(4, far.id(), "")); // 3 күннен кейін — әзірге жоқ

        int smsBefore = f.sms.outbox().size();
        assertEquals(2, f.reminders.sendDueReminders());
        assertEquals(smsBefore + 1, f.sms.outbox().size());
        assertTrue(f.emails().contains("Еске салу"));
        assertTrue(b.reminderSent());
        assertEquals(0, f.reminders.sendDueReminders(), "second run sends nothing");
    }
}
