package data.schematic;

import java.io.Serializable;

import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Stairs;

@SuppressWarnings("serial")
public class StairsData implements Serializable {

	public byte facing, shape;
	public boolean half;
	
	protected StairsData(final Stairs stairs) {
		this.facing = (byte)stairs.getFacing().ordinal();
		this.shape = (byte)stairs.getShape().ordinal();
		this.half = stairs.getHalf() == Half.TOP;
	}
	
}
