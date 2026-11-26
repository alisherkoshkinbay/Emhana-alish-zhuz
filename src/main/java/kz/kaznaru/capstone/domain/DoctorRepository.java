package kz.kaznaru.capstone.domain;

import java.util.List;
import java.util.Optional;

/** Port: дәрігерлер қоймасы (Repository, 14-дәріс). Іске асыруы — infra. */
public interface DoctorRepository {
    void save(Doctor doctor);

    Optional<Doctor> findById(long id);

    List<Doctor> findAll();
}
