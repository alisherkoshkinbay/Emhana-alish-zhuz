package kz.kaznaru.capstone.infra.sms;

import kz.kaznaru.capstone.domain.Patient;
import kz.kaznaru.capstone.service.notify.ReminderChannel;

/**
 * Adapter (4-дәріс): біздің {@link ReminderChannel} интерфейсімізді сыртқы
 * {@link KazSmsGatewayClient} API-іне бейімдейді — нөмірді қалыпқа келтіреді,
 * ұзын мәтінді қысқартады, int кодты boolean-ға аударады.
 */
public class SmsChannelAdapter implements ReminderChannel {
    private final KazSmsGatewayClient client;
    private final String senderId;

    public SmsChannelAdapter(KazSmsGatewayClient client, String senderId) {
        this.client = client;
        this.senderId = senderId;
    }

    @Override
    public String code() {
        return "SMS";
    }

    @Override
    public boolean send(Patient to, String message) {
        String text = message.length() > KazSmsGatewayClient.MAX_TEXT
            ? message.substring(0, KazSmsGatewayClient.MAX_TEXT - 1) + "…"
            : message;
        return client.submit(toMsisdn(to.phone()), text, senderId) == KazSmsGatewayClient.OK;
    }

    /** "+7 701 111-22-33", "8 701 1112233" → "77011112233". */
    static String toMsisdn(String phone) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11 && digits.startsWith("8")) {
            digits = "7" + digits.substring(1);
        } else if (digits.length() == 10) {
            digits = "7" + digits;
        }
        return digits;
    }
}
