package commands.bases;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import data.bases.BaseInvite;
import data.bases.BaseMember;
import data.player.PlayerSessionData;
import enums.bases.BaseRank;
import enums.bases.BaseType;
import main.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import objects.bases.Base;

public class BaseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Unknown sub-command, available:\nclaim <base-name>\nadd-member <base-name> <name>\nremove-member <base-name> <name>\nset-rank <base-name> <name> <rank>\ndisband <base-name>\naccept <base-name>\ndecline <base-name>\nbase list\nbase leave <base-name>\nbase chat <base-name> <message>");
            return true;
        }
        switch (args[0].toLowerCase()) {
        case "claim":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a name for your base.");
                return true;
            }
            byte smallCount = 0, mediumCount = 0;
            boolean fort = false;
            if (Base.getBaseFromName(args[1]) != null) {
                sender.sendMessage("A base with the name \'" + args[1] + "\' already exists.");
                return true;
            }
            for (final Base base : Base.bases)
                if (base.isInUnclaimedBaseRegion(((Player)sender).getLocation())) {
                    for (final Base b : Base.bases) {
                        if (b.owner == null)
                            continue;
                        else if (b.owner.equals(((Player)sender).getUniqueId())) {
                            switch (b.type) {
                            case SMALL:
                                smallCount++;
                                break;
                            case MEDIUM:
                                mediumCount++;
                                break;
                            case FORT:
                                fort = true;
                                break;
                            }
                            continue;
                        }
                        for (final BaseMember m : b.members)
                            if (m.uuid.equals(((Player)sender).getUniqueId())) {
                                switch (b.type) {
                                case SMALL:
                                    smallCount++;
                                    break;
                                case MEDIUM:
                                    mediumCount++;
                                    break;
                                case FORT:
                                    fort = true;
                                    break;
                                }
                                break;
                            }
                    }
                    switch (base.type) {
                    case SMALL:
                        if (smallCount == 7) {
                            sender.sendMessage("You have has reached the maximum for small bases.");
                            return true;
                        }
                        break;
                    case MEDIUM:
                        if (mediumCount == 4) {
                            sender.sendMessage("You have reached the maximum for medium bases.");
                            return true;
                        }
                        break;
                    case FORT:
                        if (fort) {
                            sender.sendMessage("You have reached the maximum for fort bases.");
                            return true;
                        }
                        break;
                    }
                    if (base.owner != null) {
                        sender.sendMessage("This base is already claimed.");
                        return true;
                    }
                    base.owner = ((Player)sender).getUniqueId();
                    base.name = args[1];
                    base.updateHologram();
                    sender.sendMessage("You have claimed the base with the name \'" + args[1] + "\'.");
                    return true;
                }
            sender.sendMessage("You are not within the radius of any bases.");
            break;
        }
        case "add-member":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a base.");
                return true;
            } else if (args.length == 2) {
                sender.sendMessage("You did not specify a player.");
                return true;
            }
            final Base base = Base.getBaseFromName(args[1]);
            if (base == null) {
                sender.sendMessage("You do not have the correct base permissions to perform this action.");
                return true;
            } else if ((base.type == BaseType.SMALL && base.members.size() == base.level) || (base.type == BaseType.MEDIUM && base.members.size() == base.level * 2) || (base.type == BaseType.FORT && base.members.size() == base.level * 3)) {
                sender.sendMessage("This base exceeds the maximum amount of players.");
                return true;
            }
            final Player player = Bukkit.getPlayer(args[2]);
            if (player == null) {
                sender.sendMessage("That player is not online.");
                return true;
            }
            byte smallCount = 0, mediumCount = 0;
            boolean fort = false;
            for (final Base b : Base.bases) {
                if (b.owner == null)
                    continue;
                else if (b.owner.equals(player.getUniqueId())) {
                    switch (b.type) {
                    case SMALL:
                        smallCount++;
                        break;
                    case MEDIUM:
                        mediumCount++;
                        break;
                    case FORT:
                        fort = true;
                        break;
                    }
                    continue;
                }
                for (final BaseMember m : b.members)
                    if (m.uuid.equals(player.getUniqueId())) {
                        switch (b.type) {
                        case SMALL:
                            smallCount++;
                            break;
                        case MEDIUM:
                            mediumCount++;
                            break;
                        case FORT:
                            fort = true;
                            break;
                        }
                        break;
                    }
            }
            switch (base.type) {
            case SMALL:
                if (smallCount == 7) {
                    sender.sendMessage("That player has reached the maximum for small bases.");
                    return true;
                }
                break;
            case MEDIUM:
                if (mediumCount == 4) {
                    sender.sendMessage("That player has reached the maximum for medium bases.");
                    return true;
                }
                break;
            case FORT:
                if (fort) {
                    sender.sendMessage("That player has reached the maximum for fort bases.");
                    return true;
                }
                break;
            }
            if (base.owner.equals(player.getUniqueId())) {
                sender.sendMessage("That player is already a member of this base.");
                return true;
            }
            for (final BaseMember member : base.members)
                if (member.uuid.equals(player.getUniqueId())) {
                    sender.sendMessage("That player is already a member of this base.");
                    return true;
                }
            boolean hasPermissions = false;
            if (base.owner.equals(((Player)sender).getUniqueId()))
                hasPermissions = true;
            else
                for (final BaseMember member : base.members)
                    if (member.uuid.equals(((Player)sender).getUniqueId())) {
                        if (member.rank == BaseRank.ADMIN)
                            hasPermissions = true;
                        break;
                    }
            if (hasPermissions) {
                final PlayerSessionData data = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId());
                for (final BaseInvite invite : data.activeBaseInvites)
                    if (invite.base == base && invite.player.equals(player.getUniqueId())) {
                        sender.sendMessage("You already have an active base invite to this player for the base named \'" + args[1] + "\'.");
                        return true;
                    }
                final TextComponent component = new TextComponent(((Player)sender).getDisplayName() + " has sent you an invite to their base named \'" + base.name + "\', it will expire in 30 seconds.\n");
                final TextComponent accept = new TextComponent(ChatColor.GREEN + "ACCEPT");
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/base accept " + base.name + " " + ((Player)sender).getDisplayName()));
                component.addExtra(accept);
                component.addExtra(" ");
                final TextComponent decline = new TextComponent(ChatColor.RED + "DECLINE");
                decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/base decline " + base.name + " " + ((Player)sender).getDisplayName()));
                component.addExtra(decline);
                final BaseInvite invite = new BaseInvite(base, player.getUniqueId());
                data.activeBaseInvites.add(invite);
                Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), new Runnable() {
                    @Override
                    public void run() {
                        if (data.activeBaseInvites.contains(invite)) {
                            data.activeBaseInvites.remove(invite);
                            sender.sendMessage("Your base invite to " + args[2] + " has expired.");
                            player.sendMessage("Your base invite from " + ((Player)sender).getDisplayName() + " has expired.");
                        }
                    }
                }, 600L);
                sender.sendMessage("You have sent a base invite to " + args[2] + ".");
                player.spigot().sendMessage(component);
            } else
                sender.sendMessage("You do not have the correct base permissions to perform this action.");
            break;
        }
        case "remove-member":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a base.");
                return true;
            } else if (args.length == 2) {
                sender.sendMessage("You did not specify a player.");
                return true;
            }
            final Base base = Base.getBaseFromName(args[1]);
            if (base == null) {
                sender.sendMessage("You do not have the correct base permissions to perform this action.");
                return true;
            }
            @SuppressWarnings("deprecation") final OfflinePlayer player = Bukkit.getOfflinePlayer(args[2]);
            if (player.getUniqueId().equals(((Player)sender).getUniqueId())) {
                sender.sendMessage("Why would you even try to kick yourself...");
                return true;
            } else if (base.owner.equals(player.getUniqueId())) {
                sender.sendMessage("You cannot kick the owner.");
                return true;
            }
            for (final BaseMember member : base.members)
                if (member.uuid.equals(player.getUniqueId())) {
                    boolean hasPermissions = false;
                    if (base.owner.equals(((Player)sender).getUniqueId()))
                        hasPermissions = true;
                    else
                        for (final BaseMember subMember : base.members)
                            if (subMember.uuid.equals(((Player)sender).getUniqueId()) && subMember.rank == BaseRank.ADMIN)
                                hasPermissions = true;
                    if (hasPermissions) {
                        base.members.remove(member);
                        if (player.isOnline())
                            ((Player)player).sendMessage("You have been kicked from the base named \'" + args[1] + "\'.");
                        sender.sendMessage("You have kicked " + player.getName() + " from the base \'" + base.name + "\'.");
                        base.updateHologram();
                    } else
                        sender.sendMessage("You do not have the correct base permissions to perform this action.");
                    return true;
                }
            sender.sendMessage("That player is not in the base \'" + args[1] + "\'.");
            break;
        }
        case "set-rank":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a base.");
                return true;
            } else if (args.length == 2) {
                sender.sendMessage("You did not specify a player.");
                return true;
            } else if (args.length == 3) {
                sender.sendMessage("You did not specify a rank.");
                return true;
            }
            final Base base = Base.getBaseFromName(args[1]);
            if (base == null) {
                sender.sendMessage("You do not have the correct base permissions to perform this action.");
                return true;
            }
            @SuppressWarnings("deprecation") final OfflinePlayer player = Bukkit.getOfflinePlayer(args[2]);
            if (player.getUniqueId().equals(((Player)sender).getUniqueId())) {
                sender.sendMessage("You can't modify your own rank.");
                return true;
            } else if (base.owner.equals(player.getUniqueId())) {
                sender.sendMessage("You cannot modify the rank of the owner.");
                return true;
            }
            boolean hasPermissions = false;
            if (base.owner.equals(((Player)sender).getUniqueId()))
                hasPermissions = true;
            for (final BaseMember member : base.members)
                if ((member.uuid.equals(((Player)sender).getUniqueId()) && member.rank == BaseRank.ADMIN) || hasPermissions) {
                    BaseRank newRank;
                    switch (args[3].toUpperCase()) {
                    case "MEMBER":
                        newRank = BaseRank.MEMBER;
                        break;
                    case "MODERATOR":
                        newRank = BaseRank.MODERATOR;
                        break;
                    case "ADMIN":
                        newRank = BaseRank.ADMIN;
                        break;
                    default:
                        sender.sendMessage("Unknown rank, available: member, moderator, admin");
                        return true;
                    }
                    member.rank = newRank;
                    sender.sendMessage("You have set " + (player.getName().charAt(player.getName().length() - 1) == 's' ? (player.getName() + '\'') : (player.getName() + "\'s")) + " rank to " + newRank.name() + " in the base named \'" + base.name + "\'.");
                    if (player.isOnline())
                        ((Player)player).sendMessage("Your rank in the base named \'" + base.name + "\' has been set to " + newRank.name() + '.');
                    return true;
                }
            sender.sendMessage("You do not have the correct base permissions to perform this action.");
            break;
        }
        case "disband":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a base.");
                return true;
            }
            final Base base = Base.getBaseFromName(args[1]);
            if (base == null || !base.owner.equals(((Player)sender).getUniqueId())) {
                sender.sendMessage("You do not own that base.");
                return true;
            }
            final TextComponent component = new TextComponent("Are you sure you want to permanently delete your base named \'" + args[1] + "\'? This message will expire in 30 seconds.\n");
            final TextComponent accept = new TextComponent(ChatColor.GREEN + "YES");
            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/base confirmdisband " + args[1]));
            component.addExtra(accept);
            component.addExtra(" ");
            final TextComponent decline = new TextComponent(ChatColor.RED + "NO");
            decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/base undisband " + args[1]));
            component.addExtra(decline);
            sender.spigot().sendMessage(component);
            final PlayerSessionData data = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId());
            data.activeBaseDisollutionRequests.add(base);
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), new Runnable() {
                @Override
                public void run() {
                    if (data.activeBaseDisollutionRequests.contains(base)) {
                        data.activeBaseDisollutionRequests.remove(base);
                        sender.sendMessage("The dissolution request for your base \'" + args[1] + "\' has expired.");
                    }
                }
            }, 600L);
            break;
        }
        case "confirmdisband":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a base.");
                return true;
            }
            final PlayerSessionData data = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId());
            final Base base = Base.getBaseFromName(args[1]);
            if (base == null || !data.activeBaseDisollutionRequests.contains(base)) {
                sender.sendMessage("You do not have an active dissolution request for that base.");
                return true;
            }
            data.activeBaseDisollutionRequests.remove(base);
            base.disband();
            ((Player)sender).playSound(((Player)sender).getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.f, 1.f);
            sender.sendMessage("You have disbanded your base named \'" + args[1] + "\'.");
            break;
        }
        case "undisband":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a base.");
                return true;
            }
            final PlayerSessionData data = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId());
            final Base base = Base.getBaseFromName(args[1]);
            if (base == null || !data.activeBaseDisollutionRequests.contains(base)) {
                sender.sendMessage("You do not have an active dissolution request for that base.");
                return true;
            }
            data.activeBaseDisollutionRequests.remove(base);
            sender.sendMessage("You have cancelled your dissolution request for the base named \'" + args[1] + "\'.");
            break;
        }
        case "accept":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a base.");
                return true;
            } else if (args.length == 2) {
                sender.sendMessage("You did not specify a player.");
                return true;
            }
            final Player player = Bukkit.getPlayer(args[2]);
            if (player == null) {
                sender.sendMessage("That player is not online.");
                return true;
            }
            final PlayerSessionData data = PlayerSessionData.PlayerData.get(player.getUniqueId());
            final Base base = Base.getBaseFromName(args[1]);
            for (final BaseInvite invite : data.activeBaseInvites)
                if (invite.base == base && invite.player.equals(((Player)sender).getUniqueId())) {
                    data.activeBaseInvites.remove(invite);
                    base.members.add(new BaseMember(BaseRank.MEMBER, ((Player)sender).getUniqueId()));
                    base.updateHologram();
                    sender.sendMessage("You accepted " + (player.getDisplayName().charAt(player.getDisplayName().length() - 1) == 's' ? (player.getDisplayName() + '\'') : (player.getDisplayName() + "\'s")) + " invite to the base named \'" + args[1] + "\'.");
                    player.sendMessage(((Player)sender).getDisplayName() + " has accepted your invite to the base named \'" + args[1] + "\'.");
                    ((Player)sender).playNote(((Player)sender).getLocation(), Instrument.CHIME, Note.flat(1, Tone.F));
                    player.playNote(player.getLocation(), Instrument.CHIME, Note.flat(1, Tone.F));
                    return true;
                }
            sender.sendMessage("You do not have an invite from a base named \'" + args[1] + "\'.");
            break;
        }
        case "decline":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a base.");
                return true;
            } else if (args.length == 2) {
                sender.sendMessage("You did not specify a player.");
                return true;
            }
            final Player player = Bukkit.getPlayer(args[2]);
            if (player == null) {
                sender.sendMessage("That player is not online.");
                return true;
            }
            final PlayerSessionData data = PlayerSessionData.PlayerData.get(player.getUniqueId());
            final Base base = Base.getBaseFromName(args[1]);
            for (final BaseInvite invite : data.activeBaseInvites)
                if (invite.base == base && invite.player.equals(((Player)sender).getUniqueId())) {
                    data.activeBaseInvites.remove(invite);
                    sender.sendMessage("You declined " + (player.getDisplayName().charAt(player.getDisplayName().length() - 1) == 's' ? (player.getDisplayName() + '\'') : (player.getDisplayName() + "\'s")) + " invite to the base named \'" + args[1] + "\'.");
                    player.sendMessage(((Player)sender).getDisplayName() + " has declined your invite to the base named \'" + args[1] + "\'.");
                    ((Player)sender).playNote(((Player)sender).getLocation(), Instrument.BASS_GUITAR, Note.sharp(0, Tone.B));
                    player.playNote(player.getLocation(), Instrument.BASS_GUITAR, Note.sharp(0, Tone.B));
                    return true;
                }
            sender.sendMessage("You do not have an invite from " + player.getDisplayName() + " for a base named \'" + args[1] + "\'.");
            break;
        }
        case "list":
            final ArrayList<Base> bases = new ArrayList<>();
            for (final Base base : Base.bases) {
                if (base.owner == null)
                    continue;
                else if (base.owner.equals(((Player)sender).getUniqueId())) {
                    bases.add(base);
                    continue;
                }
                for (final BaseMember member : base.members)
                    if (member.uuid.equals(((Player)sender).getUniqueId())) {
                        bases.add(base);
                        break;
                    }
            }
            ((Player)sender).openInventory(Base.createListInventory(bases, (Player)sender));
            break;
        case "leave":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a base.");
                return true;
            }
            final Base base = Base.getBaseFromName(args[1]);
            if (base == null) {
                sender.sendMessage("There is not a base named \'" + args[1] + "\'.");
                return true;
            }
            if (base.owner.equals(((Player)sender).getUniqueId()))
                sender.sendMessage("You cannot leave as the owner, you must disband your base if you wish to leave.");
            else {
                for (final BaseMember member : base.members)
                    if (member.uuid.equals(((Player)sender).getUniqueId())) {
                        base.members.remove(member);
                        for (final BaseMember subMember : base.members) {
                            final Player player = Bukkit.getPlayer(subMember.uuid);
                            if (player != null)
                                player.sendMessage(((Player)sender).getDisplayName() + " has left the base you are in named \'" + base.name + "\'.");
                        }
                        sender.sendMessage("You left the base named \'" + base.name + "\'.");
                        base.updateHologram();
                        return true;
                    }
                sender.sendMessage("You are not a member of that base.");
            }
            break;
        }
        case "chat":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a base.");
                return true;
            } else if (args.length == 2) {
                sender.sendMessage("You did not specify a message.");
                return true;
            }
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
            break;
        }
        default:
            sender.sendMessage("Unknown sub-command, available:\nclaim <base-name>\nadd-member <base-name> <name>\nremove-member <base-name> <name>\nset-rank <base-name> <name> <rank>\ndisband <base-name>\naccept <base-name>\ndecline <base-name>\base list\nbase leave <base-name>\nbase chat <base-name> <message>");
        }
        return true;
    }
    
}