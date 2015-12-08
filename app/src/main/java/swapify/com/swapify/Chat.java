package swapify.com.swapify;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.List;

@ParseClassName("Chat")
public class Chat extends ParseObject {
    public String getUserOneId() {
        return getString("userOneId");
    }

    public String getUserTwoId() {
        return getString("userTwoId");
    }

    public String getCombinedUserId() {
        return getString("combinedUserId");
    }

    public List<List<String>> getMessages() {
        return getList("messages");
    }

    public void setUserOneId(String userOneId) {
        put("userOneId", userOneId);
    }

    public void setUserTwoId(String userTwoId) {
        put("userTwoId", userTwoId);
    }

    public void setCombinedUserId(String combinedUserId) {
        put("combinedUserId", combinedUserId);
    }

    public void setMessages(List<List<String>> messages) {
        put("messages", messages);
    }
}
