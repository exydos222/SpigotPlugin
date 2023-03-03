package commands.player;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class PMCommand implements CommandExecutor {
    
    @Override
    public final boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        switch (args.length) {
        case 0:
            sender.sendMessage("You did not specify a player.");
            break;
        case 1:
            sender.sendMessage("You did not specify a message.");
            break;
        default:
            if (args[0].equals(sender.getName())) {
                sender.sendMessage("You cannot message yourself.");
                return true;
            }
            final Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage("No player by that name was found.");
                return true;
            }
            sender.sendMessage(ChatColor.of("#0E7C61") + "You " + ChatColor.WHITE + "to " + player.getDisplayName() + ChatColor.WHITE + ": " + args[1]);
            player.sendMessage(((Player)sender).getDisplayName() + " §fto "+ChatColor.of("#0E7C61") + "You§f: " + args[1]);       
        }
        return true;
    }
    
}