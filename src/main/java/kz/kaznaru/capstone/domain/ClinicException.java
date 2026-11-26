package kz.kaznaru.capstone.domain;

/** Бизнес-ереже бұзылғанда лақтырылады (хабарламасы пайдаланушыға көрсетіледі). */
public class ClinicException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ClinicException(String message) {
        super(message);
    }
}
