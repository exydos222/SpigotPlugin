package commands.regions;

import java.io.File;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import data.player.PlayerSessionData;
import main.Main;
import objects.region.Region;

public class RegionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
    	if (!sender.isOp()) {
    		sender.sendMessage("This is an admin-only command.");
    		return true;
    	} else if (args.length == 0) {
    		sender.sendMessage("Unknown sub-command, available:\ncreate <name>\ndelete <name>\nwand");
    		return true;
    	}
    	switch (args[0].toLowerCase()) {
    	case "create":
    	{
    		if (args.length == 1) {
        		sender.sendMessage("You did not specify a name.");
        		return true;
        	}
    		final PlayerSessionData data = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId());
    		if (data.selectionPosition1 == null) {
    			sender.sendMessage("You have not set your first position.");
    			return true;
    		} else if (data.selectionPosition2 == null) {
    			sender.sendMessage("You have not set your second position.");
    			return true;
    		} else if (data.selectionPosition1.getWorld() != data.selectionPosition2.getWorld()) {
    			sender.sendMessage("These points are in seperate worlds.");
    			return true;
    		}
    		new Region(args[1], data.selectionPosition1, data.selectionPosition2).saveRegion();
    		sender.sendMessage("Saved region to disk.");
    		break;
    	}
    	case "delete":
    		if (args.length == 1) {
        		sender.sendMessage("You did not specify a name.");
        		return true;
        	}
    		final File file = new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/RegionData/" + args[1]);
    		if (!file.exists()) {
    			sender.sendMessage("That region does not exist.");
    			return true;
    		}
    		file.delete();
    		sender.sendMessage("Schematic named has been deleted.");
    		break;
    	case "wand":
    		((Player)sender).getInventory().addItem(new ItemStack(Material.GOLDEN_AXE));
    		break;
    	default:
    		sender.sendMessage("Unknown sub-command, available:\nsave <name>\npaste <name>\ndelete <name>\nwand");
    	}
        return true;
    }
    
}