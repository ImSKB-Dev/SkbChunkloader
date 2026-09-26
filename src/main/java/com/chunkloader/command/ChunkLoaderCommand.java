package com.chunkloader.command;

import com.chunkloader.ChunkLoaderPlugin;
import com.chunkloader.model.ChunkLoaderData;
import com.chunkloader.util.MaterialAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChunkLoaderCommand implements CommandExecutor, TabCompleter {

    private final ChunkLoaderPlugin plugin;

    public ChunkLoaderCommand(ChunkLoaderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            if (!sender.hasPermission("chunkloader.admin")) {
                sender.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }
            plugin.reloadPluginConfig();
            sender.sendMessage(plugin.getMessage("plugin-reloaded"));
            return true;
        }

        if (sub.equals("setblock")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessage("only-players"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("chunkloader.admin")) {
                player.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType().name().equals("AIR")) {
                player.sendMessage(plugin.getMessage("hold-item-error"));
                return true;
            }

            plugin.setLoaderBlockItem(inHand);
            player.sendMessage(plugin.getMessage("setblock-success"));
            return true;
        }

        if (sub.equals("setitem")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessage("only-players"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("chunkloader.admin")) {
                player.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType().name().equals("AIR")) {
                player.sendMessage(plugin.getMessage("hold-item-error"));
                return true;
            }

            plugin.setActivatorItem(inHand);
            player.sendMessage(plugin.getMessage("setitem-success"));
            return true;
        }

        if (sub.equals("give")) {
            if (!sender.hasPermission("chunkloader.admin")) {
                sender.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Uso: /cl give <jogador> <block|item> [quantidade]");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getMessage("player-not-found"));
                return true;
            }

            String type = args[2].toLowerCase();
            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException ignored) {
                }
            }

            ItemStack giveStack;
            if (type.equals("block")) {
                giveStack = plugin.getLoaderBlockItemStack().clone();
            } else if (type.equals("item")) {
                giveStack = plugin.getActivatorItemStack().clone();
            } else {
                sender.sendMessage(ChatColor.RED + "Tipo inválido! Use 'block' ou 'item'.");
                return true;
            }

            giveStack.setAmount(amount);
            target.getInventory().addItem(giveStack);
            sender.sendMessage(plugin.getMessage("give-success").replace("{amount}", String.valueOf(amount)).replace("{type}", type));
            return true;
        }

        if (sub.equals("list")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessage("only-players"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("chunkloader.use")) {
                player.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            List<ChunkLoaderData> list = plugin.getDataManager().getLoadersByOwner(player.getUniqueId());
            if (list.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "Você não possui nenhum ChunkLoader registrado.");
                return true;
            }

            player.sendMessage(ChatColor.GREEN + "=== Seus ChunkLoaders (" + list.size() + ") ===");
            for (ChunkLoaderData data : list) {
                String status = data.isActive() ? ChatColor.GREEN + "ATIVO" : ChatColor.RED + "DESATIVADO";
                player.sendMessage(ChatColor.GOLD + "- " + data.getWorldName() + " (" + data.getX() + ", " + data.getY() + ", " + data.getZ() + ") | Raio: " + data.getRadiusString() + " | Status: " + status);
            }
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Comandos ChunkLoader ===");
        sender.sendMessage(ChatColor.YELLOW + "/cl list " + ChatColor.WHITE + "- Lista seus ChunkLoaders");
        if (sender.hasPermission("chunkloader.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/cl setblock " + ChatColor.WHITE + "- Define o item da sua mão como o Bloco ChunkLoader");
            sender.sendMessage(ChatColor.YELLOW + "/cl setitem " + ChatColor.WHITE + "- Define o item da sua mão como o Ativador");
            sender.sendMessage(ChatColor.YELLOW + "/cl give <player> <block|item> [qtd] " + ChatColor.WHITE + "- Dá blocos/itens de ChunkLoader");
            sender.sendMessage(ChatColor.YELLOW + "/cl reload " + ChatColor.WHITE + "- Recarrega as configurações");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> sub = new ArrayList<>(Arrays.asList("list"));
            if (sender.hasPermission("chunkloader.admin")) {
                sub.addAll(Arrays.asList("setblock", "setitem", "give", "reload"));
            }
            return sub.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("chunkloader.admin")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give") && sender.hasPermission("chunkloader.admin")) {
            return Arrays.asList("block", "item").stream().filter(s -> s.startsWith(args[2].toLowerCase())).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
