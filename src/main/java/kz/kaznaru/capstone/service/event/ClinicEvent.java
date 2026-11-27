package kz.kaznaru.capstone.service.event;
import java.time.LocalDateTime;

import kz.kaznaru.capstone.domain.AppointmentStatus;

/**
 * Жазылуда болған оқиға. Тыңдаушылар доменді қайта сұрамай-ақ жұмыс істей алуы үшін
 * экранға керек деректер (талон, кабинет, күй) осында көшіріледі.
 */
public record ClinicEvent(
    EventType type,
    long appointmentId,
    long patientId,
    long doctorId,
    String ticket,
    String room,
    LocalDateTime slotStart,
    AppointmentStatus state) {
}
