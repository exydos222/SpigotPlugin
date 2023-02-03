package data.schematic;

import java.io.Serializable;

import org.bukkit.block.data.type.Ladder;

@SuppressWarnings("serial")
public class LadderData implements Serializable {
	
	public byte facing;
	
	protected LadderData(final Ladder ladder) {
		this.facing = (byte)ladder.getFacing().ordinal();
	}
	
}
