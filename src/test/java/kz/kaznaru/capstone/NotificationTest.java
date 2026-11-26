package kz.kaznaru.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.Patient;
import kz.kaznaru.capstone.domain.TimeSlot;
import kz.kaznaru.capstone.infra.DemoData;
import kz.kaznaru.capstone.infra.EmailChannel;
import kz.kaznaru.capstone.infra.InMemoryAppointmentRepository;
import kz.kaznaru.capstone.infra.InMemoryDoctorRepository;
import kz.kaznaru.capstone.infra.InMemoryPatientRepository;
import kz.kaznaru.capstone.infra.InMemorySlotRepository;
import kz.kaznaru.capstone.infra.sms.KazSmsGatewayClient;
import kz.kaznaru.capstone.infra.sms.SmsChannelAdapter;
import kz.kaznaru.capstone.service.ReminderService;
import kz.kaznaru.capstone.service.event.ClinicEvent;
import kz.kaznaru.capstone.service.event.EventType;
import kz.kaznaru.capstone.service.notify.PatientNotifier;
import kz.kaznaru.capstone.service.notify.ReminderChannelFactory;
import org.junit.jupiter.api.Test;

/** Factory арнаны таңдайды, хабарлама пациенттің арнасымен жеткізіледі. */
class NotificationTest {
    private static final ZoneId ZONE = ZoneOffset.ofHours(5);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 18, 8, 0);

    private final Clock clock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
    private final InMemoryDoctorRepository doctors = new InMemoryDoctorRepository();
    private final InMemoryPatientRepository patients = new InMemoryPatientRepository();
    private final InMemorySlotRepository slots = new InMemorySlotRepository();
    private final InMemoryAppointmentRepository appointments = new InMemoryAppointmentRepository();
    private final ByteArrayOutputStream emails = new ByteArrayOutputStream();
    private final ByteArrayOutputStream smsEcho = new ByteArrayOutputStream();
    private final KazSmsGatewayClient sms = new KazSmsGatewayClient(new PrintStream(smsEcho, true, StandardCharsets.UTF_8));
    private final ReminderChannelFactory channels = new ReminderChannelFactory()
        .register("SMS", () -> new SmsChannelAdapter(sms, "TEST"))
        .register("EMAIL", () -> new EmailChannel(new PrintStream(emails, true, StandardCharsets.UTF_8)));

    NotificationTest() {
        DemoData.seed(doctors, patients, slots, clock);
    }

    private Appointment save(long patientId, LocalDateTime start) {
        TimeSlot slot = new TimeSlot(slots.nextId(), 1, start, Duration.ofMinutes(20));
        slots.save(slot);
        Appointment a = new Appointment(appointments.nextId(), patientId, 1, slot.id(), start, "");
        appointments.save(a);
        return a;
    }

    private String emailText() {
        return emails.toString(StandardCharsets.UTF_8);
    }

    @Test
    void reminderGoesOnceThroughPatientsChannel() {
        save(1, NOW.plusHours(2));
        save(2, NOW.plusHours(3));
        save(3, NOW.plusDays(3));
        Appointment cancelled = save(4, NOW.plusHours(1));
        cancelled.cancel();
        ReminderService reminders = new ReminderService(appointments, patients, doctors, channels, clock,
            Duration.ofHours(24));
        assertEquals(2, reminders.sendDueReminders());
        assertEquals(1, sms.outbox().size());
        assertTrue(smsEcho.toString(StandardCharsets.UTF_8).contains("Еске салу"));
        assertTrue(emailText().contains("erlan@mail.kz"));
        assertEquals(0, reminders.sendDueReminders());
    }

    @Test
    void notifierSendsBookingCancelRestoreAndCallMessages() {
        PatientNotifier notifier = new PatientNotifier(patients, doctors, channels);
        LocalDateTime nine = NOW.plusHours(1);
        notifier.onEvent(new ClinicEvent(EventType.BOOKED, 1, 2, 1, null, "101", nine, "BOOKED"));
        notifier.onEvent(new ClinicEvent(EventType.CANCELLED, 1, 2, 1, null, "101", nine, "CANCELLED"));
        notifier.onEvent(new ClinicEvent(EventType.RESTORED, 1, 2, 1, null, "101", nine, "BOOKED"));
        notifier.onEvent(new ClinicEvent(EventType.CALLED, 1, 2, 1, "A-001", "101", nine, "IN_PROGRESS"));
        notifier.onEvent(new ClinicEvent(EventType.COMPLETED, 1, 2, 1, "A-001", "101", nine, "COMPLETED"));
        String text = emailText();
        assertTrue(text.contains("жазылдыңыз"), text);
        assertTrue(text.contains("жойылды"));
        assertTrue(text.contains("қалпына келтірілді"));
        assertTrue(text.contains("A-001: 101-кабинетке"));
        assertEquals(4, text.lines().count());
    }

    @Test
    void emailChannelNeedsAnAddress() {
        EmailChannel email = new EmailChannel(new PrintStream(emails, true, StandardCharsets.UTF_8));
        assertEquals("EMAIL", email.code());
        assertFalse(email.send(new Patient(9, "X", "", "", "EMAIL"), "hi"));
    }
}
