package kz.kaznaru.capstone;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import kz.kaznaru.capstone.domain.AppointmentStatus;
import kz.kaznaru.capstone.service.event.ClinicEvent;
import kz.kaznaru.capstone.service.event.ClinicEventListener;
import kz.kaznaru.capstone.service.event.EventPublisher;
import kz.kaznaru.capstone.service.event.EventType;
import kz.kaznaru.capstone.service.queue.QueueBoard;

/** Observer: жариялаушы оқиғаны барлық тыңдаушыға жеткізеді, кезек экраны соған қарай жаңарады. */
class ObserverTest {
    private static final LocalDateTime NINE = LocalDateTime.of(2026, 11, 18, 9, 0);

    private static ClinicEvent event(EventType type, long id, String ticket, AppointmentStatus state) {
        return new ClinicEvent(type, id, 1, 1, ticket, "101", NINE, state);
    }

    @Test
    void everySubscriberReceivesTheEvent() {
        EventPublisher publisher = new EventPublisher();
        List<ClinicEvent> first = new ArrayList<>();
        List<ClinicEvent> second = new ArrayList<>();
        publisher.subscribe(first::add);
        publisher.subscribe(second::add);
        publisher.publish(event(EventType.BOOKED, 1, null, AppointmentStatus.BOOKED));
        assertEquals(1, first.size());
        assertEquals(1, second.size());
    }

    @Test
    void queueBoardShowsWaitingAndCalledTickets() {
        EventPublisher publisher = new EventPublisher();
        QueueBoard board = new QueueBoard();
        publisher.subscribe(board);
        publisher.publish(event(EventType.CHECKED_IN, 1, "A-001", AppointmentStatus.WAITING));
        publisher.publish(event(EventType.CHECKED_IN, 2, "A-002", AppointmentStatus.WAITING));
        assertEquals(2, board.waitingCount());
        publisher.publish(event(EventType.CALLED, 1, "A-001", AppointmentStatus.IN_PROGRESS));
        String screen = String.join("\n", board.render());
        assertTrue(screen.contains("A-001  →  101-кабинет"), screen);
        assertTrue(screen.contains("A-002 (101-каб.)"), screen);
        publisher.publish(event(EventType.COMPLETED, 1, "A-001", AppointmentStatus.COMPLETED));
        assertFalse(String.join("\n", board.render()).contains("A-001"));
    }

    @Test
    void restoredWaitingTicketReturnsToScreen() {
        QueueBoard board = new QueueBoard();
        board.onEvent(event(EventType.CHECKED_IN, 1, "A-001", AppointmentStatus.WAITING));
        board.onEvent(event(EventType.CANCELLED, 1, "A-001", AppointmentStatus.CANCELLED));
        assertEquals(0, board.waitingCount());
        board.onEvent(event(EventType.RESTORED, 1, "A-001", AppointmentStatus.WAITING));
        assertEquals(1, board.waitingCount());
    }

    @Test
    void failingListenerDoesNotBreakOthers() {
        EventPublisher publisher = new EventPublisher();
        List<ClinicEvent> got = new ArrayList<>();
        ClinicEventListener broken = e -> {
            throw new IllegalStateException("boom");
        };
        publisher.subscribe(broken);
        publisher.subscribe(got::add);
        publisher.publish(event(EventType.BOOKED, 1, null, AppointmentStatus.BOOKED));
        assertEquals(1, got.size());
        assertEquals(1, publisher.failedDeliveries());
        publisher.unsubscribe(broken);
        publisher.publish(event(EventType.BOOKED, 1, null, AppointmentStatus.BOOKED));
        assertEquals(1, publisher.failedDeliveries());
    }
}
