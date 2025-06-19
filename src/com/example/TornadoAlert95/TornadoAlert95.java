package com.example.TornadoAlert95;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import org.json.*;

public class TornadoAlert95 {
    private static final String API_KEY = "df4f2ac31b932a79bbe7f80a4ed6bb8e";
    private static String location = null;
    private static final long startTime = System.currentTimeMillis();
    private static final List<String> scanLog = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TornadoAlert95");

            // Load and scale logo
            ImageIcon icon = new ImageIcon("tornado95.png");
            Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel(new ImageIcon(scaled));
            logo.setHorizontalAlignment(SwingConstants.CENTER);
            logo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            // Terminal window setup
            JTextArea console = new JTextArea();
            console.setEditable(false);
            console.setBackground(Color.BLACK);
            console.setForeground(Color.GREEN);
            console.setFont(new Font("Monospaced", Font.PLAIN, 14));

            JTextField input = new JTextField();
            input.setBackground(Color.BLACK);
            input.setForeground(Color.GREEN);
            input.setCaretColor(Color.GREEN);
            input.setFont(new Font("Monospaced", Font.PLAIN, 14));

            frame.setLayout(new BorderLayout());
            frame.add(logo, BorderLayout.NORTH);
            frame.add(new JScrollPane(console), BorderLayout.CENTER);
            frame.add(input, BorderLayout.SOUTH);
            frame.setSize(640, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            // Blinking cursor simulation
            Timer blink = new Timer(500, new ActionListener() {
                private boolean on = true;
                public void actionPerformed(ActionEvent e) {
                    if (on) {
                        console.append("_");
                    } else {
                        int len = console.getDocument().getLength();
                        if (len > 0) {
                            try {
                                console.getDocument().remove(len - 1, 1);
                            } catch (Exception ignored) {}
                        }
                    }
                    on = !on;
                }
            });
            blink.start();

            // Boot sequence
            console.append(">> CRT SYSTEM BOOT\n>> LOADING: TORNADOALERT95 v3.6\n");
            console.append(">> ENTER STARTUP LOCATION (ZIP or city,COUNTRY) (e.g. 32284,US or Indianapolis,Indiana,US):\n> ");
            final boolean[] initialized = {false};
            input.addActionListener(e -> {
                String command = input.getText().trim();
                String lower = command.toLowerCase();
                input.setText("");
                console.append("> " + command + "\n");

                if (!initialized[0]) {
                    if (!command.isEmpty()) {
                        location = command;
                        console.append(">> LOCATION SET TO: " + location + "\n");
                        console.append(">> TYPE 'help' FOR AVAILABLE COMMANDS\n");
                        initialized[0] = true;
                        return;
                    } else {
                        console.append(">> LOCATION CANNOT BE EMPTY. TRY AGAIN.\n> ");
                        return;
                    }
                }

                switch (lower) {
                    case "scan" -> {
                        console.append(">> INITIATING ATMOSPHERIC SCAN...\n");
                        Timer delay = new Timer(2000, evt2 -> {
                            String report = fetchWeatherReport();
                            scanLog.add(report);
                            console.append(report);
                        });
                        delay.setRepeats(false);
                        delay.start();
                    }
                    case "scan log" -> {
                        if (scanLog.isEmpty()) {
                            console.append(">> NO HISTORICAL SCANS AVAILABLE\n");
                        } else {
                            console.append(">> HISTORICAL SCAN LOG:\n");
                            for (String entry : scanLog) {
                                console.append(entry + "\n");
                            }
                        }
                    }
                    case "about" -> console.append(">> TornadoAlert95 v3.6\n>> CRT Weather Interface. SkyWatch Licensed.\n");
                    case "help" -> console.append("""
                        >> AVAILABLE COMMANDS
                        >> scan              - run weather scan
                        >> scan log          - show scan history
                        >> status            - show system status
                        >> set location LOC  - e.g. set location 90210,US
                        >> reload            - reboot radar modules
                        >> update            - check for updates
                        >> clear             - clear the screen
                        >> uptime            - show run time
                        >> theme invert      - toggle color scheme
                        >> eject             - floppy disk
                        >> godmode           - N/A
                        >> the radar is lying - N/A
                        >> exit              - shut down terminal
                        """);
                    case "exit" -> {
                        console.append(">> TERMINAL SHUTTING DOWN... GOODBYE.\n");
                        Timer t = new Timer(1000, evt -> System.exit(0));
                        t.setRepeats(false);
                        t.start();
                    }
                    case "godmode" -> {
                        console.append("GODMODE ACTIVATED. Weather can't touch you now.");
                    }
                    case "the radar is lying" -> {
                        console.append("Nuh uh.");
                    }
                    default -> {
                        if (lower.startsWith("set location ")) {
                            String newLoc = command.substring(13).trim();
                            if (!newLoc.isEmpty()) {
                                location = newLoc;
                                console.append(">> LOCATION UPDATED TO: " + location + "\n");
                            } else {
                                console.append(">> LOCATION UPDATE FAILED: NO INPUT DETECTED\n");
                            }
                        } else {
                            console.append(">> UNKNOWN COMMAND: `" + command + "`\n>> TYPE `help` FOR OPTIONS\n");
                        }
                    }
                }

                console.setCaretPosition(console.getDocument().getLength());
            });
        });
    }

    private static String fetchWeatherReport() {
        try {
            String endpoint = "https://api.openweathermap.org/data/2.5/weather?q=" +
                    location + "&units=imperial&appid=" + API_KEY;

            JSONObject weather = new JSONObject(readURL(endpoint));
            String city = weather.getString("name");
            String condition = weather.getJSONArray("weather").getJSONObject(0).getString("description").toUpperCase();

            JSONObject main = weather.getJSONObject("main");
            double temp = main.getDouble("temp");
            int humidity = main.getInt("humidity");
            double visibility = weather.has("visibility") ? weather.getDouble("visibility") / 1000.0 : -1;
            double wind = weather.getJSONObject("wind").getDouble("speed");

            double rain = 0.0;
            if (weather.has("rain")) {
                JSONObject rainData = weather.getJSONObject("rain");
                rain = rainData.has("1h") ? rainData.getDouble("1h") :
                        rainData.has("3h") ? rainData.getDouble("3h") : 0.0;
            }

            return String.format("""
                >> ATMOSPHERIC SCAN COMPLETE
                >> LOCATION: %s (%s)
                >> CONDITION: %s
                >> TEMP: %.1f°F
                >> HUMIDITY: %d%%
                >> PRECIPITATION (1h): %.2f mm
                >> VISIBILITY: %.1f km
                >> WIND SPEED: %.1f mph
                >> RADAR STATUS: NOMINAL\n
                """, location, city, condition, temp, humidity, rain, visibility, wind);

        } catch (Exception e) {
            return ">> SCAN FAILURE :: RADAR MODULE UNRESPONSIVE\n>> ERROR: " + e.getMessage() + "\n";
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
}
