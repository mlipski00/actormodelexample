import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Random;

public class WorkerBehavior extends AbstractBehavior<WorkerCommand> {

    public static ServiceKey<WorkerCommand> serviceKey = ServiceKey.create(WorkerCommand.class, "WorkerBehaviorServiceKey");
    private static final Logger logger = LoggerFactory.getLogger(WorkerBehavior.class);


    private final String id;

    private WorkerBehavior(ActorContext<WorkerCommand> context, String id) {
        super(context);
        this.id = id + "_" + getContext().getSystem().address().getPort().get();
        logger.info("Setup of worker with id " + id);
    }

    public static Behavior<WorkerCommand> create(String id) {
        Behavior<WorkerCommand> behavior = Behaviors.setup(context -> {
            context.getSystem().receptionist().tell(Receptionist.register(serviceKey, context.getSelf()));
            return new WorkerBehavior(context, id);
        });
        return Behaviors.supervise(behavior)
                .onFailure(IllegalStateException.class, SupervisorStrategy.restart());
    }

    private BigInteger currentPrime;

    @Override
    public Receive<WorkerCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(DoWorkCommand.class, doWorkCommand -> {
                    if (doWorkCommand.message().equals("start")) {
                        logger.info("worker " + id + " received compute prime command");
                        BigInteger bigInteger = new BigInteger(5000, new Random());
                        currentPrime = bigInteger.nextProbablePrime();
                        doWorkCommand.sender().tell(new ResultCommand(currentPrime, id));
                    }
                    return this;
                }).onMessage(ThrowExceptionCommand.class, message -> {
                    logger.info("Received 'ThrowExceptionCommand' command. Actor: "+ id);
                    throw new IllegalStateException();
                }).onMessage(StopCommand.class, message -> {
                    logger.info("Received 'StopCommand' command. Actor " + id + " is about to be stopped");
                    return Behaviors.stopped();
                }).build();
    }
}
