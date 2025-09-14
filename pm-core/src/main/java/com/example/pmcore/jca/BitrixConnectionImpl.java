package com.example.pmcore.jca;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class BitrixConnectionImpl implements BitrixConnection {

    private final BitrixManagedConnection mc;
    private final String baseUrl;
    private final String userId;
    private final String token;
    private boolean closed = false;

    public BitrixConnectionImpl(
            BitrixManagedConnection mc,
            String baseUrl,
            String userId,
            String token) {
        this.mc = mc;
        this.baseUrl = baseUrl;
        this.userId = userId;
        this.token = token;
    }

    @Override
    public String callApi(String endpoint, String jsonBody) throws Exception {
        if (closed) {
            throw new IllegalStateException("Соединение закрыто");
        }

        String fullUrl = baseUrl + endpoint;
        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        StringBuilder response = new StringBuilder();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        conn.disconnect();
        return response.toString();
    }

    @Override
    public void close() {
        closed = true;
        if (mc != null) {
            mc.notifyConnectionClosed();
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
}