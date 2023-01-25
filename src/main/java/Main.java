import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.DispatcherSelector;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import commands.Command;
import commands.InstructionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        String clusterName = "Eyzee_Actor_System";
        Config config = ConfigFactory.load();
        config = ConfigFactory.parseString("akka.actor.provider=\"cluster\"").withFallback(config);
        config = ConfigFactory.parseString("akka.remote.artery.canonical.hostname=\"127.0.0.1\"").withFallback(config);
        config = ConfigFactory.parseString("akka.remote.artery.canonical.port=\"2551\"").withFallback(config);

        ActorSystem<Command> system = ActorSystem.create(RootActor.create(), clusterName, config);
        ClusterSingleton clusterSingleton = ClusterSingleton.get(system);
        ActorRef<Command> proxy = clusterSingleton.init(SingletonActor.of(ManagerBehavior.create(), "ManagerBehavior"));

        for (int i = 0; i < 10; i++) {
            String workerId = "worker" + i;
            system.systemActorOf(WorkerBehavior.create(workerId), workerId, DispatcherSelector.fromConfig("custom-eyzee-dispatcher"));
        }

        logger.info("Waiting 30s...");
        Thread.sleep(30000);

        proxy.tell(new InstructionCommand("start"));
    }
}
