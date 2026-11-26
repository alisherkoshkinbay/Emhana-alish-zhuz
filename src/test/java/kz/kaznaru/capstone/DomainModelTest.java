package kz.kaznaru.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.AppointmentStatus;
import kz.kaznaru.capstone.domain.ClinicException;
import kz.kaznaru.capstone.domain.Doctor;
import kz.kaznaru.capstone.domain.Patient;
import kz.kaznaru.capstone.domain.TimeSlot;
import kz.kaznaru.capstone.infra.DemoData;
import kz.kaznaru.capstone.infra.InMemoryAppointmentRepository;
import kz.kaznaru.capstone.infra.InMemoryDoctorRepository;
import kz.kaznaru.capstone.infra.InMemoryPatientRepository;
import kz.kaznaru.capstone.infra.InMemorySlotRepository;
import org.junit.jupiter.api.Test;

/** Домен кластары мен жадтағы репозиторийлер. */
class DomainModelTest {
    private static final LocalDateTime NINE = LocalDateTime.of(2026, 11, 18, 9, 0);

    @Test
    void slotIsOccupiedAndReleased() {
        TimeSlot slot = new TimeSlot(1, 1, NINE, Duration.ofMinutes(20));
        assertTrue(slot.isFree());
        assertEquals(NINE.plusMinutes(20), slot.end());
        slot.occupy(7);
        assertFalse(slot.isFree());
        assertEquals(7L, slot.appointmentId().orElseThrow().longValue());
        assertThrows(ClinicException.class, () -> slot.occupy(8));
        slot.release();
        assertTrue(slot.isFree());
        assertThrows(IllegalArgumentException.class, () -> new TimeSlot(2, 1, NINE, Duration.ZERO));
    }

    @Test
    void doctorAndPatientValidateRequiredFields() {
        assertThrows(IllegalArgumentException.class, () -> new Doctor(1, " ", "Терапевт", "101"));
        assertThrows(IllegalArgumentException.class, () -> new Doctor(1, "Др. А", "Терапевт", ""));
        assertThrows(IllegalArgumentException.class, () -> new Patient(1, "", "8701", "a@b.kz", "SMS"));
        assertThrows(IllegalArgumentException.class, () -> new Patient(1, "Айгерім", "8701", "a@b.kz", " "));
        assertEquals("101", new Doctor(1, "Др. А", "Терапевт", "101").room());
    }

    @Test
    void appointmentKeepsTicketAndCanBeRestored() {
        Appointment a = new Appointment(1, 2, 3, 4, NINE, "жөтел");
        assertEquals(2, a.patientId());
        assertEquals(3, a.doctorId());
        assertEquals(4, a.slotId());
        assertEquals("жөтел", a.complaint());
        a.checkIn("A-001", NINE.minusMinutes(5));
        assertEquals("A-001", a.ticket());
        assertEquals(NINE.minusMinutes(5), a.checkedInAt());
        a.cancel();
        a.restoreStatus(AppointmentStatus.WAITING);
        assertEquals(AppointmentStatus.WAITING, a.status());
        assertEquals("Кезекте", a.status().label());
        a.markReminderSent();
        assertTrue(a.reminderSent());
        Appointment b = new Appointment(2, 2, 3, 5, NINE, null);
        b.markNoShow();
        assertEquals(AppointmentStatus.NO_SHOW, b.status());
        assertThrows(IllegalStateException.class, b::complete);
        assertThrows(IllegalStateException.class, b::markNoShow);
    }

    @Test
    void demoDataFillsRepositories() {
        ZoneId zone = ZoneOffset.ofHours(5);
        Clock clock = Clock.fixed(LocalDateTime.of(2026, 11, 18, 8, 0).atZone(zone).toInstant(), zone);
        InMemoryDoctorRepository doctors = new InMemoryDoctorRepository();
        InMemoryPatientRepository patients = new InMemoryPatientRepository();
        InMemorySlotRepository slots = new InMemorySlotRepository();
        DemoData.seed(doctors, patients, slots, clock);
        assertEquals(3, doctors.findAll().size());
        assertEquals(4, patients.findAll().size());
        assertEquals(12, slots.findByDoctor(1).size());
        assertEquals(NINE, slots.findByDoctor(1).get(0).start());
        assertEquals("Ерлан Бекұлы", patients.findById(2).orElseThrow().fullName());
        assertTrue(doctors.findById(9).isEmpty());
        assertTrue(slots.findById(1).isPresent());
    }

    @Test
    void appointmentRepositoryStoresFindsAndDeletes() {
        InMemoryAppointmentRepository repo = new InMemoryAppointmentRepository();
        Appointment a = new Appointment(repo.nextId(), 1, 1, 1, NINE, "");
        Appointment b = new Appointment(repo.nextId(), 2, 2, 2, NINE, "");
        repo.save(a);
        repo.save(b);
        assertEquals(2, repo.findAll().size());
        assertEquals(1, repo.findByDoctor(2).size());
        assertEquals(1, repo.findByPatient(1).size());
        repo.delete(a.id());
        assertTrue(repo.findById(a.id()).isEmpty());
    }
}
