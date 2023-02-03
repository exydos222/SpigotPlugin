package data.schematic;

import java.io.Serializable;

import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.TrapDoor;

@SuppressWarnings("serial")
public class TrapDoorData implements Serializable {

	public byte facing;
	public boolean open, half;
	
	protected TrapDoorData(final TrapDoor trapdoor) {
		this.facing = (byte)trapdoor.getFacing().ordinal();
		this.open = trapdoor.isOpen();
		this.half = trapdoor.getHalf() == Half.TOP;
	}
	
}
