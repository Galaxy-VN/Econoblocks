package dev.flrp.econoblocks.managers;

import dev.flrp.econoblocks.Econoblocks;
import dev.flrp.econoblocks.configuration.Locale;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class MessageManager {

    private final Econoblocks plugin;
    private final BukkitScheduler scheduler;
    private static MessageType messageType;
    private static AnimationType animationType;

    public MessageManager(Econoblocks plugin) {
        this.plugin = plugin;
        scheduler = plugin.getServer().getScheduler();
        messageType = MessageType.getType(plugin.getConfig().getString("message.message-type"));
        animationType = AnimationType.getType(plugin.getConfig().getString("message.holograms.animation"));
    }

    public void sendMessage(Player player, Block block, double amount) {
        switch(messageType) {
            case CHAT:
                player.sendMessage(Locale.parse(Locale.PREFIX + Locale.ECONOMY_GIVEN.replace("{0}", String.valueOf(amount))));
                break;
            case ACTION_BAR:
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Locale.parse(Locale.ECONOMY_GIVEN.replace("{0}", String.valueOf(amount)))));
                break;
            case HOLOGRAM:
                Location location = block.getLocation();
                location.add(0.5, 0.2,0.5);
                ArmorStand title = location.getWorld().spawn(location, ArmorStand.class);
                title.setVisible(false);
                title.setCustomName(Locale.parse(Locale.ECONOMY_GIVEN.replace("{0}", String.valueOf(amount))));
                title.setCustomNameVisible(true);
                title.setGravity(false);
                title.setInvulnerable(true);
                title.setMarker(true);
                playAnimation(title);
                break;
            case TITLES:
                player.sendTitle(Locale.parse(Locale.TITLE.replace("{0}", String.valueOf(amount))), Locale.parse(Locale.SUB_TITLE.replace("{0}", String.valueOf(amount))), Locale.FADE_IN, Locale.STAY, Locale.FADE_OUT);
                break;
            default:
        }
    }

    public void playAnimation(ArmorStand stand) {
        switch (animationType) {
            case STAY:
                scheduler.runTaskLater(plugin, stand::remove, plugin.getConfig().getLong("message.holograms.duration") * 20);
                break;
            case FLOAT:
                new FloatAnimationTask(stand);
                break;
            case BOUNCE:
                new BounceAnimationTask(stand);
                break;
            default:
        }
    }

    public enum MessageType {

        CHAT,
        ACTION_BAR,
        HOLOGRAM,
        TITLES;

        public static MessageType getType(String identifier) {
            try {
                System.out.println("[Econoblocks] Message Type: " + MessageType.valueOf(identifier));
                return MessageType.valueOf(identifier);
            } catch (IllegalArgumentException e) {
                System.out.println("[Econoblocks] Invalid message type found, resorting to default: CHAT");
                return CHAT;
            }
        }

    }

    public enum AnimationType {

        STAY,
        FLOAT,
        BOUNCE;

        public static AnimationType getType(String identifier) {
            try {
                if(messageType == MessageType.HOLOGRAM) System.out.println("[Econoblocks] Selected Animation: " + AnimationType.valueOf(identifier));
                return AnimationType.valueOf(identifier);
            } catch (IllegalArgumentException e) {
                System.out.println("[Econoblocks] Invalid animation type found, resorting to default: STAY");
                return STAY;
            }
        }

    }

    public class FloatAnimationTask extends BukkitRunnable {

        private final ArmorStand stand;
        private final BukkitTask task;
        private int frames = plugin.getConfig().getInt("message.holograms.duration") * 20;

        FloatAnimationTask(ArmorStand stand) {
            this.stand = stand;
            this.task = runTaskTimer(plugin, 0, 1L);
        }

        @Override
        public void run() {
            if(frames == 0) {
                stand.remove();
                task.cancel();
                return;
            }
            stand.teleport(stand.getLocation().add(0, 0.1,0));
            frames--;
        }

    }

    public class BounceAnimationTask extends BukkitRunnable {

        private final ArmorStand stand;
        private final BukkitTask task;
        private int frames = plugin.getConfig().getInt("message.holograms.duration") * 20;
        private int counter = 1;

        BounceAnimationTask(ArmorStand stand) {
            this.stand = stand;
            this.task = runTaskTimer(plugin, 0, 1L);
        }

        @Override
        public void run() {
            if(frames == 0) {
                stand.remove();
                task.cancel();
                return;
            }
            if(Math.sin(counter * 0.628) > 0) {
                stand.teleport(stand.getLocation().add(0, 0.05,0));
            } else {
                stand.teleport(stand.getLocation().add(0, -0.05,0));
            }
            frames--;
            counter++;
        }

    }

}
