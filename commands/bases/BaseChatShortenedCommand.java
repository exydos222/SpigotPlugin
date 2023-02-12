package commands.bases;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import objects.bases.Base;
import objects.bases.BaseMember;

public class BaseChatShortenedCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        switch (args.length) {
        case 0:
            sender.sendMessage("You did not specify a base.");
            break;
        case 1:
            sender.sendMessage("You did not specify a message.");
            break;
        default:
            final Base base = Base.getBaseFromName(args[1]);
            if (base == null) {
                sender.sendMessage("There is not a base named \'" + args[1] + "\'.");
                return true;
            }
            if (!base.owner.equals(((Player)sender).getUniqueId())) {
                for (final BaseMember member : base.members)
                    if (member.uuid.equals(((Player)sender).getUniqueId()))
                        break;
                return true;
            }
            Player player = Bukkit.getPlayer(base.owner);
            if (player != null)
                player.sendMessage(ChatColor.AQUA + "<" + base.name + "> " + ChatColor.WHITE + ((Player)sender).getDisplayName() + ChatColor.GRAY + ": " + ChatColor.WHITE + args[2]);
            for (final BaseMember member : base.members) {
                player = Bukkit.getPlayer(member.uuid);
                if (player != null)
                    player.sendMessage(ChatColor.AQUA + "<" + base.name + "> " + ChatColor.WHITE + ((Player)sender).getDisplayName() + ChatColor.GRAY + ": " + ChatColor.WHITE + args[2]);
            }   
        }
        return true;
    }
    
}