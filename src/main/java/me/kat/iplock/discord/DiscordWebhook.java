package me.kat.iplock.discord;

import me.kat.iplock.IPLockPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class DiscordWebhook {

  public static void sendAlert(String user, String oldIp, String newIp, Instant time) {
    new Thread(() -> {
      try {
        String webhook = IPLockPlugin.get()
            .getConfig()
            .getString("discord.webhook");

        if (webhook == null || webhook.isBlank() || "PUT_WEBHOOK_URL_HERE".equals(webhook)) {
          IPLockPlugin.get().getLogger().info("Discord webhook skipped: Not configured.");
          return;
        }

        IPLockPlugin.get().getLogger().info("Sending IP block alert to Discord for player: " + user);

        if (!webhook.startsWith("https://discord.com/api/webhooks/")) {
          IPLockPlugin.get().getLogger().warning("Invalid Discord webhook URL format");
          return;
        }

        URL url = URI.create(webhook).toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setRequestProperty("User-Agent", "KatIPAuth");

        String payload = """
            {
              "embeds": [
                {
                  "title": "🚫 IP LOGIN BLOCKED",
                  "color": 15158332,
                  "fields": [
                    {
                      "name": "Player",
                      "value": "%s",
                      "inline": true
                    },
                    {
                      "name": "Time",
                      "value": "%s",
                      "inline": true
                    },
                    {
                      "name": "Stored / Owner",
                      "value": "`%s`",
                      "inline": false
                    },
                    {
                      "name": "Attempt / Unauthorized",
                      "value": "`%s`",
                      "inline": false
                    }
                  ],
                  "footer": {
                    "text": "KatIPAuth • Cracked Server Protection"
                  },
                  "timestamp": "%s"
                }
              ]
            }
            """.formatted(
            user,
            time,
            oldIp,
            newIp,
            time);

        byte[] data = payload.getBytes(StandardCharsets.UTF_8);

        try (OutputStream os = con.getOutputStream()) {
          os.write(data);
          os.flush();
        }

        int code = con.getResponseCode();
        if (code < 200 || code >= 300) {
          IPLockPlugin.get().getLogger()
              .warning("Discord webhook failed (HTTP " + code + ")");
        } else {
          IPLockPlugin.get().getLogger()
              .info("Discord webhook sent successfully for player: " + user);
        }

        con.disconnect();

      } catch (Exception e) {
        IPLockPlugin.get().getLogger()
            .warning("Discord webhook error: " + e.getMessage());
      }
    }).start();
  }
}
