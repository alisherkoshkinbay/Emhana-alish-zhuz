package kz.kaznaru.capstone.service.notify;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Factory (3-дәріс): пациенттің таңдаған арна кодынан нақты {@link ReminderChannel} жасайды.
 * Жасаушылар composition root-та тіркеледі, сондықтан жаңа арна қосу бұл класты өзгертпейді
 * (switch жоқ — Open/Closed).
 */
public class ReminderChannelFactory {
    private final Map<String, Supplier<? extends ReminderChannel>> creators = new LinkedHashMap<>();

    public ReminderChannelFactory register(String code, Supplier<? extends ReminderChannel> creator) {
        creators.put(normalize(code), creator);
        return this;
    }

    public ReminderChannel create(String code) {
        Supplier<? extends ReminderChannel> creator = creators.get(normalize(code));
        if (creator == null) {
            throw new IllegalArgumentException("Белгісіз хабарлама арнасы: " + code);
        }
        return creator.get();
    }

    public Set<String> supportedCodes() {
        return creators.keySet();
    }

    private static String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}
