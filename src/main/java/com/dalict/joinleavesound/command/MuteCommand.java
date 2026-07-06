package com.dalict.joinleavesound.command;

import com.dalict.joinleavesound.JoinLeaveSound;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MuteCommand implements CommandExecutor, TabCompleter {

    private final JoinLeaveSound plugin;

    public MuteCommand(JoinLeaveSound plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getMessage("command-usage"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload":
                if (!sender.hasPermission("joinleavesound.reload")) {
                    sender.sendMessage(plugin.getMessage("no-permission"));
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(plugin.getMessage("reload-success"));
                break;
            case "mute":
            case "unmute":
                if (!sender.hasPermission("joinleavesound.mute")) {
                    sender.sendMessage(plugin.getMessage("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessage("command-usage"));
                    return true;
                }
                String targetName = args[1];

                if (sub.equals("mute")) {
                    if (plugin.addMutedPlayer(targetName)) {
                        sender.sendMessage(plugin.getMessage("mute-success").replace("{player}", targetName));
                    } else {
                        sender.sendMessage(plugin.getMessage("mute-already").replace("{player}", targetName));
                    }
                } else {
                    if (plugin.removeMutedPlayer(targetName)) {
                        sender.sendMessage(plugin.getMessage("unmute-success").replace("{player}", targetName));
                    } else {
                        sender.sendMessage(plugin.getMessage("unmute-not-found").replace("{player}", targetName));
                    }
                }
                break;
            default:
                sender.sendMessage(plugin.getMessage("command-usage"));
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("joinleavesound.reload") && "reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            if (sender.hasPermission("joinleavesound.mute")) {
                if ("mute".startsWith(args[0].toLowerCase())) completions.add("mute");
                if ("unmute".startsWith(args[0].toLowerCase())) completions.add("unmute");
            }
            return completions;
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("mute") || args[0].equalsIgnoreCase("unmute"))) {
            String prefix = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
