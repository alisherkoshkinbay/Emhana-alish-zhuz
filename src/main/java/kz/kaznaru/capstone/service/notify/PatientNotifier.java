package kz.kaznaru.capstone.service.notify;

import java.time.format.DateTimeFormatter;
import kz.kaznaru.capstone.domain.Doctor;
import kz.kaznaru.capstone.domain.DoctorRepository;
import kz.kaznaru.capstone.domain.Patient;
import kz.kaznaru.capstone.domain.PatientRepository;
import kz.kaznaru.capstone.service.event.ClinicEvent;
import kz.kaznaru.capstone.service.event.ClinicEventListener;

/** Observer: жазылу оқиғаларын пациенттің таңдаған арнасы арқылы хабарлама етіп жібереді. */
public class PatientNotifier implements ClinicEventListener {
    static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final ReminderChannelFactory channels;

    public PatientNotifier(PatientRepository patients, DoctorRepository doctors, ReminderChannelFactory channels) {
        this.patients = patients;
        this.doctors = doctors;
        this.channels = channels;
    }

    @Override
    public void onEvent(ClinicEvent e) {
        String text = switch (e.type()) {
            case BOOKED -> "Сіз " + when(e) + " " + doctorName(e) + " қабылдауына жазылдыңыз.";
            case CANCELLED -> when(e) + " " + doctorName(e) + " қабылдауына жазылу жойылды.";
            case RESTORED -> when(e) + " " + doctorName(e) + " қабылдауына жазылу қалпына келтірілді.";
            case CALLED -> "Талон " + e.ticket() + ": " + e.room() + "-кабинетке кіріңіз.";
            default -> null;
        };
        if (text == null) {
            return;
        }
        patients.findById(e.patientId()).ifPresent(p -> send(p, text));
    }

    private void send(Patient p, String text) {
        channels.create(p.preferredChannel()).send(p, text);
    }

    private static String when(ClinicEvent e) {
        return e.slotStart().format(TIME);
    }

    private String doctorName(ClinicEvent e) {
        return doctors.findById(e.doctorId()).map(Doctor::fullName).orElse("дәрігер");
    }
}
