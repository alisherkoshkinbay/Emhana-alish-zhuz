package kz.kaznaru.capstone.domain;

/** Дәрігер: аты, мамандығы және кабинеті (кезек экранында көрсетіледі). */
public record Doctor(long id, String fullName, String specialty, String room) {
    public Doctor {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("doctor name must not be blank");
        }
        if (room == null || room.isBlank()) {
            throw new IllegalArgumentException("room must not be blank");
        }
    }
}
