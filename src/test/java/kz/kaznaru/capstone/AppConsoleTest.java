package kz.kaznaru.capstone;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/** Консоль UI басынан аяғына: App composition root + сценарий енгізу. */
class AppConsoleTest {

    private String run(String script) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        App.run(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)),
            new PrintStream(buf, true, StandardCharsets.UTF_8), new Fixture().clock);
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fullScenarioThroughConsole() {
        String out = run(String.join("\n",
            "1", "2", "1",
            "3", "1", "1", "бас ауруы",
            "3", "2", "2", "",
            "4", "2", "5", "5", "5", "5",
            "3", "1", "1", "", "6", "3", "6", "3", "9", "7", "1", "9", "8", "3",
            "10", "11", "1", "text", "11", "1", "csv",
            "3", "2", "2", "", "12", "4", "12", "3",
            "x", "6", "abc", "0", ""));
        assertTrue(out.contains("Др. Гүлнар Сейітова"), out);
        assertTrue(out.contains("Жазылу #1 құрылды."));
        assertTrue(out.contains("Кері қайтарылды: Бас тарту #2"));
        assertTrue(out.contains("Кері қайтарылды: Жазылу #2"));
        assertTrue(out.contains("Кері қайтарылды: Жазылу #1"));
        assertTrue(out.contains("Кері қайтаратын әрекет жоқ."));
        assertTrue(out.contains("Талон: A-001"));
        assertTrue(out.contains("Шақырылды: A-001"));
        assertTrue(out.contains("A-001  →  101-кабинет"));
        assertTrue(out.contains("Қабылдау аяқталды."));
        assertTrue(out.contains("! cannot check in"), "second check-in is rejected");
        assertTrue(out.contains("«Келмеді» деп белгіленді."), "no-show from the menu");
        assertTrue(out.contains("Белгісіз пункт."));
        assertTrue(out.contains("! Сан енгізіңіз"));
        assertTrue(out.contains("Сау болыңыз!"));
    }

    @Test
    void endOfInputExitsGracefully() {
        assertTrue(run("").contains("Сау болыңыз!"));
    }
}
