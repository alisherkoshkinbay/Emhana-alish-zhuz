package kz.kaznaru.capstone.ui;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.ClinicException;
import kz.kaznaru.capstone.domain.Doctor;
import kz.kaznaru.capstone.domain.TimeSlot;
import kz.kaznaru.capstone.service.BookingRequest;
import kz.kaznaru.capstone.service.BookingService;
import kz.kaznaru.capstone.service.QueueService;
import kz.kaznaru.capstone.service.ReminderService;
import kz.kaznaru.capstone.service.ScheduleService;
import kz.kaznaru.capstone.service.command.BookAppointmentCommand;
import kz.kaznaru.capstone.service.command.CancelAppointmentCommand;
import kz.kaznaru.capstone.service.command.CommandHistory;
import kz.kaznaru.capstone.service.queue.QueueBoard;

/** Консоль мәзірі. Тек service қабатын шақырады; бизнес-логика мұнда жоқ. */
public class ConsoleUi {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    private final BookingService booking;
    private final QueueService queue;
    private final ReminderService reminders;
    private final ScheduleService schedule;
    private final CommandHistory history;
    private final QueueBoard board;
    private final Scanner in;
    private final PrintStream out;

    public ConsoleUi(BookingService booking, QueueService queue, ReminderService reminders,
                     ScheduleService schedule, CommandHistory history, QueueBoard board,
                     Scanner in, PrintStream out) {
        this.booking = booking;
        this.queue = queue;
        this.reminders = reminders;
        this.schedule = schedule;
        this.history = history;
        this.board = board;
        this.in = in;
        this.out = out;
    }

    public void run() {
        while (true) {
            printMenu();
            String choice = readLine("> ");
            if (choice == null || "0".equals(choice)) {
                out.println("Сау болыңыз!");
                return;
            }
            try {
                handle(choice);
            } catch (ClinicException | IllegalStateException | IllegalArgumentException e) {
                out.println("! " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        out.println();
        out.println("1) Дәрігерлер  2) Бос слоттар  3) Жазылу  4) Бас тарту  5) Undo");
        out.println("6) Check-in  7) Келесі пациент  8) Аяқтау  9) Кезек экраны");
        out.println("10) Еске салғыштар  11) Дәрігер кестесі  12) Келмеді  0) Шығу");
    }

    private void handle(String choice) {
        switch (choice) {
            case "1" -> booking.doctors().forEach(this::printDoctor);
            case "2" -> booking.freeSlots(readLong("Дәрігер id: ")).forEach(this::printSlot);
            case "3" -> {
                BookingRequest r = new BookingRequest(readLong("Пациент id: "), readLong("Слот id: "),
                    readLine("Шағым: "));
                Appointment a = history.run(new BookAppointmentCommand(booking, r)).result();
                out.println("Жазылу #" + a.id() + " құрылды.");
            }
            case "4" -> {
                history.run(new CancelAppointmentCommand(booking, readLong("Жазылу id: ")));
                out.println("Жазылудан бас тартылды.");
            }
            case "5" -> out.println(history.undoLast().map(d -> "Кері қайтарылды: " + d)
                .orElse("Кері қайтаратын әрекет жоқ."));
            case "6" -> out.println("Талон: " + queue.checkIn(readLong("Жазылу id: ")).ticket());
            case "7" -> out.println(queue.callNext(readLong("Дәрігер id: "))
                .map(a -> "Шақырылды: " + a.ticket()).orElse("Кезек бос."));
            case "8" -> {
                queue.complete(readLong("Жазылу id: "));
                out.println("Қабылдау аяқталды.");
            }
            case "9" -> board.render().forEach(out::println);
            case "10" -> out.println("Жіберілген еске салғыштар: " + reminders.sendDueReminders());
            case "11" -> out.print(schedule.doctorSchedule(readLong("Дәрігер id: "),
                readLine("Формат " + schedule.formats() + ": ")));
            case "12" -> {
                queue.markNoShow(readLong("Жазылу id: "));
                out.println("«Келмеді» деп белгіленді.");
            }
            default -> out.println("Белгісіз пункт.");
        }
    }

    private void printDoctor(Doctor d) {
        out.println("  " + d.id() + ". " + d.fullName() + " — " + d.specialty() + ", " + d.room() + "-каб.");
    }

    private void printSlot(TimeSlot s) {
        out.println("  слот " + s.id() + ": " + s.start().format(TIME));
    }

    private String readLine(String prompt) {
        out.print(prompt);
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }

    private long readLong(String prompt) {
        String line = readLine(prompt);
        try {
            return Long.parseLong(line == null ? "" : line);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Сан енгізіңіз");
        }
    }
}
