package akka.doodle.cluster.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import akka.actor.ActorRef;
import akka.actor.ActorSelectionMessage;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import akka.doodle.cluster.ClusterListenerActor;
import akka.doodle.cluster.Messages;
import akka.doodle.cluster.Messages.FilterableProfile;
import akka.doodle.cluster.Messages.RoutedFilterableProfile;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

public class InterceptorMaster extends ClusterListenerActor {
	private Router router;
	private final String port;
	private AtomicLong recordCounter = new AtomicLong(0L);
	
	List<ActorRef> processorWorkers = new ArrayList<>();
	
	public InterceptorMaster(String port, List<ActorRef> workers) {
		this.port = port;
		List<Routee> routees = new ArrayList<>();
		workers.forEach(w -> {
			getContext().watch(w);
			routees.add(new ActorRefRoutee(w));
		});
		router = new Router(new RoundRobinRoutingLogic(), routees);
	}
	public String getPort() {
		return port;
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(CurrentClusterState.class, e -> {
					log.info(" ====> InterceptorMaster - [{}] current state: {}", port, e);
					
				})
				
				.match(MemberUp.class, e -> {
					register(getCollectorPath(e.member()));
					log.info(" ====> InterceptorMaster - [{}] get a message: Member is up: {}, {}", port, e.member(), e.member().address());
				})
				.match(MemberRemoved.class, e -> {
					log.info(" ====> InterceptorMaster - [{}] get a message: Member is Removed: {}, {}", port, e.member(), e.member().address());
				})
				.match(UnreachableMember.class, e -> {
					log.info(" ====> InterceptorMaster - [{}] get a message: Member is Unreachable: {}, {}", port, e.member(), e.member().address());
				})
				.match(MemberEvent.class, e -> {
					log.info(" ====> InterceptorMaster - [{}] get a message: Member event: {}, {}", port, e.member(), e.member().address());
				})
				.match(ActorSelectionMessage.class, e -> {
					log.info(" ====> InterceptorMaster - [{}] get a message: ActorSelectionMessage: {}", port, e);
				})
				.match(Terminated.class, e -> {
					// remove the dead actor
			        router = router.removeRoutee(e.actor());
			        // create/add a new actor
			        ActorRef r = getContext().actorOf(Props.create(InterceptorWorker.class, port, -1), "interceptor_worker_-1");
			        getContext().watch(r);
			        router = router.addRoutee(new ActorRefRoutee(r));
			        log.info(" ====> InterceptorMaster - [{}]: get a message: Terminated: {}", port,  e.getActor().path());
				})
				
				.match(FilterableProfile.class, e -> {
					long counter = recordCounter.incrementAndGet();
					
//					if (processorWorkers.size() > 0) {
//						int interceptorIndex = (int) ((counter < 0 ? 0 : counter) % processorWorkers.size());
//						router.route(new RoutedFilterableProfile(processorWorkers.get(interceptorIndex) ,e), getSender());
	
						int interceptorIndex = 0;
						router.route(new RoutedFilterableProfile(self() ,e), getSender());
						
						log.info(" ====> InterceptorMaster - [{}]: route message to processor worker index: {}/{}", port, interceptorIndex, processorWorkers.size());
//					} else {
//						log.info(" ====> InterceptorMaster - [{}]: no processor work registered. Message lost");
//					}
				})
				.matchAny(o -> {
					log.info(" ====> InterceptorMaster - [{}] get a message: Unknown event: {}", port, o);
				})
				.build();
	}
	
	private String getCollectorPath(Member member) {
		return member.address() + "/user/collector";
	}
}
