package kz.kaznaru.capstone.infra;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kz.kaznaru.capstone.domain.SlotRepository;
import kz.kaznaru.capstone.domain.TimeSlot;

public class InMemorySlotRepository implements SlotRepository {
    private final Map<Long, TimeSlot> store = new LinkedHashMap<>();
    private long next = 1;

    @Override
    public long nextId() {
        return next++;
    }

    @Override
    public void save(TimeSlot slot) {
        store.put(slot.id(), slot);
    }

    @Override
    public Optional<TimeSlot> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<TimeSlot> findByDoctor(long doctorId) {
        return store.values().stream()
            .filter(s -> s.doctorId() == doctorId)
            .sorted(Comparator.comparing(TimeSlot::start))
            .toList();
    }
}
