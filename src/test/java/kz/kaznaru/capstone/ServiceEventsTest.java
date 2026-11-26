package kz.kaznaru.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.service.event.ClinicEvent;
import kz.kaznaru.capstone.service.event.EventType;
import org.junit.jupiter.api.Test;

/** Сервистер оқиғаларды дұрыс ретпен жариялайды; кезек экраны мен хабарлама соларды естиді. */
class ServiceEventsTest {
    private final Fixture f = new Fixture();

    @Test
    void servicesPublishEventsInOrder() {
        Appointment a = f.book(1, 1, 0);
        f.queue.checkIn(a.id());
        f.queue.callNext(1);
        f.queue.complete(a.id());
        List<EventType> types = f.received.stream().map(ClinicEvent::type).toList();
        assertEquals(List.of(EventType.BOOKED, EventType.CHECKED_IN, EventType.CALLED, EventType.COMPLETED), types);
    }

    @Test
    void queueBoardFollowsTheRealQueue() {
        Appointment a = f.book(1, 1, 0);
        Appointment b = f.book(3, 1, 1);
        f.queue.checkIn(a.id());
        f.queue.checkIn(b.id());
        f.queue.callNext(1);
        String screen = String.join("\n", f.board.render());
        assertTrue(screen.contains("A-001  →  101-кабинет"), screen);
        assertTrue(screen.contains("A-002 (101-каб.)"), screen);
    }

    @Test
    void patientIsNotifiedThroughPreferredChannel() {
        f.book(1, 1, 0);
        f.book(2, 1, 1);
        assertEquals(1, f.sms.outbox().size(), "patient 1 prefers SMS");
        assertTrue(f.sms.outbox().get(0).contains("77011112233"));
        assertTrue(f.emails().contains("erlan@mail.kz"), "patient 2 prefers EMAIL");
    }
}
