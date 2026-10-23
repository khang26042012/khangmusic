package vn.pika.khangmusic;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class KhangMusicServer {
    private final KhangMusic plugin;
    private HttpServer server;
    private final int port;
    private final String packFileName;

    public KhangMusicServer(KhangMusic plugin, int port, String packFileName) {
        this.plugin = plugin;
        this.port = port;
        this.packFileName = packFileName;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new PackHandler());
            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();
            plugin.getLogger().info("KhangMusic HTTP Pack Server da bat tren port " + port + "!");
        } catch (Exception e) {
            plugin.getLogger().warning("Khong the khoi dong KhangMusic HTTP Server tren port " + port + ": " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
            server = null;
            plugin.getLogger().info("KhangMusic HTTP Pack Server da tat.");
        }
    }

    private class PackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                File file = new File(plugin.getDataFolder(), packFileName);
                if (!file.exists()) {
                    file = new File(packFileName);
                }

                if (!file.exists()) {
                    String response = "404 Not Found: Resource pack file not found on server.";
                    exchange.sendResponseHeaders(404, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    return;
                }

                long fileLength = file.length();
                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
                exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
                exchange.sendResponseHeaders(200, fileLength);

                try (FileInputStream fis = new FileInputStream(file);
                     OutputStream os = exchange.getResponseBody()) {
                    byte[] buffer = new byte[65536];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Loi khi truyen tai resource pack: " + e.getMessage());
            }
        }
    }
}
