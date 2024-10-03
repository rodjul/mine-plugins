package me.bot;

import me.bot.commands.CommandRevive;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class RevivePeople implements Listener, CommandExecutor {

    private HashMap<UUID, Location> playerLocationDeaths = new HashMap<>();
    private MainPlugin plugin;
    private CommandRevive cmdRevive;

    private Utils utils = new Utils();

    public RevivePeople(MainPlugin plugin) {
        this.playerLocationDeaths = new HashMap<>();
        this.plugin = plugin;
        this.cmdRevive = new CommandRevive();
    }

    public void disable(){
        // desabilitando o Invulnerable se existir
        for (UUID key : this.playerLocationDeaths.keySet()) {
            Player player = Bukkit.getPlayer(key);
            if(player == null) continue;
            player.setInvulnerable(false);;
        }
        this.playerLocationDeaths.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        System.out.println(command.getName());
        switch (label) {
            case "revive":
                try{
                    Player player = this.utils.findPlayer(this.utils.getFirstArgument(args));
                    boolean result = this.cmdRevive.run(sender, player, label, args);
                    if(result){
                        this.playerLocationDeaths.remove(player.getUniqueId());
                    }
                    return result;
                } catch (Exception e) {
                    sender.sendMessage(String.format("[ReviveCommand ERROR] %s", e.toString()));
                    return false;
                }
            case "respawn":
                if(sender instanceof Player){
//                    System.out.println("executei " + ((Player) sender).getPlayer().getName());
                    Player player = (Player) sender;
                    player.setMetadata("forceKill", new FixedMetadataValue(this.plugin, true));

                    player.setInvulnerable(false);
                    player.damage(10000);
                    return true;
                }
            default:
                System.out.println("command " + label + "not found ");
                return false;
        }
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
    public void onPlayerRightClickPlayer(PlayerInteractEntityEvent e) {
        // redundante mas valida se é ele mesmo
        if(this.playerLocationDeaths.containsKey(e.getPlayer().getUniqueId())) {
            return;
        }
//        System.out.println("PlayerInteractEntityEvent aaa : " + e.getPlayer().getName());
        if(e.getRightClicked() instanceof Player) {
            Player playerDead = (Player) e.getRightClicked();
//            System.out.println("PlayerInteractEntityEvent bbb : " + playerDead.getName());
//            System.out.println("PlayerInteractEntityEvent ccc : " + e.getPlayer().getName());

            playerDead.setMetadata("forceKill", new FixedMetadataValue(this.plugin, true));

            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),"revive " + playerDead.getName());
        }
    }

    @EventHandler
    public void onPlayerDeath(EntityDamageEvent ev) //Listens to EntityDamageEvent
    {
        // https://www.spigotmc.org/threads/cancel-death-event.187841/
//        System.out.println("player: " + ev.getEntity().getName());
//        System.out.println("player: " + ev);

        if(ev.getEntity() instanceof Player){
            Player p = (Player) ev.getEntity();
//            System.out.println("Entrei" + p.getName());
            if(p.hasMetadata("forceKill") && p.getMetadata("forceKill").get(0).asBoolean()){
                System.out.println("player executed cmd respawn");
                p.removeMetadata("forceKill", this.plugin);
                this.playerLocationDeaths.remove(p.getUniqueId());
                p.setInvulnerable(false);
                return;
            }

            if(this.playerLocationDeaths.containsKey(p.getUniqueId())){
                ev.setCancelled(true);
                return;
            }

            if (((p.getHealth() - ev.getFinalDamage()) <= 0) //Checks if the entity will die and if entity is player
                    && ev.getEntity() instanceof Player)
            {
//                System.out.println("teste asdasd" + p.getName());
                Location playerLocation = p.getLocation();

                EntityDamageEvent deathCause = p.getLastDamageCause();
                String causedBy = "Deus sabem quem";
                if(deathCause != null && deathCause.getCause() == p.getLastDamageCause().getCause().ENTITY_ATTACK) {
                    Entity entity = (((EntityDamageByEntityEvent)deathCause).getDamager());
                    if(entity instanceof Player) {
                        Player killerPlayer = (Player)entity;
                        causedBy = killerPlayer.getName();
                    }
                    else {
                        Monster killerMob = (Monster)entity;
                        causedBy = killerMob.getType().toString();
                    }
                }

                Bukkit.broadcastMessage(ChatColor.RED + String.format("Player %s morreu pelo %s, posição x=%f, y=%f, z=%f",
                    p.getName(),
                    causedBy,
                    playerLocation.getX(),
                    playerLocation.getY(),
                    playerLocation.getZ()
                ));

                this.playerLocationDeaths.put(p.getUniqueId(), playerLocation);

                ev.setCancelled(true);
                p.setHealth(0.1);
                p.setInvulnerable(true);

                this.runTimeout(p);
            }
        }
    }

    private final static int secondsWaitTime = 300;
    private void runTimeout(Player player){
        int ticks = 20; // 20 ticks é 1 segundo pro servidor
        int secondsToWaitToRun = 1;
        int period = secondsToWaitToRun * ticks;
        int delay = 0;

        sendPrivateMessageToPlayerAboutRespawnOrTimeout(player, secondsWaitTime);

        new BukkitRunnable(){
            private int seconds = secondsWaitTime;
            private int waitSecondsToSendMessage = 60;
            public void run(){
                this.seconds--;
                this.waitSecondsToSendMessage--;

                if(this.seconds <= 0){
                    String message = ChatColor.RED + "Tempo limite atingido, executando "+ ChatColor.WHITE + "/respawn";
                    player.sendMessage(message);
                    player.chat("/respawn");
                    this.cancel();
                    return;
                }

                if(this.waitSecondsToSendMessage <= 0){
                    sendPrivateMessageToPlayerAboutRespawnOrTimeout(player, this.seconds);
                    this.waitSecondsToSendMessage = 60; // reseta o count
                }
            }
        }.runTaskTimer(this.plugin, delay, period);
    }

    private static void sendPrivateMessageToPlayerAboutRespawnOrTimeout(Player player, int seconds){
        String message = String.format(ChatColor.RED + "Você morreu! Se em 5 minutos alguém não te reviver, você perderá os items! Restando %d segundos", seconds);
        player.sendMessage(message);
        message = ChatColor.RED + "Para pular a espera, faça "+ ChatColor.WHITE + "/respawn" + ChatColor.RED + " mas você perderá os items!";
        player.sendMessage(message);
    }


    // ----
    //  Eventos para bloquear o player
    // ----

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

//    @EventHandler
//    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
//        if(this.playerLocationDeaths.containsKey(e.getPlayer().getUniqueId())) {
//            e.setCancelled(true);
//        }
//    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
//        System.out.println("PlayerMoveEvent aaa : " + e.getPlayer().getName());
        if(this.playerLocationDeaths.containsKey(e.getPlayer().getUniqueId())){
//            System.out.println(this.playerLocationDeaths.toString());
            e.getPlayer().teleport(this.playerLocationDeaths.get(e.getPlayer().getUniqueId()));
        }
    }

}

