package actors;

import akka.actor.ActorRef;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Publisher<T> {

	// actorRefというデータ構造?が複数ある？
	// それをmap構造として保存しておく
	public final Map<String, ActorRef> actorRefs = new HashMap<String, ActorRef>();

	public Source<T, ?> register(String userId) {

		// OverflowStrategy.dropHead()は新しいデータが来たら古いデータを削除する
		Source<T, ?> source = Source.<T>actorRef(256, OverflowStrategy.dropHead()).mapMaterializedValue(actorRef -> {
			Publisher.this.actorRefs.put(userId, actorRef);
			return actorRef;
		}).watchTermination((actorRef, termination) -> {
			termination.whenComplete((done, cause) -> Publisher.this.actorRefs.remove(userId, actorRef));
			return null;
		});
		return source;
	}

	/**
	 * 全てのユーザにmessageを流す
	 **/
	public void broadcast(final T message){
		//userIdがString型の場合(実際はLongなので後で直す)
		for (String userId: this.actorRefs.keySet()){
			ActorRef actorRef = this.actorRefs.get(userId);
			actorRef.tell(message, ActorRef.noSender());
		}
	}
	
	/**
	 * 特定ユーザのみmessageを流す
	 **/
	public void broadcastOnlyUser(String userId, final T message) {
		ActorRef actorRef = this.actorRefs.get(userId);
		actorRef.tell(message, ActorRef.noSender());
	}
	
	/**
	 * 複数のユーザにmessageを流す
	 **/
	public void broadcastOnlyGroup(ArrayList<String> userIdArray, final T message) {
		//userIdがString型の場合(実際はLongなので後で直す)
		for (String userId: userIdArray){
			ActorRef actorRef = this.actorRefs.get(userId);
			actorRef.tell(message, ActorRef.noSender());
		}
	}
	
	
}
