package actors;

import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

/*
 * sessionをつかうため
 */
//import play.mvc.*;
import play.mvc.Controller;
/*
import play.mvc.Http.HeaderNames;
import play.mvc.Http;
//import play.mvc.Http.Context;
import play.mvc.Http.Session;
import play.mvc.Http.Status;
*/
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import static controllers.HomeController.publisher;
import static controllers.HomeController.sessionFromActor;
//import static controllers.CookieTest.setCookie;


import java.util.ArrayList;

/**
 * websocketを通してメッセージを受け取った後の処理
 * 
 * @author naoyabuzz, keisukeuema
 *
 **/
public class ChatRoomActor extends UntypedActor {


	/*
	 * sessionについて
	 * https://github.com/playframework/playframework/blob/38abd1ca6d17237950c82b1483057c5c39929cb4/framework/src/play/src/main/java/play/mvc/Controller.java
	 */
	/*
    public static Session session() {
        return Http.Context.current().session();
    }
    
    public static void session(String key, String value) {
        session().put(key, value);
    }

    public static String session(String key) {
        return session().get(key);
}
*/
    
	//private final WSClient wsc;
	
	@Override
	public void onReceive(Object message) throws Throwable {
		if (message instanceof JsonNode) {
			JsonNode jsonMessage = (JsonNode) message;
			String type = jsonMessage.get("type").textValue();

			ArrayList<Long> members = new ArrayList<Long>();
			String membersCSV = "";	
			switch (type) {

			case "join":
				
				/*
				 * 複数のユーザIDを取得
				 */
				JsonNode membersNode = jsonMessage.get("members");

				for (int count = 0; count < membersNode.size(); count++) {
					Long userId = membersNode.get(count).asLong();
					members.add(userId);
					membersCSV+= userId.toString()+",";
				}
				//System.out.println(membersCSV);
				/*
				 * sessionにメンバーを保存
				 * csv形式
				 */
				sessionFromActor("members", membersCSV);
				//setCookie("members", membersCSV);
				
				String joinedUser = jsonMessage.get("username").asText();
				JsonNode joinToClient = Json.newObject().put("type", "joined").put("username", joinedUser)
						.put("member_1", members.get(0)).put("member_2", members.get(1));			
				publisher.broadcastOnlyGroup(members, joinToClient);
				break;

			case "talk":	
				/*
				 * sessionからユーザIDを取得
				 */
				for (String userIdStr: sessionFromActor("members").split(",", 0)) {
					members.add(Long.parseLong(userIdStr));
				}
				
				String talkedUser = jsonMessage.get("username").asText();
				String chatMessage = jsonMessage.get("chatMessage").asText();
				JsonNode talkToClient = Json.newObject().put("type", "talked").put("username", talkedUser)
						.put("chatMessage", chatMessage);
				publisher.broadcastOnlyGroup(members, talkToClient);
				break;
				
			case "exit":		
				/*
				 * sessionのmembersから外す処理
				 */
				Long removeUserId = (long) 1;//適当に
				for (String userIdStr: sessionFromActor("members").split(",", 0)) {
					Long userId = Long.parseLong(userIdStr);
					if (removeUserId != userId){
						members.add(userId);
						membersCSV += userId.toString()+",";
					}
				}
				sessionFromActor("members", membersCSV);
				
				/*
				 * 退室の通知
				 */
				String exitedUser = jsonMessage.get("username").asText();
				JsonNode exitToClient = Json.newObject().put("type", "exit").put("user", exitedUser);
				publisher.broadcastOnlyGroup(members, exitToClient);
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
