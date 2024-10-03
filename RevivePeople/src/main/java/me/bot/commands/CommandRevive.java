package me.bot.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandRevive {


    public boolean run(CommandSender sender, Player player, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            // Give the player our items (comma-seperated list of all ItemStack)
//            if(!player.isDead()){
//                sender.sendMessage(String.format("[ReviveCommand ERROR] %s player is alive", playerName));
//                return false;
//            }
            player.setHealth(10);
            player.setInvulnerable(false);

//            ItemStack item = player.getItemInHand();
//            player.getInventory().remove(item);
//            Item itemDropped = player.getWorld().dropItemNaturally(player.getLocation(), item);
//            itemDropped.setPickupDelay(40);

//            playerToSend.getInventory().addItem(bricks, diamond);

            sender.sendMessage(String.format("[ReviveCommand INFO] revive %s player", player.getName()));

            return true;
        }else if(sender instanceof Player) {
            if(!player.isOp()){
                sender.sendMessage(String.format("[ReviveCommand ERROR] %s not authorized", player.getName()));
                return false;
            }

            player.setHealth(10);
            player.setInvulnerable(false);

            sender.sendMessage(String.format("[ReviveCommand INFO] revive %s player", player.getName()));
        }

        sender.sendMessage("[ReviveCommand ERROR] not implemented - "+ sender.getName());
        return false;
    }
}
