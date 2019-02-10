package akka.doodle.cluster.interceptor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class InterceptorClusterStarter {
	
	
	public static void main(String[] args) throws InterruptedException {
		String[] params = args.length == 0? new String[]{"3551", "3552", "3553", "3", "3", "3"} : args;
//		String[] params = args.length == 0? new String[]{"3561", "3562", "3563", "2", "3", "3"} : args;
		
		int clusterSize = params.length / 2;
		
		String[] ports = new String[clusterSize];
		int[] workerSize = new int[clusterSize];
		for (int i = 0 ; i< clusterSize; i++) {
			ports[i] = params[i];
			workerSize[i] = Integer.parseInt(params[i + clusterSize]);
		}
		
		for (int i = 0; i<ports.length;i++){
			System.out.println("Starting interceptor server with port: " + ports[i] + " =====================================");
			
			
			Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + ports[i])
					.withFallback(ConfigFactory.parseString("akka.cluster.roles = [interceptor]"))
					.withFallback(ConfigFactory.load("server")
					.getConfig("member-profile-cluster"));
			ActorSystem system = ActorSystem.create("ClusterSystem", config);
			
			final String port = ports[i];
			// create worker actors
			List<ActorRef> interceptorRoutees = IntStream.rangeClosed(0, workerSize[i]-1).boxed()
				.map(x -> system.actorOf(
						Props.create(InterceptorWorker.class, port, x)
							.withDispatcher("nonblocking-io-dispatcher"), "interceptor_worker_" + x))
				.collect(Collectors.toList());
			
			Props props = Props.create(InterceptorMaster.class, ports[i], interceptorRoutees)
					.withDispatcher("nonblocking-io-dispatcher");
			system.actorOf(props, "interceptor");
			
			System.out.println("Starting interceptor server with port: " + ports[i] + " ======================================");
			
		}
	}
}
