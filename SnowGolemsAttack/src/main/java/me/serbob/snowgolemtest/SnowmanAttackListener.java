package me.serbob.snowgolemtest;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftEnderman;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class SnowmanAttackListener implements Listener {
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Snowman) {
            Snowman snowman = (Snowman) event.getEntity();
            if (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile) {
                Player player = null;
                if (event.getDamager() instanceof Player) {
                    player = (Player) event.getDamager();
                } else if (event.getDamager() instanceof Projectile) {
                    Projectile projectile = (Projectile) event.getDamager();
                    if (projectile.getShooter() instanceof Player) {
                        player = (Player) projectile.getShooter();
                    }
                }

                boolean isFriendly = MainPlugin.instance.getConfig().getBoolean("friendly", false); // Get the value from the config.yml
                if (player != null && (isFriendly && !(player instanceof Player) || (!isFriendly && player instanceof Player))) {
                    snowman.setTarget(player);
                    snowman.setAI(true);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getEntity();
            if (snowball.getShooter() instanceof Snowman) {
//                System.out.println("onProjectileHit atira ent: " + event + ", " + event.getClass().getName());
                if (event.getHitEntity() instanceof LivingEntity) { // Changed from Player to LivingEntity to target any mob
                    LivingEntity target = (LivingEntity) event.getHitEntity();
                    target.damage(MainPlugin.instance.getConfig().getInt("damage")); //deals 5 hearts of damage
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getEntity();

            if (snowball.getShooter() instanceof Snowman) {
                Mob target = getTargetPlayer(snowball.getLocation(), 10);
                if (target != null) {
//                    System.out.println("onProjectileLaunch atira ent: " + target + ", " + target.getClass().getName());
                    Location snowballLoc = snowball.getLocation();
                    Location targetLoc = target.getEyeLocation();
                    Vector direction = targetLoc.clone().subtract(snowballLoc).toVector();
                    snowball.setVelocity(direction);
                }
            }
        }
    }

    private Mob getTargetPlayer(Location location, double radius) {
        List<Entity> nearbyEntities = location.getWorld().getEntities();
        Mob target = null;
        double closestDistance = Double.MAX_VALUE;
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Monster && !(entity instanceof CraftEnderman)) {
                double distance = entity.getLocation().distance(location);
                if (distance < radius && distance < closestDistance) {
//                    System.out.println("getTargetPlayer  ent: " + entity + ", " + entity.getClass().getName());
                    target = (Mob) entity;
                    closestDistance = distance;
                }
            }
        }
        return target;
    }
}

