package objects.bases;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import enums.bases.BaseType;
import main.Main;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.line.ClickableHologramLine;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLineClickEvent;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLineClickListener;
import objects.schematic.Schematic;
import objects.schematic.SchematicOperator;

public class Base implements Externalizable {

    private static final short version = 5;
    
    public static final ArrayList<Base> bases = new ArrayList<>();
    public static final Schematic unclaimedSchematic = Schematic.loadSchematic("UnclaimedBase");
    public static final Schematic level1Base = Schematic.loadSchematic("Level1Base");
    public static final Schematic level2Base = Schematic.loadSchematic("Level2Base");
    public static final Schematic level3Base = Schematic.loadSchematic("Level3Base");
    public static final Schematic level4Base = Schematic.loadSchematic("Level4Base");
    public static final Schematic level5Base = Schematic.loadSchematic("Level5Base");
    
    public ArrayList<BaseMember> members = new ArrayList<>();
    public UUID owner, world;
    public long x, y, z;
    public String uuid, name, world_name;
    public Hologram hologram;
    public byte level = 1;
    public BaseType type;
    public int rags, metal, wood;
    public Schematic oldBlocks;
    public boolean pasteInProgress;
    public short health = 1000, secondsWithoutRaid = Short.MAX_VALUE;
    
    public Base(final Location location, final BaseType type) {
        this.x = Math.round(location.getX());
        this.y = Math.round(location.getY());
        this.z = Math.round(location.getZ());
        this.uuid = Long.toString(x) + Long.toString(y) + Long.toString(z);
        this.world = location.getWorld().getUID();
        this.world_name = location.getWorld().getName();
        this.type = type;
        location.getWorld().getBlockAt(location).setType(Material.AIR);
        this.oldBlocks = SchematicOperator.createSchematic(this.uuid + "_OLD_TERRAIN",
                new Location(location.getWorld(),
                        location.getX() - Math.max(unclaimedSchematic.sizeX, Math.max(level1Base.sizeX, Math.max(level2Base.sizeX, Math.max(level3Base.sizeX, Math.max(level4Base.sizeX, level5Base.sizeX))))) - 5,
                        location.getY() - Math.max(unclaimedSchematic.sizeY, Math.max(level1Base.sizeY, Math.max(level2Base.sizeY, Math.max(level3Base.sizeY, Math.max(level4Base.sizeY, level5Base.sizeY))))),
                        location.getZ() - Math.max(unclaimedSchematic.sizeZ, Math.max(level1Base.sizeZ, Math.max(level2Base.sizeZ, Math.max(level3Base.sizeZ, Math.max(level4Base.sizeZ, level5Base.sizeZ)))))),
                new Location(location.getWorld(),
                        location.getX() + Math.max(unclaimedSchematic.sizeX, Math.max(level1Base.sizeX, Math.max(level2Base.sizeX, Math.max(level3Base.sizeX, Math.max(level4Base.sizeX, level5Base.sizeX))))),
                        location.getY() + Math.max(unclaimedSchematic.sizeY, Math.max(level1Base.sizeY, Math.max(level2Base.sizeY, Math.max(level3Base.sizeY, Math.max(level4Base.sizeY, level5Base.sizeY))))) + 4,
                        location.getZ() + Math.max(unclaimedSchematic.sizeZ, Math.max(level1Base.sizeZ, Math.max(level2Base.sizeZ, Math.max(level3Base.sizeZ, Math.max(level4Base.sizeZ, level5Base.sizeZ))))) + 3));
        location.getWorld().getBlockAt(location).setType((type == BaseType.SMALL ? Material.SEA_LANTERN : (type == BaseType.MEDIUM ? Material.JACK_O_LANTERN : Material.GLOWSTONE)));
        SchematicOperator.pasteSchematic(Base.unclaimedSchematic, new Location(location.getWorld(), this.x - 5, this.y + 4, this.z + 3));
    }
    
    public Base() {}
    
