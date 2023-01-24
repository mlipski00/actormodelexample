import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class ManagerBehavior extends AbstractBehavior<Command> {

    private static final Logger logger = LoggerFactory.getLogger(ManagerBehavior.class);
    private final SortedSet<BigInteger> primes = new TreeSet<>();
    private final Map<String, ActorRef<WorkerCommand>> workers = new HashMap<>();

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, command -> {
                    if (command.message().equals("start")) {
                        for (int i = 0; i < 20; i++) {
                            String workerId = "worker" + i;
                            ActorRef<WorkerCommand> worker = getContext().spawn(WorkerBehavior.create(workerId), workerId, DispatcherSelector.fromConfig("custom-eyzee-dispatcher"));
                            workers.put(workerId, worker);
                            worker.tell(new DoWorkCommand("start", getContext().getSelf()));
                        }
                    }
                    return this;
                })
                .onMessage(ResultCommand.class, command -> {
                    primes.add(command.prime());
                    logger.info("I have received prime: " + command.prime() + " prime number from worker id " + command.senderId());
                    if (primes.size() % 30 == 0) {
                        logger.info("Computed primes: " + primes.size());
                        workers.forEach((k, v) -> v.tell(new StopCommand()));
                    } else {
                        workers.get(command.senderId()).tell(new DoWorkCommand("start", getContext().getSelf()));
                    }
                    return Behaviors.same();
                }).build();
    }
}
