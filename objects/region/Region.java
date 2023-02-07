package objects.region;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import main.Main;

@SuppressWarnings("serial")
public class Region implements Serializable {

    public static final ArrayList<Region> regions = new ArrayList<>();
    
    public int lowX, lowY, lowZ, highX, highY, highZ;
    
    public String name;
    
    public Region(final String name, final Location selectionPosition1, final Location selectionPosition2) {
        this.name = name;
        this.lowX = (int)Math.min(selectionPosition1.getX(), selectionPosition2.getX());
        this.highX = (int)Math.max(selectionPosition1.getX(), selectionPosition2.getX());
        this.lowY = (int)Math.min(selectionPosition1.getY(), selectionPosition2.getY());
        this.highY = (int)Math.max(selectionPosition1.getY(), selectionPosition2.getY());
        this.lowZ = (int)Math.min(selectionPosition1.getZ(), selectionPosition2.getZ());
        this.highZ = (int)Math.max(selectionPosition1.getZ(), selectionPosition2.getZ());
    }
    
    public boolean isInsideRegion(final Player player) {
        if (player.getLocation().getX() < this.lowX && player.getLocation().getY() < this.lowY && player.getLocation().getZ() < this.lowZ && player.getLocation().getX() > this.highX && player.getLocation().getY() < this.highY && player.getLocation().getZ() > this.highZ)
            return false;
        return true;
    }
    
    public void saveRegion() {
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/RegionData/" + this.name));
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.flush();
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    
    public double distanceToCenter(final Player player) {
        return player.getLocation().distance(new Location(player.getWorld(), this.lowX + Math.abs(this.lowX - this.highX) / 2, this.lowY + Math.abs(this.lowY - this.highY) / 2, this.lowZ + Math.abs(this.lowZ - this.highZ) / 2));
    }
    
    public double distanceToCenterInversed(final Player player) {
        return (Math.abs(this.highX - this.lowX) - Math.abs((this.lowX + Math.abs(this.lowX - this.highX) / 2) - player.getLocation().getX())) + (Math.abs(this.highY - this.lowY) - Math.abs((this.lowY + Math.abs(this.lowY - this.highY) / 2) - player.getLocation().getY())) + (Math.abs(this.highZ - this.lowZ) - Math.abs((this.lowZ + Math.abs(this.lowZ - this.highZ) / 2) - player.getLocation().getZ()));
    }
    
    // TODO
    
    public double distanceToCenterScaled(final Player player, final float max) {
        return player.getLocation().distance(new Location(player.getWorld(), this.lowX + Math.abs(this.lowX - this.highX) / 2, this.lowY + Math.abs(this.lowY - this.highY) / 2, this.lowZ + Math.abs(this.lowZ - this.highZ) / 2));
    }
    
    public double distanceToCenterInversedScaled(final Player player, final float max) {
        return (Math.abs(this.highX - this.lowX) - Math.abs((this.lowX + Math.abs(this.lowX - this.highX) / 2) - player.getLocation().getX())) + (Math.abs(this.highY - this.lowY) - Math.abs((this.lowY + Math.abs(this.lowY - this.highY) / 2) - player.getLocation().getY())) + (Math.abs(this.highZ - this.lowZ) - Math.abs((this.lowZ + Math.abs(this.lowZ - this.highZ) / 2) - player.getLocation().getZ()));
    }
    
    public static Region isInsideAnyRegion(final Player player) {
        for (final Region region : regions)
            if (player.getLocation().getX() < region.lowX && player.getLocation().getY() < region.lowY && player.getLocation().getZ() < region.lowZ && player.getLocation().getX() > region.highX && player.getLocation().getY() < region.highY && player.getLocation().getZ() > region.highZ)
                continue;
            else
                return region;
        return null;
    }
    
    public static Region isInsideAnyRegionWithANameStartingWith(final Player player, final String name) {
        for (final Region region : regions)
            if (!region.name.startsWith(name))
                continue;
            else if (player.getLocation().getX() < region.lowX && player.getLocation().getY() < region.lowY && player.getLocation().getZ() < region.lowZ && player.getLocation().getX() > region.highX && player.getLocation().getY() < region.highY && player.getLocation().getZ() > region.highZ)
                continue;
            else
                return region;
        return null;
    }
    
    public static void loadRegions() {
        for (final File file : new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/BaseData/").listFiles()) {
            Region region = null;
            try {
                final FileInputStream fileInputStream = new FileInputStream(file);
                final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                region = (Region)objectInputStream.readObject();
                objectInputStream.close();
                fileInputStream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            }
            regions.add(region);
        }
    }
    
}
