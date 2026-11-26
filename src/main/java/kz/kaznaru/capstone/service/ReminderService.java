package kz.kaznaru.capstone.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.AppointmentRepository;
import kz.kaznaru.capstone.domain.AppointmentStatus;
import kz.kaznaru.capstone.domain.Doctor;
import kz.kaznaru.capstone.domain.DoctorRepository;
import kz.kaznaru.capstone.domain.Patient;
import kz.kaznaru.capstone.domain.PatientRepository;
import kz.kaznaru.capstone.service.notify.ReminderChannelFactory;

/** Жақындап қалған қабылдаулар туралы бір реттік еске салғыштар. */
public class ReminderService {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private final AppointmentRepository appointments;
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final ReminderChannelFactory channels;
    private final Clock clock;
    private final Duration lead;

    public ReminderService(AppointmentRepository appointments, PatientRepository patients,
                           DoctorRepository doctors, ReminderChannelFactory channels,
                           Clock clock, Duration lead) {
        this.appointments = appointments;
        this.patients = patients;
        this.doctors = doctors;
        this.channels = channels;
        this.clock = clock;
        this.lead = lead;
    }

    /** @return сәтті жіберілген еске салғыштар саны. */
    public int sendDueReminders() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime horizon = now.plus(lead);
        int sent = 0;
        for (Appointment a : appointments.findAll()) {
            boolean due = a.status() == AppointmentStatus.BOOKED
                && !a.reminderSent()
                && a.start().isAfter(now)
                && !a.start().isAfter(horizon);
            if (due && remind(a)) {
                a.markReminderSent();
                appointments.save(a);
                sent++;
            }
        }
        return sent;
    }

    private boolean remind(Appointment a) {
        Patient p = patients.findById(a.patientId()).orElse(null);
        if (p == null) {
            return false;
        }
        Doctor d = doctors.findById(a.doctorId()).orElse(null);
        String who = d == null ? "дәрігер" : d.fullName() + " (" + d.room() + "-кабинет)";
        String text = "Еске салу: " + a.start().format(TIME) + " " + who + " қабылдауы.";
        return channels.create(p.preferredChannel()).send(p, text);
    }
}
