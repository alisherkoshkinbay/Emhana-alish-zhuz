package kz.kaznaru.capstone.service;

/** UI-дан келетін жазылу сұрауы. */
public record BookingRequest(long patientId, long slotId, String complaint) {
}
