package akka.doodle.cluster.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import akka.actor.ActorRef;
import akka.actor.ActorSelectionMessage;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.cluster.Member;
import akka.doodle.cluster.ClusterListenerActor;
import akka.doodle.cluster.Messages;
import akka.doodle.cluster.Messages.MemberProfile;
import akka.doodle.cluster.Messages.RawProfile;
import akka.doodle.cluster.Messages.Registration;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;

public class CollectorMaster extends ClusterListenerActor {
	
	private Router router;
	private final String port;
	private AtomicLong recordCounter = new AtomicLong(0L);
	
	List<ActorRef> intercaptorWorkers = new ArrayList<>();
	
	public CollectorMaster(String port, List<ActorRef> collectorWorkers) {
		this.port = port;
		List<Routee> routees = new ArrayList<>();
		collectorWorkers.forEach(w -> {
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
					log.info(" ====> CollectorMaster current state: {}", e);
					Iterable<Member> members = e.getMembers();
					members.forEach(m -> {
						System.out.println(m.roles());
					});
				})
				.match(MemberUp.class, e -> {
					log.info(" ====> CollectorMaster - [{}]: get a message: Member is up: {}, {}", port, e.member(), e.member().address());
				})
				.match(MemberRemoved.class, e -> {
					log.info(" ====> CollectorMaster - [{}]: get a message: Member is Removed: {}, {}", port, e.member(), e.member().address());
				})
				.match(UnreachableMember.class, e -> {
					log.info(" ====> CollectorMaster - [{}]: get a message: Member is Unreachable: {}, {}", port, e.member(), e.member().address());
				})
				.match(MemberEvent.class, e -> {
					log.info(" ====> CollectorMaster - [{}]: get a message: Member event: {}, {}", port, e.member(), e.member().address());
				})
				.match(ActorSelectionMessage.class, e -> {
					log.info(" ====> CollectorMaster - [{}]: get a message: ActorSelectionMessage: {}", port,  e);
				})
				.match(Terminated.class, e -> {
					// remove the dead actor
			        router = router.removeRoutee(e.actor());
			        // create/add a new actor
			        ActorRef r = getContext().actorOf(Props.create(CollectorWorker.class, port, -1), "collector_worker_-1");
			        getContext().watch(r);
			        router = router.addRoutee(new ActorRefRoutee(r));
			        log.info(" ====> CollectorMaster - [{}]: get a message: Terminated: {}", port,  e.getActor().path());
				})
				
				.match(Messages.Registration.class, r ->{
					getContext().watch(getSender());
					intercaptorWorkers.add(getSender());
		            log.info(" ====> CollectorMaster - [{}]: {} Interceptor(s) registered. Registered interceptors: {}", port, intercaptorWorkers.size(), getSender());
		            
				})
				.match(RawProfile.class, rp -> {
					long counter = recordCounter.incrementAndGet();
					
					if (intercaptorWorkers.size() > 0) {
						int interceptorIndex = (int) ((counter < 0 ? 0 : counter) % intercaptorWorkers.size());
						router.route(new Messages.MemberProfile(intercaptorWorkers.get(interceptorIndex), rp.getData()), getSender());
						log.info(" ====> CollectorMaster - [{}]: route message to interceptor worker index: {}/{}", port, interceptorIndex, intercaptorWorkers.size());
					} else {
						log.info(" ====> CollectorMaster - [{}]: no interceptor work registed. Message lost");
					}
				})
				.matchAny(o -> {
					log.info(" ====> CollectorMaster - [{}]: get a message: Unknown event: {}", port, o);
				})
				.build();
	}

}
