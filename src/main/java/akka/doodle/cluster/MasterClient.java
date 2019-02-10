package akka.doodle.cluster;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.doodle.cluster.collector.CollectorClusterStarter;
import akka.doodle.cluster.interceptor.InterceptorClusterStarter;

public class MasterClient {
	public static void main(String[] args) throws InterruptedException {
		// creating collector servers in the cluster
		// each server holds a router and 3 collector actors
		CollectorClusterStarter.main(new String[] {"2551","3"});
		CollectorClusterStarter.main(new String[] {"2552","3"});
		CollectorClusterStarter.main(new String[] {"2553","3"});
		CollectorClusterStarter.main(new String[] {"2554","3"});
		
		// creating intercepter servers in the cluster
		// each server holds a router and 5 interceptor actors.
		InterceptorClusterStarter.main(new String[] {"2561","5"});
		InterceptorClusterStarter.main(new String[] {"2562","5"});
		InterceptorClusterStarter.main(new String[] {"2563","5"});
		InterceptorClusterStarter.main(new String[] {"2564","5"});
		InterceptorClusterStarter.main(new String[] {"2565","5"});
		InterceptorClusterStarter.main(new String[] {"2566","5"});
		
		// build remote client system to remotely call the cluster services 		
		ActorSystem system = ActorSystem.create("remote-client", ConfigFactory.load("client").getConfig("member-profile-client"));
		ActorSelection auctor1 = system.actorSelection("akka.tcp://ClusterSystem@127.0.0.1:2551/user/collector");
		ActorSelection auctor2 = system.actorSelection("akka.tcp://ClusterSystem@127.0.0.1:2552/user/collector");
		ActorSelection auctor3 = system.actorSelection("akka.tcp://ClusterSystem@127.0.0.1:2553/user/collector");
		ActorSelection auctor4 = system.actorSelection("akka.tcp://ClusterSystem@127.0.0.1:2554/user/collector");
		ActorSelection[] remoteActors = new ActorSelection[] {auctor1, auctor2, auctor3, auctor4};
	
		AtomicLong recordCounter = new AtomicLong(0L);
		
		try (Stream<String> stream = Files.lines(Paths.get(new URI("file:/Users/zhouqi/Downloads/KPOS_Pikato_Member_Profile_1.csv")))) {
	        stream.forEach(l -> {
	        	if (recordCounter.incrementAndGet() >= 10000000)
	        		return;
	        	remoteActors[Math.abs(l.hashCode()) % 2].tell(new Messages.RawProfile(l), ActorRef.noSender());
	        });
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		
		System.out.println("Processed 100000 lines.");
		
	}
}
