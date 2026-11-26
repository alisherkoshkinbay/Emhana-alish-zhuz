package kz.kaznaru.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import kz.kaznaru.capstone.domain.ClinicException;
import kz.kaznaru.capstone.service.ScheduleService;
import kz.kaznaru.capstone.service.report.CsvScheduleReport;
import kz.kaznaru.capstone.service.report.TextScheduleReport;
import org.junit.jupiter.api.Test;

class ScheduleReportTest {
    private final Fixture f = new Fixture();
    private final ScheduleService schedule = new ScheduleService(f.doctors, f.patients, f.slots, f.appointments,
        List.of(new TextScheduleReport(), new CsvScheduleReport()));

    @Test
    void textReportHasHeaderRowsAndFooter() {
        f.book(1, 1, 0);
        String text = schedule.doctorSchedule(1, "TEXT");
        assertTrue(text.startsWith("Др. Асқар Ахметов — Терапевт, 101-кабинет"), text);
        assertTrue(text.contains("Айгерім Сапарова"));
        assertTrue(text.endsWith("Барлығы: 12 слот, бос емес: 1\n"), text);
    }

    @Test
    void csvReportUsesSameSkeletonWithoutFooter() {
        f.book(2, 1, 0);
        String[] lines = schedule.doctorSchedule(1, "csv").split("\n");
        assertEquals("start,end,status,patient", lines[0]);
        assertEquals(13, lines.length);
        assertEquals("2026-11-18T09:00,2026-11-18T09:20,Жазылды,Ерлан Бекұлы", lines[1]);
    }

    @Test
    void unknownFormatOrDoctorIsRejected() {
        assertThrows(ClinicException.class, () -> schedule.doctorSchedule(1, "pdf"));
        assertThrows(ClinicException.class, () -> schedule.doctorSchedule(99, "text"));
    }
}
