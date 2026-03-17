package com.example.mc.api;

import com.example.mc.dto.AnnounceMsgReq;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import lombok.RequiredArgsConstructor;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class HttpApiServer {

    private final JavaPlugin plugin;
    private HttpServer server;
    private final Gson gson = new Gson();

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // /announce 엔드포인트
        server.createContext("/announce", this::handleAnnounce);

        // /status 엔드포인트
        server.createContext("/status", this::handleStatus);

        server.setExecutor(null);
        server.start();

        plugin.getLogger().info("[ApiServer] HTTP Server Initialized : Port " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("[ApiServer] HTTP Server Stopped");
        }
    }


    private void handleAnnounce(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes());
        AnnounceMsgReq announceMsgReq = gson.fromJson(body, AnnounceMsgReq.class);

        plugin.getServer().broadcastMessage("§a" + announceMsgReq.getTitle() + " " + announceMsgReq.getMessage());

        sendResponse(exchange, "OK");
    }

    private void handleStatus(HttpExchange exchange) throws IOException {
//        String response = "Online players: " + plugin.getServer().getOnlinePlayers().size();
//        sendResponse(exchange, response);
        int players = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        double tps = Bukkit.getTPS()[0];

        String json = """
                        {
                          "online": true,
                          "players": %d,
                          "maxPlayers": %d,
                          "tps": %.2f
                        }
                        """.formatted(players, max, tps);

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);


        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
