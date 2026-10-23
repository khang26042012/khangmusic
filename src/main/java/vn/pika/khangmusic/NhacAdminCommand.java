package vn.pika.khangmusic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class NhacAdminCommand implements CommandExecutor, TabCompleter {
    private final KhangMusic plugin;
    private final MusicManager manager;

    public NhacAdminCommand(KhangMusic plugin, MusicManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("khangmusic.admin")) {
            sender.sendMessage(ChatColor.RED + "Ban khong co quyen su dung lenh quan tri nay (khangmusic.admin)!");
            return true;
        }

        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", "&d[&e♫ KhangMusic&d] &7"));

        if (args.length == 0) {
            sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════ " + ChatColor.LIGHT_PURPLE + "[♫ QUẢN TRỊ NHẠC PIKAMC] " + ChatColor.DARK_PURPLE + "═══════════════");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad on     " + ChatColor.GRAY + "→ Bat he thong phat nhac toan server");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad off    " + ChatColor.GRAY + "→ Tat he thong phat nhac toan server");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad next   " + ChatColor.GRAY + "→ Bo qua va chuyen sang bai tiep theo");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad check  " + ChatColor.GRAY + "→ Kiem tra thong tin bai dang phat");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad list   " + ChatColor.GRAY + "→ Xem danh sach tat ca bai hat");
            sender.sendMessage(ChatColor.GOLD + "  /nhacad reload " + ChatColor.GRAY + "→ Nap lai config.yml");
            sender.sendMessage(ChatColor.DARK_PURPLE + "═════════════════════════════════════════════════");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "on":
                if (manager.isServerEnabled()) {
                    sender.sendMessage(prefix + ChatColor.YELLOW + "He thong nhac server da dang BAT roi!");
                } else {
                    manager.start();
                    sender.sendMessage(prefix + ChatColor.GREEN + "Da bat he thong phat nhac toan server thanh cong!");
                    Bukkit.broadcastMessage(prefix + ChatColor.GREEN + "He thong nhac toan server da duoc kich hoat boi Admin!");
                }
                break;

            case "off":
                if (!manager.isServerEnabled()) {
                    sender.sendMessage(prefix + ChatColor.YELLOW + "He thong nhac server da dang TAT roi!");
                } else {
                    manager.stop();
                    sender.sendMessage(prefix + ChatColor.RED + "Da tat he thong phat nhac toan server va dung am thanh!");
                    Bukkit.broadcastMessage(prefix + ChatColor.RED + "He thong nhac toan server da duoc tam dung boi Admin.");
                }
                break;

            case "next":
            case "skip":
                if (!manager.isServerEnabled()) {
                    sender.sendMessage(prefix + ChatColor.RED + "Khong the chuyen bai vi he thong dang tat! Hay go /nhacad on truoc.");
                    return true;
                }
                manager.nextSong();
                Song next = manager.getCurrentSong();
                String name = (next != null) ? next.getName() : "Khong xac dinh";
                sender.sendMessage(prefix + ChatColor.GREEN + "Da chuyen sang bai hat ke tiep: " + ChatColor.GOLD + name);
                break;

            case "check":
                Song current = manager.getCurrentSong();
                sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════ " + ChatColor.LIGHT_PURPLE + "[♫ STATUS QUẢN TRỊ] " + ChatColor.DARK_PURPLE + "═══════════════");
                sender.sendMessage(ChatColor.WHITE + "• He thong toan server: " + (manager.isServerEnabled() ? ChatColor.GREEN + "BAT [ON]" : ChatColor.RED + "TAT [OFF]"));
                if (current != null) {
                    int elapsed = manager.getElapsedSeconds();
                    sender.sendMessage(ChatColor.WHITE + "• Bai hat hien tai: " + ChatColor.GOLD + current.getName());
                    sender.sendMessage(ChatColor.WHITE + "• ID bai hat: " + ChatColor.GRAY + current.getId());
                    sender.sendMessage(ChatColor.WHITE + "• Tien do: " + current.getProgressBar(elapsed) + ChatColor.GRAY + " [" + Song.formatTime(elapsed) + " / " + current.getFormattedDuration() + "]");
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "• Khong co bai hat nao dang phat.");
                }
                sender.sendMessage(ChatColor.DARK_PURPLE + "═════════════════════════════════════════════════");
                break;

            case "list":
                List<Song> songs = manager.getPlaylist();
                sender.sendMessage(ChatColor.DARK_PURPLE + "═══════════════ " + ChatColor.LIGHT_PURPLE + "[♫ DANH SÁCH QUẢN TRỊ] " + ChatColor.DARK_PURPLE + "═══════════════");
                int curIdx = manager.getCurrentIndex();
                for (int i = 0; i < songs.size(); i++) {
                    Song s = songs.get(i);
                    boolean isPlaying = (i == curIdx && manager.isServerEnabled());
                    String marker = isPlaying ? ChatColor.GREEN + "▶ " : ChatColor.GRAY + "• ";
                    sender.sendMessage(marker + ChatColor.GOLD + (i + 1) + ". " + ChatColor.WHITE + s.getName() + ChatColor.GRAY + " [" + s.getFormattedDuration() + "]");
                }
                sender.sendMessage(ChatColor.DARK_PURPLE + "═════════════════════════════════════════════════");
                break;

            case "reload":
                plugin.reloadConfig();
                manager.loadPlaylist();
                sender.sendMessage(prefix + ChatColor.GREEN + "Da nap lai thanh cong file config.yml cua KhangMusic!");
                break;

            default:
                sender.sendMessage(prefix + ChatColor.RED + "Lenh khong hop le! Go " + ChatColor.YELLOW + "/nhacad" + ChatColor.RED + " de xem huong dan quan tri.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("khangmusic.admin")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            List<String> subs = Arrays.asList("on", "off", "next", "skip", "check", "list", "reload");
            List<String> result = new ArrayList<>();
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) {
                    result.add(s);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
}
