package kz.kaznaru.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import kz.kaznaru.capstone.domain.Patient;
import kz.kaznaru.capstone.infra.sms.KazSmsGatewayClient;
import kz.kaznaru.capstone.infra.sms.SmsChannelAdapter;
import kz.kaznaru.capstone.service.notify.ReminderChannel;
import kz.kaznaru.capstone.service.notify.ReminderChannelFactory;
import org.junit.jupiter.api.Test;

class ChannelFactoryAndAdapterTest {
    private final KazSmsGatewayClient gateway = new KazSmsGatewayClient();
    private final Patient aigerim = new Patient(1, "Айгерім", "8 (701) 111-22-33", "a@mail.kz", "sms");

    @Test
    void factoryCreatesChannelByPatientPreferenceCaseInsensitive() {
        ReminderChannelFactory factory = new ReminderChannelFactory()
            .register("SMS", () -> new SmsChannelAdapter(gateway, "T"));
        ReminderChannel channel = factory.create(aigerim.preferredChannel());
        assertEquals("SMS", channel.code());
        assertTrue(factory.supportedCodes().contains("SMS"));
        assertThrows(IllegalArgumentException.class, () -> factory.create("PIGEON"));
    }

    @Test
    void adapterNormalisesKazakhPhoneFormats() {
        assertTrue(new SmsChannelAdapter(gateway, "T").send(aigerim, "сәлем"));
        assertTrue(gateway.outbox().get(0).contains("77011112233"));
        assertEquals("77021112233", adapterMsisdn("+7 702 111 22 33"));
        assertEquals("77051112233", adapterMsisdn("7051112233"));
    }

    @Test
    void adapterMapsGatewayErrorCodesToFalseAndTruncatesLongText() {
        Patient badPhone = new Patient(2, "X", "123", "x@mail.kz", "SMS");
        assertFalse(new SmsChannelAdapter(gateway, "T").send(badPhone, "hi"));
        assertTrue(new SmsChannelAdapter(gateway, "T").send(aigerim, "ә".repeat(600)));
        assertEquals(KazSmsGatewayClient.TEXT_TOO_LONG, gateway.submit("77011112233", "x".repeat(500), "T"));
    }

    private String adapterMsisdn(String phone) {
        Patient p = new Patient(9, "P", phone, "p@mail.kz", "SMS");
        KazSmsGatewayClient g = new KazSmsGatewayClient();
        new SmsChannelAdapter(g, "T").send(p, "x");
        String line = g.outbox().get(0);
        return line.substring(line.indexOf("→ ") + 2, line.indexOf("]"));
    }
}
