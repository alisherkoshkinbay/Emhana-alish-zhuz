package kz.kaznaru.capstone.infra;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import kz.kaznaru.capstone.domain.Doctor;
import kz.kaznaru.capstone.domain.DoctorRepository;
import kz.kaznaru.capstone.domain.Patient;
import kz.kaznaru.capstone.domain.PatientRepository;
import kz.kaznaru.capstone.domain.SlotRepository;
import kz.kaznaru.capstone.domain.TimeSlot;

/** Іске қосқанда демо деректерді толтырады: 3 дәрігер, 4 пациент, слоттар. */
public final class DemoData {
    public static final Duration SLOT = Duration.ofMinutes(20);

    private DemoData() { }

    public static void seed(DoctorRepository doctors, PatientRepository patients, SlotRepository slots, Clock clock) {
        doctors.save(new Doctor(1, "Др. Асқар Ахметов", "Терапевт", "101"));
        doctors.save(new Doctor(2, "Др. Гүлнар Сейітова", "Педиатр", "102"));
        doctors.save(new Doctor(3, "Др. Дәурен Қасымов", "Кардиолог", "205"));

        patients.save(new Patient(1, "Айгерім Сапарова", "8 701 111 22 33", "aigerim@mail.kz", "SMS"));
        patients.save(new Patient(2, "Ерлан Бекұлы", "+7 702 222 33 44", "erlan@mail.kz", "EMAIL"));
        patients.save(new Patient(3, "Мадина Омарова", "87053334455", "madina@mail.kz", "SMS"));
        patients.save(new Patient(4, "Нұрлан Әбенов", "+7 707 444 55 66", "nurlan@mail.kz", "EMAIL"));

        LocalDateTime nextHour = LocalDateTime.now(clock).truncatedTo(ChronoUnit.HOURS).plusHours(1);
        LocalDateTime tomorrowMorning = nextHour.toLocalDate().plusDays(1).atTime(9, 0);
        for (long doctorId = 1; doctorId <= 3; doctorId++) {
            addSlots(slots, doctorId, nextHour, 6);
            addSlots(slots, doctorId, tomorrowMorning, 6);
        }
    }

    private static void addSlots(SlotRepository slots, long doctorId, LocalDateTime from, int count) {
        for (int i = 0; i < count; i++) {
            slots.save(new TimeSlot(slots.nextId(), doctorId, from.plus(SLOT.multipliedBy(i)), SLOT));
        }
    }
}
