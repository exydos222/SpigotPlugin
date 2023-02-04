package main;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import commands.bases.BaseCommand;
import commands.schematics.SchematicCommand;
import commands.teams.TeamCommand;
import data.blocks.MinedBlockData;
import data.cars.CarData;
import data.cars.Cars;
import data.player.PlayerSessionData;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import objects.bases.Base;

public class Main extends JavaPlugin {
	
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
    	Base.loadBases();
    	getServer().getPluginManager().registerEvents(new EventListener(), this);
    	this.getCommand("team").setExecutor(new TeamCommand());
    	this.getCommand("base").setExecutor(new BaseCommand());
    	this.getCommand("schemdata").setExecutor(new SchematicCommand());
    	Cars.loadCars();
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
        		} else if (data.carDriver == null || !PlayerSessionData.PlayerData.containsKey(data.carDriver))
		        		return;
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