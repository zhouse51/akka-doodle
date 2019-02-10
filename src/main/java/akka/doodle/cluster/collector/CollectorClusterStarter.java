package akka.doodle.cluster.collector;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class CollectorClusterStarter {
	
	
	public static void main(String[] args) throws InterruptedException {
		String[] params = args.length == 0? new String[]{"2551", "2552", "2553", "3", "2", "2"} : args;
//		String[] params = args.length == 0? new String[]{"2561", "2562", "2563", "2", "2", "2"} : args;
		
		int clusterSize = params.length / 2;
		
		String[] ports = new String[clusterSize];
		int[] workerSize = new int[clusterSize];
		for (int i = 0 ; i< clusterSize; i++) {
			ports[i] = params[i];
			workerSize[i] = Integer.parseInt(params[i + clusterSize]);
		}
		
		for (int i = 0; i<ports.length;i++){
			System.out.println("Starting collector server with port: " + ports[i] + " =====================================");
			
			
			Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + ports[i])
					.withFallback(ConfigFactory.parseString("akka.cluster.roles = [collector]"))
					.withFallback(ConfigFactory.load("server")
					.getConfig("member-profile-cluster"));
			ActorSystem system = ActorSystem.create("ClusterSystem", config);
			
			final String port = ports[i];
			// create worker actors
			List<ActorRef> collectorRoutees = IntStream.rangeClosed(0, workerSize[i]-1).boxed()
				.map(x -> system.actorOf(
						Props.create(CollectorWorker.class, port, x)
							.withDispatcher("nonblocking-io-dispatcher"), "collector_worker_" + x))
				.collect(Collectors.toList());
			
			Props props = Props.create(CollectorMaster.class, ports[i], collectorRoutees)
					.withDispatcher("nonblocking-io-dispatcher");
			system.actorOf(props, "collector");
			
			System.out.println("Starting collector server with port: " + ports[i] + " ======================================");
			
		}
	}
}
