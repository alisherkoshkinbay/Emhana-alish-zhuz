package kz.kaznaru.capstone.infra;

import java.io.PrintStream;
import kz.kaznaru.capstone.domain.Patient;
import kz.kaznaru.capstone.service.notify.ReminderChannel;

/** Email арнасы. Оқу жобасында SMTP орнына берілген ағынға жазады. */
public class EmailChannel implements ReminderChannel {
    private final PrintStream out;

    public EmailChannel(PrintStream out) {
        this.out = out;
    }

    @Override
    public String code() {
        return "EMAIL";
    }

    @Override
    public boolean send(Patient to, String message) {
        if (to.email() == null || !to.email().contains("@")) {
            return false;
        }
        out.println("  [EMAIL → " + to.email() + "] " + message);
        return true;
    }
}
