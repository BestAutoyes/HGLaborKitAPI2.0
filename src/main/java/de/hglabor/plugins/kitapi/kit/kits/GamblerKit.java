package de.hglabor.plugins.kitapi.kit.kits;

import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.KitManager;
import de.hglabor.plugins.kitapi.player.KitPlayer;
import de.hglabor.plugins.kitapi.util.RandomCollection;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.function.Consumer;

public class GamblerKit extends AbstractKit implements Listener {
    public static final GamblerKit INSTANCE = new GamblerKit();

    public RandomCollection<RandomCollection<Consumer<Player>>> badLuckCollection;
    public RandomCollection<RandomCollection<Consumer<Player>>> goodLuckCollection;

    private GamblerKit() {
        super("Gambler", Material.OAK_BUTTON);
        setMainKitItem(getDisplayMaterial());
        setCooldown(30);
        badLuckCollection = new RandomCollection<>();
        goodLuckCollection = new RandomCollection<>();
        initRandomEffects();
    }

    @Override
    public void disable(KitPlayer kitOwner) {
        GambleWin gambleWin = kitOwner.getKitAttribute(this);
        if (gambleWin != null) {
            gambleWin.end();
        }
        if (kitOwner.isValid()) {
            return;
        }
        //TODO: player.getWorld().getEntitiesByClass(Wolf.class).stream().filter(wolf -> wolf.getOwnerUniqueId() != null).filter(wolf -> wolf.getOwnerUniqueId().equals(player.getUniqueId())).forEach(wolf -> wolf.setOwner(null));
    }

    @Override
    public void onPlayerRightClickKitItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        KitPlayer kitPlayer = KitManager.getInstance().getPlayer(player);
        int tick = 2;
        kitPlayer.activateKitCooldown(this, this.getCooldown());
        GambleWin gambleWin = new GambleWin(kitPlayer, player, 3, tick);
        kitPlayer.putKitAttribute(this, gambleWin);
        gambleWin.runTaskTimer(KitManager.getInstance().getPlugin(), 0, tick);
    }

    private void initRandomEffects() {

        int potionDauer = 7 * 20;

        RandomCollection<Consumer<Player>> cantBeClassifiedBad = new RandomCollection<>();
        cantBeClassifiedBad.add("§4§lInstant Death", 0.01, p -> p.setHealth(0));

        RandomCollection<Consumer<Player>> badPotionEffects = new RandomCollection<>();
        badPotionEffects.add("§aPoison", 1, p -> p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, potionDauer, 0)));
        badPotionEffects.add("§7Weakness", 1, p -> p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, potionDauer, 0)));
        badPotionEffects.add("§dLevitation", 1, p -> p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, potionDauer, 0)));
        badPotionEffects.add("§0Blindness", 1, p -> p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, potionDauer, 0)));
        badPotionEffects.add("§8Slowness", 1, p -> p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, potionDauer, 0)));


        badLuckCollection.add(1, cantBeClassifiedBad);
        badLuckCollection.add(1, badPotionEffects);

        //GOOD EFFECTS

        RandomCollection<Consumer<Player>> goodPotionEffects = new RandomCollection<>();
        goodPotionEffects.add("§cStrength", 1, p -> p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, potionDauer, 0)));
        goodPotionEffects.add("§eFire Resistance", 1, p -> p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, potionDauer, 0)));
        goodPotionEffects.add("§3Damage Resistance", 1, p -> p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, potionDauer, 0)));
        goodPotionEffects.add("§bSpeed", 1, p -> p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, potionDauer, 0)));
        goodPotionEffects.add("Invisibility", 1, p -> p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, potionDauer, 0)));

        RandomCollection<Consumer<Player>> goodItems = new RandomCollection<>();
        goodItems.add("§6Wood", 1, p -> KitManager.getInstance().giveKitItemsIfSlotEmpty(
                KitManager.getInstance().getPlayer(p), this,
                Collections.singletonList(new ItemStack(Material.OAK_PLANKS, 10))));
        goodItems.add("§6Recraft", 1, p -> KitManager.getInstance().giveKitItemsIfSlotEmpty(
                KitManager.getInstance().getPlayer(p), this,
                Arrays.asList(
                        new ItemStack(Material.RED_MUSHROOM, 10),
                        new ItemStack(Material.BROWN_MUSHROOM, 10),
                        new ItemStack(Material.BOWL, 10)
                )));
        goodItems.add("§9Water Bucket", 1, p -> KitManager.getInstance().giveKitItemsIfSlotEmpty(KitManager.getInstance().getPlayer(p), this,
                Collections.singletonList(new ItemStack(Material.WATER_BUCKET))));

        RandomCollection<Consumer<Player>> cantBeClassified = new RandomCollection<>();
        cantBeClassified.add("§dDoggos", 1, p -> {
            Wolf wolf = (Wolf) p.getWorld().spawnEntity(p.getLocation(), EntityType.WOLF);
            wolf.setOwner(p);
        });

        goodLuckCollection.add(1, goodPotionEffects);
        goodLuckCollection.add(1, goodItems);
        goodLuckCollection.add(1, cantBeClassified);
    }

    private static class GambleWin extends BukkitRunnable {
        private final long END;
        private final Player player;
        private final KitPlayer kitPlayer;
        private final Random random;
        private final int tick;

        private boolean forceEnd;

        public GambleWin(KitPlayer kitPlayer, Player player, int gambleDuration, int tick) {
            this.player = player;
            this.kitPlayer = kitPlayer;
            this.random = new Random();
            this.tick = tick;
            this.END = System.currentTimeMillis() + gambleDuration * 1000L;
        }

        @Override
        public void run() {
            if (forceEnd) {
                return;
            }
            boolean goodOrBad = random.nextBoolean();
            RandomCollection<Consumer<Player>> randomCollection = goodOrBad ? GamblerKit.INSTANCE.goodLuckCollection.getRandom() : GamblerKit.INSTANCE.badLuckCollection.getRandom();
            Consumer<Player> randomEffect = randomCollection.getRandom();
            String name = randomCollection.getName(randomEffect);
            if (System.currentTimeMillis() >= END) {
                randomEffect.accept(player);
                player.sendTitle(name, "", 0, 20, 0);
                player.playSound(player.getLocation(), goodOrBad ? Sound.ENTITY_PLAYER_LEVELUP : Sound.ENTITY_DONKEY_HURT, 0.8F, 1.0F);
                cancel();
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_BAMBOO_BREAK, 0.8F, 0.75F + random.nextFloat() / 2.0F);
                player.sendTitle(name, "", 0, tick, 0);
            }
        }

        public void end() {
            kitPlayer.putKitAttribute(GamblerKit.INSTANCE, null);
            forceEnd = true;
            cancel();
        }
    }
}