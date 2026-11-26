package kz.kaznaru.capstone.service.report;

import java.time.LocalDateTime;

/** Кестедегі бір жол: слот уақыты, күйі және (болса) пациенттің аты. */
public record SlotLine(LocalDateTime start, LocalDateTime end, String status, String patientName) {
}
