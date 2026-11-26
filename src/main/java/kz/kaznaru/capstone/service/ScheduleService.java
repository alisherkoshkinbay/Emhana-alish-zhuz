package kz.kaznaru.capstone.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.AppointmentRepository;
import kz.kaznaru.capstone.domain.ClinicException;
import kz.kaznaru.capstone.domain.Doctor;
import kz.kaznaru.capstone.domain.DoctorRepository;
import kz.kaznaru.capstone.domain.Patient;
import kz.kaznaru.capstone.domain.PatientRepository;
import kz.kaznaru.capstone.domain.SlotRepository;
import kz.kaznaru.capstone.domain.TimeSlot;
import kz.kaznaru.capstone.service.report.ScheduleReport;
import kz.kaznaru.capstone.service.report.SlotLine;

/** Дәрігер кестесін таңдалған форматта (Template Method іске асырулары) құрастырады. */
public class ScheduleService {
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final SlotRepository slots;
    private final AppointmentRepository appointments;
    private final Map<String, ScheduleReport> reports = new LinkedHashMap<>();

    public ScheduleService(DoctorRepository doctors, PatientRepository patients, SlotRepository slots,
                           AppointmentRepository appointments, List<ScheduleReport> reports) {
        this.doctors = doctors;
        this.patients = patients;
        this.slots = slots;
        this.appointments = appointments;
        reports.forEach(r -> this.reports.put(r.format().toLowerCase(Locale.ROOT), r));
    }

    public Set<String> formats() {
        return reports.keySet();
    }

    public String doctorSchedule(long doctorId, String format) {
        Doctor doctor = doctors.findById(doctorId)
            .orElseThrow(() -> new ClinicException("Дәрігер табылмады: " + doctorId));
        ScheduleReport report = reports.get(format == null ? "" : format.toLowerCase(Locale.ROOT));
        if (report == null) {
            throw new ClinicException("Белгісіз формат: " + format + ". Бар форматтар: " + formats());
        }
        List<SlotLine> lines = new ArrayList<>();
        for (TimeSlot slot : slots.findByDoctor(doctorId)) {
            lines.add(slot.appointmentId()
                .flatMap(appointments::findById)
                .map(a -> line(slot, a))
                .orElseGet(() -> new SlotLine(slot.start(), slot.end(), "Бос", null)));
        }
        return report.render(doctor, lines);
    }

    private SlotLine line(TimeSlot slot, Appointment a) {
        String name = patients.findById(a.patientId()).map(Patient::fullName).orElse("?");
        return new SlotLine(slot.start(), slot.end(), a.status().label(), name);
    }
}
