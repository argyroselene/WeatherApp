package application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherService {
    private static final String WEATHER_API_KEY = "YOUR_API_KEY";

    private static String fetch(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }

    public static JSONObject getCurrentWeather(double lat, double lon) throws Exception {
        String url = "http://api.weatherapi.com/v1/current.json?key=" + WEATHER_API_KEY
                   + "&q=" + lat + "," + lon + "&aqi=yes";
        return new JSONObject(fetch(url));
    }

    public static List<ForecastData> getForecast(double lat, double lon) throws Exception {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=" + WEATHER_API_KEY
                   + "&q=" + lat + "," + lon + "&days=3&aqi=no&alerts=no";
        JSONObject json = new JSONObject(fetch(url));
        JSONArray days = json.getJSONObject("forecast").getJSONArray("forecastday");

        List<ForecastData> list = new ArrayList<>();
        for (int i = 0; i < days.length(); i++) {
            JSONObject day = days.getJSONObject(i);
            String date = day.getString("date");
            JSONObject d = day.getJSONObject("day");
            list.add(new ForecastData(date, d.getDouble("maxtemp_c"), d.getDouble("mintemp_c"),
                    d.getJSONObject("condition").getString("text")));
        }
        return list;
    }

    public static List<HistoricalData> getHistory(double lat, double lon) throws Exception {
        List<HistoricalData> list = new ArrayList<>();
        
        // Add today using current weather
        JSONObject current = getCurrentWeather(lat, lon);
        String todayDate = LocalDate.now().toString();
        double avgTemp = current.getJSONObject("current").getDouble("temp_c");
        String condition = current.getJSONObject("current").getJSONObject("condition").getString("text");
        list.add(new HistoricalData(todayDate, avgTemp, condition));

        LocalDate today = LocalDate.now();
        for (int i = 1; i <= 7; i++) {
            String date = today.minusDays(i).toString();
            System.out.println("Fetching history for: " + date); // debug
            String url = "http://api.weatherapi.com/v1/history.json?key=" + WEATHER_API_KEY
                       + "&q=" + lat + "," + lon + "&dt=" + date;
            JSONObject json = new JSONObject(fetch(url));
            JSONObject day = json.getJSONObject("forecast")
                                 .getJSONArray("forecastday")
                                 .getJSONObject(0)
                                 .getJSONObject("day");
            list.add(new HistoricalData(date, day.getDouble("avgtemp_c"),
                    day.getJSONObject("condition").getString("text")));
        }

        return list;
    }

}

