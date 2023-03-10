package main;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import commands.bases.BaseChatShortenedCommand;
import commands.bases.BaseCommand;
import commands.player.PMCommand;
import commands.regions.RegionCommand;
import commands.schematics.SchematicCommand;
import commands.teams.TeamCommand;
import data.blocks.MinedBlockData;
import data.cars.CarData;
import data.cars.Cars;
import data.player.PlayerSaveData;
import data.player.PlayerSessionData;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import objects.bases.Base;
import objects.bases.BaseMember;
import objects.region.Region;
import objects.region.RegionOperator;

public class Main extends JavaPlugin {
    
    // TODO : Fix car passenger system.
    
    public static HolographicDisplaysAPI holoapi;
    
    @Override
    public void onEnable() {
        holoapi = HolographicDisplaysAPI.get(this);
        if (!this.getDataFolder().exists())
            this.getDataFolder().mkdir();
        File file = new File(this.getDataFolder().toString() + "/PlayerData/");
        if (!file.exists())
            file.mkdir();
        file = new File(this.getDataFolder().toString() + "/BaseData/");
        if (!file.exists())
            file.mkdir();
        file = new File(this.getDataFolder().toString() + "/SchematicData/");
        if (!file.exists())
            file.mkdir();
        file = new File(this.getDataFolder().toString() + "/RegionData/");
        if (!file.exists())
            file.mkdir();
        Base.loadBases();
        Cars.loadCars();
        Region.loadRegions();
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        this.getCommand("team").setExecutor(new TeamCommand());
        this.getCommand("base").setExecutor(new BaseCommand());
        this.getCommand("schematicdata").setExecutor(new SchematicCommand());
        this.getCommand("regiondata").setExecutor(new RegionCommand());
        this.getCommand("pm").setExecutor(new PMCommand());
        this.getCommand("bc").setExecutor(new BaseChatShortenedCommand());
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Client.STEER_VEHICLE) {
            @Override
            public void onPacketReceiving(final PacketEvent e) {
                if (e.getPlayer().getVehicle() != null && Cars.CarData.containsKey(e.getPlayer().getVehicle().getUniqueId())) {
                    final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getPlayer().getUniqueId());
                    if (!data.isDrivingCar)
                        return;
                    final float sideways = e.getPacket().getFloat().read(0);
                    final float forwards = e.getPacket().getFloat().read(1);
                    boolean turning = false;
                    if (sideways > 0) {
                        data.vehicleAcceleration *= .995f;
                        turning = true;
                    } else if (sideways < 0) {
                        data.vehicleAcceleration *= .995f;
                        turning = true;
                    }
                    if (forwards > 0) {
                        data.vehicleAcceleration += 0.1f * (1 / (1 + Math.pow(data.vehicleAcceleration, 10)));
                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + String.format("%.1f", data.vehicleAcceleration * 20) + (turning ? "blocks/s (Turning, Accelerating)" : "blocks/s (Accelerating)")));
                    } else if (forwards < 0) {
                        data.vehicleAcceleration *= .93f;
                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + String.format("%.1f", data.vehicleAcceleration * 20) + (turning ? "blocks/s (Turning, Braking)" : "blocks/s (Braking)")));
                    } else {
                        data.vehicleAcceleration *= .995f;
                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + String.format("%.1f", data.vehicleAcceleration * 20) + (turning ? "blocks/s (Turning)" : "blocks/s")));
                    }
                    for (final UUID passenger : data.carPassengers)
                        Bukkit.getPlayer(passenger).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + e.getPlayer().getDisplayName() + " is currently driving this vehicle"));
                }
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK) {
            @SuppressWarnings({ "deprecation", "incomplete-switch" })
            @Override
            public void onPacketReceiving(final PacketEvent e) {
                if (!PlayerSessionData.PlayerData.containsKey(e.getPlayer().getUniqueId()))
                    return;
                final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getPlayer().getUniqueId());
                if (data.oldCarDriver != null) {
                    for (final CarData car : Cars.CarData.values())
                        for (final UUID uuid : car.passengers)
                            if (uuid.equals(e.getPlayer().getUniqueId())) {
                                car.passengers.remove(uuid);
                                data.oldCarDriver = null;
                                return;
                            }
                    data.oldCarDriver = null;
                    return;
                } else if (data.carDriver != null && PlayerSessionData.PlayerData.containsKey(data.carDriver)) {
                    ArrayList<UUID> passengers = PlayerSessionData.PlayerData.get(data.carDriver).carPassengers;
                    if (!passengers.contains(e.getPlayer().getUniqueId()))
                        return;
                    passengers.remove(e.getPlayer().getUniqueId());
                    final Player driver = Bukkit.getPlayer(data.carDriver);
                    passengers = Cars.CarData.get(driver.getVehicle().getUniqueId()).passengers;
                    if (!passengers.contains(e.getPlayer().getUniqueId()))
                        return;
                    passengers.remove(e.getPlayer().getUniqueId());
                    e.getPlayer().setInvisible(false);
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
                        @Override
                        public void run() {
                            if (data.carDriver == null)
                                return;
                            e.getPlayer().teleport(new Location(driver.getWorld(), driver.getLocation().getX(), driver.getLocation().getY(), driver.getLocation().getZ(), e.getPlayer().getLocation().getYaw(), e.getPlayer().getLocation().getPitch()));
                            data.carDriver = null;
                        }
                    }, 1);
                } else {
                    switch (e.getPlayer().getItemInHand().getType()) {
                    case WOODEN_SWORD:
                    case STONE_SWORD:
                    case IRON_SWORD:
                        for (final Entity entity : e.getPlayer().getNearbyEntities(2, 2, 2))
                            if (entity instanceof Player && Math.abs(((Player)entity).getLocation().getYaw() - e.getPlayer().getLocation().getYaw()) >= 155)
                                e.getPlayer().sendMessage("If this were implemented you would be able to backstab them right now!");
                        break;
                    }
                }
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.MOUNT) {
            @Override
            public void onPacketSending(final PacketEvent e) {
                if (PlayerSessionData.PlayerData.get(e.getPlayer().getUniqueId()).carDriver != null)
                    e.setCancelled(true);
            }
        });
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("Performing automatic save of player data, you may experience lag.");
                for (final Player player : getServer().getOnlinePlayers())
                    PlayerSessionData.PlayerData.get(player.getUniqueId()).savedata.saveData(player.getUniqueId());
                Base.saveBases();
                Cars.saveCars();
            }
        }, 6000, 6000);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (final Base base : Base.bases) {
                    base.secondsWithoutRaid = (short)Math.min(Short.MAX_VALUE, base.secondsWithoutRaid + 1);
                    if (base.secondsWithoutRaid == 300) {
                        for (final BaseMember member : base.members) {
                            final Player player = Bukkit.getPlayer(member.uuid);
                            if (player != null)
                                player.sendMessage("Your base named \'" + base.name + "\' is no longer being raided and will now start regenerating health.");
                        }
                        final Player owner = Bukkit.getPlayer(base.owner);
                        if (owner != null)
                            owner.sendMessage("Your base named \'" + base.name + "\' is being raided.");
                        base.health = (short)Math.min(base.level * 1000, base.health + base.level ^ 2);
                    } else if (base.secondsWithoutRaid > 300)
                        base.health = (short)Math.min(base.level * 1000, base.health + base.level ^ 2);
                }
                for (final Player player : getServer().getOnlinePlayers()) {
                    for (final Base base : Base.bases)
                        if (base.name != null && base.isInUnclaimedBaseRegion(player.getLocation()))
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + base.name + " | " + base.health + '/' + base.level * 1000 + "HP"));
                    final Region inInfectedZone = RegionOperator.isInsideAnyRegionWithANameStartingWith(player, "Infected");
                    final PlayerSaveData data = PlayerSessionData.PlayerData.get(player.getUniqueId()).savedata;
                    if (inInfectedZone != null) {
                        final double infectionStrength = Math.max(0, RegionOperator.distanceToCenterInversedScaled(inInfectedZone, player.getLocation(), 5) - ((player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.GOLDEN_BOOTS) ? 1 : 0) -
                                ((player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() == Material.GOLDEN_LEGGINGS) ? 1 : 0) -
                                ((player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.GOLDEN_CHESTPLATE) ? 1 : 0) -
                                ((player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() == Material.GOLDEN_HELMET) ? 1 : 0));
                        player.setHealth(Math.max(0, player.getHealth() - infectionStrength));
                        if (player.hasPotionEffect(PotionEffectType.SLOW))
                            player.removePotionEffect(PotionEffectType.SLOW);
                        if (infectionStrength >= 2)
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, (int)Math.floor(infectionStrength / 2)));
                        if (data.inInfectedZone == null)
                            player.sendMessage("You have entered an infected zone, you will take damage without the proper equipment.");
                        data.inInfectedZone = inInfectedZone;
                    } else if (inInfectedZone == null && data.inInfectedZone != null) {
                        player.sendMessage("You have left the infected zone.");
                        player.removePotionEffect(PotionEffectType.SLOW);
                        data.inInfectedZone = inInfectedZone;
                    }
                }
            }
        }, 20, 20);
    }
    
    @Override
    public void onDisable() {
        for (final MinedBlockData minedBlockData : EventListener.minedBlocks)
            minedBlockData.location.getWorld().getBlockAt(minedBlockData.location).setType(minedBlockData.oldMaterial);
        for (final Player player : getServer().getOnlinePlayers()) {
            final PlayerSessionData data = PlayerSessionData.PlayerData.get(player.getUniqueId());
            if (data.carDriver != null) {
                Cars.CarData.get(Bukkit.getPlayer(data.carDriver).getVehicle().getUniqueId()).passengers.clear();
                PlayerSessionData.PlayerData.get(data.carDriver).carPassengers.clear();
                final Player driver = Bukkit.getPlayer(data.carDriver);
                player.teleport(new Location(driver.getWorld(), driver.getLocation().getX(), driver.getLocation().getY() + 1, driver.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch()));
                data.carDriver = null;
            } else if (data.isDrivingCar) {
                player.getVehicle().eject();
                data.isDrivingCar = false;
                for (final UUID passenger : data.carPassengers)
                    PlayerSessionData.PlayerData.get(passenger).carDriver = null;
                data.carPassengers.clear();
            }
            if (data.combatLogged)
                player.setHealth(0);
            data.savedata.saveData(player.getUniqueId());
        }
        Base.saveBases();
        Cars.saveCars();
    }
}