package application;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

public class WeatherController {

    @FXML private TextField cityField;
    @FXML private Button searchButton;
    @FXML private Button myLocationButton;
    @FXML private ToggleButton modeToggle;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private BorderPane rootPane;

    @FXML private VBox weatherBox;
    @FXML private Label locationLabel, temperatureLabel, conditionLabel, detailsLabel;
    @FXML private Label feelsLikeLabel, humidityLabel, cloudCoverageLabel, precipitationLabel;
    @FXML private Label timeLabel, aqiLabel;
    @FXML private ImageView currentWeatherIcon;

    @FXML private HBox forecastBox, historyBox;
    @FXML private Label forecastTitle, historyTitle;

    private double[] myLocationCoords;

    @FXML
    public void initialize() {
        searchButton.setOnAction(e -> fetchWeather());
        myLocationButton.setOnAction(e -> {
            if (myLocationCoords != null)
                fetchWeatherByCoordinates(myLocationCoords[0], myLocationCoords[1]);
        });

        modeToggle.setOnAction(e -> toggleTheme());
        new Thread(() -> {
            try {
                myLocationCoords = LocationService.getCurrentLocation();
                if (myLocationCoords != null)
                    Platform.runLater(() -> fetchWeatherByCoordinates(myLocationCoords[0], myLocationCoords[1]));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void toggleTheme() {
        Scene scene = rootPane.getScene();
        if (scene == null) return;

        scene.getStylesheets().clear();
        if (modeToggle.isSelected()) {
            scene.getStylesheets().add(getClass().getResource("dark.css").toExternalForm());
            modeToggle.setText("Light Mode");
        } else {
            scene.getStylesheets().add(getClass().getResource("light.css").toExternalForm());
            modeToggle.setText("Dark Mode");
        }
    }

    private Image getWeatherIcon(String condition) {
        String iconFile = "season.png";
        if (condition != null) {
            condition = condition.toLowerCase();
            if (condition.contains("sun") || condition.contains("clear")) iconFile = "sun.png";
            else if (condition.contains("cloud")) iconFile = "cloudy.png";
            else if (condition.contains("rain")) iconFile = "rainy-day.png";
            else if (condition.contains("thunder")) iconFile = "storm.png";
            else if (condition.contains("snow")) iconFile = "snow.png";
            else if (condition.contains("fog")) iconFile = "fog.png";
            else if (condition.contains("light drizzle")) iconFile = "rainy-day.png";
        }
        try {
            InputStream is = getClass().getResourceAsStream(iconFile);
            if (is == null) {
                System.out.println("Missing icon: " + iconFile);
                return new Image("https://via.placeholder.com/80");
            }
            return new Image(is);
        } catch (Exception e) {
            e.printStackTrace();
            return new Image("https://via.placeholder.com/80");
        }
    }


    private void fetchWeather() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            locationLabel.setText("Please enter a city.");
            return;
        }
        loadingIndicator.setVisible(true);

        new Thread(() -> {
            try {
                double[] coords = LocationService.getCoordinates(city);
                if (coords == null) {
                    updateUIError("Could not find city.");
                    return;
                }
                fetchWeatherByCoordinates(coords[0], coords[1]);
            } catch (Exception ex) {
                ex.printStackTrace();
                updateUIError("Error fetching weather.");
            }
        }).start();
    }

    private VBox createMiniWeatherCard(String date, double tempMax, double tempMin, String condition) {
        VBox card = new VBox();
        card.getStyleClass().add("mini-weather-card");
        card.setMaxWidth(200); 
        card.setSpacing(5);

        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("mini-weather-card-label");

        ImageView icon = new ImageView(getWeatherIcon(condition));
        icon.setFitWidth(50);
        icon.setFitHeight(50);
        icon.setPreserveRatio(true);

        Label tempLabel = new Label("Max: " + tempMax + "°C | Min: " + tempMin + "°C");
        tempLabel.getStyleClass().add("mini-weather-card-label");

        Label condLabel = new Label(condition);
        condLabel.getStyleClass().add("mini-weather-card-label");

        card.getChildren().addAll(dateLabel, icon, tempLabel, condLabel);
        return card;
    }

    private VBox createMiniHistoryCard(String date, double avgTemp, String condition) {
        VBox card = new VBox();
        card.getStyleClass().add("mini-weather-card");
        card.setMaxWidth(200);
        card.setSpacing(5);

        Label dateLabel = new Label(date);
        dateLabel.getStyleClass().add("mini-weather-card-label");

        ImageView icon = new ImageView(getWeatherIcon(condition));
        icon.setFitWidth(50);
        icon.setFitHeight(50);
        icon.setPreserveRatio(true);

        Label tempLabel = new Label("Avg: " + avgTemp + "°C");
        tempLabel.getStyleClass().add("mini-weather-card-label");

        Label condLabel = new Label(condition);
        condLabel.getStyleClass().add("mini-weather-card-label");

        card.getChildren().addAll(dateLabel, icon, tempLabel, condLabel);
        return card;
    }



    private void fetchWeatherByCoordinates(double lat, double lon) {
        Platform.runLater(() -> loadingIndicator.setVisible(true));

        try {
            JSONObject current = WeatherService.getCurrentWeather(lat, lon);

            // Validate returned city name against input
            String apiCity = current.getJSONObject("location").getString("name");
            String userCity = cityField.getText().trim();
            if (!userCity.isEmpty() && !apiCity.toLowerCase().contains(userCity.toLowerCase())) {
                updateUIError("City not found. Please check spelling.");
                return;
            }

            List<ForecastData> forecast = WeatherService.getForecast(lat, lon);
            List<HistoricalData> history = WeatherService.getHistory(lat, lon);

            Platform.runLater(() -> {
                // Current weather
                locationLabel.setText(apiCity);
                temperatureLabel.setText(current.getJSONObject("current").getDouble("temp_c") + "°C");
                conditionLabel.setText(current.getJSONObject("current").getJSONObject("condition").getString("text"));
                detailsLabel.setText("Feels like: " + current.getJSONObject("current").getDouble("feelslike_c") +
                        "°C | Humidity: " + current.getJSONObject("current").getInt("humidity") + "%");
                feelsLikeLabel.setText("Feels like: " + current.getJSONObject("current").getDouble("feelslike_c") + "°C");
                humidityLabel.setText("Humidity: " + current.getJSONObject("current").getInt("humidity") + "%");
                cloudCoverageLabel.setText("Cloud: " + current.getJSONObject("current").getInt("cloud") + "%");
                precipitationLabel.setText("Precipitation: " + current.getJSONObject("current").getDouble("precip_mm") + " mm");
                timeLabel.setText("Local time: " + current.getJSONObject("location").getString("localtime"));
                aqiLabel.setText("AQI: " + current.getJSONObject("current").getJSONObject("air_quality").getInt("us-epa-index"));
                currentWeatherIcon.setImage(getWeatherIcon(conditionLabel.getText()));

                // Forecast boxes
                forecastBox.getChildren().clear();
                if (forecast != null) {
                    for (ForecastData f : forecast) {
                        forecastBox.getChildren().add(createMiniWeatherCard(
                                f.getDate(), f.getMaxTemp(), f.getMinTemp(), f.getCondition()
                        ));
                    }
                }

                // History boxes
                historyBox.getChildren().clear();
                if (history != null) {
                    for (HistoricalData h : history) {
                        historyBox.getChildren().add(createMiniHistoryCard(
                                h.getDate(), h.getAvgTemp(), h.getCondition()
                        ));
                    }
                }

                loadingIndicator.setVisible(false);
            });

        } catch (Exception e) {
            e.printStackTrace();
            updateUIError("Error fetching weather.");
        }
    }


    private void updateUIError(String msg) {
        Platform.runLater(() -> {
            locationLabel.setText(msg);
            loadingIndicator.setVisible(false);
        });
    }
}
