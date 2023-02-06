package objects.region;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import main.Main;

@SuppressWarnings("serial")
public class Region implements Serializable {

	public int lowX, lowY, lowZ, highX, highY, highZ;
	
	public String name;
	
	public Region(final String name, final Location selectionPosition1, final Location selectionPosition2) {
		this.name = name;
		this.lowX = (int)Math.min(selectionPosition1.getX(), selectionPosition2.getX());
		this.lowY = (int)Math.max(selectionPosition1.getX(), selectionPosition2.getX());
		this.lowZ = (int)Math.min(selectionPosition1.getY(), selectionPosition2.getY());
		this.highX = (int)Math.max(selectionPosition1.getY(), selectionPosition2.getY());
		this.highY = (int)Math.min(selectionPosition1.getZ(), selectionPosition2.getZ());
		this.highZ = (int)Math.max(selectionPosition1.getZ(), selectionPosition2.getZ());
	}
	
	public boolean isInsideRegion(final Player player) {
		if (player.getLocation().getX() < this.lowX && player.getLocation().getY() < this.lowY && player.getLocation().getZ() < this.lowZ && player.getLocation().getX() > this.highX && player.getLocation().getY() < this.highY && player.getLocation().getZ() > this.highZ)
			return false;
		return true;
	}
	
	public static Region loadRegion(final String name) {
		Region region = null;
		try {
		    final FileInputStream fileInputStream = new FileInputStream(new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/RegionData/" + name));
		    final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		    region = (Region)objectInputStream.readObject();
		    objectInputStream.close();
		    fileInputStream.close();
		} catch (final IOException e) {
		    e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
		return region;
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
	
}
