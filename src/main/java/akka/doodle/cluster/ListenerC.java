package akka.doodle.cluster;

import akka.actor.AbstractActor;
import akka.actor.ActorSelectionMessage;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ListenerC extends AbstractActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(CurrentClusterState.class, e -> {
					log.info(" ====> ListenerC current state: {}", e);
				})
				.match(MemberUp.class, e -> {
					log.info(" ====> ListenerC get a message: Member is up: {}, {}", e.member(), e.member().address());
				})
				.match(MemberRemoved.class, e -> {
					log.info(" ====> ListenerC get a message: Member is Removed: {}, {}", e.member(), e.member().address());
				})
				.match(UnreachableMember.class, e -> {
					log.info(" ====> ListenerC get a message: Member is Unreachable: {}, {}", e.member(), e.member().address());
				})
				.match(MemberEvent.class, e -> {
					log.info(" ====> ListenerC get a message: Member event: {}, {}", e.member(), e.member().address());
				})
				.match(ActorSelectionMessage.class, e -> {
					log.info(" ====> ListenerC get a message: ActorSelectionMessage: {}, {}", e);
				})
				.matchAny(o -> {
					log.info(" ====> ListenerC get a message: Unknown event: {}, {}", o);
				})
				.build();
	}
}
