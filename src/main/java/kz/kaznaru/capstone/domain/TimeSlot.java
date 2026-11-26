package kz.kaznaru.capstone.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/** Дәрігердің кестесіндегі бір уақыт аралығы. Бір слотта ең көбі бір белсенді жазылу болады. */
public final class TimeSlot {
    private final long id;
    private final long doctorId;
    private final LocalDateTime start;
    private final Duration duration;
    private Long appointmentId;

    public TimeSlot(long id, long doctorId, LocalDateTime start, Duration duration) {
        this.id = id;
        this.doctorId = doctorId;
        this.start = Objects.requireNonNull(start, "start");
        this.duration = Objects.requireNonNull(duration, "duration");
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("slot duration must be positive");
        }
    }

    public long id() {
        return id;
    }

    public long doctorId() {
        return doctorId;
    }

    public LocalDateTime start() {
        return start;
    }

    public LocalDateTime end() {
        return start.plus(duration);
    }

    public boolean isFree() {
        return appointmentId == null;
    }

    public Optional<Long> appointmentId() {
        return Optional.ofNullable(appointmentId);
    }

    public void occupy(long byAppointmentId) {
        if (!isFree()) {
            throw new ClinicException("Слот бос емес: " + start);
        }
        this.appointmentId = byAppointmentId;
    }

    public void release() {
        this.appointmentId = null;
    }
}
