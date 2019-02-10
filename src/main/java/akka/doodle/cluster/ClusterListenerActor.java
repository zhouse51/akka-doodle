package akka.doodle.cluster;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public abstract class ClusterListenerActor extends AbstractActor {

	protected LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	private Cluster cluster = Cluster.get(getContext().system());
	
	@Override
	public void preStart() {
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
	}

	@Override
	public void postStop() {
		cluster.unsubscribe(getSelf());
	}

	protected void register(String actorPath) {
        ActorSelection actorSelection = getContext().actorSelection(actorPath);
        actorSelection.tell(new Messages.Registration(), getSelf());
    }
}
