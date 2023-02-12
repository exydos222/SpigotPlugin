package commands.teams;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.Note.Tone;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import data.player.PlayerSaveData;
import data.player.PlayerSessionData;
import main.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TeamCommand implements CommandExecutor {

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Unknown sub-command, available:\nadd <name>\nremove <name>\nlist\naccept <name>\ndecline <name>");
            return true;
        }
        switch (args[0].toLowerCase()) {
        case "add-member":
        case "add_member":
        case "add":
        case "add-player":
        case "add_player":
        case "invite-member":
        case "invite_member":
        case "invite":
        case "invite-player":
        case "invite_player":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a player.");
                return true;
            }
            final Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("That player is not online.");
                return true;
            } else if (player.getUniqueId().equals(((Player)sender).getUniqueId())) {
                sender.sendMessage("You cannot send a team invite to yourself.");
                return true;
            }
            final PlayerSessionData data = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId());
            if (data.activeTeamInvites.contains(player.getUniqueId())) {
                sender.sendMessage("You already have an active team invite sent to this player.");
                return true;
            } else if (data.savedata.teams.contains(player.getUniqueId())) {
                sender.sendMessage("You are already in a team with this player.");
                return true;
            }
            final TextComponent component = new TextComponent(((Player)sender).getDisplayName() + " has sent you a team invite, it will expire in 30 seconds.\n");
            final TextComponent accept = new TextComponent(ChatColor.GREEN + "ACCEPT");
            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team accept " + ((Player)sender).getDisplayName()));
            component.addExtra(accept);
            component.addExtra(" ");
            final TextComponent decline = new TextComponent(ChatColor.RED + "DECLINE");
            decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team decline " + ((Player)sender).getDisplayName()));
            component.addExtra(decline);
            data.activeTeamInvites.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
                @Override
                public void run() {
                    if (data.activeTeamInvites.contains(((Player)sender).getUniqueId())) {
                        data.activeTeamInvites.remove(((Player)sender).getUniqueId());
                        sender.sendMessage("Your team invite to " + args[1] + " has expired.");
                        player.sendMessage("Your team invite from " + ((Player)sender).getDisplayName() + " has expired.");
                    }
                }
            }, 600L);
            sender.sendMessage("You have sent a team invite to " + args[1] + ".");
            player.spigot().sendMessage(component);
            break;
        }
        case "remove":
        case "delete":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a player.");
                return true;
            }
            final Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                final PlayerSessionData sessionData = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId());
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (!sessionData.activeTeamInvites.contains(offlinePlayer.getUniqueId())) {
                    sender.sendMessage("You are not in a team with this player.");
                    return true;
                }
                PlayerSaveData data = PlayerSaveData.loadData(offlinePlayer.getUniqueId());
                data.teams.remove(((Player)sender).getUniqueId());
                data = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId()).savedata;
                data.teams.remove(offlinePlayer.getUniqueId());
                data.saveData(offlinePlayer.getUniqueId());
            } else {
                PlayerSaveData data = PlayerSessionData.PlayerData.get(player.getUniqueId()).savedata;
                if (!data.teams.contains(((Player)sender).getUniqueId())) {
                    sender.sendMessage("You are not in a team with this player.");
                    return true;
                }
                data.teams.remove(((Player)sender).getUniqueId());
                data = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId()).savedata;
                data.teams.remove(player.getUniqueId());
                player.sendMessage("You are no longer teamed with " + ((Player)sender).getDisplayName() + '.');
            }
            sender.sendMessage("You are no longer teamed with " + args[1] + '.');
            try {
                PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
                packet.getPlayerInfoAction().write(0, PlayerInfoAction.UPDATE_DISPLAY_NAME);
                final ArrayList<PlayerInfoData> playerData = new ArrayList<PlayerInfoData>();
                playerData.add(new PlayerInfoData(new WrappedGameProfile(player.getUniqueId(), player.getName()), 0, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(player.getName())));
                packet.getPlayerInfoDataLists().write(0, playerData);
                ProtocolLibrary.getProtocolManager().sendServerPacket(((Player)sender), packet);
                packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
                packet.getPlayerInfoAction().write(0, PlayerInfoAction.UPDATE_DISPLAY_NAME);
                playerData.clear();
                playerData.add(new PlayerInfoData(new WrappedGameProfile(((Player)sender).getUniqueId(), ((Player)sender).getName()), 0, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(((Player)sender).getName())));
                packet.getPlayerInfoDataLists().write(0, playerData);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (final InvocationTargetException e) {
                e.printStackTrace();
            }
            break;
        }
        case "list":
        {
            String members = "Your team members: ";
            final PlayerSaveData data = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId()).savedata;
            if (data.teams.size() > 0) {
                for (final UUID uuid : data.teams)
                    members += Bukkit.getOfflinePlayer(uuid).getName() + ", ";
                members = members.substring(0, members.length() - 2) + '.';
                sender.sendMessage(members);
            } else
                sender.sendMessage("You have no teammates.");
            break;
        }
        case "accept":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a player.");
                return true;
            }
            final Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("This player is not online.");
                return true;
            }
            PlayerSessionData data = PlayerSessionData.PlayerData.get(player.getUniqueId());
            if (data.activeTeamInvites.contains(((Player)sender).getUniqueId())) {
                data.activeTeamInvites.remove(((Player)sender).getUniqueId());
                data.savedata.teams.add(((Player)sender).getUniqueId());
                data = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId());
                data.savedata.teams.add(player.getUniqueId());
                player.sendMessage(args[1] + " has accepted your team invite.");
                sender.sendMessage("You accepted " + (((Player)sender).getDisplayName().charAt(((Player)sender).getDisplayName().length() - 1) == 's' ? (((Player)sender).getDisplayName() + '\'') : (((Player)sender).getDisplayName() + "\'s")) + " team invite.");
                ((Player)sender).playNote(((Player)sender).getLocation(), Instrument.CHIME, Note.flat(1, Tone.F));
                player.playNote(player.getLocation(), Instrument.CHIME, Note.flat(1, Tone.F));
                try {
                    PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
                    packet.getPlayerInfoAction().write(0, PlayerInfoAction.UPDATE_DISPLAY_NAME);
                    final ArrayList<PlayerInfoData> playerData = new ArrayList<PlayerInfoData>();
                    playerData.add(new PlayerInfoData(new WrappedGameProfile(player.getUniqueId(), ChatColor.GREEN + "(Teamed) " + player.getName()), 0, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(ChatColor.GREEN + "(Teamed) " + player.getName())));
                    packet.getPlayerInfoDataLists().write(0, playerData);
                    ProtocolLibrary.getProtocolManager().sendServerPacket(((Player)sender), packet);
                    packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
                    packet.getPlayerInfoAction().write(0, PlayerInfoAction.UPDATE_DISPLAY_NAME);
                    playerData.clear();
                    playerData.add(new PlayerInfoData(new WrappedGameProfile(((Player)sender).getUniqueId(), ChatColor.GREEN + "(Teamed) " + ((Player)sender).getName()), 0, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(ChatColor.GREEN + "(Teamed) " + ((Player)sender).getName())));
                    packet.getPlayerInfoDataLists().write(0, playerData);
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                } catch (final InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else
                sender.sendMessage("You do not have an active team invite from " + args[1]);
            break;
        }
        case "decline":
        {
            if (args.length == 1) {
                sender.sendMessage("You did not specify a player.");
                return true;
            }
            final Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("This player is not online.");
                return true;
            }
            PlayerSessionData data = PlayerSessionData.PlayerData.get(player.getUniqueId());
            if (data.activeTeamInvites.contains(((Player)sender).getUniqueId())) {
                data.activeTeamInvites.remove(((Player)sender).getUniqueId());
                data.savedata.teams.remove(((Player)sender).getUniqueId());
                data = PlayerSessionData.PlayerData.get(((Player)sender).getUniqueId());
                data.savedata.teams.remove(player.getUniqueId());
                sender.sendMessage(args[1] + " has declined your team invite.");
                player.sendMessage("You declined " + (((Player)sender).getDisplayName().charAt(((Player)sender).getDisplayName().length() - 1) == 's' ? (((Player)sender).getDisplayName() + '\'') : (((Player)sender).getDisplayName() + "\'s")) + " team invite.");
                ((Player)sender).playNote(((Player)sender).getLocation(), Instrument.BASS_GUITAR, Note.sharp(0, Tone.B));
                player.playNote(player.getLocation(), Instrument.BASS_GUITAR, Note.sharp(0, Tone.B));
            } else
                sender.sendMessage("You do not have an active team invite from " + args[1]);
            break;
        }
        default:
            sender.sendMessage("Unknown sub-command, available:\nadd <name>\nremove <name>\nlist\naccept <name>\ndecline <name>");
        }
        return true;
    }
    
}