package me.bot.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class CommandKit implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Create a new ItemStack (type: diamond)
            ItemStack diamond = new ItemStack(Material.DIAMOND);

            // Create a new ItemStack (type: brick)
            ItemStack bricks = new ItemStack(Material.BRICK);

            // Set the amount of the ItemStack
            bricks.setAmount(20);

            // Give the player our items (comma-seperated list of all ItemStack)
            player.getInventory().addItem(bricks, diamond);
        }else if (sender instanceof ConsoleCommandSender) {
            if(args.length == 0) {
                sender.sendMessage("[KitCommand ERROR] when using the command from console, is required the nickname");
                return false;
            }
            String playerName = args[0];
            boolean foundPlayer = false;
            Player playerToSend = null;
            for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                if(player.getName().equals(playerName)) {
                    foundPlayer = true;
                    playerToSend = player;
                    break;
                }
            }
            if(!foundPlayer) {
                sender.sendMessage(String.format("[KitCommand ERROR] %s player is not online", playerName));
                return false;
            }


            ItemStack diamond = new ItemStack(Material.DIAMOND);
            // Create a new ItemStack (type: brick)
            ItemStack bricks = new ItemStack(Material.BRICK);
            // Set the amount of the ItemStack
            bricks.setAmount(20);
            // Give the player our items (comma-seperated list of all ItemStack)
//            playerToSend.getInventory().addItem(bricks, diamond);
            playerToSend.getInventory().addItem(bricks);

            sender.sendMessage(String.format("[KitCommand INFO] sent %s player kit", playerName));

        }else {
            System.out.println("sender: "+ sender + " "+ sender.getClass().getName());
            System.out.println("command: "+ command.getName());
            System.out.println("label: "+ label);
            System.out.println("args: "+ Arrays.toString(args));
        }

        return true;
    }


}
