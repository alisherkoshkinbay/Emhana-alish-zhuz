package kz.kaznaru.capstone.service.command;

import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.service.BookingRequest;
import kz.kaznaru.capstone.service.BookingService;

/** Жазылу. Undo — жазылуды жояды және слотты босатады. */
public class BookAppointmentCommand implements Command {
    private final BookingService receiver;
    private final BookingRequest request;
    private Appointment result;

    public BookAppointmentCommand(BookingService receiver, BookingRequest request) {
        this.receiver = receiver;
        this.request = request;
    }

    @Override
    public void execute() {
        result = receiver.book(request);
    }

    @Override
    public void undo() {
        if (result == null) {
            throw new IllegalStateException("command was not executed");
        }
        receiver.removeBooking(result.id());
    }

    @Override
    public String description() {
        return "Жазылу #" + (result == null ? "?" : result.id());
    }

    public Appointment result() {
        return result;
    }
}
