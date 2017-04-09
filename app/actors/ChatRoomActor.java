package actors;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import static controllers.HomeController.publisher;

import java.util.ArrayList;

public class ChatRoomActor extends UntypedActor {

	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof JsonNode) {
			JsonNode jsonMessage = (JsonNode) message;
			String type = jsonMessage.get("type").textValue();

			//複数のユーザIDを取得	
			JsonNode userIdNode = jsonMessage.get("uuids");
			ArrayList<String> userIdArray = new ArrayList<String>();
			for (int count = 0; count < userIdNode.size(); count++){
				userIdArray.add(userIdNode.get(count).textValue());
			}
			
			switch (type) {

			case "join":
				String joinedUser = jsonMessage.get("username").asText();
				JsonNode joinToClient = Json.newObject().put("type", "joined").put("username", joinedUser)
						.put("uuid_1",userIdArray.get(0)).put("uuid_2",userIdArray.get(1));

				publisher.broadcastOnlyGroup(userIdArray, joinToClient);
				//publisher.broadcastOnlyUser(userId, joinToClient);//特定のユーザに
				//publisher.broadcast(joinToClient);//全てのユーザに
				
				break;

			case "talk":
				String talkedUser = jsonMessage.get("username").asText();
				String chatMessage = jsonMessage.get("chatMessage").asText();
				JsonNode talkToClient = Json.newObject().put("type", "talked").put("username", talkedUser)
						.put("chatMessage", chatMessage)
						.put("uuid_1",userIdArray.get(0)).put("uuid_2",userIdArray.get(1));

				publisher.broadcastOnlyGroup(userIdArray, talkToClient);
				//publisher.broadcastOnlyUser(userId, talkToClient);
				//publisher.broadcast(talkToClient);
				
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
