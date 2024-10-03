package me.bot;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Utils {

    public String getFirstArgument(String[] args) {
        if (args.length == 0) return "";
        return args[0];
    }

    public Player findPlayer(String nickname) throws Exception{
        if(nickname == ""){
            throw new Exception("Nickname cannot be empty");
        }
        //if(Bukkit.getPlayer(nickname) != null){}

        boolean foundPlayer = false;
        Player player = null;
        for(Player p : Bukkit.getServer().getOnlinePlayers()) {
            if(p.getName().equals(nickname)) {
                foundPlayer = true;
                player = p;
                break;
            }
        }
        if(!foundPlayer) {
            throw new Exception(String.format("\"%s\" player not found", nickname));
        }
        return player;
    }
}
