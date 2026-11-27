package kz.kaznaru.capstone.service.queue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kz.kaznaru.capstone.domain.AppointmentStatus;
import kz.kaznaru.capstone.service.event.ClinicEvent;
import kz.kaznaru.capstone.service.event.ClinicEventListener;

/**
 * Observer: холлдағы кезек экраны. Сервистерге сұрау жібермейді — тек оқиғалардан
 * өз көрінісін жаңартады.
 */
public class QueueBoard implements ClinicEventListener {
    private final Map<Long, String> serving = new LinkedHashMap<>();
    private final Map<Long, String> waiting = new LinkedHashMap<>();

    @Override
    public void onEvent(ClinicEvent e) {
        switch (e.type()) {
            case CHECKED_IN -> waiting.put(e.appointmentId(), e.ticket() + " (" + e.room() + "-каб.)");
            case CALLED -> {
                waiting.remove(e.appointmentId());
                serving.put(e.appointmentId(), e.ticket() + "  →  " + e.room() + "-кабинет");
            }
            case COMPLETED, NO_SHOW, CANCELLED -> {
                serving.remove(e.appointmentId());
                waiting.remove(e.appointmentId());
            }
            case RESTORED -> {
                if (e.state() == AppointmentStatus.WAITING && e.ticket() != null) {
                    waiting.put(e.appointmentId(), e.ticket() + " (" + e.room() + "-каб.)");
                }
            }
            default -> { }
        }
    }

    public List<String> render() {
        List<String> lines = new ArrayList<>();
        lines.add("========== КЕЗЕК ЭКРАНЫ ==========");
        lines.add("Қабылдауда:");
        if (serving.isEmpty()) {
            lines.add("  —");
        }
        serving.values().forEach(s -> lines.add("  " + s));
        lines.add("Кезекте:");
        if (waiting.isEmpty()) {
            lines.add("  —");
        }
        waiting.values().forEach(s -> lines.add("  " + s));
        lines.add("==================================");
        return lines;
    }

    public int waitingCount() {
        return waiting.size();
    }
}
