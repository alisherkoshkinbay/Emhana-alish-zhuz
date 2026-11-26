package kz.kaznaru.capstone.infra;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kz.kaznaru.capstone.domain.Patient;
import kz.kaznaru.capstone.domain.PatientRepository;

public class InMemoryPatientRepository implements PatientRepository {
    private final Map<Long, Patient> store = new LinkedHashMap<>();

    @Override
    public void save(Patient patient) {
        store.put(patient.id(), patient);
    }

    @Override
    public Optional<Patient> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Patient> findAll() {
        return new ArrayList<>(store.values());
    }
}
