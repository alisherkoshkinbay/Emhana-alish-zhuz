package kz.kaznaru.capstone.service.notify;

import kz.kaznaru.capstone.domain.Patient;

/** Хабарлама арнасы (port). Жаңа арна = осы интерфейстің жаңа іске асыруы. */
public interface ReminderChannel {
    /** Арнаның коды, пациенттің preferredChannel өрісімен сәйкес келеді. */
    String code();

    /** @return хабарлама жеткізілсе true. */
    boolean send(Patient to, String message);
}
