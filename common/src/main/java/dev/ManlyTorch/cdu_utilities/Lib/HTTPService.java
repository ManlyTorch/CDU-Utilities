package dev.ManlyTorch.cdu_utilities.Lib;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;

public class HTTPService {
    private HTTPService() {};

    public static NativeImage getImage(String imgUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(imgUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);
            try (InputStream in = conn.getInputStream()) {return NativeImage.read(in);}
            finally {conn.disconnect();}
        } catch (IOException e) {return null;}
    };

    public static JsonObject getJson(String urlStr) {
        try {
            URL url = new URL("https://" + urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(10000);
            int code = conn.getResponseCode();
            InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject jsonObj = JsonParser.parseReader(reader).getAsJsonObject();
                if (code != 200) throw new IOException("HTTPService returned " + code + ": " + jsonObj);
                return jsonObj;
            } finally {conn.disconnect();}
        } catch (Exception e) { return null; }
    }
}
