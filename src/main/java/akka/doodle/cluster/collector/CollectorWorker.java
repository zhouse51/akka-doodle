package akka.doodle.cluster.collector;

import akka.doodle.cluster.Messages;
import akka.doodle.cluster.WorkerActor;
import akka.doodle.cluster.Messages.FilterableProfile;
import akka.doodle.cluster.Messages.MemberProfile;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class CollectorWorker extends WorkerActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	public CollectorWorker(String port, int id) {
		super(port, id);
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				
				.match(MemberProfile.class, rp -> {
					String memberId = rp.getProfile().split(",")[0];
					String age = rp.getProfile().split(",")[1];
					String gender = rp.getProfile().split(",")[2];
					log.info(" ====> CollectorWorker - [{}.{}]: Build filterable profile for interceptor: {}", port, id, rp.getWorker());
					rp.getWorker().tell(new FilterableProfile(memberId, age, gender, rp.getProfile()), getSelf());
				})
				.matchAny(o -> {
					log.info(" ====> CollectorWorker - [{}.{}]: get a message: Unknown event: {}", port, id, o);
				})
				.build();
	}
}
