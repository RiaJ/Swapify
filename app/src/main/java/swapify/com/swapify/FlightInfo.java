package swapify.com.swapify;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@ParseClassName("FlightInfo")
public class FlightInfo extends ParseObject {
    public String getFlightNo() {
        return getString("flightNo");
    }

    public String getEquipment() {
        return getString("equipment");
    }

    public String getTakeOffTime() {
        return getString("takeOffTime");
    }

    public String getSeatMap() {
        return getString("seatMap");
    }

    public String getLogo() {
        return getString("logo");
    }

    public List<Map.Entry<BigInteger, String>> getSeats() {
        return getList("seats");
    }

    public void setFlightNo(String flightNo) {
        put("flightNo", flightNo);
    }

    public void setEquipment(String equipment) {
        put("equipment", equipment);
    }

    public void setTakeOffTime(String takeOffTime) {
        put("takeOffTime", takeOffTime);
    }

    public void setSeatMap(String seatMap) {
        put("seatMap", seatMap);
    }

    public void setLogo(String logo) {
        put("logo", logo);
    }

    public void setSeats(List<Map.Entry<BigInteger, String>> seats) {
        put("seats", seats);
    }
}
