package kz.kaznaru.capstone.infra;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kz.kaznaru.capstone.domain.Doctor;
import kz.kaznaru.capstone.domain.DoctorRepository;

public class InMemoryDoctorRepository implements DoctorRepository {
    private final Map<Long, Doctor> store = new LinkedHashMap<>();

    @Override
    public void save(Doctor doctor) {
        store.put(doctor.id(), doctor);
    }

    @Override
    public Optional<Doctor> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Doctor> findAll() {
        return new ArrayList<>(store.values());
    }
}
