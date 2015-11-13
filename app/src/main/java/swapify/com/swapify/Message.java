package swapify.com.swapify;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Thomas on 2015-11-13.
 */
@ParseClassName("Message")
public class Message extends ParseObject {
    public String getUserId() {
        return getString("userId");
    }

    public String getBody() {
        return getString("body");
    }

    public void setUserId(String userId) {
        put("userId", userId);
    }

    public void setBody(String body) {
        put("body", body);
    }
}
