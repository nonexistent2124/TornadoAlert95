package com.example.TornadoAlert95;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private JLabel timeLabel; // NEW: Local time label

    private String currentLocation = "Enter location here... (e.g Detroit,US)";
    private final Timer refreshTimer = new Timer();

    public TornadoAlertGUI95() {
        setTitle("TornadoAlert95 – Weather");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(550, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Dark mode colors
        Color bgColor = new Color(30, 30, 30);
        Color fgColor = Color.WHITE;
        Color borderColor = new Color(70, 70, 70);

        // Set dark background for JFrame content pane
        getContentPane().setBackground(bgColor);

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
        locationInputPanel.setBackground(bgColor);
        locationInputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor), "Set Location", TitledBorder.LEFT, TitledBorder.TOP,
                pixelFont, fgColor));
        locationInputField = new JTextField(currentLocation, 20);
        locationInputField.setFont(pixelFont);
        locationInputField.setBackground(new Color(50, 50, 50));
        locationInputField.setForeground(fgColor);
        locationInputField.setCaretColor(fgColor);
        locationInputField.setBorder(BorderFactory.createLineBorder(borderColor));
        locationInputField.addActionListener(e -> {
            currentLocation = locationInputField.getText().trim();
            fetchWeather(currentLocation);
        });
        JLabel locLabel = new JLabel("City or ZIP,Country:");
        locLabel.setForeground(fgColor);
        locLabel.setFont(pixelFont);
        locationInputPanel.add(locLabel);
        locationInputPanel.add(locationInputField);

        // 🌦️ Forecast visuals
        weatherIconLabel = new JLabel();
        weatherIconLabel.setPreferredSize(new Dimension(160, 160));

        temperatureLabel = new JLabel("--°F");
        temperatureLabel.setFont(pixelFont.deriveFont(Font.BOLD, 28f));
        temperatureLabel.setForeground(fgColor);

        conditionLabel = new JLabel("CONDITION");
        conditionLabel.setFont(pixelFont.deriveFont(Font.PLAIN, 16f));
        conditionLabel.setForeground(fgColor);

        locationLabel = new JLabel("LOCATION");
        locationLabel.setFont(pixelFont.deriveFont(Font.PLAIN, 18f));
        locationLabel.setForeground(fgColor);

        timeLabel = new JLabel("Local Time: --:--");  // NEW: Initialize local time label
        timeLabel.setFont(pixelFont.deriveFont(Font.PLAIN, 12f));
        timeLabel.setForeground(fgColor);

        JPanel textStack = new JPanel();
        textStack.setBackground(bgColor);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.add(temperatureLabel);
        textStack.add(Box.createVerticalStrut(5));   // spacing
        textStack.add(conditionLabel);
        textStack.add(Box.createVerticalStrut(5));   // spacing
        textStack.add(locationLabel);
        textStack.add(Box.createVerticalStrut(5));   // spacing
        textStack.add(timeLabel);  // ADD time label at bottom of Now panel

        JPanel forecastPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        forecastPanel.setBackground(bgColor);
        forecastPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor), "Now", TitledBorder.LEFT, TitledBorder.TOP,
                pixelFont, fgColor));
        forecastPanel.add(weatherIconLabel);
        forecastPanel.add(textStack);

        // 📊 Stats Panel
        JPanel statsPanel = new JPanel();
        statsPanel.setBackground(bgColor);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor), "Details", TitledBorder.CENTER, TitledBorder.TOP,
                pixelFont.deriveFont(Font.BOLD, 16f), fgColor));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        humidityLabel = new JLabel("Humidity: --%");
        humidityLabel.setFont(pixelFont.deriveFont(Font.PLAIN, 14f));
        humidityLabel.setForeground(fgColor);
        windLabel = new JLabel("Wind Speed: -- mph");
        windLabel.setFont(pixelFont.deriveFont(Font.PLAIN, 14f));
        windLabel.setForeground(fgColor);
        visibilityLabel = new JLabel("Visibility: -- mi");
        visibilityLabel.setFont(pixelFont.deriveFont(Font.PLAIN, 14f));
        visibilityLabel.setForeground(fgColor);
        precipLabel = new JLabel("Precipitation (1h): -- mm");
        precipLabel.setFont(pixelFont.deriveFont(Font.PLAIN, 14f));
        precipLabel.setForeground(fgColor);
        // Center detail labels horizontally
        humidityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        windLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        visibilityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        precipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsPanel.add(humidityLabel);
        statsPanel.add(Box.createVerticalStrut(8));
        statsPanel.add(windLabel);
        statsPanel.add(Box.createVerticalStrut(8));
        statsPanel.add(visibilityLabel);
        statsPanel.add(Box.createVerticalStrut(8));
        statsPanel.add(precipLabel);

        // 🔽 Status Bar
        statusBar = new JLabel("System ready.");
        statusBar.setFont(pixelFont.deriveFont(Font.PLAIN, 10f));
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.setBackground(bgColor);
        statusBar.setForeground(fgColor);
        statusBar.setOpaque(true);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(bgColor);
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

        // Optional: Update time label every minute so clock ticks even if no weather fetch
        new javax.swing.Timer(60000, e -> {
            LocalDateTime now = LocalDateTime.now();
            String timeString = now.format(DateTimeFormatter.ofPattern("hh:mm a"));
            timeLabel.setText("Time: " + timeString);
        }).start();

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

                    // Local time display update
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                    String timeString = now.format(formatter);
                    timeLabel.setText("Local Time: " + timeString);

                    // Determine hour for night/day icon
                    int hour = now.getHour(); // 0-23
                    boolean isNight = hour >= 20 || hour < 6; // night if after 8pm or before 6am

                    String iconName = mapConditionToIcon(condition, isNight);
                    String iconPath = "assets/" + iconName;
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
                    timeLabel.setText("Time: --:--");
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

    // Modified to include night icons
    private String mapConditionToIcon(String desc, boolean isNight) {
        desc = desc.toLowerCase();

        if (isNight) {
            // Night icons
            if (desc.contains("thunderstorm")) return "thunderstorm_night.png";
            if (desc.contains("drizzle") || desc.contains("rain")) return "rain_night.png";
            if (desc.contains("snow") || desc.contains("sleet")) return "mist_night.png"; // treat snow/sleet as mist at night per your request
            if (desc.contains("mist") || desc.contains("haze") || desc.contains("fog") ||
                    desc.contains("smoke") || desc.contains("dust") || desc.contains("tornado"))
                return "mist_night.png";
            if (desc.equals("clear sky")) return "clear_night.png";
            if (desc.contains("cloud") || desc.contains("overcast")) return "cloudy_night.png";

            // Default night icon fallback
            return "default_night.png";
        } else {
            // Day icons (your original mapping plus slight adjustments)
            if (desc.contains("thunderstorm")) return "thunderstorm.png";
            if (desc.contains("drizzle") || desc.contains("rain")) return "rain.png";
            if (desc.contains("snow") || desc.contains("sleet")) return "snow.png";
            if (desc.contains("mist") || desc.contains("haze") || desc.contains("fog") ||
                    desc.contains("smoke") || desc.contains("dust") || desc.contains("tornado"))
                return "mist.png";
            if (desc.equals("clear sky")) return "clear_sky.png";
            if (desc.contains("overcast")) return "broken_clouds.png";
            if (desc.contains("cloud")) return "few_clouds.png";

            // Default day icon fallback
            return "default.png";
        }
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
