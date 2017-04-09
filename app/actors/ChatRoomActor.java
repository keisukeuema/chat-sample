package actors;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import static controllers.HomeController.publisher;

import java.util.ArrayList;

/**
 * websocketを通してメッセージを受け取った後の処理
 * 
 * @author naoyabuzz, keisukeuema
 *
 **/
public class ChatRoomActor extends UntypedActor {

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof JsonNode) {
			JsonNode jsonMessage = (JsonNode) message;
			String type = jsonMessage.get("type").textValue();

			/*
			 *  複数のユーザIDを取得
			 */
			JsonNode membersNode = jsonMessage.get("members");
			ArrayList<Long> members = new ArrayList<Long>();
			for (int count = 0; count < membersNode.size(); count++) {
				Long userId = membersNode.get(count).asLong();
				members.add(userId);
			}

			switch (type) {

			case "join":
				String joinedUser = jsonMessage.get("username").asText();
				JsonNode joinToClient = Json.newObject().put("type", "joined").put("username", joinedUser)
						.put("member_1", members.get(0)).put("member_2", members.get(1));

				publisher.broadcastOnlyGroup(members, joinToClient);
				// publisher.broadcastOnlyUser(userId, joinToClient);//特定のユーザに
				// publisher.broadcast(joinToClient);//全てのユーザに

				break;

			case "talk":
				String talkedUser = jsonMessage.get("username").asText();
				String chatMessage = jsonMessage.get("chatMessage").asText();
				JsonNode talkToClient = Json.newObject().put("type", "talked").put("username", talkedUser)
						.put("chatMessage", chatMessage).put("member_1", members.get(0))
						.put("member_2", members.get(1));

				publisher.broadcastOnlyGroup(members, talkToClient);
				// publisher.broadcastOnlyUser(userId, talkToClient);
				// publisher.broadcast(talkToClient);

				break;

			default:
				System.out.println("Json Error: type is not allowed");
				break;
			}
		} else {
			System.out.println("chatRoomActor received not Json");
		}
	}
}
