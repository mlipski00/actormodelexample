import akka.actor.typed.ActorSystem;
import commands.Command;
import commands.InstructionCommand;

public class Main {

    public static void main(String[] args) {
        ActorSystem<Command> bigPrimes = ActorSystem.create(ManagerBehavior.create(), "Eyzee_Actor_System");
        bigPrimes.tell(new InstructionCommand("start"));
    }
}
