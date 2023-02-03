package main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;

import data.cars.CarData;
import data.cars.Cars;
import data.player.PlayerSaveData;
import data.player.PlayerSessionData;
import enums.bases.BaseRank;
import enums.bases.BaseType;
import objects.bases.Base;
import objects.bases.BaseMember;

public class EventListener implements Listener {
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(final PlayerJoinEvent e)
    {
		final File dataFile = PlayerSaveData.getDataFile(e.getPlayer().getUniqueId());
		if (dataFile.exists()) {
			final PlayerSaveData data = PlayerSaveData.loadData(e.getPlayer().getUniqueId());
			PlayerSessionData.PlayerData.put(e.getPlayer().getUniqueId(), new PlayerSessionData(data));
			for (final UUID teammate : data.teams) {
				final Player player = Bukkit.getPlayer(teammate);
				if (player == null)
					continue;
				Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
	                @Override
	                public void run() {
	                	try {
	    					PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
	    			        packet.getPlayerInfoAction().write(0, PlayerInfoAction.UPDATE_DISPLAY_NAME);
	    			        final ArrayList<PlayerInfoData> playerData = new ArrayList<PlayerInfoData>();
	    			        playerData.add(new PlayerInfoData(new WrappedGameProfile(player.getUniqueId(), ChatColor.GREEN + "(Teamed) " + player.getName()), 0, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(ChatColor.GREEN + "(Teamed) " + player.getName())));
	    			        packet.getPlayerInfoDataLists().write(0, playerData);
	    			        ProtocolLibrary.getProtocolManager().sendServerPacket(e.getPlayer(), packet);
	    			        packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
	    			        packet.getPlayerInfoAction().write(0, PlayerInfoAction.UPDATE_DISPLAY_NAME);
	    			        playerData.clear();
	    			        playerData.add(new PlayerInfoData(new WrappedGameProfile(e.getPlayer().getUniqueId(), ChatColor.GREEN + "(Teamed) " + e.getPlayer().getName()), 0, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(ChatColor.GREEN + "(Teamed) " + e.getPlayer().getName())));
	    			        packet.getPlayerInfoDataLists().write(0, playerData);
	    			        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
	    				} catch (final InvocationTargetException ex) {
	    				    ex.printStackTrace();
	    				}
	                }
	            }, 1);
			}
		} else {
			try {
				dataFile.createNewFile();
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
			PlayerSessionData.PlayerData.put(e.getPlayer().getUniqueId(), new PlayerSessionData());
		}
    }
	
	@EventHandler
	public void onPlayerDisconnect(final PlayerQuitEvent e)
    {
		final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getPlayer().getUniqueId());
		if (data.carDriver != null) {
			final CarData carData = Cars.CarData.get(Bukkit.getPlayer(data.carDriver).getVehicle().getUniqueId());
			carData.passengers.clear();
			PlayerSessionData.PlayerData.get(data.carDriver).carPassengers.clear();
			final Player driver = Bukkit.getPlayer(data.carDriver);
			e.getPlayer().teleport(new Location(driver.getWorld(), driver.getLocation().getX(), driver.getLocation().getY() + 1, driver.getLocation().getZ(), e.getPlayer().getLocation().getYaw(), e.getPlayer().getLocation().getPitch()));
			data.carDriver = null;
		} else if (data.isDrivingCar) {
			e.getPlayer().getVehicle().eject();
			data.isDrivingCar = false;
			for (final UUID passenger : data.carPassengers)
				PlayerSessionData.PlayerData.get(passenger).carDriver = null;
			data.carPassengers.clear();
		}
		if (data.combatLogged)
			e.getPlayer().setHealth(0);
		data.savedata.saveData(e.getPlayer().getUniqueId());
		PlayerSessionData.PlayerData.remove(e.getPlayer().getUniqueId());
    }
	
	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent e)
    {
		final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getEntity().getUniqueId());
		if (data.carDriver != null) {
			Cars.CarData.get(Bukkit.getPlayer(data.carDriver).getVehicle().getUniqueId()).passengers.clear();
			PlayerSessionData.PlayerData.get(data.carDriver).carPassengers.clear();
			data.carDriver = null;
		} else if (data.isDrivingCar) {
			data.isDrivingCar = false;
			for (final UUID passenger : data.carPassengers)
				PlayerSessionData.PlayerData.get(passenger).carDriver = null;
			data.carPassengers.clear();
		}
		data.combatLogged = false;
    }
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityClick(final PlayerInteractEntityEvent e) {
		if (Cars.CarData.containsKey(e.getRightClicked().getUniqueId()) && e.getRightClicked().getPassenger() != null) {
			final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getRightClicked().getPassenger().getUniqueId());
			if (data.carPassengers.size() == 3) {
				e.getPlayer().sendMessage("This car is full.");
				return;
			}
			final CarData carData = Cars.CarData.get(e.getRightClicked().getUniqueId());
			if (carData.passengers.size() == 0)
				Bukkit.getEntity(carData.model).setPassenger(e.getPlayer());
			else
				Bukkit.getPlayer(carData.passengers.get(carData.passengers.size() - 1)).setPassenger(e.getPlayer());
			data.carPassengers.add(e.getPlayer().getUniqueId());
			carData.passengers.add(e.getPlayer().getUniqueId());
			try {
				final PacketContainer mountPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.MOUNT);
				mountPacket.getIntegers().write(0, e.getRightClicked().getEntityId());
				mountPacket.getIntegerArrays().write(0, new int[]{e.getRightClicked().getPassenger().getEntityId(), e.getPlayer().getEntityId()});
				ProtocolLibrary.getProtocolManager().sendServerPacket(e.getPlayer(), mountPacket);
				e.getPlayer().setInvisible(true);
			} catch (final InvocationTargetException ex) {
			    ex.printStackTrace();
			}
			Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
                @Override
                public void run() {
                	PlayerSessionData.PlayerData.get(e.getPlayer().getUniqueId()).carDriver = e.getRightClicked().getPassenger().getUniqueId();
                }
            }, 1);
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onVehicleDestroyed(final VehicleDestroyEvent e) {
	    if (Cars.CarData.containsKey(e.getVehicle().getUniqueId())) {
	    	final CarData carData = Cars.CarData.get(e.getVehicle().getUniqueId());
	    	Cars.CarData.remove(e.getVehicle().getUniqueId());
	    	for (final UUID passenger : carData.passengers)
	    		Bukkit.getPlayer(passenger).teleport(new Location(e.getVehicle().getWorld(), e.getVehicle().getLocation().getX(), e.getVehicle().getLocation().getY() + 1, e.getVehicle().getLocation().getZ(), e.getVehicle().getLocation().getYaw(), 0));
	    	Bukkit.getEntity(carData.model).remove();
	    }
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
    public void onVehicleMove(final VehicleMoveEvent e) {
		if (Cars.CarData.containsKey(e.getVehicle().getUniqueId()))
			if (e.getVehicle().getPassenger() != null) {
				final float vehicleAcceleration = PlayerSessionData.PlayerData.get(e.getVehicle().getPassenger().getUniqueId()).vehicleAcceleration;
				final double x = Math.cos(Math.toRadians(e.getVehicle().getLocation().getYaw() + 90)) * vehicleAcceleration;
				final double z = Math.sin(Math.toRadians(e.getVehicle().getLocation().getYaw() + 90)) * vehicleAcceleration;
				e.getVehicle().setVelocity(new Vector(x, -.4f, z));
			}
			else {
				final Entity model = Bukkit.getEntity(Cars.CarData.get(e.getVehicle().getUniqueId()).model);
				model.teleport(new Location(e.getVehicle().getWorld(), e.getVehicle().getLocation().getX(), e.getVehicle().getLocation().getY() + 1, e.getVehicle().getLocation().getZ(), e.getVehicle().getLocation().getYaw(), 0));
				model.setVelocity(e.getVehicle().getVelocity());
			}
    }
	
	@EventHandler
	public void onVehicleExit(final VehicleExitEvent e) {
		if (!PlayerSessionData.PlayerData.containsKey(e.getExited().getUniqueId()))
			return;
		if (Cars.CarData.containsKey(e.getVehicle().getUniqueId())) {
			final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getExited().getUniqueId());
			data.vehicleAcceleration = 0;
			data.isDrivingCar = false;
			for (final UUID uuid : data.carPassengers)
				Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
                    @Override
                    public void run() {
    					try {
    						final PlayerSessionData passengerData = PlayerSessionData.PlayerData.get(uuid);
    						passengerData.oldCarDriver = passengerData.carDriver;
    						passengerData.carDriver = null;
    						final Player passenger = Bukkit.getPlayer(uuid);
    						final PacketContainer mountPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.MOUNT);
        					mountPacket.getIntegers().write(0, e.getVehicle().getEntityId());
        					mountPacket.getIntegerArrays().write(0, new int[]{0, passenger.getEntityId()});
							ProtocolLibrary.getProtocolManager().sendServerPacket(passenger, mountPacket);
						} catch (final InvocationTargetException ex) {
							ex.printStackTrace();
						}
                    }
                }, 10);
			data.carPassengers.clear();
			e.getVehicle().setVelocity(new Vector(0, 0, 0));
			final Entity model = Bukkit.getEntity(Cars.CarData.get(e.getVehicle().getUniqueId()).model);
			model.teleport(new Location(e.getVehicle().getWorld(), e.getVehicle().getLocation().getX(), e.getVehicle().getLocation().getY() + 1, e.getVehicle().getLocation().getZ(), e.getVehicle().getLocation().getYaw(), 0));
			model.setVelocity(new Vector(0, 0, 0));
			e.getExited().setInvisible(false);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onVehicleEnter(final VehicleEnterEvent e) {
		if (Cars.CarData.containsKey(e.getVehicle().getUniqueId())) {
			if (!(e.getEntered() instanceof Player) || e.getVehicle().getPassenger() != null) {
				e.setCancelled(true);
				return;
			}
			final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getEntered().getUniqueId());
			data.isDrivingCar = true;
			final CarData carData = Cars.CarData.get(e.getVehicle().getUniqueId());
			e.getEntered().setPassenger(Bukkit.getEntity(carData.model));
			for (final UUID uuid : carData.passengers)
				Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
                    @Override
                    public void run() {
    					try {
    						PlayerSessionData.PlayerData.get(uuid).carDriver = e.getEntered().getUniqueId();
    						data.carPassengers.add(uuid);
    						final Player passenger = Bukkit.getPlayer(uuid);
    						final PacketContainer mountPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.MOUNT);
        					mountPacket.getIntegers().write(0, e.getVehicle().getEntityId());
        					mountPacket.getIntegerArrays().write(0, new int[]{e.getVehicle().getPassenger().getEntityId(), passenger.getEntityId()});
							ProtocolLibrary.getProtocolManager().sendServerPacket(passenger, mountPacket);
						} catch (final InvocationTargetException ex) {
							ex.printStackTrace();
						}
                    }
                }, 10);
			((Player)e.getEntered()).setInvisible(true);
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	@EventHandler
    public void onBlockClick(final PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND)
			return;
		switch (e.getAction()) {
		case LEFT_CLICK_BLOCK:
			if (e.getItem() != null && e.getPlayer().getGameMode() == GameMode.CREATIVE && e.getItem().getType() == Material.GOLDEN_AXE) {
				PlayerSessionData.PlayerData.get(e.getPlayer().getUniqueId()).schematicPosition1 = e.getClickedBlock().getLocation();
				e.getPlayer().sendMessage("You have set your first position.");
				e.setCancelled(true);
			}
			break;
		case RIGHT_CLICK_BLOCK:
			if (e.getItem() != null && e.getPlayer().getGameMode() == GameMode.CREATIVE && e.getItem().getType() == Material.GOLDEN_AXE) {
				PlayerSessionData.PlayerData.get(e.getPlayer().getUniqueId()).schematicPosition2 = e.getClickedBlock().getLocation();
				e.getPlayer().sendMessage("You have set your second position.");
				e.setCancelled(true);
			} else if (e.getItem() != null && e.getItem().getType() == Material.NETHER_BRICK) {
				final Entity car = e.getClickedBlock().getWorld().spawnEntity(new Location(e.getClickedBlock().getWorld(), e.getClickedBlock().getX(), e.getClickedBlock().getY() + 1, e.getClickedBlock().getZ(), e.getPlayer().getLocation().getYaw(), 0), EntityType.BOAT);
				final LivingEntity model = (LivingEntity)e.getClickedBlock().getWorld().spawnEntity(new Location(e.getClickedBlock().getWorld(), e.getClickedBlock().getX(), e.getClickedBlock().getY() + 2, e.getClickedBlock().getZ(), e.getPlayer().getLocation().getYaw(), 0), EntityType.ARMOR_STAND);
				model.setInvulnerable(true);
				model.setGravity(false);
				model.setInvisible(true);
				Cars.CarData.put(car.getUniqueId(), new CarData(model.getUniqueId()));
				e.setCancelled(true);
			} else if (e.getClickedBlock().getBlockData().getMaterial() == Material.SEA_LANTERN || e.getClickedBlock().getBlockData().getMaterial() == Material.JACK_O_LANTERN || e.getClickedBlock().getBlockData().getMaterial() == Material.GLOWSTONE) {
	        	final Base base = Base.getBaseFromLocation(e.getClickedBlock().getLocation());
	        	for (final BaseMember member : base.members)
	        		if (member.uuid.equals(e.getPlayer().getUniqueId())) {
	        			e.getPlayer().openInventory(base.createInventory());
	        			return;
	        		}
	        	if (base.owner == null || !base.owner.equals(e.getPlayer().getUniqueId())) {
	        		e.getPlayer().sendMessage("You are not a member of this base.");
	        		return;
	        	}
	            e.getPlayer().openInventory(base.createInventory());
	        }
			break;
		}
    }
	
	@SuppressWarnings("incomplete-switch")
	@EventHandler
    public void onBlockPlace(final BlockPlaceEvent e) {
        switch (e.getBlock().getBlockData().getMaterial()) {
        case SEA_LANTERN:
        {
        	final Base base = new Base(e.getBlock().getLocation(), BaseType.SMALL);
        	base.addBaseAndHologram();
        	break;
        }
        case JACK_O_LANTERN:
        {
        	final Base base = new Base(e.getBlock().getLocation(), BaseType.MEDIUM);
        	base.addBaseAndHologram();
        	break;
        }
        case GLOWSTONE:
        {
        	final Base base = new Base(e.getBlock().getLocation(), BaseType.FORT);
        	base.addBaseAndHologram();
        	break;
        }
        }
    }
	
	@EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
		if (!e.getPlayer().isOp()) {
        	e.setCancelled(true);
        	return;
		}
        if (e.getBlock().getBlockData().getMaterial() == Material.SEA_LANTERN || e.getBlock().getBlockData().getMaterial() == Material.JACK_O_LANTERN || e.getBlock().getBlockData().getMaterial() == Material.GLOWSTONE)
        	Base.getBaseFromLocation(e.getBlock().getLocation()).removeBaseAndHologram();
    }
	
    @EventHandler
    public void onPlayerAttack(final EntityDamageByEntityEvent e)
    {
    	if (e.getEntity() instanceof Player)
	    	if (e.getDamager() instanceof Player && PlayerSessionData.PlayerData.get(((Player)e.getDamager()).getUniqueId()).savedata.teams.contains(((Player)e.getEntity()).getUniqueId()))
	    		e.setCancelled(true);
	    	else {
	    		final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getEntity().getUniqueId());
	    		data.combatLogged = true;
	    		Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
                    @Override
                    public void run() {
                    	data.combatLogged = false;
                    	e.getEntity().sendMessage("You are no longer combat-logged.");
                    }
                }, 600);
	    		e.getEntity().sendMessage("You have been combat-logged for 30 seconds.");
	    	}
    }
    
    @SuppressWarnings("incomplete-switch")
	@EventHandler
    public void onInventoryOpen(final InventoryOpenEvent e) {
    	for (final Base base : Base.bases)
    		if (e.getView().getTitle().equals(ChatColor.AQUA + base.name))
    			for (final ItemStack item : e.getPlayer().getInventory().getContents()) {
        			if (item == null)
        				continue;
    				switch (item.getType()) {
    				case BLUE_DYE:
    				{
    					final ItemMeta meta = item.getItemMeta();
    					List<String> lore = meta.getLore();
    					if (lore != null)
    						continue;
    					lore = new ArrayList<>();
    					lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Click to deposit into base (Shift-click to deposit entire stack).");
    					meta.setLore(lore);
    					item.setItemMeta(meta);
    					break;
    				}
    				case ORANGE_DYE:
    				{
    					final ItemMeta meta = item.getItemMeta();
    					List<String> lore = meta.getLore();
    					if (lore != null)
    						continue;
    					lore = new ArrayList<>();
    					lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Click to deposit into base (Shift-click to deposit entire stack).");
    					meta.setLore(lore);
    					item.setItemMeta(meta);
    					break;
    				}
    				case RED_DYE:
    				{
    					final ItemMeta meta = item.getItemMeta();
    					List<String> lore = meta.getLore();
    					if (lore != null)
    						continue;
    					lore = new ArrayList<>();
    					lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Click to deposit into base (Shift-click to deposit entire stack).");
    					meta.setLore(lore);
    					item.setItemMeta(meta);
    					break;
    				}
    				}
        		}
    }
    
    @SuppressWarnings("incomplete-switch")
	@EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
    	if (!PlayerSessionData.PlayerData.containsKey(e.getPlayer().getUniqueId()))
			return;
    	final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getPlayer().getUniqueId());
    	if (data.shouldRespondToInventoryCloseEvents) {
	    	for (final Base base : Base.bases) {
	    		final short index = (short)e.getView().getTitle().indexOf(" |");
	    		if (index != -1 && e.getView().getTitle().substring(0, index).equals(ChatColor.AQUA + base.name)) {
	    			final String title = e.getView().getTitle().substring(index + 3);
	    			if (title.equals("Manage Members") || title.equals("Manage Resources") || title.equals("Upgrade Base")) {
	    				Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
		                    @Override
		                    public void run() {
		                    	e.getPlayer().openInventory(base.createInventory());
		                    }
		                }, 1L);
	    			} else if (title.equals("Confirm Base Dissolution")) {
	    				Bukkit.getServer().dispatchCommand(e.getPlayer(), "base undisband " + base.name);
	    				Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
		                    @Override
		                    public void run() {
		                    	e.getPlayer().openInventory(base.createInventory());
		                    }
		                }, 1L);
	    			} else if (title.equals("Remove Members") || title.startsWith("Add Members") || title.equals("Change Member Rank")) {
	    				Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
		                    @Override
		                    public void run() {
		                    	e.getPlayer().openInventory(base.createManagementInventory());
		                    }
		                }, 1L);
	    			} else if (title.contains("Base Rank")) {
	    				Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
		                    @Override
		                    public void run() {
		                    	e.getPlayer().openInventory(base.createSetRankInventory());
		                    }
		                }, 1L);
	    			} else if (title.equals("Materials") || title.equals("Money")) {
	    				Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
		                    @Override
		                    public void run() {
		                    	e.getPlayer().openInventory(base.createResourcesInventory());
		                    }
		                }, 1L);
	    			}
	    		} else if (e.getView().getTitle().equals(ChatColor.AQUA + base.name))
	    			for (short i = 0; i < e.getPlayer().getInventory().getContents().length; i++) {
	    				final ItemStack item = e.getPlayer().getInventory().getContents()[i];
	        			if (item == null)
	        				continue;
	    				switch (item.getType()) {
	    				case BLUE_DYE:
	    				{
	    					final ItemMeta meta = item.getItemMeta();
	    					List<String> lore = meta.getLore();
	    					lore.remove(lore.size() - 1);
	    					meta.setLore(lore);
	    					item.setItemMeta(meta);
	    					e.getPlayer().getInventory().setItem(i, item);
	    					break;
	    				}
	    				case ORANGE_DYE:
	    				{
	    					final ItemMeta meta = item.getItemMeta();
	    					List<String> lore = meta.getLore();
	    					lore.remove(lore.size() - 1);
	    					meta.setLore(lore);
	    					item.setItemMeta(meta);
	    					e.getPlayer().getInventory().setItem(i, item);
	    					break;
	    				}
	    				case RED_DYE:
	    				{
	    					final ItemMeta meta = item.getItemMeta();
	    					List<String> lore = meta.getLore();
	    					lore.remove(lore.size() - 1);
	    					meta.setLore(lore);
	    					item.setItemMeta(meta);
	    					e.getPlayer().getInventory().setItem(i, item);
	    					break;
	    				}
	    				}
	        		}
	    	}
    	} else
	    	data.shouldRespondToInventoryCloseEvents = true;
    }
    
    @SuppressWarnings({ "incomplete-switch", "deprecation" })
	@EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
    	if (e.getCurrentItem() == null)
    		return;
    	if (e.getView().getTitle().startsWith("You are in ") && e.getView().getTitle().endsWith(" bases") && (e.getCurrentItem().getType() == Material.SEA_LANTERN || e.getCurrentItem().getType() == Material.JACK_O_LANTERN || e.getCurrentItem().getType() == Material.GLOWSTONE)) {
			final Base base = Base.getBaseFromName(e.getCurrentItem().getItemMeta().getDisplayName().substring(2));
			e.getWhoClicked().teleport(new Location(Bukkit.getWorld(base.world), base.x + 0.5f, base.y + 1, base.z + 0.5f));
			e.getWhoClicked().sendMessage("You have teleported to the base named \'" + base.name + "\'.");
    	} else
	    	for (final Base base : Base.bases)
		    	if (e.getView().getTitle().equals(ChatColor.AQUA + base.name)) {
		    		e.setCancelled(true);
		    		switch (e.getCurrentItem().getType()) {
		    		case BIRCH_BOAT:
		    			PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    			Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "base disband " + base.name);
		    			e.getWhoClicked().openInventory(base.createDisbandInventory());
		    			break;
		    		case PLAYER_HEAD:
		    			PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    			e.getWhoClicked().openInventory(base.createManagementInventory());
		    			break;
		    		case PRISMARINE_SHARD:
		    			PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    			e.getWhoClicked().openInventory(base.createResourcesInventory());
		    			break;
		    		case SNOWBALL:
		    			PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    			e.getWhoClicked().openInventory(base.createUpgradesInventory());
		    			break;
		    		case BLUE_DYE:
		    			if (e.getCurrentItem().getAmount() == 1 || e.isShiftClick()) {
		    				base.rags+=e.getCurrentItem().getAmount();
			    			e.getWhoClicked().getInventory().removeItem(e.getCurrentItem());
		    			} else {
		    				base.rags++;
		    				e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
		    			}
		    			base.updateHologram();
		    			break;
		    		case ORANGE_DYE:
		    			if (e.getCurrentItem().getAmount() == 1 || e.isShiftClick()) {
		    				base.metal+=e.getCurrentItem().getAmount();
			    			e.getWhoClicked().getInventory().removeItem(e.getCurrentItem());
		    			} else {
		    				base.metal++;
		    				e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
		    			}
		    			base.updateHologram();
		    			break;
		    		case RED_DYE:
		    			if (e.getCurrentItem().getAmount() == 1 || e.isShiftClick()) {
		    				base.wood+=e.getCurrentItem().getAmount();
			    			e.getWhoClicked().getInventory().removeItem(e.getCurrentItem());
		    			} else {
		    				base.wood++;
		    				e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
		    			}
		    			base.updateHologram();
		    			break;
		    		}
		    	} else {
		    		final short index = (short)e.getView().getTitle().indexOf(" |");
		    		if (index != -1 && e.getView().getTitle().substring(0, index).equals(ChatColor.AQUA + base.name)) {
			    		e.setCancelled(true);
			    		if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
				    		switch (e.getCurrentItem().getType()) {
				    		case BLUE_DYE:
				    			if (e.getCurrentItem().getAmount() == 1 || e.isShiftClick()) {
				    				base.rags+=e.getCurrentItem().getAmount();
					    			e.getWhoClicked().getInventory().removeItem(e.getCurrentItem());
				    			} else {
				    				base.rags++;
				    				e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
				    			}
				    			base.updateHologram();
				    			return;
				    		case ORANGE_DYE:
				    			if (e.getCurrentItem().getAmount() == 1 || e.isShiftClick()) {
				    				base.metal+=e.getCurrentItem().getAmount();
					    			e.getWhoClicked().getInventory().removeItem(e.getCurrentItem());
				    			} else {
				    				base.metal++;
				    				e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
				    			}
				    			base.updateHologram();
				    			return;
				    		case RED_DYE:
				    			if (e.getCurrentItem().getAmount() == 1 || e.isShiftClick()) {
				    				base.wood+=e.getCurrentItem().getAmount();
					    			e.getWhoClicked().getInventory().removeItem(e.getCurrentItem());
				    			} else {
				    				base.wood++;
				    				e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
				    			}
				    			base.updateHologram();
				    			return;
				    		}
			    		}
		    			final String title = e.getView().getTitle().substring(index + 3);
		    			if (title.equals("Manage Members"))
		    				switch (e.getCurrentItem().getType()) {
		    				case RED_CONCRETE:
		    				{
		    	    			if (base.members.size() == 0) {
		    	    				e.getWhoClicked().sendMessage("There are no other members in this base.");
		    	    				return;
		    	    			}
		    	    			final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId());
		    	    			data.shouldRespondToInventoryCloseEvents = false;
		    	    			e.getWhoClicked().openInventory(base.createKickInventory());
		    	    			break;
		    				}
		    	    		case YELLOW_CONCRETE:
		    	    		{
		    	    			if (base.members.size() == 0) {
		    	    				e.getWhoClicked().sendMessage("There are no other members in this base.");
		    	    				return;
		    	    			}
		    	    			final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId());
		    	    			data.shouldRespondToInventoryCloseEvents = false;
		    	    			e.getWhoClicked().openInventory(base.createSetRankInventory());
		    	    			break;
		    	    		}
		    	    		case GREEN_CONCRETE:
		    	    		{
		    	    			final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId());
		    	    			data.shouldRespondToInventoryCloseEvents = false;
		    	    			e.getWhoClicked().openInventory(base.createInviteInventory((byte)0));
		    	    			break;
		    	    		}
		    				}
		    			else if (title.equals("Remove Members"))
		    				switch (e.getCurrentItem().getType()) {
		    				case JUNGLE_BOAT:
		    				{
		    					final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId());
		    					data.shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createKickInventory());
		    					break;
		    				}
		    				case IRON_ORE:
		    				{
		    					final byte page = Byte.valueOf(title.substring(title.lastIndexOf("Page") + 5, title.length() - 1));
		    					if (base.members.size() <= page * 45) {
		    						e.getWhoClicked().sendMessage("There are no next pages.");
		    						return;
		    					}
		    					PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createKickInventory());
		    					break;
		    				}
		    				case PLAYER_HEAD:
		    					Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "base remove-member " + base.name + ' ' + ((SkullMeta)e.getCurrentItem().getItemMeta()).getOwner());
		    					PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    					if (base.members.size() > 0)
		    						e.getWhoClicked().openInventory(base.createKickInventory());
		    					else
		    						e.getWhoClicked().openInventory(base.createManagementInventory());
		    					break;
		    				}
		    			else if (title.startsWith("Add Members"))
		    				switch (e.getCurrentItem().getType()) {
		    				case JUNGLE_BOAT:
		    				{
		    					final byte page = Byte.valueOf(title.substring(title.lastIndexOf("Page") + 5, title.length() - 1));
		    					if (page == 1) {
		    						e.getWhoClicked().sendMessage("There are no previous pages.");
		    						return;
		    					}
		    					final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId());
		    					data.shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createInviteInventory((byte)(page - 2)));
		    					break;
		    				}
		    				case IRON_ORE:
		    				{
		    					final byte page = Byte.valueOf(title.substring(title.lastIndexOf("Page") + 5, title.length() - 1));
		    					if (Bukkit.getOnlinePlayers().size() <= page * 45) {
		    						e.getWhoClicked().sendMessage("There are no next pages.");
		    						return;
		    					}
		    					PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createInviteInventory(page));
		    					break;
		    				}
		    				case PLAYER_HEAD:
		    					Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "base add-member " + base.name + ' ' + ((SkullMeta)e.getCurrentItem().getItemMeta()).getOwner());
		    					break;
		    				}
		    			else if (title.equals("Change Member Rank"))
		    				switch (e.getCurrentItem().getType()) {
		    				case JUNGLE_BOAT:
		    				{
		    					final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId());
		    					data.shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createSetRankInventory());
		    					break;
		    				}
		    				case IRON_ORE:
		    				{
		    					PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createSetRankInventory());
		    					break;
		    				}
		    				case PLAYER_HEAD:
		    					final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId());
		    					data.shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createSetRankSubInventory(((SkullMeta)e.getCurrentItem().getItemMeta()).getOwner()));
		    					break;
		    				}
		    			else if (title.endsWith("Base Rank"))
		    				switch (e.getCurrentItem().getType()) {
		    				case GRAY_CONCRETE:
		    				{
		    					final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId());
		    					Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "base set-rank " + base.name + ' ' + title.substring(title.indexOf("Set") + 4, title.indexOf("\'")) + " member");
		    					data.shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createSetRankInventory());
		    					break;
		    				}
		    				case GREEN_CONCRETE:
		    				{
		    					final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId());
		    					Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "base set-rank " + base.name + ' ' + title.substring(title.indexOf("Set") + 4, title.indexOf("\'")) + " moderator");
		    					data.shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createSetRankInventory());
		    					break;
		    				}
		    				case RED_CONCRETE:
		    				{
		    					final PlayerSessionData data = PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId());
		    					Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "base set-rank " + base.name + ' ' + title.substring(title.indexOf("Set") + 4, title.indexOf("\'")) + " admin");
		    					data.shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createSetRankInventory());
		    					break;
		    				}
		    				}
		    			else if (title.equals("Confirm Base Dissolution"))
		    				switch (e.getCurrentItem().getType()) {
		    				case GREEN_CONCRETE:
		    					Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "base confirmdisband " + base.name);
		    					e.getWhoClicked().closeInventory();
		    					break;
		    				case RED_CONCRETE:
		    					Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "base undisband " + base.name);
		    					e.getWhoClicked().openInventory(base.createInventory());
		    					break;
		    				}
		    			else if (title.equals("Manage Resources"))
		    				switch (e.getCurrentItem().getType()) {
		    				case SUNFLOWER:
		    					PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createMaterialsInventory());
		    					break;
		    				case BEEF:
		    					e.getWhoClicked().sendMessage("This feature is coming soon.");
		    					//PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    					//e.getWhoClicked().openInventory(base.createMoneyInventory());
		    					break;
		    				}
		    			else if (title.equals("Materials"))
		    				switch (e.getCurrentItem().getType()) {
		    				case BLUE_DYE:
		    				{
		    					boolean hasPermissions = false;
		    					if (base.owner.equals(e.getWhoClicked().getUniqueId()))
		    						hasPermissions = true;
		    					else
		    						for (final BaseMember member : base.members)
		    							if (member.uuid.equals(e.getWhoClicked().getUniqueId())) {
		    								if (member.rank == BaseRank.MODERATOR || member.rank == BaseRank.ADMIN)
		    									hasPermissions = true;
		    								break;
		    							}
		    					if (!hasPermissions) {
		    						e.getWhoClicked().sendMessage("You do not have the correct base permissions to perform this action.");
		    						return;
		    					}
		    					final byte amount = (byte)(e.isShiftClick() ? Math.min(64, base.rags) : 1);
		    					if (amount > base.rags || amount == 0) {
		    						e.getWhoClicked().sendMessage("There are no rags in your base.");
		    						return;
		    					} else if (e.getWhoClicked().getInventory().first(Material.BLUE_DYE) == -1 && e.getWhoClicked().getInventory().firstEmpty() == -1) {
		    						e.getWhoClicked().sendMessage("You do not have any inventory space.");
		    						return;
		    					}
		    					final ItemStack material = new ItemStack(Material.BLUE_DYE);
		    					final ItemMeta meta = material.getItemMeta();
		    					final ArrayList<String> lore = new ArrayList<>();
		    					lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Click to deposit into base (Shift-click to deposit entire stack).");
		    					meta.setLore(lore);
		    					material.setItemMeta(meta);
		    					for (byte i = 0; i < amount; i++)
		    						e.getWhoClicked().getInventory().addItem(material);
		    					base.rags -= amount;
		    					PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createMaterialsInventory());
		    					base.updateHologram();
		    					break;
		    				}
		    				case ORANGE_DYE:
		    				{
		    					boolean hasPermissions = false;
		    					if (base.owner.equals(e.getWhoClicked().getUniqueId()))
		    						hasPermissions = true;
		    					else
		    						for (final BaseMember member : base.members)
		    							if (member.uuid.equals(e.getWhoClicked().getUniqueId())) {
		    								if (member.rank == BaseRank.MODERATOR || member.rank == BaseRank.ADMIN)
		    									hasPermissions = true;
		    								break;
		    							}
		    					if (!hasPermissions) {
		    						e.getWhoClicked().sendMessage("You do not have the correct base permissions to perform this action.");
		    						return;
		    					}
		    					final byte amount = (byte)(e.isShiftClick() ? Math.min(64, base.metal) : 1);
		    					if (amount > base.metal || amount == 0) {
		    						e.getWhoClicked().sendMessage("There is no metal in your base.");
		    						return;
		    					} else if (e.getWhoClicked().getInventory().first(Material.ORANGE_DYE) == -1 && e.getWhoClicked().getInventory().firstEmpty() == -1) {
		    						e.getWhoClicked().sendMessage("You do not have any inventory space.");
		    						return;
		    					}
		    					final ItemStack material = new ItemStack(Material.ORANGE_DYE);
		    					final ItemMeta meta = material.getItemMeta();
		    					final ArrayList<String> lore = new ArrayList<>();
		    					lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Click to deposit into base (Shift-click to deposit entire stack).");
		    					meta.setLore(lore);
		    					material.setItemMeta(meta);
		    					for (byte i = 0; i < amount; i++)
		    						e.getWhoClicked().getInventory().addItem(material);
		    					base.metal -= amount;
		    					PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createMaterialsInventory());
		    					base.updateHologram();
		    					break;
		    				}
		    				case RED_DYE:
		    				{
		    					boolean hasPermissions = false;
		    					if (base.owner.equals(e.getWhoClicked().getUniqueId()))
		    						hasPermissions = true;
		    					else
		    						for (final BaseMember member : base.members)
		    							if (member.uuid.equals(e.getWhoClicked().getUniqueId())) {
		    								if (member.rank == BaseRank.MODERATOR || member.rank == BaseRank.ADMIN)
		    									hasPermissions = true;
		    								break;
		    							}
		    					if (!hasPermissions) {
		    						e.getWhoClicked().sendMessage("You do not have the correct base permissions to perform this action.");
		    						return;
		    					}
		    					final byte amount = (byte)(e.isShiftClick() ? Math.min(64, base.wood) : 1);
		    					if (amount > base.wood || amount == 0) {
		    						e.getWhoClicked().sendMessage("There is no wood in your base.");
		    						return;
		    					} else if (e.getWhoClicked().getInventory().first(Material.RED_DYE) == -1 && e.getWhoClicked().getInventory().firstEmpty() == -1) {
		    						e.getWhoClicked().sendMessage("You do not have any inventory space.");
		    						return;
		    					}
		    					final ItemStack material = new ItemStack(Material.RED_DYE);
		    					final ItemMeta meta = material.getItemMeta();
		    					final ArrayList<String> lore = new ArrayList<>();
		    					lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Click to deposit into base (Shift-click to deposit entire stack).");
		    					meta.setLore(lore);
		    					material.setItemMeta(meta);
		    					for (byte i = 0; i < amount; i++)
		    						e.getWhoClicked().getInventory().addItem(material);
		    					base.wood -= amount;
		    					PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createMaterialsInventory());
		    					base.updateHologram();
		    					break;
		    				}
		    				}
		    			else if (title.equals("Upgrade Base"))
		    				switch (e.getCurrentItem().getType()) {
		    				case GREEN_CONCRETE:
		    					if (base.pasteInProgress) {
		    						e.getWhoClicked().sendMessage("Your base is already in the middle of an upgrade.");
		    						return;
		    					}
		    					if (base.rags > ((base.level + 1) ^ 4) && base.metal > ((base.level + 1) ^ 4) && base.wood > ((base.level + 1) ^ 4))
		    						base.levelUp();
		    					else {
		    						e.getWhoClicked().sendMessage("You do not meet the requirements to level up your base.");
		    						return;
		    					}
		    					PlayerSessionData.PlayerData.get(e.getWhoClicked().getUniqueId()).shouldRespondToInventoryCloseEvents = false;
		    					e.getWhoClicked().openInventory(base.createUpgradesInventory());
		    					break;
		    				}
		    		}
		    	}
    }
    
}
