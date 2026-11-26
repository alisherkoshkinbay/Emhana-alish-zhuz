package kz.kaznaru.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.ClinicException;
import kz.kaznaru.capstone.domain.TimeSlot;
import kz.kaznaru.capstone.service.BookingRequest;
import kz.kaznaru.capstone.service.command.BookAppointmentCommand;
import kz.kaznaru.capstone.service.command.CancelAppointmentCommand;
import kz.kaznaru.capstone.service.command.CommandHistory;
import org.junit.jupiter.api.Test;

/** 5.2-талап: Command арқылы бас тарту және күйдің ДӘЛ қалпына келуі. */
class CommandUndoTest {
    private final Fixture f = new Fixture();
    private final CommandHistory history = new CommandHistory();

    @Test
    void undoCancelRestoresExactStateTicketAndSlot() {
        Appointment a = f.book(1, 1, 0);
        f.queue.checkIn(a.id());
        String stateBefore = a.status().name();
        String ticketBefore = a.ticket();
        var checkedInBefore = a.checkedInAt();

        history.run(new CancelAppointmentCommand(f.booking, a.id()));
        assertFalse(a.isActive());
        assertTrue(f.slots.findById(a.slotId()).orElseThrow().isFree());

        assertEquals("Бас тарту #" + a.id(), history.undoLast().orElseThrow());
        assertEquals(stateBefore, a.status().name());
        assertEquals(ticketBefore, a.ticket());
        assertEquals(checkedInBefore, a.checkedInAt());
        assertEquals(a.id(), f.slots.findById(a.slotId()).orElseThrow().appointmentId().orElseThrow().longValue());
        assertEquals(1, f.board.waitingCount(), "queue screen shows the restored ticket again");
    }

    @Test
    void undoBookRemovesAppointmentAndFreesSlot() {
        TimeSlot slot = f.freeSlot(1, 0);
        Appointment a = history.run(new BookAppointmentCommand(f.booking,
            new BookingRequest(2, slot.id(), ""))).result();
        assertEquals("Жазылу #" + a.id(), history.undoLast().orElseThrow());
        assertTrue(f.booking.find(a.id()).isEmpty());
        assertTrue(f.slots.findById(slot.id()).orElseThrow().isFree());
    }

    @Test
    void undoFailsIfSlotWasTakenMeanwhile() {
        Appointment a = f.book(1, 1, 0);
        history.run(new CancelAppointmentCommand(f.booking, a.id()));
        f.booking.book(new BookingRequest(2, a.slotId(), ""));
        assertThrows(ClinicException.class, history::undoLast);
        assertEquals(0, history.size(), "failed command is discarded");
    }

    @Test
    void undoOnEmptyHistoryReturnsNothing() {
        assertTrue(history.undoLast().isEmpty());
    }

    @Test
    void notExecutedCommandCannotBeUndone() {
        assertThrows(IllegalStateException.class, () -> new CancelAppointmentCommand(f.booking, 1).undo());
        assertThrows(IllegalStateException.class,
            () -> new BookAppointmentCommand(f.booking, new BookingRequest(1, 1, "")).undo());
    }
}
