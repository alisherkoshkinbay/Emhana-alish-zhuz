package kz.kaznaru.capstone;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class AppMainTest {
    @Test
    void appStarts() {
        assertDoesNotThrow(() -> App.main(new String[0]));
    }
}
