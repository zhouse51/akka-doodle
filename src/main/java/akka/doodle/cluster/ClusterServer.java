package akka.doodle.cluster;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.doodle.cluster.collector.CollectorWorker;
import akka.doodle.cluster.interceptor.InterceptorMaster;

public class ClusterServer {
	public static void main(String[] args) throws InterruptedException {
		String[] name = new String[]{"A", "B", "C"};
		String[] ports = new String[]{"2551", "2552", "2553"};
		Class<?>[] actors = new Class<?>[] {CollectorWorker.class, InterceptorMaster.class, ListenerC.class};
		for (int i = 0; i<ports.length;i++){
			System.out.println("Starting server with port: " + ports[i] + " =====================================");
			Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + ports[i])
					.withFallback(ConfigFactory.parseString("akka.cluster.roles = [role_"+i+", testRole]"))
					.withFallback(ConfigFactory.load("server")
					.getConfig("server-cluster"));
			ActorSystem system = ActorSystem.create("ClusterSystem", config);
			system.actorOf(Props.create(actors[i]), "clusterListener_"+name[i]);
			System.out.println("Started server with port: " + ports[i] + " ======================================");
			
			Thread.sleep(5000);
		};
	}
}
