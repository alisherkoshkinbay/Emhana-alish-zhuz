package kz.kaznaru.capstone.domain;

import java.util.List;
import java.util.Optional;

/** Port: пациенттер қоймасы. */
public interface PatientRepository {
    void save(Patient patient);

    Optional<Patient> findById(long id);

    List<Patient> findAll();
}
