package kz.kaznaru.capstone.domain;

import java.util.List;
import java.util.Optional;

/** Port: слоттар қоймасы. */
public interface SlotRepository {
    long nextId();

    void save(TimeSlot slot);

    Optional<TimeSlot> findById(long id);

    List<TimeSlot> findByDoctor(long doctorId);
}
