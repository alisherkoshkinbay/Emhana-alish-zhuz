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
import kz.kaznaru.capstone.domain.Patient;
import kz.kaznaru.capstone.domain.PatientRepository;
import kz.kaznaru.capstone.domain.SlotRepository;
import kz.kaznaru.capstone.domain.TimeSlot;
import kz.kaznaru.capstone.service.event.ClinicEvent;
import kz.kaznaru.capstone.service.event.EventPublisher;
import kz.kaznaru.capstone.service.event.EventType;

/** Жазылу, бас тарту және олардың кері операциялары. Command-тардың receiver-і. */
public class BookingService {
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final SlotRepository slots;
    private final AppointmentRepository appointments;
    private final EventPublisher events;
    private final Clock clock;

    public BookingService(DoctorRepository doctors, PatientRepository patients, SlotRepository slots,
                          AppointmentRepository appointments, EventPublisher events, Clock clock) {
        this.doctors = doctors;
        this.patients = patients;
        this.slots = slots;
        this.appointments = appointments;
        this.events = events;
        this.clock = clock;
    }

    public List<Doctor> doctors() {
        return doctors.findAll();
    }

    public List<Patient> patients() {
        return patients.findAll();
    }

    public Doctor doctor(long id) {
        return doctors.findById(id).orElseThrow(() -> new ClinicException("Дәрігер табылмады: " + id));
    }

    public Patient patient(long id) {
        return patients.findById(id).orElseThrow(() -> new ClinicException("Пациент табылмады: " + id));
    }

    public List<TimeSlot> freeSlots(long doctorId) {
        LocalDateTime now = LocalDateTime.now(clock);
        return slots.findByDoctor(doctorId).stream()
            .filter(TimeSlot::isFree)
            .filter(s -> s.start().isAfter(now))
            .sorted(Comparator.comparing(TimeSlot::start))
            .toList();
    }

    public List<TimeSlot> slotsOf(long doctorId) {
        return slots.findByDoctor(doctorId);
    }

    public Optional<Appointment> find(long appointmentId) {
        return appointments.findById(appointmentId);
    }

    public Appointment get(long appointmentId) {
        return find(appointmentId)
            .orElseThrow(() -> new ClinicException("Жазылу табылмады: " + appointmentId));
    }

    public List<Appointment> appointmentsOf(long patientId) {
        return appointments.findByPatient(patientId);
    }

    public Appointment book(BookingRequest request) {
        Patient patient = patient(request.patientId());
        TimeSlot slot = slots.findById(request.slotId())
            .orElseThrow(() -> new ClinicException("Слот табылмады: " + request.slotId()));
        if (!slot.isFree()) {
            throw new ClinicException("Слот бос емес");
        }
        if (!slot.start().isAfter(LocalDateTime.now(clock))) {
            throw new ClinicException("Өткен уақытқа жазылуға болмайды");
        }
        boolean clash = appointments.findByPatient(patient.id()).stream()
            .anyMatch(a -> a.isActive() && a.start().equals(slot.start()));
        if (clash) {
            throw new ClinicException("Пациенттің осы уақытта басқа жазылуы бар");
        }
        Appointment appointment = new Appointment(appointments.nextId(), patient.id(), slot.doctorId(),
            slot.id(), slot.start(), request.complaint());
        slot.occupy(appointment.id());
        slots.save(slot);
        appointments.save(appointment);
        publish(EventType.BOOKED, appointment);
        return appointment;
    }

    /** @return бас тартуға дейінгі күй (undo үшін). */
    public AppointmentStatus cancel(long appointmentId) {
        Appointment appointment = get(appointmentId);
        AppointmentStatus previous = appointment.status();
        appointment.cancel();
        releaseSlot(appointment);
        appointments.save(appointment);
        publish(EventType.CANCELLED, appointment);
        return previous;
    }

    /** Бас тартуды кері қайтарады: күй мен слот бұрынғыдай болады. */
    public void restoreCancelled(long appointmentId, AppointmentStatus previous) {
        Appointment appointment = get(appointmentId);
        TimeSlot slot = slotOf(appointment);
        if (!slot.isFree()) {
            throw new ClinicException("Кері қайтару мүмкін емес: слотты басқа пациент алып қойды");
        }
        appointment.restoreStatus(previous);
        slot.occupy(appointment.id());
        slots.save(slot);
        appointments.save(appointment);
        publish(EventType.RESTORED, appointment);
    }

    /** Жазылуды толығымен жояды (жазылу командасының undo-сы). */
    public void removeBooking(long appointmentId) {
        Appointment appointment = get(appointmentId);
        if (appointment.status() != AppointmentStatus.BOOKED) {
            throw new ClinicException("Жазылуды кері қайтаруға болмайды: пациент келіп қойған");
        }
        releaseSlot(appointment);
        appointments.delete(appointmentId);
        publish(EventType.CANCELLED, appointment);
    }

    private void releaseSlot(Appointment appointment) {
        TimeSlot slot = slotOf(appointment);
        if (slot.appointmentId().filter(id -> id == appointment.id()).isPresent()) {
            slot.release();
            slots.save(slot);
        }
    }

    private TimeSlot slotOf(Appointment appointment) {
        return slots.findById(appointment.slotId())
            .orElseThrow(() -> new ClinicException("Слот табылмады: " + appointment.slotId()));
    }

    private void publish(EventType type, Appointment a) {
        String room = doctors.findById(a.doctorId()).map(Doctor::room).orElse("?");
        events.publish(new ClinicEvent(type, a.id(), a.patientId(), a.doctorId(), a.ticket(), room,
            a.start(), a.status().name()));
    }
}
