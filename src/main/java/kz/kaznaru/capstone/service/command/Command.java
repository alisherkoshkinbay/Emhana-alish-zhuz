package kz.kaznaru.capstone.service.command;

/** Command (5-дәріс): орындалатын және кері қайтарылатын операция. */
public interface Command {
    void execute();

    void undo();

    String description();
}
