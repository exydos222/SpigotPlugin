package objects.schematic;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Door.Hinge;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.plugin.java.JavaPlugin;

import data.schematic.BlockData;
import main.Main;

public class SchematicOperator {

    public static void pasteSchematic(final Schematic schematic, final Location center) {
        for (final BlockData block : schematic.blocks) {
            final Block b = center.getWorld().getBlockAt(center.getBlockX() + block.x, center.getBlockY() + block.y, center.getBlockZ() + block.z);
            final Material material = Material.values()[block.material];
            if (b.getType() == Material.SEA_LANTERN || b.getType() == Material.JACK_O_LANTERN || b.getType() == Material.GLOWSTONE)
                continue;
            if (block.doorData != null) {
                b.setType(material, false);
                final Door door = (Door)b.getBlockData();
                door.setHalf(block.doorData.half ? Half.TOP : Half.BOTTOM);
                door.setFacing(BlockFace.values()[block.doorData.facing]);
                door.setOpen(block.doorData.open);
                door.setHinge(block.doorData.hinge ? Hinge.RIGHT : Hinge.LEFT);
                b.setBlockData(door);
            } else if (block.bedData != null) {
                b.setType(material, false);
                final Bed bed = (Bed)b.getBlockData();
                bed.setFacing(BlockFace.values()[block.bedData.facing]);
                bed.setPart(block.bedData.part ? Part.HEAD : Part.FOOT);
                b.setBlockData(bed);
            } else if (block.trapDoorData != null) {
                b.setType(material, false);
                final TrapDoor trapDoor = (TrapDoor)b.getBlockData();
                trapDoor.setFacing(BlockFace.values()[block.trapDoorData.facing]);
                trapDoor.setHalf(block.trapDoorData.half ? Half.TOP : Half.BOTTOM);
                trapDoor.setOpen(block.trapDoorData.open);
                b.setBlockData(trapDoor);
            } else if (block.ladderData != null) {
                b.setType(material, false);
                final Ladder ladder = (Ladder)b.getBlockData();
                ladder.setFacing(BlockFace.values()[block.ladderData.facing]);
                b.setBlockData(ladder);
            } else if (block.slabData != null) {
                b.setType(material, false);
                final Slab slab = (Slab)b.getBlockData();
                slab.setType(Type.values()[block.slabData.type]);
                b.setBlockData(slab);
            } else if (block.stairsData != null) {
                b.setType(material, false);
                final Stairs stairs = (Stairs)b.getBlockData();
                stairs.setFacing(BlockFace.values()[block.stairsData.facing]);
                stairs.setShape(Shape.values()[block.stairsData.facing]);
                stairs.setHalf(block.stairsData.half ? Half.TOP : Half.BOTTOM);
                b.setBlockData(stairs);
            } else
                b.setType(material);
        }
    }
    
    public static int pasteSchematicWithAnimation(final Schematic schematic, final Location center, final ArrayList<OfflinePlayer> players, final byte delay) {
        int i = delay;
        for (final BlockData block : schematic.blocks) {
            final Block b = center.getWorld().getBlockAt(center.getBlockX() + block.x, center.getBlockY() + block.y, center.getBlockZ() + block.z);
            final Material material = Material.values()[block.material];
            if (b.getType() == material || b.getType() == Material.SEA_LANTERN || b.getType() == Material.JACK_O_LANTERN || b.getType() == Material.GLOWSTONE)
                continue;
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), new Runnable() {
                @Override
                public void run() {
                    if (material == Material.AIR)
                        for (final OfflinePlayer player : players) {
                            if (player.isOnline())
                                player.getPlayer().playSound(b.getLocation(), b.getBlockData().getSoundGroup().getBreakSound(), b.getBlockData().getSoundGroup().getVolume() * 2, b.getBlockData().getSoundGroup().getPitch());
                        }
                    else
                        for (final OfflinePlayer player : players) {
                            if (player.isOnline())
                                player.getPlayer().playSound(b.getLocation(), material.createBlockData().getSoundGroup().getPlaceSound(), material.createBlockData().getSoundGroup().getVolume() * 2, material.createBlockData().getSoundGroup().getPitch());
                        }
                    if (block.doorData != null) {
                        b.setType(material, false);
                        final Door door = (Door)b.getBlockData();
                        door.setHalf(block.doorData.half ? Half.TOP : Half.BOTTOM);
                        door.setFacing(BlockFace.values()[block.doorData.facing]);
                        door.setOpen(block.doorData.open);
                        door.setHinge(block.doorData.hinge ? Hinge.RIGHT : Hinge.LEFT);
                        b.setBlockData(door);
                    } else if (block.bedData != null) {
                        b.setType(material, false);
                        final Bed bed = (Bed)b.getBlockData();
                        bed.setFacing(BlockFace.values()[block.bedData.facing]);
                        bed.setPart(block.bedData.part ? Part.HEAD : Part.FOOT);
                        b.setBlockData(bed);
                    } else if (block.trapDoorData != null) {
                        b.setType(material, false);
                        final TrapDoor trapDoor = (TrapDoor)b.getBlockData();
                        trapDoor.setFacing(BlockFace.values()[block.trapDoorData.facing]);
                        trapDoor.setHalf(block.trapDoorData.half ? Half.TOP : Half.BOTTOM);
                        trapDoor.setOpen(block.trapDoorData.open);
                        b.setBlockData(trapDoor);
                    } else if (block.ladderData != null) {
                        b.setType(material, false);
                        final Ladder ladder = (Ladder)b.getBlockData();
                        ladder.setFacing(BlockFace.values()[block.ladderData.facing]);
                        b.setBlockData(ladder);
                    } else if (block.slabData != null) {
                        b.setType(material, false);
                        final Slab slab = (Slab)b.getBlockData();
                        slab.setType(Type.values()[block.slabData.type]);
                        b.setBlockData(slab);
                    } else if (block.stairsData != null) {
                        b.setType(material, false);
                        final Stairs stairs = (Stairs)b.getBlockData();
                        stairs.setFacing(BlockFace.values()[block.stairsData.facing]);
                        stairs.setShape(Shape.values()[block.stairsData.facing]);
                        stairs.setHalf(block.stairsData.half ? Half.TOP : Half.BOTTOM);
                        b.setBlockData(stairs);
                    } else
                        b.setType(material);
                }
            }, i);
            i+=delay;
        }
        return i;
    }

    public static Schematic createSchematic(final String name, final Location selectionPosition1, final Location selectionPosition2) {
        final int minX = (int)Math.min(selectionPosition1.getX(), selectionPosition2.getX());
        final int maxX = (int)Math.max(selectionPosition1.getX(), selectionPosition2.getX());
        final int minY = (int)Math.min(selectionPosition1.getY(), selectionPosition2.getY());
        final int maxY = (int)Math.max(selectionPosition1.getY(), selectionPosition2.getY());
        final int minZ = (int)Math.min(selectionPosition1.getZ(), selectionPosition2.getZ());
        final int maxZ = (int)Math.max(selectionPosition1.getZ(), selectionPosition2.getZ());
        final int offsetX = minX + (maxX - minX) / 2;
        final int offsetY = minY + (maxY - minY) / 2;
        final int offsetZ = minZ + (maxZ - minZ) / 2;
        final Schematic schematic = new Schematic(name, maxX - offsetX, maxY - offsetY, maxZ - offsetZ);
        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++)
                    schematic.blocks.add(new BlockData(selectionPosition1.getWorld().getBlockAt(x, y, z), x - offsetX, y - offsetY, z - offsetZ));
        return schematic;
    }
    
}