    public Inventory createInventory() {
        final Inventory inventory = Bukkit.createInventory(null, 45, ChatColor.AQUA + name);
        final ItemStack filler1 = new ItemStack(Material.BAMBOO);
        final ItemStack filler2 = new ItemStack(Material.SUGAR_CANE);
        final ItemStack manageMembers = new ItemStack(Material.PLAYER_HEAD);
        final ItemStack manageResources = new ItemStack(Material.PRISMARINE_SHARD);
        final ItemStack updateBase = new ItemStack(Material.SNOWBALL);
        final ItemStack disband = new ItemStack(Material.BIRCH_BOAT);
        final ArrayList<String> lore = new ArrayList<>();
        ItemMeta meta = filler1.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "");
        filler1.setItemMeta(meta);
        filler2.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "Manage Members");
        manageMembers.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "Manage Resources");
        manageResources.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "Upgrade Base");
        updateBase.setItemMeta(meta);
        meta.setDisplayName(ChatColor.DARK_RED + "Disband Base");
        lore.add(ChatColor.RESET + "" + ChatColor.BOLD + ChatColor.WHITE + "WARNING: THIS ACTION CANNOT BE UNDONE!");
        meta.setLore(lore);
        disband.setItemMeta(meta);
        inventory.setItem(0, filler1);
        inventory.setItem(1, filler1);
        inventory.setItem(2, filler1);
        inventory.setItem(3, filler1);
        inventory.setItem(4, filler1);
        inventory.setItem(5, filler1);
        inventory.setItem(6, filler1);
        inventory.setItem(7, filler1);
        inventory.setItem(8, filler1);
        inventory.setItem(9, filler1);
        inventory.setItem(10, filler1);
        inventory.setItem(11, manageMembers);
        inventory.setItem(12, filler2);
        inventory.setItem(13, manageResources);
        inventory.setItem(14, filler2);
        inventory.setItem(15, updateBase);
        inventory.setItem(16, filler2);
        inventory.setItem(17, filler1);
        inventory.setItem(18, filler1);
        inventory.setItem(19, filler1);
        inventory.setItem(20, filler1);
        inventory.setItem(21, filler1);
        inventory.setItem(22, filler1);
        inventory.setItem(23, filler1);
        inventory.setItem(24, filler1);
        inventory.setItem(25, filler1);
        inventory.setItem(26, filler1);
        inventory.setItem(27, filler1);
        inventory.setItem(28, filler1);
        inventory.setItem(29, filler1);
        inventory.setItem(30, filler1);
        inventory.setItem(31, disband);
        inventory.setItem(32, filler2);
        inventory.setItem(33, filler1);
        inventory.setItem(34, filler1);
        inventory.setItem(35, filler1);
        inventory.setItem(36, filler1);
        inventory.setItem(37, filler1);
        inventory.setItem(38, filler1);
        inventory.setItem(39, filler1);
        inventory.setItem(40, filler1);
        inventory.setItem(41, filler1);
        inventory.setItem(42, filler1);
        inventory.setItem(43, filler1);
        inventory.setItem(44, filler1);
        return inventory;
    }
    
    public Inventory createManagementInventory() {
        final Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.AQUA + name + " | Manage Members");
        final ItemStack filler1 = new ItemStack(Material.BAMBOO);
        final ItemStack filler2 = new ItemStack(Material.SUGAR_CANE);
        final ItemStack filler3 = new ItemStack(Material.ENDER_PEARL);
        final ItemStack filler4 = new ItemStack(Material.SPRUCE_BOAT);
        final ItemStack inviteMembers = new ItemStack(Material.GREEN_CONCRETE);
        final ItemStack removeMembers = new ItemStack(Material.RED_CONCRETE);
        final ItemStack changeMemberRank = new ItemStack(Material.YELLOW_CONCRETE);
        ItemMeta meta = filler1.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "");
        filler1.setItemMeta(meta);
        filler2.setItemMeta(meta);
        filler3.setItemMeta(meta);
        filler4.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "Add Members");
        inviteMembers.setItemMeta(meta);
        meta.setDisplayName(ChatColor.YELLOW + "Change Member Rank");
        changeMemberRank.setItemMeta(meta);
        meta.setDisplayName(ChatColor.RED + "Remove Members");
        removeMembers.setItemMeta(meta);
        inventory.setItem(0, filler1);
        inventory.setItem(1, filler1);
        inventory.setItem(2, filler1);
        inventory.setItem(3, filler1);
        inventory.setItem(4, filler1);
        inventory.setItem(5, filler1);
        inventory.setItem(6, filler1);
        inventory.setItem(7, filler1);
        inventory.setItem(8, filler1);
        inventory.setItem(9, filler1);
        inventory.setItem(10, filler1);
        inventory.setItem(11, inviteMembers);
        inventory.setItem(12, filler2);
        inventory.setItem(13, changeMemberRank);
        inventory.setItem(14, filler1);
        inventory.setItem(15, removeMembers);
        inventory.setItem(16, filler4);
        inventory.setItem(17, filler1);
        inventory.setItem(18, filler1);
        inventory.setItem(19, filler1);
        inventory.setItem(20, filler1);
        inventory.setItem(21, filler1);
        inventory.setItem(22, filler3);
        inventory.setItem(23, filler1);
        inventory.setItem(24, filler1);
        inventory.setItem(25, filler1);
        inventory.setItem(26, filler1);
        return inventory;
    }
    
    public Inventory createKickInventory() {
        final Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.AQUA + name + " | Remove Members");
        final ItemStack filler1 = new ItemStack(Material.BAMBOO);
        final ItemStack filler2 = new ItemStack(Material.NETHER_WART);
        final ItemStack filler3 = new ItemStack(Material.PINK_DYE);
        ItemMeta meta = filler1.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "");
        filler1.setItemMeta(meta);
        filler2.setItemMeta(meta);
        filler3.setItemMeta(meta);
        for (short i = 0; i < this.members.size(); i++) {
            final BaseMember member = this.members.get(i);
            final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            final SkullMeta skullMeta = (SkullMeta)skull.getItemMeta();
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.uuid);
            skullMeta.setOwningPlayer(offlinePlayer);
            final ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.RESET + "" + ChatColor.WHITE + member.rank.name());
            skullMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Click to remove " + offlinePlayer.getName() + " from the base.");
            skullMeta.setLore(lore);
            skull.setItemMeta(skullMeta);
            inventory.setItem((int)(10 + i + (Math.floor(i/7) * 2)), skull);
        }
        inventory.setItem(0, filler1);
        inventory.setItem(1, filler2);
        inventory.setItem(2, filler2);
        inventory.setItem(3, filler2);
        inventory.setItem(4, filler2);
        inventory.setItem(5, filler2);
        inventory.setItem(6, filler2);
        inventory.setItem(7, filler2);
        inventory.setItem(8, filler1);
        inventory.setItem(9, filler1);
        inventory.setItem(17, filler1);
        inventory.setItem(18, filler1);
        inventory.setItem(26, filler1);
        inventory.setItem(27, filler1);
        inventory.setItem(35, filler1);
        inventory.setItem(36, filler1);
        inventory.setItem(37, filler3);
        inventory.setItem(38, filler3);
        inventory.setItem(39, filler3);
        inventory.setItem(40, filler3);
        inventory.setItem(41, filler3);
        inventory.setItem(42, filler3);
        inventory.setItem(43, filler3);
        inventory.setItem(44, filler1);
        inventory.setItem(45, filler1);
        inventory.setItem(46, filler1);
        inventory.setItem(47, filler1);
        inventory.setItem(48, filler1);
        inventory.setItem(49, filler1);
        inventory.setItem(50, filler1);
        inventory.setItem(51, filler1);
        inventory.setItem(52, filler1);
        inventory.setItem(53, filler1);
        return inventory;
    }
    
    public Inventory createInviteInventory(final byte page) {
        final Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.AQUA + name + " | Add Members (Page " + (page + 1) + ')');
        final ItemStack filler1 = new ItemStack(Material.BAMBOO);
        final ItemStack filler2 = new ItemStack(Material.NETHER_WART);
        final ItemStack filler3 = new ItemStack(Material.PINK_DYE);
        final ItemStack lastPage = new ItemStack(Material.JUNGLE_BOAT);
        final ItemStack nextPage = new ItemStack(Material.IRON_ORE);
        ItemMeta meta = filler1.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "");
        filler1.setItemMeta(meta);
        filler2.setItemMeta(meta);
        filler3.setItemMeta(meta);
        meta.setDisplayName(ChatColor.RED + "Previous Page");
        lastPage.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "Next Page");
        nextPage.setItemMeta(meta);
        int size = Bukkit.getOnlinePlayers().size() - page * 21;
        final ArrayList<String> players = new ArrayList<>();
        for (final Player player : Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]))
            players.add(player.getName());
        Collections.sort(players);
        while (size > 0) {
            short i = (short)(page * 21);
            for (; i < Math.min(size, 21); i++) {
                final String name = players.get(i);
                final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                final SkullMeta skullMeta = (SkullMeta)skull.getItemMeta();
                skullMeta.setOwningPlayer(Bukkit.getPlayer(name));
                skullMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "Click to invite " + name + " to the base.");
                skull.setItemMeta(skullMeta);
                inventory.setItem((int)(10 + i + (Math.floor(i/7) * 2)), skull);
            }
            i -= page * 21;
            inventory.setItem(0, filler1);
            inventory.setItem(1, filler2);
            inventory.setItem(2, filler2);
            inventory.setItem(3, filler2);
            inventory.setItem(4, filler2);
            inventory.setItem(5, filler2);
            inventory.setItem(6, filler2);
            inventory.setItem(7, filler2);
            inventory.setItem(8, filler1);
            inventory.setItem(9, filler1);
            inventory.setItem(17, filler1);
            inventory.setItem(18, filler1);
            inventory.setItem(26, filler1);
            inventory.setItem(27, filler1);
            inventory.setItem(35, filler1);
            inventory.setItem(36, filler1);
            inventory.setItem(37, filler3);
            inventory.setItem(38, filler3);
            inventory.setItem(39, filler3);
            inventory.setItem(40, filler3);
            inventory.setItem(41, filler3);
            inventory.setItem(42, filler3);
            inventory.setItem(43, filler3);
            inventory.setItem(44, filler1);
            inventory.setItem(45, lastPage);
            inventory.setItem(46, filler1);
            inventory.setItem(47, filler1);
            inventory.setItem(48, filler1);
            inventory.setItem(49, filler1);
            inventory.setItem(50, filler1);
            inventory.setItem(51, filler1);
            inventory.setItem(52, filler1);
            inventory.setItem(53, nextPage);
            size -= 21;
        }
        return inventory;
    }
    
    public Inventory createSetRankInventory() {
        final Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.AQUA + name + " | Change Member Rank");
        final ItemStack filler1 = new ItemStack(Material.BAMBOO);
        final ItemStack filler2 = new ItemStack(Material.NETHER_WART);
        final ItemStack filler3 = new ItemStack(Material.PINK_DYE);
        ItemMeta meta = filler1.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "");
        filler1.setItemMeta(meta);
        filler2.setItemMeta(meta);
        filler3.setItemMeta(meta);
        for (short i = 0; i < this.members.size(); i++) {
            final BaseMember member = this.members.get(i);
            final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            final SkullMeta skullMeta = (SkullMeta)skull.getItemMeta();
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.uuid);
            skullMeta.setOwningPlayer(offlinePlayer);
            final ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.RESET + "" + ChatColor.WHITE + member.rank.name());
            skullMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + "Click to change " + (offlinePlayer.getName().charAt(offlinePlayer.getName().length() - 1) == 's' ? (offlinePlayer.getName() + '\'') : (offlinePlayer.getName() + "\'s")) + " rank in the base.");
            skullMeta.setLore(lore);
            skull.setItemMeta(skullMeta);
            inventory.setItem((int)(10 + i + (Math.floor(i/7) * 2)), skull);
        }
        inventory.setItem(0, filler1);
        inventory.setItem(1, filler2);
        inventory.setItem(2, filler2);
        inventory.setItem(3, filler2);
        inventory.setItem(4, filler2);
        inventory.setItem(5, filler2);
        inventory.setItem(6, filler2);
        inventory.setItem(7, filler2);
        inventory.setItem(8, filler1);
        inventory.setItem(9, filler1);
        inventory.setItem(17, filler1);
        inventory.setItem(18, filler1);
        inventory.setItem(26, filler1);
        inventory.setItem(27, filler1);
        inventory.setItem(35, filler1);
        inventory.setItem(36, filler1);
        inventory.setItem(37, filler3);
        inventory.setItem(38, filler3);
        inventory.setItem(39, filler3);
        inventory.setItem(40, filler3);
        inventory.setItem(41, filler3);
        inventory.setItem(42, filler3);
        inventory.setItem(43, filler3);
        inventory.setItem(44, filler1);
        inventory.setItem(45, filler1);
        inventory.setItem(46, filler1);
        inventory.setItem(47, filler1);
        inventory.setItem(48, filler1);
        inventory.setItem(49, filler1);
        inventory.setItem(50, filler1);
        inventory.setItem(51, filler1);
        inventory.setItem(52, filler1);
        inventory.setItem(53, filler1);
        return inventory;
    }
    
    public Inventory createSetRankSubInventory(final String name) {
        final Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.AQUA + this.name + " | Set " + (name.charAt(name.length() - 1) == 's' ? (name + '\'') : (name + "\'s")) + " Base Rank");
        final ItemStack filler1 = new ItemStack(Material.BAMBOO);
        final ItemStack filler2 = new ItemStack(Material.SUGAR_CANE);
        final ItemStack member = new ItemStack(Material.GRAY_CONCRETE);
        final ItemStack moderator = new ItemStack(Material.GREEN_CONCRETE);
        final ItemStack admin = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = filler1.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "");
        filler1.setItemMeta(meta);
        filler2.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GRAY + "Set Rank To Member");
        member.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "Set Rank To Moderator");
        moderator.setItemMeta(meta);
        meta.setDisplayName(ChatColor.RED + "Set Rank To Admin");
        admin.setItemMeta(meta);
        inventory.setItem(0, filler1);
        inventory.setItem(1, filler1);
        inventory.setItem(2, filler1);
        inventory.setItem(3, filler1);
        inventory.setItem(4, filler1);
        inventory.setItem(5, filler1);
        inventory.setItem(6, filler1);
        inventory.setItem(7, filler1);
        inventory.setItem(8, filler1);
        inventory.setItem(9, filler1);
        inventory.setItem(10, filler1);
        inventory.setItem(11, member);
        inventory.setItem(12, filler2);
        inventory.setItem(13, moderator);
        inventory.setItem(14, filler2);
        inventory.setItem(15, admin);
        inventory.setItem(16, filler2);
        inventory.setItem(17, filler1);
        inventory.setItem(18, filler1);
        inventory.setItem(19, filler1);
        inventory.setItem(20, filler1);
        inventory.setItem(21, filler1);
        inventory.setItem(22, filler1);
        inventory.setItem(23, filler1);
        inventory.setItem(24, filler1);
        inventory.setItem(25, filler1);
        inventory.setItem(26, filler1);
        return inventory;
    }
    
    public static void loadBases() {
        for (final File file : new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/BaseData/").listFiles()) {
            final Base base = new Base();
            try {
                final FileInputStream fileInputStream = new FileInputStream(file);
                final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                base.readExternal(objectInputStream);       
                objectInputStream.close();
                fileInputStream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            }
            base.addBaseAndHologram();
        }
    }
    
    public static void saveBases() {
        for (final File file : new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/BaseData/").listFiles()) {
            boolean found = false;
            for (final Base base : bases)
                if (base.uuid.equals(file.getName())) {
                    found = true;
                    break;
                }
            if (!found)
                file.delete();
        }
        for (final Base base : bases)
            try {
                final File file = new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/BaseData/" + base.uuid);
                if (!file.exists())
                    file.createNewFile();
                final FileOutputStream fileOutputStream = new FileOutputStream(file);
                final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                base.writeExternal(objectOutputStream);
                objectOutputStream.flush();
                objectOutputStream.close();
                fileOutputStream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
    }
    
    public static void checkAndAddBase(final Base base) {
        for (final Base b : bases)
            if (b.uuid.equals(base.uuid))
                return;
        base.addBaseAndHologram();
    }
    
    public static Base getBaseFromLocation(final Location location) {
        for (final Base base : bases)
            if (base.uuid.equals(Long.toString(Math.round(location.getX())) + Long.toString(Math.round(location.getY())) + Long.toString(Math.round(location.getZ()))))
                return base;
        return null;
    }
    
    public static Base getBaseFromName(final String name) {
        for (final Base base : bases)
            if (base.name != null && base.name.equals(name))
                return base;
        return null;
    }
    
    public static Inventory createListInventory(final ArrayList<Base> bases, final Player player) {
        final Inventory inventory = Bukkit.createInventory(null, 54, "You are in " + bases.size() + " bases");
        final ItemStack filler1 = new ItemStack(Material.BAMBOO);
        final ItemStack filler2 = new ItemStack(Material.SUGAR_CANE);
        final ItemStack filler3 = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = filler1.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "");
        filler1.setItemMeta(meta);
        filler2.setItemMeta(meta);
        filler3.setItemMeta(meta);
        inventory.setItem(0, filler1);
        inventory.setItem(1, filler1);
        inventory.setItem(2, filler1);
        inventory.setItem(3, filler1);
        inventory.setItem(4, filler1);
        inventory.setItem(5, filler1);
        inventory.setItem(6, filler1);
        inventory.setItem(7, filler1);
        inventory.setItem(8, filler1);
        inventory.setItem(9, filler1);
        inventory.setItem(17, filler1);
        inventory.setItem(18, filler1);
        inventory.setItem(19, filler3);
        inventory.setItem(20, filler3);
        inventory.setItem(21, filler3);
        inventory.setItem(22, filler3);
        inventory.setItem(23, filler3);
        inventory.setItem(24, filler3);
        inventory.setItem(25, filler3);
        inventory.setItem(26, filler1);
        inventory.setItem(27, filler1);
        inventory.setItem(32, filler1);
        inventory.setItem(34, filler2);
        inventory.setItem(35, filler1);
        inventory.setItem(36, filler1);
        inventory.setItem(37, filler3);
        inventory.setItem(38, filler3);
        inventory.setItem(39, filler3);
        inventory.setItem(40, filler3);
        inventory.setItem(41, filler1);
        inventory.setItem(42, filler1);
        inventory.setItem(43, filler1);
        inventory.setItem(44, filler1);
        inventory.setItem(45, filler1);
        inventory.setItem(46, filler1);
        inventory.setItem(47, filler1);
        inventory.setItem(48, filler1);
        inventory.setItem(49, filler1);
        inventory.setItem(50, filler1);
        inventory.setItem(51, filler1);
        inventory.setItem(52, filler1);
        inventory.setItem(53, filler1);
        byte smallCount = 0, mediumCount = 0, fortCount = 0;
        for (final Base base : Base.bases) {
            if (base.owner == null)
                continue;
            else if (base.owner.equals(player.getUniqueId())) {
                switch (base.type) {
                case SMALL:
                {
                    final ItemStack baseItem = new ItemStack(Material.SEA_LANTERN);
                    meta.setDisplayName(ChatColor.GREEN + base.name);
                    final ArrayList<String> lore = new ArrayList<>();
                    lore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to teleport to base. (Must be in safezone)");
                    meta.setLore(lore);
                    baseItem.setItemMeta(meta);
                    inventory.setItem(smallCount + 10, baseItem);
                    smallCount++;
                    break;
                }
                case MEDIUM:
                {
                    final ItemStack baseItem = new ItemStack(Material.JACK_O_LANTERN);
                    meta.setDisplayName(ChatColor.GREEN + base.name);
                    final ArrayList<String> lore = new ArrayList<>();
                    lore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to teleport to base. (Must be in safezone)");
                    meta.setLore(lore);
                    baseItem.setItemMeta(meta);
                    inventory.setItem(mediumCount + 28, baseItem);
                    mediumCount++;
                    break;
                }
                case FORT:
                {
                    final ItemStack baseItem = new ItemStack(Material.GLOWSTONE);
                    meta.setDisplayName(ChatColor.GREEN + base.name);
                    final ArrayList<String> lore = new ArrayList<>();
                    lore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to teleport to base. (Must be in safezone)");
                    meta.setLore(lore);
                    baseItem.setItemMeta(meta);
                    inventory.setItem(fortCount + 33, baseItem);
                    fortCount++;
                    break;
                }
                }
                continue;
            }
            for (final BaseMember m : base.members)
                if (m.uuid.equals(player.getUniqueId())) {
                    switch (base.type) {
                    case SMALL:
                    {
                        final ItemStack baseItem = new ItemStack(Material.SEA_LANTERN);
                        meta.setDisplayName(ChatColor.GREEN + base.name);
                        final ArrayList<String> lore = new ArrayList<>();
                        lore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to teleport to base.");
                        meta.setLore(lore);
                        baseItem.setItemMeta(meta);
                        inventory.setItem(smallCount + 10, baseItem);
                        smallCount++;
                        break;
                    }
                    case MEDIUM:
                    {
                        final ItemStack baseItem = new ItemStack(Material.JACK_O_LANTERN);
                        meta.setDisplayName(ChatColor.GREEN + base.name);
                        final ArrayList<String> lore = new ArrayList<>();
                        lore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to teleport to base.");
                        meta.setLore(lore);
                        baseItem.setItemMeta(meta);
                        inventory.setItem(mediumCount + 28, baseItem);
                        mediumCount++;
                        break;
                    }
                    case FORT:
                    {
                        final ItemStack baseItem = new ItemStack(Material.GLOWSTONE);
                        meta.setDisplayName(ChatColor.GREEN + base.name);
                        final ArrayList<String> lore = new ArrayList<>();
                        lore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to teleport to base.");
                        meta.setLore(lore);
                        baseItem.setItemMeta(meta);
                        inventory.setItem(fortCount + 33, baseItem);
                        fortCount++;
                        break;
                    }
                    }
                    break;
                }
        }
        return inventory;
    }
    
    public Inventory createDisbandInventory() {
        final Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.AQUA + this.name + " | Confirm Base Dissolution");
        final ItemStack filler1 = new ItemStack(Material.BAMBOO);
        final ItemStack filler2 = new ItemStack(Material.SUGAR_CANE);
        final ItemStack filler3 = new ItemStack(Material.SPRUCE_BOAT);
        final ItemStack confirm = new ItemStack(Material.GREEN_CONCRETE);
        final ItemStack decline = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = filler1.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "");
        filler1.setItemMeta(meta);
        filler2.setItemMeta(meta);
        filler3.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "Confirm Deletion");
        confirm.setItemMeta(meta);
        meta.setDisplayName(ChatColor.RED + "Decline");
        decline.setItemMeta(meta);
        inventory.setItem(0, filler1);
        inventory.setItem(1, filler1);
        inventory.setItem(2, filler1);
        inventory.setItem(3, filler1);
        inventory.setItem(4, filler1);
        inventory.setItem(5, filler1);
        inventory.setItem(6, filler1);
        inventory.setItem(7, filler1);
        inventory.setItem(8, filler1);
        inventory.setItem(9, filler1);
        inventory.setItem(10, filler1);
        inventory.setItem(11, filler1);
        inventory.setItem(12, confirm);
        inventory.setItem(13, filler2);
        inventory.setItem(14, decline);
        inventory.setItem(15, filler3);
        inventory.setItem(16, filler1);
        inventory.setItem(17, filler1);
        inventory.setItem(18, filler1);
        inventory.setItem(19, filler1);
        inventory.setItem(20, filler1);
        inventory.setItem(21, filler1);
        inventory.setItem(22, filler1);
        inventory.setItem(23, filler1);
        inventory.setItem(24, filler1);
        inventory.setItem(25, filler1);
        inventory.setItem(26, filler1);
        return inventory;
    }
    
    public Inventory createResourcesInventory() {
        final Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.AQUA + this.name + " | Manage Resources");
        final ItemStack filler1 = new ItemStack(Material.BAMBOO);
        final ItemStack filler2 = new ItemStack(Material.SUGAR_CANE);
        final ItemStack materials = new ItemStack(Material.SUNFLOWER);
        final ItemStack money = new ItemStack(Material.BEEF);
        ItemMeta meta = filler1.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "");
        filler1.setItemMeta(meta);
        filler2.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "Materials");
        materials.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "Money");
        money.setItemMeta(meta);
        inventory.setItem(0, filler1);
        inventory.setItem(1, filler1);
        inventory.setItem(2, filler1);
        inventory.setItem(3, filler1);
        inventory.setItem(4, filler1);
        inventory.setItem(5, filler1);
        inventory.setItem(6, filler1);
        inventory.setItem(7, filler1);
        inventory.setItem(8, filler1);
        inventory.setItem(9, filler1);
        inventory.setItem(10, filler1);
        inventory.setItem(11, filler1);
        inventory.setItem(12, materials);
        inventory.setItem(13, filler2);
        inventory.setItem(14, money);
        inventory.setItem(15, filler2);
        inventory.setItem(16, filler1);
        inventory.setItem(17, filler1);
        inventory.setItem(18, filler1);
        inventory.setItem(19, filler1);
        inventory.setItem(20, filler1);
        inventory.setItem(21, filler1);
        inventory.setItem(22, filler1);
        inventory.setItem(23, filler1);
        inventory.setItem(24, filler1);
        inventory.setItem(25, filler1);
        inventory.setItem(26, filler1);
        return inventory;
    }
    
    public Inventory createMaterialsInventory() {
        final Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.AQUA + this.name + " | Materials");
        final ItemStack filler1 = new ItemStack(Material.BAMBOO);
        final ItemStack filler2 = new ItemStack(Material.SUGAR_CANE);
        final ItemStack rags = new ItemStack(Material.BLUE_DYE);
        final ItemStack metal = new ItemStack(Material.ORANGE_DYE);
        final ItemStack wood = new ItemStack(Material.RED_DYE);
        ItemMeta meta = filler1.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "");
        filler1.setItemMeta(meta);
        filler2.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "You have " + this.rags + " rags");
        final ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to withdraw into base (Shift-click to withdraw entire stack)");
        meta.setLore(lore);
        rags.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "You have " + this.metal + " metal");
        lore.clear();
        lore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to withdraw into base (Shift-click to withdraw entire stack)");
        meta.setLore(lore);
        metal.setItemMeta(meta);
        meta.setDisplayName(ChatColor.GREEN + "You have " + this.wood + " wood");
        lore.clear();
        lore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to withdraw into base (Shift-click to withdraw entire stack)");
        meta.setLore(lore);
        wood.setItemMeta(meta);
        inventory.setItem(0, filler1);
        inventory.setItem(1, filler1);
        inventory.setItem(2, filler1);
        inventory.setItem(3, filler1);
        inventory.setItem(4, filler1);
        inventory.setItem(5, filler1);
        inventory.setItem(6, filler1);
        inventory.setItem(7, filler1);
        inventory.setItem(8, filler1);
        inventory.setItem(9, filler1);
        inventory.setItem(10, filler1);
        inventory.setItem(11, rags);
        inventory.setItem(12, filler2);
        inventory.setItem(13, metal);
        inventory.setItem(14, filler2);
        inventory.setItem(15, wood);
        inventory.setItem(16, filler2);
        inventory.setItem(17, filler1);
        inventory.setItem(18, filler1);
        inventory.setItem(19, filler1);
        inventory.setItem(20, filler1);
        inventory.setItem(21, filler1);
        inventory.setItem(22, filler1);
        inventory.setItem(23, filler1);
        inventory.setItem(24, filler1);
        inventory.setItem(25, filler1);
        inventory.setItem(26, filler1);
        return inventory;
    }
    
    public Inventory createMoneyInventory() {
        final Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.AQUA + this.name + " | Money");
        return inventory;
    }
    
    public Inventory createUpgradesInventory() {
        final Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.AQUA + this.name + " | Upgrade Base");
        final ItemStack filler1 = new ItemStack(Material.BAMBOO);
        final ItemStack filler2 = new ItemStack(Material.SUGAR_CANE);
        ItemMeta meta = filler1.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GRAY + "");
        filler1.setItemMeta(meta);
        filler2.setItemMeta(meta);
        inventory.setItem(0, filler1);
        inventory.setItem(1, filler1);
        inventory.setItem(2, filler1);
        inventory.setItem(3, filler1);
        inventory.setItem(4, filler1);
        inventory.setItem(5, filler1);
        inventory.setItem(6, filler1);
        inventory.setItem(7, filler1);
        inventory.setItem(8, filler1);
        inventory.setItem(9, filler1);
        inventory.setItem(10, filler1);
        inventory.setItem(11, filler1);
        inventory.setItem(12, filler1);
        if (this.level < 5) {
            final ItemStack upgrade = new ItemStack(Material.GREEN_CONCRETE);
            meta.setDisplayName(ChatColor.GREEN + "Click to upgrade base to level " + (this.level + 1));
            final ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Requirements: ");
            final short ragRequirement = (short)((this.level + 1) ^ 4);
            final short metalRequirement = (short)((this.level + 1) ^ 4);
            final short woodRequirement = (short)((this.level + 1) ^ 4);
            lore.add(ChatColor.RESET + "" + (this.rags >= ragRequirement ? ChatColor.GREEN : ChatColor.RED) + ragRequirement + " Rags");
            lore.add(ChatColor.RESET + "" + (this.metal >= metalRequirement ? ChatColor.GREEN : ChatColor.RED) + metalRequirement + " Metal");
            lore.add(ChatColor.RESET + "" + (this.wood >= woodRequirement ? ChatColor.GREEN : ChatColor.RED) + woodRequirement + " Wood");
            lore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Benefits: ");
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Max members increases to " + (this.type == BaseType.SMALL ? (this.level + 1) : (this.type == BaseType.MEDIUM ? (this.level + 1) * 2 : (this.level + 1) * 3)));
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Base\'s max HP increases to " + (this.level + 1) * 1000);
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Base\'s HP regeneration speed increases to " + Math.pow(this.level + 1, 2) + "HP/s");
            meta.setLore(lore);
            upgrade.setItemMeta(meta);
            inventory.setItem(13, upgrade);
        } else {
            final ItemStack upgrade = new ItemStack(Material.GRAY_CONCRETE);
            meta.setDisplayName(ChatColor.GREEN + "Your base has reached max level.");
            upgrade.setItemMeta(meta);
            inventory.setItem(13, upgrade);
        }
        inventory.setItem(14, filler2);
        inventory.setItem(15, filler1);
        inventory.setItem(16, filler1);
        inventory.setItem(17, filler1);
        inventory.setItem(18, filler1);
        inventory.setItem(19, filler1);
        inventory.setItem(20, filler1);
        inventory.setItem(21, filler1);
        inventory.setItem(22, filler1);
        inventory.setItem(23, filler1);
        inventory.setItem(24, filler1);
        inventory.setItem(25, filler1);
        inventory.setItem(26, filler1);
        return inventory;
    }
    
    public void disband() {
        final World world = Bukkit.getWorld(this.world);
        world.playSound(new Location(world, this.x, this.y, this.z), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.f, 1.f);
        this.level = 1;
        this.health = 1000;
        this.secondsWithoutRaid = Short.MAX_VALUE;
        for (final BaseMember member : this.members) {
            final Player player = Bukkit.getPlayer(member.uuid);
            if (player != null)
                player.sendMessage("The base you were a member of named \'" + this.name + "\' has been disbanded by the owner.");
        }
        this.name = null;
        this.attemptToRevertSchematic();
        SchematicOperator.pasteSchematic(Base.unclaimedSchematic, new Location(Bukkit.getWorld(this.world), this.x - 5, this.y + 4, this.z + 3));
        this.members.clear();
        this.updateHologram();
    }
    
    public void disbandDueToRaid() {
        final World world = Bukkit.getWorld(this.world);
        world.playSound(new Location(world, this.x, this.y, this.z), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.f, 1.f);
        this.level = 1;
        this.health = 1000;
        this.secondsWithoutRaid = Short.MAX_VALUE;
        for (final BaseMember member : this.members) {
            final Player player = Bukkit.getPlayer(member.uuid);
            if (player != null)
                player.sendMessage("The base you were a member of named \'" + this.name + "\' has been disbanded by raiders.");
        }
        final Player owner = Bukkit.getPlayer(this.owner);
        if (owner != null)
            owner.sendMessage("The base you owned with the name \'" + this.name + "\' has been disbanded by raiders.");
        this.owner = null;
        this.name = null;
        this.owner = null;
        this.attemptToRevertSchematic();
        SchematicOperator.pasteSchematic(Base.unclaimedSchematic, new Location(Bukkit.getWorld(this.world), this.x - 5, this.y + 4, this.z + 3));
        this.members.clear();
        this.updateHologram();
    }
    
    public void updateHologram() {
        this.hologram.getLines().clear();
        if (this.owner == null) {
            this.hologram.setPosition(Bukkit.getWorld(this.world), this.x - 4.5f, this.y + 3.5f, this.z - 7.5f);
            this.hologram.getLines().appendText(ChatColor.RED + "Unclaimed Base");
            this.hologram.getLines().appendText(ChatColor.RED + "Type \'/base claim <name>\' to claim this base");
            this.hologram.getLines().appendText(ChatColor.RED + "Type: " + this.type.name());
        } else {
            this.hologram.setPosition(Bukkit.getWorld(this.world), this.x - 4.5f, this.y + 6.5f, this.z - 7.5f);
            this.hologram.getLines().appendText(ChatColor.GREEN + this.name);
            this.hologram.getLines().appendText(ChatColor.GREEN + "Owner: " + Bukkit.getOfflinePlayer(this.owner).getName());
            this.hologram.getLines().appendText(ChatColor.GREEN + "Members: " + (this.members.size() + 1) + '/' + (this.type == BaseType.SMALL ? this.level : (this.type == BaseType.MEDIUM ? this.level * 2 : this.level * 3)));
            this.hologram.getLines().appendText(ChatColor.GREEN + "Type: " + this.type.name());
            this.hologram.getLines().appendText(ChatColor.GREEN + "Level: " + this.level);
            this.hologram.getLines().appendItem(new ItemStack(Material.BLUE_DYE));
            this.hologram.getLines().appendText(ChatColor.GREEN + "" + this.rags + " Rags");
            this.hologram.getLines().appendItem(new ItemStack(Material.ORANGE_DYE));
            this.hologram.getLines().appendText(ChatColor.GREEN + "" + this.metal + " Metal");
            this.hologram.getLines().appendItem(new ItemStack(Material.RED_DYE));
            this.hologram.getLines().appendText(ChatColor.GREEN + "" + this.wood + " Wood");
        }
        final Base base = this;
        for (byte i = 0; i < this.hologram.getLines().size(); i++)
            ((ClickableHologramLine)this.hologram.getLines().get(i)).setClickListener(new HologramLineClickListener() {
                @Override
                public void onClick(final HologramLineClickEvent e)
                {
                    for (final BaseMember member : base.members)
                        if (member.uuid.equals(e.getPlayer().getUniqueId())) {
                            e.getPlayer().openInventory(base.createInventory());
                            return;
                        }
                    if (base.owner != null && base.owner.equals(e.getPlayer().getUniqueId())) {
                        e.getPlayer().openInventory(base.createInventory());
                        return;
                    }
                    e.getPlayer().sendMessage("You are not a member of this base.");
                }
            });
    }
    
    public void addBaseAndHologram() {
        bases.add(this);
        World world = Bukkit.getWorld(this.world);
        if (world == null)
            world = Bukkit.createWorld(new WorldCreator(this.world_name));
        this.hologram = Main.holoapi.createHologram(new Location(world, this.x - 4.5f, this.y + (this.owner == null ? 3.5f : 6.5f), this.z - 7.5f));
        this.updateHologram();
        if (this.pasteInProgress)
            this.updateSchematic();
    }

    public void removeBaseAndHologram() {
        bases.remove(this);
        this.attemptToRevertSchematic();
        this.hologram.delete();
    }
    
    public void attemptToRevertSchematic() {
        if (this.oldBlocks != null)
            switch (this.level) {
            case 1:
                SchematicOperator.pasteSchematic(this.oldBlocks, new Location(Bukkit.getWorld(this.world), this.x - 2.5f, this.y + 2, this.z + 1.5f));
                break;
            case 2:
                SchematicOperator.pasteSchematic(this.oldBlocks, new Location(Bukkit.getWorld(this.world), this.x - 2.5f, this.y + 2.5f, this.z + 1));
                break;
            case 3:
                SchematicOperator.pasteSchematic(this.oldBlocks, new Location(Bukkit.getWorld(this.world), this.x - 2.5f, this.y + 2.5f, this.z + 1));
                break;
            case 4:
                SchematicOperator.pasteSchematic(this.oldBlocks, new Location(Bukkit.getWorld(this.world), this.x - 2.5f, this.y + 3.5f, this.z + 1));
                break;
            case 5:
                SchematicOperator.pasteSchematic(this.oldBlocks, new Location(Bukkit.getWorld(this.world), this.x - 2.5f, this.y + 3.5f, this.z + 1));
                break;
            }
    }
    
    public void levelUp() {
        this.level++;
        this.rags -= (this.level + 1) ^ 4;
        this.metal -= (this.level + 1) ^ 4;
        this.wood -= (this.level + 1) ^ 4;
        this.updateSchematic();
        this.updateHologram();
        Player player = Bukkit.getPlayer(this.owner);
        if (player != null)
            player.sendMessage("Your base has leveled up to level " + this.level + '.');
        for (final BaseMember member : this.members) {
            player = Bukkit.getPlayer(member.uuid);
            if (player != null)
                player.sendMessage("Your base has leveled up to level " + this.level + '.');
        }
    }
    
    public boolean isInUnclaimedBaseRegion(final Location location) {
        if (!location.getWorld().getUID().equals(location.getWorld().getUID()))
            return false;
        else if (this.oldBlocks != null) {
            if (location.getX() < this.x - unclaimedSchematic.sizeX - 5 || location.getX() > this.x + unclaimedSchematic.sizeX - 5 || location.getY() < this.y - unclaimedSchematic.sizeY + 4 || location.getY() > this.y + unclaimedSchematic.sizeY + 4 || location.getZ() < this.z - unclaimedSchematic.sizeZ + 3 || location.getZ() > this.z + unclaimedSchematic.sizeZ + 3)
                return false;
        } else {
            if (Math.abs(location.getX() - (this.x - 5)) > unclaimedSchematic.sizeX || Math.abs(location.getY() - (this.y + 4)) > unclaimedSchematic.sizeY || Math.abs(location.getZ() - (this.z + 3)) > unclaimedSchematic.sizeZ)
                return false;
        }
        return true;
    }
    
    public void updateSchematic() {
        final ArrayList<OfflinePlayer> players = new ArrayList<>();
        players.add(Bukkit.getOfflinePlayer(this.owner));
        for (final BaseMember member : this.members)
            players.add(Bukkit.getOfflinePlayer(member.uuid));
        switch (this.level) {
        case 1:
            this.pasteInProgress = true;
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), new Runnable() {
                @Override
                public void run() {
                    pasteInProgress = false;
                }
            }, SchematicOperator.pasteSchematicWithAnimation(Base.level1Base, new Location(Bukkit.getWorld(this.world), this.x - 5, this.y + 4, this.z + 3), players, (byte)1));
            break;
        case 2:
            this.pasteInProgress = true;
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), new Runnable() {
                @Override
                public void run() {
                    pasteInProgress = false;
                }
            }, SchematicOperator.pasteSchematicWithAnimation(Base.level2Base, new Location(Bukkit.getWorld(this.world), this.x - 5, this.y + 5, this.z + 2), players, (byte)1));
            break;
        case 3:
            this.pasteInProgress = true;
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), new Runnable() {
                @Override
                public void run() {
                    pasteInProgress = false;
                }
            }, SchematicOperator.pasteSchematicWithAnimation(Base.level3Base, new Location(Bukkit.getWorld(this.world), this.x - 5, this.y + 5, this.z + 2), players, (byte)1));
            break;
        case 4:
            this.pasteInProgress = true;
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), new Runnable() {
                @Override
                public void run() {
                    pasteInProgress = false;
                }
            }, SchematicOperator.pasteSchematicWithAnimation(Base.level4Base, new Location(Bukkit.getWorld(this.world), this.x - 5, this.y + 7, this.z + 2), players, (byte)1));
            break;
        case 5:
            this.pasteInProgress = true;
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), new Runnable() {
                @Override
                public void run() {
                    pasteInProgress = false;
                }
            }, SchematicOperator.pasteSchematicWithAnimation(Base.level5Base, new Location(Bukkit.getWorld(this.world), this.x - 5, this.y + 7, this.z + 1), players, (byte)1));
            break;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        switch (in.readShort()) {
        case 1:
            this.members = (ArrayList<BaseMember>)in.readObject();
            this.owner = (UUID)in.readObject();
            this.world = (UUID)in.readObject();
            this.x = in.readLong();
            this.y = in.readLong();
            this.z = in.readLong();
            this.uuid = (String)in.readObject();
            this.name = (String)in.readObject();
            this.world_name = (String)in.readObject();
            this.type = BaseType.SMALL;
            break;
        case 2:
            this.members = (ArrayList<BaseMember>)in.readObject();
            this.owner = (UUID)in.readObject();
            this.world = (UUID)in.readObject();
            this.x = in.readLong();
            this.y = in.readLong();
            this.z = in.readLong();
            this.uuid = (String)in.readObject();
            this.name = (String)in.readObject();
            this.world_name = (String)in.readObject();
            this.level = in.readByte();
            this.type = (BaseType)in.readObject();
            break;
        case 3:
            this.members = (ArrayList<BaseMember>)in.readObject();
            this.owner = (UUID)in.readObject();
            this.world = (UUID)in.readObject();
            this.x = in.readLong();
            this.y = in.readLong();
            this.z = in.readLong();
            this.uuid = (String)in.readObject();
            this.name = (String)in.readObject();
            this.world_name = (String)in.readObject();
            this.level = in.readByte();
            this.type = (BaseType)in.readObject();
            this.rags = in.readInt();
            this.metal = in.readInt();
            this.wood = in.readInt();
            break;
        case 4:
            this.members = (ArrayList<BaseMember>)in.readObject();
            this.owner = (UUID)in.readObject();
            this.world = (UUID)in.readObject();
            this.x = in.readLong();
            this.y = in.readLong();
            this.z = in.readLong();
            this.uuid = (String)in.readObject();
            this.name = (String)in.readObject();
            this.world_name = (String)in.readObject();
            this.level = in.readByte();
            this.type = (BaseType)in.readObject();
            this.rags = in.readInt();
            this.metal = in.readInt();
            this.wood = in.readInt();
            this.oldBlocks = (Schematic)in.readObject();
            break;
        case 5:
            this.members = (ArrayList<BaseMember>)in.readObject();
            this.owner = (UUID)in.readObject();
            this.world = (UUID)in.readObject();
            this.x = in.readLong();
            this.y = in.readLong();
            this.z = in.readLong();
            this.uuid = (String)in.readObject();
            this.name = (String)in.readObject();
            this.world_name = (String)in.readObject();
            this.level = in.readByte();
            this.type = (BaseType)in.readObject();
            this.rags = in.readInt();
            this.metal = in.readInt();
            this.wood = in.readInt();
            this.oldBlocks = (Schematic)in.readObject();
            this.pasteInProgress = in.readBoolean();
            break;
        }
    }
    
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeShort(version);
        out.writeObject(this.members);
        out.writeObject(this.owner);
        out.writeObject(this.world);
        out.writeLong(this.x);
        out.writeLong(this.y);
        out.writeLong(this.z);
        out.writeObject(this.uuid);
        out.writeObject(this.name);
        out.writeObject(this.world_name);
        out.writeByte(this.level);
        out.writeObject(this.type);
        out.writeInt(this.rags);
        out.writeInt(this.metal);
        out.writeInt(this.wood);
        out.writeObject(this.oldBlocks);
        out.writeBoolean(this.pasteInProgress);
    }
    
}
