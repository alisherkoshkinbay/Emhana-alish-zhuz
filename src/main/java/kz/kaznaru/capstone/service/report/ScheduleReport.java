package kz.kaznaru.capstone.service.report;

import java.util.List;
import kz.kaznaru.capstone.domain.Doctor;

/**
 * Template Method (6-дәріс): кесте есебінің қаңқасы (тақырып → жолдар → қорытынды) бекітілген,
 * ал форматқа тән қадамдарды ішкі кластар береді.
 */
public abstract class ScheduleReport {

    public final String render(Doctor doctor, List<SlotLine> lines) {
        StringBuilder out = new StringBuilder();
        out.append(header(doctor));
        for (SlotLine line : lines) {
            out.append(row(line));
        }
        out.append(footer(lines));
        return out.toString();
    }

    /** Форматтың коды, мысалы "text" немесе "csv". */
    public abstract String format();

    protected abstract String header(Doctor doctor);

    protected abstract String row(SlotLine line);

    /** Hook: әдепкіде қорытынды жоқ. */
    protected String footer(List<SlotLine> lines) {
        return "";
    }
}
