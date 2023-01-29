import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class Main {

    public static void main(String[] args) {
        ActorSystem<Command> firstActor = ActorSystem.create(FirstActor.create(), "Eyzee_Actor_System");
//        for (int i = 0; i < 100; i++) {
        firstActor.tell(new Command(1 + " message", null));
//        }
    }
}

class FirstActor extends AbstractBehavior<Command> {

    private static final Logger logger = LoggerFactory.getLogger(FirstActor.class);

    public FirstActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(FirstActor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Command.class, command -> {
                    logger.info("got message: " + command.message());
                    ActorRef<Command> secondActor = getContext().spawn(SecondActor.create(), "Second_actor");
                    if (!command.message().equals("Response")) {
                        CompletionStage<Command> result =
                                AskPattern.ask(secondActor,
                                        replyTo -> new Command("message", replyTo),
                                        Duration.ofSeconds(3),
                                        getContext().getSystem().scheduler());
                        result.thenAccept(commandResponse -> logger.info("got message from ask patter: " + commandResponse.message()));
                        result.exceptionally(ex -> {
                            logger.error(ex.getMessage());
                            return null;
                        });
                    }
                    return Behaviors.same();
                }).build();
    }
}

class SecondActor extends AbstractBehavior<Command> {

    private static final Logger logger = LoggerFactory.getLogger(SecondActor.class);

    public SecondActor(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(SecondActor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Command.class, command -> {
                    logger.info("got message: " + command.message());
                    command.sender().tell(new Command("Response", getContext().getSelf()));
                    return Behaviors.same();
                })
                .build();
    }
}

record Command(String message, ActorRef<Command> sender) {
}

