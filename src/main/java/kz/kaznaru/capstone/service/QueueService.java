package kz.kaznaru.capstone.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.AppointmentRepository;
import kz.kaznaru.capstone.domain.AppointmentStatus;
import kz.kaznaru.capstone.domain.ClinicException;
import kz.kaznaru.capstone.domain.Doctor;
import kz.kaznaru.capstone.domain.DoctorRepository;
import kz.kaznaru.capstone.service.event.ClinicEvent;
import kz.kaznaru.capstone.service.event.EventPublisher;
import kz.kaznaru.capstone.service.event.EventType;

/** Тірі кезек: келгенін белгілеу (талон), шақыру, аяқтау. */
public class QueueService {
    private final AppointmentRepository appointments;
    private final DoctorRepository doctors;
    private final EventPublisher events;
    private final Clock clock;
    private int ticketCounter;

    public QueueService(AppointmentRepository appointments, DoctorRepository doctors,
                        EventPublisher events, Clock clock) {
        this.appointments = appointments;
        this.doctors = doctors;
        this.events = events;
        this.clock = clock;
    }

    public Appointment checkIn(long appointmentId) {
        Appointment a = get(appointmentId);
        a.checkIn(String.format("A-%03d", ++ticketCounter), LocalDateTime.now(clock));
        appointments.save(a);
        publish(EventType.CHECKED_IN, a);
        return a;
    }

    public List<Appointment> waiting(long doctorId) {
        return appointments.findByDoctor(doctorId).stream()
            .filter(a -> a.status() == AppointmentStatus.WAITING)
            .sorted(Comparator.comparing(Appointment::checkedInAt))
            .toList();
    }

    public Optional<Appointment> callNext(long doctorId) {
        boolean busy = appointments.findByDoctor(doctorId).stream()
            .anyMatch(a -> a.status() == AppointmentStatus.IN_PROGRESS);
        if (busy) {
            throw new ClinicException("Алдымен ағымдағы қабылдауды аяқтаңыз");
        }
        Optional<Appointment> next = waiting(doctorId).stream().findFirst();
        next.ifPresent(a -> {
            a.call();
            appointments.save(a);
            publish(EventType.CALLED, a);
        });
        return next;
    }

    public void complete(long appointmentId) {
        Appointment a = get(appointmentId);
        a.complete();
        appointments.save(a);
        publish(EventType.COMPLETED, a);
    }

    public void markNoShow(long appointmentId) {
        Appointment a = get(appointmentId);
        a.markNoShow();
        appointments.save(a);
        publish(EventType.NO_SHOW, a);
    }

    private Appointment get(long id) {
        return appointments.findById(id).orElseThrow(() -> new ClinicException("Жазылу табылмады: " + id));
    }

    private void publish(EventType type, Appointment a) {
        String room = doctors.findById(a.doctorId()).map(Doctor::room).orElse("?");
        events.publish(new ClinicEvent(type, a.id(), a.patientId(), a.doctorId(), a.ticket(), room,
            a.start(), a.status().name()));
    }
}
