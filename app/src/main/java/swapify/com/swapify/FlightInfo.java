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

    public String getDepartureDate() {
        return getString("departureDate");
    }

    public String getTakeOffTime() {
        return getString("takeOffTime");
    }

    public String getDepartureCity() {
        return getString("departureCity");
    }

    public String getDepartureIATA() {
        return getString("departureIATA");
    }

    public String getArrivalCity() {
        return getString("arrivalCity");
    }

    public String getArrivalIATA() {
        return getString("arrivalIATA");
    }


    public String getLogo() {
        return getString("logo");
    }

    public List<List<String>> getSeats() {
        return getList("seats");
    }

    public void setFlightNo(String flightNo) {
        put("flightNo", flightNo);
    }

    public void setEquipment(String equipment) {
        put("equipment", equipment);
    }

    public void setDepartureDate(String departureDate) {
        put("departureDate", departureDate);
    }

    public void setTakeOffTime(String takeOffTime) {
        put("takeOffTime", takeOffTime);
    }

    public void setDepartureCity(String departureCity) {
        put("departureCity", departureCity);
    }

    public void setDepartureIATA(String departureIATA) {
        put("departureIATA", departureIATA);
    }

    public void setArrivalCity(String arrivalCity) {
        put("arrivalCity", arrivalCity);
    }

    public void setArrivalIATA(String arrivalIATA) {
        put("arrivalIATA", arrivalIATA);
    }

    public void setLogo(String logo) {
        put("logo", logo);
    }

    public void setSeats(List<List<String>> seats) {
        put("seats", seats);
    }
}
