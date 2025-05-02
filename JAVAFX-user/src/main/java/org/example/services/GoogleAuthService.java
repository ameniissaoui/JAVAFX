package org.example.services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GoogleAuthService {
    private static final String CLIENT_ID = "26330504340-4od3uvsushsi2lclru128rvcobq2kbtu.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String SCOPE = "https://www.googleapis.com/auth/userinfo.email";

    private HttpServer server;
    private CountDownLatch latch;
    private String authCode;

    public GoogleAuthService() {
        latch = new CountDownLatch(1);
    }

    public String initiateLogin() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/callback", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                URI uri = exchange.getRequestURI();
                String query = uri.getQuery();
                if (query != null && query.contains("code=")) {
                    authCode = query.split("code=")[1].split("&")[0];
                    String response = "Login successful! You can close this window.";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    latch.countDown();
                } else {
                    String response = "Error during login.";
                    exchange.sendResponseHeaders(400, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            }
        });
        server.setExecutor(null);
        server.start();

        String loginUrl = AUTH_URL + "?client_id=" + CLIENT_ID +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8) +
                "&access_type=offline";
        java.awt.Desktop.getDesktop().browse(URI.create(loginUrl));

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }

        server.stop(0);
        return authCode;
    }

    public String exchangeCodeForTokens(String code) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(TOKEN_URL);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("code", code));
            params.add(new BasicNameValuePair("client_id", CLIENT_ID));
            params.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
            params.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));
            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            post.setEntity(new UrlEncodedFormEntity(params));

            String response = EntityUtils.toString(client.execute(post).getEntity());
            JSONObject json = new JSONObject(response);
            return json.getString("access_token"); // Changed from id_token to access_token
        }
    }

    public String getUserEmail(String accessToken) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(USER_INFO_URL);
            get.setHeader("Authorization", "Bearer " + accessToken); // Use accessToken
            String response = EntityUtils.toString(client.execute(get).getEntity());
            System.out.println("UserInfo response: " + response); // Debug log
            JSONObject json = new JSONObject(response);
            return json.getString("email");
        }
    }
}