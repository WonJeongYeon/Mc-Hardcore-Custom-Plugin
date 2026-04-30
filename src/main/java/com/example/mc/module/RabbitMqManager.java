package com.example.mc.module;

import org.bukkit.plugin.Plugin;
import com.rabbitmq.client.*;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class RabbitMqManager {

    private final Plugin plugin;

    private Connection connection;
    private Channel channel;

    private final Queue<String> queue = new ConcurrentLinkedQueue<>();

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    private final String exchange = "mc.exchange";
    private final String routingKey = "player.action";
    private final String queueName = "mc.player.log";

    private volatile boolean running = false;

    @Getter
    private boolean connected = false;

    public RabbitMqManager(Plugin plugin,
                           String host,
                           int port,
                           String username,
                           String password) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    // =========================
    // INIT
    // =========================
    public void start() {
        connect();
        startPublisher();
    }

    // =========================
    // CONNECT
    // =========================
    private void connect() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);

            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(5000);

            connection = factory.newConnection();
            channel = connection.createChannel();

            // 선언 (없으면 생성)
            channel.exchangeDeclare(exchange, "topic", true);

            channel.queueDeclare(queueName, true, false, false, null);

            channel.queueBind(queueName, exchange, routingKey);

            connected = true;
            plugin.getLogger().info("[RabbitMQ] Connected");

        } catch (Exception e) {
            connected = false;
            plugin.getLogger().severe("[RabbitMQ] Connection failed: " + e.getMessage());
        }
    }

    // =========================
    // QUEUE PUSH (메인스레드 안전)
    // =========================
    public void publish(String message) {
        // 과부하 방지
        if (queue.size() > 10000) {
            queue.clear();
            plugin.getLogger().warning("[RabbitMQ] Queue overflow - cleared");
        }

        queue.add(message);
    }

    // =========================
    // ASYNC PUBLISHER
    // =========================
    private void startPublisher() {
        running = true;

        new BukkitRunnable() {
            @Override
            public void run() {

                if (!running) return;

                if (!connected || channel == null || !channel.isOpen()) {
                    reconnect();
                    return;
                }

                int batch = 0;

                while (!queue.isEmpty() && batch < 100) {
                    String msg = queue.poll();

                    if (msg == null) continue;

                    try {
                        channel.basicPublish(
                                exchange,
                                routingKey,
                                null,
                                msg.getBytes(StandardCharsets.UTF_8)
                        );
                        batch++;

                    } catch (Exception e) {
                        connected = false;
                        plugin.getLogger().warning("[RabbitMQ] Publish failed: " + e.getMessage());
                        break;
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    // =========================
    // RECONNECT
    // =========================
    private void reconnect() {
        try {
            if (connection != null && connection.isOpen()) return;

            plugin.getLogger().warning("[RabbitMQ] Reconnecting...");
            connect();

        } catch (Exception e) {
            plugin.getLogger().severe("[RabbitMQ] Reconnect failed: " + e.getMessage());
        }
    }

    // =========================
    // SHUTDOWN
    // =========================
    public void shutdown() {
        running = false;

        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception ignored) {}

        plugin.getLogger().info("[RabbitMQ] Shutdown complete");
    }
}
