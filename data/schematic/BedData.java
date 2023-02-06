package data.schematic;

import java.io.Serializable;

import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;

@SuppressWarnings("serial")
public class BedData implements Serializable {

    public byte facing;
    public boolean part;
    
    protected BedData(final Bed bed) {
        this.facing = (byte)bed.getFacing().ordinal();
        this.part = bed.getPart() == Part.HEAD;
    }
    
}
