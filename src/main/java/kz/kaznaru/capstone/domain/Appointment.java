package kz.kaznaru.capstone.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/** Пациенттің нақты слотқа жазылуы. */
public final class Appointment {
    private final long id;
    private final long patientId;
    private final long doctorId;
    private final long slotId;
    private final LocalDateTime start;
    private final String complaint;
    private AppointmentStatus status = AppointmentStatus.BOOKED;
    private String ticket;
    private LocalDateTime checkedInAt;
    private boolean reminderSent;

    public Appointment(long id, long patientId, long doctorId, long slotId,
                       LocalDateTime start, String complaint) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.slotId = slotId;
        this.start = Objects.requireNonNull(start, "start");
        this.complaint = complaint == null ? "" : complaint;
    }

    public void cancel() {
        if (status != AppointmentStatus.BOOKED && status != AppointmentStatus.WAITING) {
            throw new IllegalStateException("cannot cancel appointment in status " + status);
        }
        status = AppointmentStatus.CANCELLED;
    }

    public void checkIn(String newTicket, LocalDateTime at) {
        if (status != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("cannot check in appointment in status " + status);
        }
        status = AppointmentStatus.WAITING;
        ticket = newTicket;
        checkedInAt = at;
    }

    public void call() {
        if (status != AppointmentStatus.WAITING) {
            throw new IllegalStateException("cannot call appointment in status " + status);
        }
        status = AppointmentStatus.IN_PROGRESS;
    }

    public void complete() {
        if (status != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("cannot complete appointment in status " + status);
        }
        status = AppointmentStatus.COMPLETED;
    }

    public void markNoShow() {
        if (status != AppointmentStatus.BOOKED && status != AppointmentStatus.WAITING) {
            throw new IllegalStateException("cannot mark no-show in status " + status);
        }
        status = AppointmentStatus.NO_SHOW;
    }

    /** Undo үшін: бұрынғы күйді қайтарады (тек Command қолданады). */
    public void restoreStatus(AppointmentStatus previous) {
        this.status = Objects.requireNonNull(previous);
    }

    /** Слотты алып тұратын күй ме (бос емес). */
    public boolean isActive() {
        return status == AppointmentStatus.BOOKED
            || status == AppointmentStatus.WAITING
            || status == AppointmentStatus.IN_PROGRESS;
    }

    public void markReminderSent() {
        this.reminderSent = true;
    }

    public long id() {
        return id;
    }

    public long patientId() {
        return patientId;
    }

    public long doctorId() {
        return doctorId;
    }

    public long slotId() {
        return slotId;
    }

    public LocalDateTime start() {
        return start;
    }

    public String complaint() {
        return complaint;
    }

    public AppointmentStatus status() {
        return status;
    }

    public String ticket() {
        return ticket;
    }

    public LocalDateTime checkedInAt() {
        return checkedInAt;
    }

    public boolean reminderSent() {
        return reminderSent;
    }
}
