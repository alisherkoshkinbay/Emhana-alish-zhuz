package kz.kaznaru.capstone.domain;

/**
 * Пациент. {@code preferredChannel} — хабарлама арнасының коды ("SMS", "EMAIL", ...);
 * нақты арнаны service қабатындағы фабрика таңдайды.
 */
public record Patient(long id, String fullName, String phone, String email, String preferredChannel) {
    public Patient {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("patient name must not be blank");
        }
        if (preferredChannel == null || preferredChannel.isBlank()) {
            throw new IllegalArgumentException("preferred channel must not be blank");
        }
    }
}
