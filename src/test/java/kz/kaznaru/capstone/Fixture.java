package kz.kaznaru.capstone;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.TimeSlot;
import kz.kaznaru.capstone.infra.DemoData;
import kz.kaznaru.capstone.infra.EmailChannel;
import kz.kaznaru.capstone.infra.InMemoryAppointmentRepository;
import kz.kaznaru.capstone.infra.InMemoryDoctorRepository;
import kz.kaznaru.capstone.infra.InMemoryPatientRepository;
import kz.kaznaru.capstone.infra.InMemorySlotRepository;
import kz.kaznaru.capstone.infra.sms.KazSmsGatewayClient;
import kz.kaznaru.capstone.infra.sms.SmsChannelAdapter;
import kz.kaznaru.capstone.service.BookingRequest;
import kz.kaznaru.capstone.service.BookingService;
import kz.kaznaru.capstone.service.QueueService;
import kz.kaznaru.capstone.service.ReminderService;
import kz.kaznaru.capstone.service.event.ClinicEvent;
import kz.kaznaru.capstone.service.event.EventPublisher;
import kz.kaznaru.capstone.service.notify.PatientNotifier;
import kz.kaznaru.capstone.service.notify.ReminderChannelFactory;
import kz.kaznaru.capstone.service.queue.QueueBoard;

/**
 * Тесттерге арналған шағын composition root: бекітілген сағат (2026-11-18 08:00),
 * демо деректер, жадтағы репозиторийлер. Желі де, ДҚ да жоқ.
 */
final class Fixture {
    static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 18, 8, 0);
    static final ZoneId ZONE = ZoneOffset.ofHours(5);

    final Clock clock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
    final InMemoryDoctorRepository doctors = new InMemoryDoctorRepository();
    final InMemoryPatientRepository patients = new InMemoryPatientRepository();
    final InMemorySlotRepository slots = new InMemorySlotRepository();
    final InMemoryAppointmentRepository appointments = new InMemoryAppointmentRepository();
    final ByteArrayOutputStream emailBuffer = new ByteArrayOutputStream();
    final KazSmsGatewayClient sms = new KazSmsGatewayClient();
    final ReminderChannelFactory channels = new ReminderChannelFactory()
        .register("SMS", () -> new SmsChannelAdapter(sms, "TEST"))
        .register("EMAIL", () -> new EmailChannel(new PrintStream(emailBuffer, true, StandardCharsets.UTF_8)));
    final EventPublisher events = new EventPublisher();
    final List<ClinicEvent> received = new java.util.ArrayList<>();
    final QueueBoard board = new QueueBoard();
    final BookingService booking;
    final QueueService queue;
    final ReminderService reminders;

    Fixture() {
        DemoData.seed(doctors, patients, slots, clock);
        events.subscribe(received::add);
        events.subscribe(board);
        events.subscribe(new PatientNotifier(patients, doctors, channels));
        booking = new BookingService(doctors, patients, slots, appointments, events, clock);
        queue = new QueueService(appointments, doctors, events, clock);
        reminders = new ReminderService(appointments, patients, doctors, channels, clock, Duration.ofHours(24));
    }

    /** Дәрігердің n-ші бос слоты (0-ден бастап). */
    TimeSlot freeSlot(long doctorId, int index) {
        return booking.freeSlots(doctorId).get(index);
    }

    Appointment book(long patientId, long doctorId, int slotIndex) {
        return booking.book(new BookingRequest(patientId, freeSlot(doctorId, slotIndex).id(), "тест"));
    }

    String emails() {
        return emailBuffer.toString(StandardCharsets.UTF_8);
    }
}
