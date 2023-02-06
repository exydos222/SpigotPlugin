package data.schematic;

import java.io.Serializable;

import org.bukkit.block.data.type.Slab;

@SuppressWarnings("serial")
public class SlabData implements Serializable {

    public byte type;
    
    protected SlabData(final Slab slab) {
        this.type = (byte)slab.getType().ordinal();
    }
    
}
