package kz.kaznaru.capstone.infra;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kz.kaznaru.capstone.domain.Appointment;
import kz.kaznaru.capstone.domain.AppointmentRepository;

public class InMemoryAppointmentRepository implements AppointmentRepository {
    private final Map<Long, Appointment> store = new LinkedHashMap<>();
    private long next = 1;

    @Override
    public long nextId() {
        return next++;
    }

    @Override
    public void save(Appointment appointment) {
        store.put(appointment.id(), appointment);
    }

    @Override
    public Optional<Appointment> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Appointment> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Appointment> findByDoctor(long doctorId) {
        return store.values().stream().filter(a -> a.doctorId() == doctorId).toList();
    }

    @Override
    public List<Appointment> findByPatient(long patientId) {
        return store.values().stream().filter(a -> a.patientId() == patientId).toList();
    }

    @Override
    public void delete(long id) {
        store.remove(id);
    }
}
