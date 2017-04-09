package actors;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import static controllers.HomeController.publisher;

public class ChatRoomActor extends UntypedActor {

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof JsonNode) {
			JsonNode jsonMessage = (JsonNode) message;
			String type = jsonMessage.get("type").textValue();

			String userId = jsonMessage.get("uuid").asText();

			switch (type) {

			case "join":
				String joinedUser = jsonMessage.get("username").asText();
				JsonNode joinToClient = Json.newObject().put("type", "joined").put("username", joinedUser).put("uuid",
						userId);

				publisher.broadcast(userId, joinToClient);
				break;

			case "talk":
				String talkedUser = jsonMessage.get("username").asText();
				String chatMessage = jsonMessage.get("chatMessage").asText();
				JsonNode talkToClient = Json.newObject().put("type", "talked").put("username", talkedUser)
						.put("chatMessage", chatMessage).put("uuid", userId);

				publisher.broadcast(userId, talkToClient);
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
