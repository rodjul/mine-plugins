package me.bot;

import me.bot.commands.CommandKit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class MainPlugin extends JavaPlugin implements Listener {
    public static MainPlugin instance;

    private RevivePeople revivePeopleListener;

    @Override
    public void onEnable() {
        instance=this;
        saveDefaultConfig();

        this.updateConfig();

        this.revivePeopleListener = new RevivePeople(instance);

        getServer().getPluginManager().registerEvents(this.revivePeopleListener, this);

        this.getCommand("kit").setExecutor(new CommandKit());
        this.getCommand("revive").setExecutor(this.revivePeopleListener);
        this.getCommand("respawn").setExecutor(this.revivePeopleListener);

    }

    @Override
    public void onDisable() {
        System.out.println("chamando custom entity onDisable");
        this.revivePeopleListener.disable();
//        this.mobListeners.onDisablePlugin();
    }

    private void updateConfig() {
        saveDefaultConfig();
        Set<String> options = getConfig().getDefaults().getKeys(false);
//        System.out.println("options: " + options);
        Set<String> current = getConfig().getKeys(false);
//        System.out.println("current: " + current);
        boolean changed = false;

        // Ensure all basic sections of the default config are present
        for (String s : options) {
            if (!current.contains(s)) {
                getConfig().set(s, getConfig().getDefaults().get(s));
                changed = true;
            }
        }

        // Delete all sections in the config which are not present in the default
        for (String s : current) {
            if (!options.contains(s)) {
                getConfig().set(s, null);
                changed = true;
            }
        }

        getConfig().options().copyHeader(true);
//        System.out.println("ALTEDO? " + changed);
        if (changed) {
            saveConfig();
        }
    }


}
