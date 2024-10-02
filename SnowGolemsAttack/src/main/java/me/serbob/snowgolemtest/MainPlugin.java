package me.serbob.snowgolemtest;

import me.serbob.snowgolemtest.customentities.MobListeners;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class MainPlugin extends JavaPlugin implements Listener {
    public static MainPlugin instance;

    private MobListeners mobListeners;

    public MainPlugin() {
        this.mobListeners = new MobListeners();
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.updateConfig();

//        enableMetrics();

        getServer().getPluginManager().registerEvents(new SnowmanAttackListener(), this);

        //getServer().getPluginManager().registerEvents(new MobListeners(), this);

        System.out.println("chamando custom entity");
        this.mobListeners.onEnablePlugin(this, this);
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

    @Override
    public void onDisable() {
        System.out.println("chamando custom entity onDisable");
        this.mobListeners.onDisablePlugin();

        //CustomEntityType.unregisterEntities();
    }

    public void enableMetrics() {
//        Metrics metrics = new Metrics(this,19317);
//        metrics.addCustomChart(new Metrics.MultiLineChart("players_and_servers", new Callable<Map<String, Integer>>() {
//            @Override
//            public Map<String, Integer> call() throws Exception {
//                Map<String, Integer> valueMap = new HashMap<>();
//                valueMap.put("servers", 1);
//                valueMap.put("players", Bukkit.getOnlinePlayers().size());
//                return valueMap;
//            }
//        }));
//        metrics.addCustomChart(new Metrics.DrilldownPie("java_version", () -> {
//            Map<String, Map<String, Integer>> map = new HashMap<>();
//            String javaVersion = System.getProperty("java.version");
//            Map<String, Integer> entry = new HashMap<>();
//            entry.put(javaVersion, 1);
//            if (javaVersion.startsWith("1.7")) {
//                map.put("Java 1.7", entry);
//            } else if (javaVersion.startsWith("1.8")) {
//                map.put("Java 1.8", entry);
//            } else if (javaVersion.startsWith("1.9")) {
//                map.put("Java 1.9", entry);
//            } else {
//                map.put("Other", entry);
//            }
//            return map;
//        }));
    }
}
