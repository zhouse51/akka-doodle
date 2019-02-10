package akka.doodle.cluster;

import java.io.Serializable;

import akka.actor.ActorRef;

public interface Messages {
	public static class Message implements Serializable {
		private static final long serialVersionUID = 1L;
	}
	
	public static final class Registration implements Serializable {
		private static final long serialVersionUID = 1L;
    }
	
	public static class RawProfile extends Message {
		private static final long serialVersionUID = 1L;
		private final String data;
		public RawProfile(String data) {
			this.data = data;
		}
		public String getData() {
			return data;
		}
	}
	
	public static class MemberProfile extends Message {
		private static final long serialVersionUID = 1L;
		private final ActorRef worker;
		private final String profile;
		public MemberProfile(ActorRef worker, String profile) {
			this.worker = worker;
			this.profile = profile;
		}
		public ActorRef getWorker() {
			return worker;
		}
		public String getProfile( ) {
			return profile;
		}
	}
	
	public static class FilterableProfile extends Message {
		private static final long serialVersionUID = 1L;
		private final String memberId;
		private final String age;
		private final String gender;
		private final String profile;
		
		public FilterableProfile(String memberId, String age, String gender, String profile) {
			this.memberId = memberId;
			this.age = age;
			this.gender = gender;
			this.profile = profile;
		}

		public String getMemberId() {
			return memberId;
		}
		public String getAge() {
			return age;
		}
		public String getGender() {
			return gender;
		}
		public String getProfile() {
			return profile;
		}
	}
	
	public static class RoutedFilterableProfile extends FilterableProfile {
		private static final long serialVersionUID = 1L;
		private final ActorRef interceptorWorker;
		
		public RoutedFilterableProfile(ActorRef interceptorWorker, FilterableProfile filterableProfile) {
			super(filterableProfile.getMemberId(), filterableProfile.getAge(), filterableProfile.getGender(), filterableProfile.getProfile());
			this.interceptorWorker = interceptorWorker;
		}
		public ActorRef getInterceptorWorker() {
			return interceptorWorker;
		}
	}
	
}
