package akka.doodle.cluster;

import akka.actor.AbstractActor;

public abstract class WorkerActor extends AbstractActor {
	protected final String port;
	protected final int id;
	
	public WorkerActor(String port, int id) {
		this.port = port;
		this.id = id;
	}
}
