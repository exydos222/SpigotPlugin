package data.schematic;

import java.io.Serializable;

import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;

@SuppressWarnings("serial")
public class BlockData implements Serializable {

	public int x, y, z, offsetX, offsetY, offsetZ;
	public short material;
	public DoorData doorData;
	public BedData bedData;
	public TrapDoorData trapDoorData;
	public LadderData ladderData;
	public SlabData slabData;
	public StairsData stairsData;
	
	public BlockData(final Block block, final int x, final int y, final int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.material = (short)block.getType().ordinal();
		if (block.getBlockData() instanceof Door)
			this.doorData = new DoorData((Door)block.getBlockData());
		else if (block.getBlockData() instanceof Bed)
			this.bedData = new BedData((Bed)block.getBlockData());
		else if (block.getBlockData() instanceof TrapDoor)
			this.trapDoorData = new TrapDoorData((TrapDoor)block.getBlockData());
		else if (block.getBlockData() instanceof Ladder)
			this.ladderData = new LadderData((Ladder)block.getBlockData());
		else if (block.getBlockData() instanceof Slab)
			this.slabData = new SlabData((Slab)block.getBlockData());
		else if (block.getBlockData() instanceof Stairs)
			this.stairsData = new StairsData((Stairs)block.getBlockData());
	}
	
}
