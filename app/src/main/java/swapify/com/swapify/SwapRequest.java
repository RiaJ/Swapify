package swapify.com.swapify;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.List;

@ParseClassName("SwapRequest")
public class SwapRequest extends ParseObject {
    public String getUserOneId() {
        return getString("userOneId");
    }

    public String getUserTwoId() {
        return getString("userTwoId");
    }

    public String getCombinedUserId() {
        return getString("combinedUserId");
    }

    public List<List<String>> getSwappedSeat() {
        return getList("swappedSeat");
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

    public void setSwappedSeat(String swappedSeat) {
        put("swappedSeat", swappedSeat);
    }
}
