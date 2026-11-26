package kz.kaznaru.capstone.infra.sms;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Сыртқы SMS-шлюз SDK-сының симуляциясы (біз өзгерте алмайтын API).
 * Нөмірді "7XXXXXXXXXX" форматында күтеді, нәтижені int кодпен қайтарады.
 * Тесттерде желі қолданылмауы үшін хабарламалар жадта сақталады.
 */
public class KazSmsGatewayClient {
    public static final int OK = 0;
    public static final int INVALID_MSISDN = 101;
    public static final int TEXT_TOO_LONG = 102;
    public static final int MAX_TEXT = 480;

    private final List<String> outbox = new ArrayList<>();
    private final PrintStream echo;

    public KazSmsGatewayClient() {
        this(null);
    }

    /** @param echo демо режимінде жіберілген SMS-терді көрсету үшін (null болуы мүмкін). */
    public KazSmsGatewayClient(PrintStream echo) {
        this.echo = echo;
    }

    public int submit(String msisdn, String text, String senderId) {
        if (msisdn == null || !msisdn.matches("7\\d{10}")) {
            return INVALID_MSISDN;
        }
        if (text.length() > MAX_TEXT) {
            return TEXT_TOO_LONG;
        }
        String line = "[SMS " + senderId + " → " + msisdn + "] " + text;
        outbox.add(line);
        if (echo != null) {
            echo.println("  " + line);
        }
        return OK;
    }

    public List<String> outbox() {
        return List.copyOf(outbox);
    }
}
