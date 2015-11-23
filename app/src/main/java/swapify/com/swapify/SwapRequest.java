package swapify.com.swapify;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.List;

@ParseClassName("SwapRequest")
public class SwapRequest extends ParseObject {
    public String getRequestKey() {
        return getString("requestKey");
    }

    public String getUserOneId() {
        return getString("userOneId");
    }

    public String getUserTwoId() {
        return getString("userTwoId");
    }

    public String getUserOneSeat() {
        return getString("userOneSeat");
    }

    public String getUserTwoSeat() {
        return getString("userTwoSeat");
    }

    public String getUserOneFlight() {
        return getString("userOneFlight");
    }

    public String getUserTwoFlight() {
        return getString("userTwoFlight");
    }

    public void setRequestKey (String requestKey) {
        put("requestKey", requestKey);
    }

    public void setUserOneId(String userOneId) {
        put("userOneId", userOneId);
    }

    public void setUserTwoId(String userTwoId) {
        put("userTwoId", userTwoId);
    }

    public void setUserOneSeat(String userOneSeat) {
        put("userOneSeat", userOneSeat);
    }

    public void setUserTwoSeat(String userTwoSeat) {
        put("userTwoSeat", userTwoSeat);
    }

    public void setUserOneFlight(String userOneFlight) {
        put("userOneFlight", userOneFlight);
    }

    public void setUserTwoFlight(String userTwoFlight) {
        put("userTwoFlight", userTwoFlight);
    }
}
