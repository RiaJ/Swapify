package swapify.com.swapify;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("FlightInfo")
public class FlightInfo extends ParseObject {
    public String getFlightNo() {
        return getString("flightNo");
    }

    public String getPlaneType() {
        return getString("planeType");
    }

    public String getSeatNo() {
        return getString("seatNo");
    }

    public void setFlightNo(String flightNo) {
        put("flightNo", flightNo);
    }

    public void setPlaneType(String planeType) {
        put("planeType", planeType);
    }

    public void setSeatNo(String seatNo) {
        put("seatNo", seatNo);
    }
}
