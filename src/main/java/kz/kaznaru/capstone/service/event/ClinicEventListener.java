package kz.kaznaru.capstone.service.event;

/** Observer: жазылу оқиғаларына жазылушы (кезек экраны, хабарлама жіберуші ...). */
@FunctionalInterface
public interface ClinicEventListener {
    void onEvent(ClinicEvent event);
}
