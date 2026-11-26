package kz.kaznaru.capstone;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import kz.kaznaru.capstone.domain.AppointmentRepository;
import kz.kaznaru.capstone.domain.DoctorRepository;
import kz.kaznaru.capstone.domain.PatientRepository;
import kz.kaznaru.capstone.domain.SlotRepository;
import kz.kaznaru.capstone.infra.DemoData;
import kz.kaznaru.capstone.infra.EmailChannel;
import kz.kaznaru.capstone.infra.InMemoryAppointmentRepository;
import kz.kaznaru.capstone.infra.InMemoryDoctorRepository;
import kz.kaznaru.capstone.infra.InMemoryPatientRepository;
import kz.kaznaru.capstone.infra.InMemorySlotRepository;
import kz.kaznaru.capstone.infra.sms.KazSmsGatewayClient;
import kz.kaznaru.capstone.infra.sms.SmsChannelAdapter;
import kz.kaznaru.capstone.service.BookingService;
import kz.kaznaru.capstone.service.QueueService;
import kz.kaznaru.capstone.service.ReminderService;
import kz.kaznaru.capstone.service.ScheduleService;
import kz.kaznaru.capstone.service.command.CommandHistory;
import kz.kaznaru.capstone.service.event.EventPublisher;
import kz.kaznaru.capstone.service.notify.PatientNotifier;
import kz.kaznaru.capstone.service.notify.ReminderChannelFactory;
import kz.kaznaru.capstone.service.queue.QueueBoard;
import kz.kaznaru.capstone.service.report.CsvScheduleReport;
import kz.kaznaru.capstone.service.report.TextScheduleReport;
import kz.kaznaru.capstone.ui.ConsoleUi;

/** Composition root: the ONLY place allowed to wire concrete classes together. */
public final class App {
    private App() { }

    public static void main(String[] args) {
        PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
        run(System.in, out, Clock.systemDefaultZone());
    }

    static void run(InputStream input, PrintStream out, Clock clock) {
        DoctorRepository doctors = new InMemoryDoctorRepository();
        PatientRepository patients = new InMemoryPatientRepository();
        SlotRepository slots = new InMemorySlotRepository();
        AppointmentRepository appointments = new InMemoryAppointmentRepository();
        DemoData.seed(doctors, patients, slots, clock);

        KazSmsGatewayClient smsGateway = new KazSmsGatewayClient(out);
        ReminderChannelFactory channels = new ReminderChannelFactory()
            .register("SMS", () -> new SmsChannelAdapter(smsGateway, "KAZCLINIC"))
            .register("EMAIL", () -> new EmailChannel(out));

        EventPublisher events = new EventPublisher();
        QueueBoard board = new QueueBoard();
        events.subscribe(board);
        events.subscribe(new PatientNotifier(patients, doctors, channels));

        BookingService booking = new BookingService(doctors, patients, slots, appointments, events, clock);
        QueueService queue = new QueueService(appointments, doctors, events, clock);
        ReminderService reminders = new ReminderService(appointments, patients, doctors, channels, clock,
            Duration.ofHours(24));
        ScheduleService schedule = new ScheduleService(doctors, patients, slots, appointments,
            List.of(new TextScheduleReport(), new CsvScheduleReport()));

        new ConsoleUi(booking, queue, reminders, schedule, new CommandHistory(), board,
            new Scanner(input, StandardCharsets.UTF_8), out).run();
    }
}
