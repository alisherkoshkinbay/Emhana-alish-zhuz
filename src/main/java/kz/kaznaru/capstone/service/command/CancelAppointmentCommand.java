package kz.kaznaru.capstone.service.command;

import kz.kaznaru.capstone.domain.AppointmentStatus;
import kz.kaznaru.capstone.service.BookingService;

/** Жазылудан бас тарту. Undo — бұрынғы күйді және слотты дәл қалпына келтіреді. */
public class CancelAppointmentCommand implements Command {
    private final BookingService receiver;
    private final long appointmentId;
    private AppointmentStatus previous;

    public CancelAppointmentCommand(BookingService receiver, long appointmentId) {
        this.receiver = receiver;
        this.appointmentId = appointmentId;
    }

    @Override
    public void execute() {
        previous = receiver.cancel(appointmentId);
    }

    @Override
    public void undo() {
        if (previous == null) {
            throw new IllegalStateException("command was not executed");
        }
        receiver.restoreCancelled(appointmentId, previous);
    }

    @Override
    public String description() {
        return "Бас тарту #" + appointmentId;
    }
}
