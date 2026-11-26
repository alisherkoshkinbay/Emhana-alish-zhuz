package kz.kaznaru.capstone.service.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer үлгісінің Subject-і. Сервистер тек осы класты біледі; кезек экраны мен
 * хабарламалар сервистерді өзгертпей қосылады/алынады.
 */
public class EventPublisher {
    private final List<ClinicEventListener> listeners = new CopyOnWriteArrayList<>();
    private int failedDeliveries;

    public void subscribe(ClinicEventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(ClinicEventListener listener) {
        listeners.remove(listener);
    }

    /** Бір тыңдаушының қатесі жазылу операциясын да, басқа тыңдаушыларды да бұзбайды. */
    public void publish(ClinicEvent event) {
        for (ClinicEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException e) {
                failedDeliveries++;
            }
        }
    }

    public int failedDeliveries() {
        return failedDeliveries;
    }
}
