package de.swa.nlq_gc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NlqGcService {

    private static final String ROUTE_HANDLE = "http://localhost:8080/handleNLQ?query=[QUERY]&user=[USER]";
    private static final String ROUTE_REQUEST = "http://localhost:8080/graphCode?transactionId=[TRANSACTION]";

    private final String transactionId;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final CompletableFuture<GraphCodeDTO> resultFuture = new CompletableFuture<>();
    private final Gson gson = new Gson();

    public NlqGcService(final String query) {
        transactionId = handleQuery(query);
    }

    public CompletableFuture<GraphCodeDTO> startChecking() {
        if (StringUtils.isEmpty(transactionId)) {
            return resultFuture;
        }

        final long timeout = System.currentTimeMillis() + 120000; // 2 minutes

        final Runnable checker = () -> {
            GraphCodeDTO gc = checkTransactionStatus();
            if (System.currentTimeMillis() > timeout) {
                // If GC still pending after timeout, abort
                gc = (gc == null || gc.getState() == State.PENDING) ? null : gc;
            }
            if (gc == null || gc.getState() != State.PENDING) {
                resultFuture.complete(gc);
                scheduler.shutdown();
            }
        };

        scheduler.scheduleAtFixedRate(checker, 0, 3, TimeUnit.SECONDS);
        return resultFuture;
    }

    private GraphCodeDTO checkTransactionStatus() {
        String urlReplaced = ROUTE_REQUEST.replace("[TRANSACTION]", URLEncoder.encode(transactionId, StandardCharsets.UTF_8));

        try {
            URI uri = new URI(urlReplaced);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new IOException("HTTP GET failed with code " + conn.getResponseCode());
            }

            String response = readResponse(conn);
            return gson.fromJson(response, GraphCodeDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String handleQuery(String query) {
        String urlReplaced = ROUTE_HANDLE
                .replace("[QUERY]", URLEncoder.encode(query, StandardCharsets.UTF_8))
                .replace("[USER]", URLEncoder.encode("User", StandardCharsets.UTF_8)); // TODO insert user identification

        try {
            URI uri = URI.create(urlReplaced);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            if (conn.getResponseCode() != 200) {
                throw new IOException("HTTP POST failed with code " + conn.getResponseCode());
            }

            String response = readResponse(conn);
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            return jsonResponse.get("transactionId").getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private String readResponse(final HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line.trim());
            }
            return result.toString();
        }
    }

}