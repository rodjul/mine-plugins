package me.serbob.snowgolemtest.customentities;

import me.serbob.snowgolemtest.MainPlugin;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftSnowman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MobListeners implements Listener {

    private HashMap<Integer, Boolean> snowManModified;

    public MobListeners() {
        snowManModified = new HashMap<>();
    }

    public void onEnablePlugin(JavaPlugin server, MainPlugin plugin) {
        System.out.println("habilitando plugin");
        server.getServer().getPluginManager().registerEvents(this, plugin);
        this.findSnowmanEntitiesAndUpdate(true);
    }

    public void onDisablePlugin() {
        System.out.println("desabilitando plugin");
        HandlerList.unregisterAll(this);
        this.findSnowmanEntitiesAndUpdate(false);
        this.snowManModified = new HashMap<>();
    }

    public void findSnowmanEntitiesAndUpdate(boolean isEnable) {
        Location location = new Location(Bukkit.getWorld("world"), 0, 1, 0);
        List<LivingEntity> nearbyEntities = Bukkit.getWorld("world").getLivingEntities();

        // System.out.println("nearbyEntities " + nearbyEntities);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof CraftSnowman) {
                if (isEnable) {
                    this.modifySnowman((CraftSnowman) entity);
                } else {
                    this.revertModificationSnowman((CraftSnowman) entity);
                }
            }
        }
    }

    public void modifySnowman(CraftSnowman ent) {
        // valida se ja foi modificado o mob
        if (this.snowManModified.get((Integer) ent.getEntityId()) != null) {
            return;
        }
        this.snowManModified.put((Integer) ent.getEntityId(), (Boolean) true);

        SnowGolem snowHandle = ent.getHandle();
        // removemos a lógica da IA
        for (WrappedGoal goal : snowHandle.goalSelector.getAvailableGoals()) {
            if (goal == null) continue;
            snowHandle.goalSelector.removeGoal(goal.getGoal());
        }
        for (WrappedGoal goal : snowHandle.targetSelector.getAvailableGoals()) {
            snowHandle.targetSelector.removeGoal(goal.getGoal());
        }

        // range de detecção do monstro

        double rangeCheckAttack = MainPlugin.instance.getConfig().getDouble("rangeCheckAttack", 600.1);
        double speedModifier = MainPlugin.instance.getConfig().getDouble("speedModifier", 1.25);
        int attackIntervalMin = MainPlugin.instance.getConfig().getInt("attackIntervalMin", 15);
        int attackIntervalMax = MainPlugin.instance.getConfig().getInt("attackIntervalMax", 15);
        float attackRadius = (float) MainPlugin.instance.getConfig().getDouble("attackRadius", 10.1F);

        ent.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(rangeCheckAttack);

        // colocando a lógica da IA de volta com novos valores
        snowHandle.goalSelector.addGoal(1, new RangedAttackGoal(snowHandle, speedModifier, attackIntervalMin, attackIntervalMax, attackRadius));
        snowHandle.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(snowHandle, 1.0, 1.0000001E-5F));
        snowHandle.goalSelector.addGoal(3, new LookAtPlayerGoal(snowHandle, Player.class, 6.0F));
        snowHandle.goalSelector.addGoal(4, new RandomLookAroundGoal(snowHandle));
        snowHandle.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(snowHandle, Mob.class, 10, true, false, (entityliving) -> {
            // adicionando condição de não atacar enderman, mas outros inimigos deixa
            return (entityliving instanceof Enemy && !(entityliving instanceof EnderMan));
        }));

        // System.out.println("goalSelector: "+ snowHandle.goalSelector.getAvailableGoals());
        // System.out.println("targetSelector: "+snowHandle.targetSelector.getAvailableGoals());
    }

    public void revertModificationSnowman(CraftSnowman ent) {
        SnowGolem snowHandle = ent.getHandle();
        for (WrappedGoal goal : snowHandle.goalSelector.getAvailableGoals()) {
            if (goal == null) continue;
            snowHandle.goalSelector.removeGoal(goal.getGoal());
        }
        for (WrappedGoal goal : snowHandle.targetSelector.getAvailableGoals()) {
            if (goal == null) continue;
            snowHandle.targetSelector.removeGoal(goal.getGoal());
        }

        ((CraftSnowman) ent).getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(16.0);

        snowHandle.goalSelector.addGoal(1, new RangedAttackGoal(snowHandle, 1.25, 20, 10.0F));
        snowHandle.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(snowHandle, 1.0, 1.0000001E-5F));
        snowHandle.goalSelector.addGoal(3, new LookAtPlayerGoal(snowHandle, Player.class, 6.0F));
        snowHandle.goalSelector.addGoal(4, new RandomLookAroundGoal(snowHandle));
        snowHandle.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(snowHandle, Mob.class, 10, true, false, (entityliving) -> {
            return entityliving instanceof Enemy;
        }));
    }

    /**
     * Quando um player loga, vai ser carregado o chunk do mapa, então forçamos a modificação dos mobs
     *
     * @param event
     */
    @EventHandler
    public void onLoadChunk(ChunkLoadEvent event) {
        Collection<Entity> nearbyEntities = event.getWorld().getEntities();

//        System.out.println("player nearbyEntities " + nearbyEntities);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof CraftSnowman) {
                this.modifySnowman((CraftSnowman) entity);
            }
        }

    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        Entity ent = event.getEntity();
        World world = ent.getWorld();

        // System.out.println("ent: " + ent + ", " + ent.getClass().getName());

        if (ent instanceof CraftSnowman) {
            // System.out.println("snowman: "+ ent);
            this.modifySnowman((CraftSnowman) ent);

        }
//        if (ent instanceof Zombie) {
//            ((Zombie) ent).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(2.00001);
//        }
    }
}
