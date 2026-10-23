package vn.pika.khangmusic;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class KhangMusic extends JavaPlugin {
    private static KhangMusic instance;
    private MusicManager musicManager;
    private KhangMusicServer httpServer;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.musicManager = new MusicManager(this);

        // Dang ky lenh
        if (getCommand("nhac") != null) {
            NhacCommand nhacCmd = new NhacCommand(this, musicManager);
            getCommand("nhac").setExecutor(nhacCmd);
            getCommand("nhac").setTabCompleter(nhacCmd);
        }

        if (getCommand("nhacad") != null) {
            NhacAdminCommand nhacadCmd = new NhacAdminCommand(this, musicManager);
            getCommand("nhacad").setExecutor(nhacadCmd);
            getCommand("nhacad").setTabCompleter(nhacadCmd);
        }

        // Dang ky listener
        Bukkit.getPluginManager().registerEvents(new MusicListener(this, musicManager), this);

        // Khoi dong HTTP Pack Server neu duoc bat trong config
        if (getConfig().getBoolean("http-server.enabled", false)) {
            int port = getConfig().getInt("http-server.port", 25043);
            String packFile = getConfig().getString("http-server.pack-file", "PikaRadio-Java.zip");
            this.httpServer = new KhangMusicServer(this, port, packFile);
            this.httpServer.start();
        }

        // Tu dong bat neu config auto-start: true
        if (getConfig().getBoolean("auto-start", true)) {
            musicManager.start();
        }

        getLogger().info("=================================================");
        getLogger().info("  KhangMusic v1.0.0 da kich hoat thanh cong!");
        getLogger().info("  Tong so bai hat: " + musicManager.getPlaylist().size());
        getLogger().info("  He thong phat nhac: " + (musicManager.isServerEnabled() ? "DANG BAT [ON]" : "DANG TAT [OFF]"));
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
        if (httpServer != null) {
            httpServer.stop();
            httpServer = null;
        }
        if (musicManager != null) {
            musicManager.stop();
            musicManager.saveDisabledPlayers();
        }
        getLogger().info("KhangMusic da tat va luu tru du lieu thanh cong.");
    }

    public static KhangMusic getInstance() {
        return instance;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }
}
