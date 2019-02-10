package akka.doodle.cluster.interceptor;

import org.apache.commons.lang3.StringUtils;

import akka.doodle.cluster.Messages;
import akka.doodle.cluster.WorkerActor;
import akka.doodle.cluster.Messages.RoutedFilterableProfile;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class InterceptorWorker extends WorkerActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	public InterceptorWorker(String port, int id) {
		super(port, id);
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(RoutedFilterableProfile.class, e ->{
				if (StringUtils.isNumeric(e.getMemberId())) {
					if (e.getAge() != null && e.getGender() != null && !e.getAge().isEmpty() && !e.getGender().isEmpty()) {
						log.info(" ====> InterceptorWorker - [{}.{}]: Valid profile: {}", port, id, e.getProfile());
					} else {
						log.info(" ====> InterceptorWorker - [{}.{}]: Profile not valid: {}", port, id, e.getProfile());
					}
				} else {
					log.info(" ====> InterceptorWorker - [{}.{}]: MemberId not valid: {}", port, id, e.getMemberId());
				}
			})
			.matchAny(o -> {
				log.info(" ====> InterceptorWorker - [{}.{}]: get a message: Unknown event: {}", port, id, o);
			})
			.build();
	}

}
