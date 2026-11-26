package kz.kaznaru.capstone.domain;

/** Жазылу күйі. */
public enum AppointmentStatus {
    BOOKED("Жазылды"),
    WAITING("Кезекте"),
    IN_PROGRESS("Қабылдауда"),
    COMPLETED("Аяқталды"),
    CANCELLED("Бас тартылды"),
    NO_SHOW("Келмеді");

    private final String label;

    AppointmentStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
