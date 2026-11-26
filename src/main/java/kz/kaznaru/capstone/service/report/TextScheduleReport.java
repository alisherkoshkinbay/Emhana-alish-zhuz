package kz.kaznaru.capstone.service.report;

import java.time.format.DateTimeFormatter;
import java.util.List;
import kz.kaznaru.capstone.domain.Doctor;

public class TextScheduleReport extends ScheduleReport {
    private static final DateTimeFormatter DAY_TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String format() {
        return "text";
    }

    @Override
    protected String header(Doctor d) {
        return d.fullName() + " — " + d.specialty() + ", " + d.room() + "-кабинет\n";
    }

    @Override
    protected String row(SlotLine l) {
        String who = l.patientName() == null ? "" : "  " + l.patientName();
        return String.format("  %s-%s  %-12s%s%n", l.start().format(DAY_TIME), l.end().format(TIME), l.status(), who);
    }

    @Override
    protected String footer(List<SlotLine> lines) {
        long busy = lines.stream().filter(l -> l.patientName() != null).count();
        return "Барлығы: " + lines.size() + " слот, бос емес: " + busy + "\n";
    }
}
