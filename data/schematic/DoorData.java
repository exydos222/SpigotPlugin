package data.schematic;

import java.io.Serializable;

import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Door.Hinge;

@SuppressWarnings("serial")
public class DoorData implements Serializable {

    public byte facing;
    public boolean open, hinge, half;
    
    protected DoorData(final Door door) {
        this.facing = (byte)door.getFacing().ordinal();
        this.open = door.isOpen();
        this.hinge = door.getHinge() == Hinge.RIGHT;
        this.half = door.getHalf() == Half.TOP;
    }
    
}
