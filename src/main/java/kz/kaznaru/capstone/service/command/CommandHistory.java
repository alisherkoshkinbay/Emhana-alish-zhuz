package kz.kaznaru.capstone.service.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/** Invoker: командаларды орындап, кері қайтару үшін стекте сақтайды. */
public class CommandHistory {
    private final Deque<Command> done = new ArrayDeque<>();

    public <T extends Command> T run(T command) {
        command.execute();
        done.push(command);
        return command;
    }

    /**
     * Соңғы команданы кері қайтарады. Undo сәтсіз болса (мысалы, слотты басқа біреу алды),
     * команда тарихтан алынады және қате лақтырылады, әйтпесе стек «тұрып қалар» еді.
     */
    public Optional<String> undoLast() {
        if (done.isEmpty()) {
            return Optional.empty();
        }
        Command command = done.pop();
        command.undo();
        return Optional.of(command.description());
    }

    public int size() {
        return done.size();
    }
}
