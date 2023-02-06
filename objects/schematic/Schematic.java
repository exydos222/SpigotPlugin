package objects.schematic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.bukkit.plugin.java.JavaPlugin;

import data.schematic.BlockData;
import main.Main;

@SuppressWarnings("serial")
public class Schematic implements Serializable {

    public ArrayList<BlockData> blocks = new ArrayList<>();
    public String name;
    public long sizeX, sizeY, sizeZ;
    
    public Schematic(final String name, final long sizeX, final long sizeY, final long sizeZ) {
        this.name = name;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }
    
    public static Schematic loadSchematic(final String name) {
        Schematic schematic = null;
        try {
            final FileInputStream fileInputStream = new FileInputStream(new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/SchematicData/" + name));
            final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            schematic = (Schematic)objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
        return schematic;
    }
    
    public void saveSchematic() {
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/SchematicData/" + this.name));
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
