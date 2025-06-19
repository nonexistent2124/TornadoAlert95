package com.example.TornadoAlert95;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import org.json.*;

public class TornadoAlertGUI95 extends JFrame {
    private static final String API_KEY = "df4f2ac31b932a79bbe7f80a4ed6bb8e";

    private JTextField locationInput;
    private JTextArea conditionArea;
    private JLabel statusBar;

    public TornadoAlertGUI95() {
        setTitle("TornadoAlert95 – Weather Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception ignored) {}

        // Top input section
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(new TitledBorder("Location"));

        locationInput = new JTextField("48176,US", 20);
        JButton fetchBtn = new JButton("Get Weather");
        topPanel.add(new JLabel("ZIP or City,Country:"));
        topPanel.add(locationInput);
        topPanel.add(fetchBtn);

        // Center conditions panel
        conditionArea = new JTextArea();
        conditionArea.setEditable(false);
        conditionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        conditionArea.setBorder(new TitledBorder("Current Conditions"));

        // Status bar
        statusBar = new JLabel("System ready.");
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(conditionArea), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        fetchBtn.addActionListener(e -> fetchWeather(locationInput.getText().trim()));

        setVisible(true);
    }

    private void fetchWeather(String location) {
        conditionArea.setText("");
        statusBar.setText("Fetching weather...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            protected Void doInBackground() {
                try {
                    String geoURL = "http://api.openweathermap.org/geo/1.0/direct?q=" +
                            URLEncoder.encode(location, "UTF-8") +
                            "&limit=1&appid=" + API_KEY;
                    JSONArray geo = new JSONArray(readURL(geoURL));
                    if (geo.isEmpty()) {
                        conditionArea.setText(">> LOCATION NOT FOUND.\n");
                        return null;
                    }
                    JSONObject coords = geo.getJSONObject(0);
                    double lat = coords.getDouble("lat");
                    double lon = coords.getDouble("lon");

                    String oneCall = String.format(
                            "https://api.openweathermap.org/data/3.0/onecall?lat=%f&lon=%f&appid=%s&units=imperial",
                            lat, lon, API_KEY);
                    JSONObject weather = new JSONObject(readURL(oneCall));
                    JSONObject current = weather.getJSONObject("current");

                    conditionArea.setText(String.format("""
                            LOCATION: %s
                            LAT, LON: %.2f, %.2f
                            TEMP: %.1f°F
                            HUMIDITY: %d%%
                            WIND SPEED: %.1f mph
                            """,
                            location.toUpperCase(), lat, lon,
                            current.getDouble("temp"),
                            current.getInt("humidity"),
                            current.getJSONObject("wind").getDouble("speed")));
                } catch (Exception e) {
                    conditionArea.setText(">> ERROR: " + e.getMessage());
                }
                return null;
            }

            protected void done() {
                statusBar.setText("Scan complete.");
            }
        };
        worker.execute();
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
