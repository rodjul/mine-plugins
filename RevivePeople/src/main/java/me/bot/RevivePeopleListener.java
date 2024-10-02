package me.bot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RevivePeopleListener implements Listener, CommandExecutor {

    private HashMap<UUID, Location> playerLocationDeaths = new HashMap<>();

    public RevivePeopleListener() {
        this.playerLocationDeaths = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            if(args.length == 0) {
                sender.sendMessage("[ReviveCommand ERROR] when using the command from console, is required the nickname");
                return false;
            }
            String playerName = args[0];
            boolean foundPlayer = false;
            Player player = null;
            for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                if(p.getName().equals(playerName)) {
                    foundPlayer = true;
                    player = p;
                    break;
                }
            }
            if(!foundPlayer) {
                sender.sendMessage(String.format("[ReviveCommand ERROR] %s player is not online", playerName));
                return false;
            }

            // Give the player our items (comma-seperated list of all ItemStack)
//            if(!player.isDead()){
//                sender.sendMessage(String.format("[ReviveCommand ERROR] %s player is alive", playerName));
//                return false;
//            }
            player.setHealth(10);
            player.setInvulnerable(false);
            this.playerLocationDeaths.remove(player.getUniqueId());
            System.out.println(this.playerLocationDeaths.toString());

//            ItemStack item = player.getItemInHand();
//            player.getInventory().remove(item);
//            Item itemDropped = player.getWorld().dropItemNaturally(player.getLocation(), item);
//            itemDropped.setPickupDelay(40);

//            playerToSend.getInventory().addItem(bricks, diamond);

            sender.sendMessage(String.format("[ReviveCommand INFO] revive %s player", playerName));

        }else {
            System.out.println("sender: "+ sender + " "+ sender.getClass().getName());
            System.out.println("command: "+ command.getName());
            System.out.println("label: "+ label);
            System.out.println("args: "+ Arrays.toString(args));
            return false;
        }

        return true;
    }


    @EventHandler
    public void onPlayerGameModeChangeEvent(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if(event.getPlayer().isOp()) {
            this.playerLocationDeaths.remove(player.getUniqueId());
            player.setInvulnerable(false);
            player.setHealth(20);
        };
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e) {
        if(this.playerLocationDeaths.containsKey(e.getPlayer().getUniqueId())) {
            return;
        }
        System.out.println("PlayerInteractEntityEvent aaa : " + e.getPlayer().getName());
        if(e.getRightClicked() instanceof Player) {
            Player playerDead = (Player) e.getRightClicked();
            System.out.println("PlayerInteractEntityEvent bbb : " + playerDead.getName());
            System.out.println("PlayerInteractEntityEvent ccc : " + e.getPlayer().getName());
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),"revive " + playerDead.getName());
        }
    }

    @EventHandler
    public void onPlayerPickupArmour(PlayerPickupItemEvent e) {
        if(this.playerLocationDeaths.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent e) {
        if(this.playerLocationDeaths.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        //Scheduled task here to do the following after 30 seconds
        e.getPlayer().setInvulnerable(false);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
        if(this.playerLocationDeaths.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
//        System.out.println("PlayerMoveEvent aaa : " + e.getPlayer().getName());
        if(this.playerLocationDeaths.containsKey(e.getPlayer().getUniqueId())){
            System.out.println(this.playerLocationDeaths.toString());
            e.getPlayer().teleport(this.playerLocationDeaths.get(e.getPlayer().getUniqueId()));
        }
    }

    @EventHandler
    public void onPlayerDeath(EntityDamageEvent ev) //Listens to EntityDamageEvent
    {
        // https://www.spigotmc.org/threads/cancel-death-event.187841/
        if(ev.getEntity() instanceof Player){
            Player p = (Player) ev.getEntity();
            if(this.playerLocationDeaths.containsKey(p.getUniqueId())){
                ev.setCancelled(true);
                return;
            }

            if (((p.getHealth() - ev.getFinalDamage()) <= 0) //Checks if the entity will die and if entity is player
                    && ev.getEntity() instanceof Player)
            {
                System.out.println("teste asdasd" + p.getName());
                Location playerLocation = p.getLocation();

                Bukkit.broadcastMessage(ChatColor.RED + String.format("player %s died at x=%f, y=%f, z=%f",
                        p.getName(),
                        playerLocation.getX(),
                        playerLocation.getY(),
                        playerLocation.getZ()
                ));

                this.playerLocationDeaths.put(p.getUniqueId(), playerLocation);

                ev.setCancelled(true);
                //Teleport, set health, whatever.
                p.setHealth(0.1);
                p.setInvulnerable(true);

            }
        }

    }

}

