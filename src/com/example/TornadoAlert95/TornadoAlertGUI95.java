package com.example.TornadoAlert95;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;
import org.json.*;

public class TornadoAlertGUI95 extends JFrame {
    private static final String API_KEY = "df4f2ac31b932a79bbe7f80a4ed6bb8e";

    private JLabel weatherIconLabel;
    private JLabel temperatureLabel;
    private JLabel conditionLabel;
    private JLabel locationLabel;
    private JTextField locationInputField;
    private JLabel humidityLabel, windLabel, visibilityLabel, precipLabel;
    private JLabel statusBar;

    private String currentLocation = "48176,US";
    private final Timer refreshTimer = new Timer();

    public TornadoAlertGUI95() {
        setTitle("TornadoAlert95 – Weather");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(550, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception ignored) {}

        // 🎨 Load Pixelated Font
        Font pixelFont;
        try {
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File("assets/fonts/Pixelated.ttf"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(pixelFont);
            pixelFont = pixelFont.deriveFont(Font.PLAIN, 12f);
        } catch (Exception e) {
            System.err.println("Pixel font failed to load, using Monospaced fallback.");
            pixelFont = new Font("Monospaced", Font.PLAIN, 12);
        }

        // ⌨️ Location input
        JPanel locationInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationInputPanel.setBorder(new TitledBorder("Set Location"));
        locationInputField = new JTextField(currentLocation, 20);
        locationInputField.setFont(pixelFont);
        locationInputField.addActionListener(e -> {
            currentLocation = locationInputField.getText().trim();
            fetchWeather(currentLocation);
        });
        locationInputPanel.add(new JLabel("City or ZIP,Country:"));
        locationInputPanel.add(locationInputField);

        // 🌦️ Forecast visuals
        weatherIconLabel = new JLabel();
        weatherIconLabel.setPreferredSize(new Dimension(160, 160));

        temperatureLabel = new JLabel("--°F");
        temperatureLabel.setFont(pixelFont.deriveFont(28f));

        conditionLabel = new JLabel("CONDITION");
        conditionLabel.setFont(pixelFont.deriveFont(14f));

        locationLabel = new JLabel("LOCATION");
        locationLabel.setFont(pixelFont);
        locationLabel.setForeground(Color.DARK_GRAY);

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.add(temperatureLabel);
        textStack.add(conditionLabel);
        textStack.add(locationLabel);

        JPanel forecastPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        forecastPanel.add(weatherIconLabel);
        forecastPanel.add(textStack);
        forecastPanel.setBorder(new TitledBorder("Now"));

        // 📊 Stats Panel
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(new TitledBorder("Details"));

        humidityLabel = new JLabel("Humidity: --%");
        humidityLabel.setFont(pixelFont);
        windLabel = new JLabel("Wind Speed: -- mph");
        windLabel.setFont(pixelFont);
        visibilityLabel = new JLabel("Visibility: -- mi");
        visibilityLabel.setFont(pixelFont);
        precipLabel = new JLabel("Precipitation (1h): -- mm");
        precipLabel.setFont(pixelFont);

        statsPanel.add(humidityLabel);
        statsPanel.add(windLabel);
        statsPanel.add(visibilityLabel);
        statsPanel.add(precipLabel);

        // 🔽 Status Bar
        statusBar = new JLabel("System ready.");
        statusBar.setFont(pixelFont.deriveFont(10f));
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statsPanel, BorderLayout.CENTER);
        bottomPanel.add(statusBar, BorderLayout.SOUTH);

        add(locationInputPanel, BorderLayout.NORTH);
        add(forecastPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // 🔁 Auto-refresh every 60 seconds
        fetchWeather(currentLocation);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> fetchWeather(currentLocation));
            }
        }, 60000, 60000);

        setVisible(true);
    }


    private void fetchWeather(String location) {
        temperatureLabel.setText("--°F");
        conditionLabel.setText("LOADING...");
        locationLabel.setText("");
        humidityLabel.setText("Humidity: --%");
        windLabel.setText("Wind Speed: -- mph");
        visibilityLabel.setText("Visibility: -- mi");
        precipLabel.setText("Precipitation (1h): -- mm");
        weatherIconLabel.setIcon(null);
        statusBar.setText("Fetching: " + location);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            protected Void doInBackground() {
                try {
                    String[] parts = location.split(",", 2);
                    String city = URLEncoder.encode(parts[0].trim(), "UTF-8");
                    String country = parts.length > 1 ? parts[1].trim() : "US";

                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" +
                            city + "," + country + "&appid=" + API_KEY + "&units=imperial";
                    JSONObject data = new JSONObject(readURL(url));

                    String name = data.getString("name");
                    JSONObject main = data.getJSONObject("main");
                    double temp = main.getDouble("temp");
                    int humidity = main.getInt("humidity");
                    double wind = data.getJSONObject("wind").getDouble("speed");
                    int visibility = data.has("visibility") ? data.getInt("visibility") : -1;
                    double precipitation = data.has("rain") ?
                            data.getJSONObject("rain").optDouble("1h", 0.0) : 0.0;
                    String condition = data.getJSONArray("weather").getJSONObject(0).getString("description");

                    temperatureLabel.setText(String.format("%.0f°F", temp));
                    conditionLabel.setText(condition.toUpperCase());
                    locationLabel.setText(name);

                    humidityLabel.setText("Humidity: " + humidity + "%");
                    windLabel.setText(String.format("Wind Speed: %.1f mph", wind));
                    visibilityLabel.setText(visibility >= 0 ?
                            String.format("Visibility: %.1f mi", visibility / 1609.34) : "Visibility: N/A");
                    precipLabel.setText(String.format("Precipitation (1h): %.2f mm", precipitation));

                    String iconPath = "assets/" + mapConditionToIcon(condition);
                    ImageIcon icon = new ImageIcon(iconPath);
                    Image scaled = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                    weatherIconLabel.setIcon(new ImageIcon(scaled));
                } catch (Exception e) {
                    conditionLabel.setText("ERROR");
                    locationLabel.setText("Invalid location");
                    humidityLabel.setText("Humidity: --%");
                    windLabel.setText("Wind Speed: -- mph");
                    visibilityLabel.setText("Visibility: -- mi");
                    precipLabel.setText("Precipitation (1h): -- mm");
                    System.err.println("Weather fetch failed: " + e.getMessage());
                }
                return null;
            }

            protected void done() {
                statusBar.setText("Last updated: " + currentLocation);
            }
        };
        worker.execute();
    }

    private String mapConditionToIcon(String desc) {
        desc = desc.toLowerCase();
        if (desc.contains("thunderstorm")) return "thunderstorm.png";
        if (desc.contains("drizzle")) return "rain.png";
        if (desc.contains("freezing rain") || desc.contains("rain") || desc.contains("shower"))
            return "shower_rain.png";
        if (desc.contains("snow") || desc.contains("sleet")) return "snow.png";
        if (desc.contains("mist") || desc.contains("haze") || desc.contains("fog") ||
                desc.contains("smoke") || desc.contains("dust") || desc.contains("tornado"))
            return "mist.png";
        if (desc.equals("clear sky")) return "clear_sky.png";
        if (desc.contains("overcast")) return "broken_clouds.png";
        if (desc.contains("cloud")) return "few_clouds.png";
        return "default.png";
    }


    private static String readURL(String endpoint) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(endpoint).openStream()));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null)
            json.append(line);
        in.close();
        return json.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TornadoAlertGUI95::new);
    }
}
