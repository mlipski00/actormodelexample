import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;


public class ManagerBehavior extends AbstractBehavior<Command> {

    private static final Logger logger = LoggerFactory.getLogger(ManagerBehavior.class);
    private final SortedSet<BigInteger> primes = new TreeSet<>();
    private final ActorRef<WorkerCommand> workersGroupRouter;

    private ManagerBehavior(ActorContext<Command> context, ActorRef<WorkerCommand> workersGroupRouter) {
        super(context);
        this.workersGroupRouter = workersGroupRouter;
    }

    static Behavior<Command> create() {
        GroupRouter<WorkerCommand> groupRouter = Routers.group(WorkerBehavior.serviceKey);
        groupRouter.withRoundRobinRouting();
        return Behaviors.setup(behaviour -> {
            ActorRef<WorkerCommand> workersGroupRouter = behaviour.spawn(groupRouter, "workersGroupRouter");
            return new ManagerBehavior(behaviour, workersGroupRouter);
        });
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, command -> {
                    logger.info("Received InstructionCommand");
                    if (command.message().equals("start")) {
                        for (int i = 0; i < 10; i++) {
                            workersGroupRouter.tell(new DoWorkCommand("start", getContext().getSelf()));
                        }
                    }
                    return this;
                })
                .onMessage(ResultCommand.class, command -> {
                    primes.add(command.prime());
                    logger.info("I have received prime from worker id " + command.senderId() + ". Prime: " + command.prime());
                    if (primes.size() % 30 == 0) {
                        logger.info("Computed primes: " + primes.size());
                    }
                    return Behaviors.same();
                })
                .build();
    }
}
