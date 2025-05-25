package bnthedev.rajce.pro.ketchupStaff.Managers;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebhookManager {

    private static String webhookUrl;

    public static void init() {
        webhookUrl = ConfigManager.getWebhookUrl();
    }

    public static void sendMessage(String message) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonPayload = String.format("{\"content\": \"%s\"}", message.replace("\"", "\\\""));

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes());
                os.flush();
            }

            connection.getInputStream().close();
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
