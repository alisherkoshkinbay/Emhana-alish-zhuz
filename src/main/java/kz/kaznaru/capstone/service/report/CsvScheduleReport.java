package kz.kaznaru.capstone.service.report;

import kz.kaznaru.capstone.domain.Doctor;

public class CsvScheduleReport extends ScheduleReport {

    @Override
    public String format() {
        return "csv";
    }

    @Override
    protected String header(Doctor doctor) {
        return "start,end,status,patient\n";
    }

    @Override
    protected String row(SlotLine l) {
        return String.join(",", escape(l.start().toString()), escape(l.end().toString()),
            escape(l.status()), escape(l.patientName() == null ? "" : l.patientName())) + "\n";
    }

    static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
