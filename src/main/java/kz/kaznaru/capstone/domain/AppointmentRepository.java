package kz.kaznaru.capstone.domain;

import java.util.List;
import java.util.Optional;

/** Port: жазылулар қоймасы. */
public interface AppointmentRepository {
    long nextId();

    void save(Appointment appointment);

    Optional<Appointment> findById(long id);

    List<Appointment> findAll();

    List<Appointment> findByDoctor(long doctorId);

    List<Appointment> findByPatient(long patientId);

    void delete(long id);
}
