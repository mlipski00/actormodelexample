package commands;

import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.Behaviors;

public class SupervisorBehavior<T> {

   public Behavior<T> superviseNested(Behavior<T> behavior) {
        return Behaviors.supervise(
                        Behaviors.supervise(behavior)
                                .onFailure(IllegalStateException.class, SupervisorStrategy.restart()))
                .onFailure(IllegalArgumentException.class, SupervisorStrategy.stop());
    }


    Behavior<T> supervise(Behavior<T> behavior) {
        return Behaviors.supervise(behavior)
                .onFailure(IllegalStateException.class, SupervisorStrategy.restart());
    }
}
