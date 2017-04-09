package controllers;

import actors.ChatRoomActor;
import actors.Publisher;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Random;

import play.mvc.*;

import views.html.*;

/**
 * 
 * @author naoyabuzz, keisukeuema
 *
 **/
public class HomeController extends Controller {

	private ActorSystem actorSystem = ActorSystem.create();
	private ActorRef chatRoomActor = actorSystem.actorOf(Props.create(ChatRoomActor.class));
	public static final Publisher<JsonNode> publisher = new Publisher<>();

	public Result index() {
		/*
		 * クライアントはindexにアクセスしたらuserIdが割り当てられる
		 */
		String userIdStr = session("userId");//sessionは文字列のみ?
		
		if (userIdStr == null) {
			/*
			 * 一時的に乱数でユーザIDを決定(あとで書き換える)
			 */
			Random rnd = new Random();
			Long userId = rnd.nextLong();
			userIdStr = userId.toString();

			session("userId", userIdStr);
		}

		return ok(testIndex.render(userIdStr));
	}

	public WebSocket ws() {
		/*
		 * クライアントのセッションからユーザIDを取得
		 */
		Long userId = Long.parseLong(session("userId"));

		return WebSocket.Json.accept(requestHeader -> {
			Source<JsonNode, ?> source = publisher.register(userId);
			Sink<JsonNode, NotUsed> sink = Sink.actorRef(chatRoomActor, "success");
			Flow<JsonNode, JsonNode, NotUsed> flow = Flow.fromSinkAndSource(sink, source);
			return flow;
		});
	}
}
