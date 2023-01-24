package commands;

import akka.actor.typed.ActorRef;

import java.io.Serial;

public record DoWorkCommand(String message, ActorRef<Command> sender) implements WorkerCommand {
    @Serial
    private static final long serialVersionUID = 1L;
}
