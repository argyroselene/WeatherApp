package application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class LocationService {
    private static final String POSITIONSTACK_API_KEY = "YOUR_API_KEY";

    // for searching getting coordinates
    public static double[] getCoordinates(String city) {
        try {
            String urlStr = "http://api.positionstack.com/v1/forward"
                    + "?access_key=" + POSITIONSTACK_API_KEY
                    + "&query=" + city.replace(" ", "%20")
                    + "&limit=1";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONObject json = new JSONObject(sb.toString());
            JSONArray data = json.getJSONArray("data");
            if (data.length() > 0) {
                JSONObject loc = data.getJSONObject(0);
                double lat = loc.getDouble("latitude");
                double lon = loc.getDouble("longitude");
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // if failed
    }

    // dynamically get current location
    public static double[] getCurrentLocation() {
        try {
            String urlStr = "http://ip-api.com/json"; // IP-based geolocation
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONObject json = new JSONObject(sb.toString());
            double lat = json.getDouble("lat");
            double lon = json.getDouble("lon");
            return new double[]{lat, lon};

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // if it fails
    }
}

