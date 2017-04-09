package actors;

import akka.actor.ActorRef;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * websocketを通して配信するクライアントを登録, データのハンドリング
 * @author naoyabuzz, keisukeuema
 **/

public class Publisher<T> {

	/*
	 * actorRefというデータ構造をmapで保存しておく
	 */
	public final Map<Long, ActorRef> actorRefs = new HashMap<Long, ActorRef>();

	public Source<T, ?> register(Long userId) {

		/*
		 * OverflowStrategy.dropHead()は新しいデータが来たら古いデータを削除する
		 */
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
	public void broadcast(final T message) {
		for (Long userId : this.actorRefs.keySet()) {
			ActorRef actorRef = this.actorRefs.get(userId);
			actorRef.tell(message, ActorRef.noSender());
		}
	}

	/**
	 * 特定ユーザのみmessageを流す
	 **/
	public void broadcastOnlyUser(Long userId, final T message) {
		ActorRef actorRef = this.actorRefs.get(userId);
		actorRef.tell(message, ActorRef.noSender());
	}

	/**
	 * 複数のユーザにmessageを流す
	 **/
	public void broadcastOnlyGroup(ArrayList<Long> members, final T message) {
		for (Long userId : members) {
			ActorRef actorRef = this.actorRefs.get(userId);
			actorRef.tell(message, ActorRef.noSender());
		}
	}

}
