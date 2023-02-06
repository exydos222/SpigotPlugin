package data.blocks;

import org.bukkit.Location;
import org.bukkit.Material;

public class MinedBlockData {

    public Material oldMaterial;
    public Location location;
    
    public MinedBlockData(final Material oldMaterial, final Location location) {
        this.oldMaterial = oldMaterial;
        this.location = location;
    }
    
}
