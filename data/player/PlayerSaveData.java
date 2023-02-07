package data.player;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import main.Main;
import objects.region.Region;

public class PlayerSaveData implements Externalizable {

    private static final short version = 2;
    
    public ArrayList<UUID> teams = new ArrayList<>();
    public Region inInfectedZone;
    
    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        switch (in.readShort()) {
        case 1:
            teams = (ArrayList<UUID>)in.readObject();
            break;
        case 2:
            teams = (ArrayList<UUID>)in.readObject();
            inInfectedZone = (Region)in.readObject();
            break;
        }
    }
    
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeShort(version);
        out.writeObject(this.teams);
        out.writeObject(inInfectedZone);
    }
    
    public void saveData(final UUID uuid) {
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(getDataFile(uuid));
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            this.writeExternal(objectOutputStream);
            objectOutputStream.flush();
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    
    public static PlayerSaveData loadData(final UUID uuid) {
        final PlayerSaveData data = new PlayerSaveData();
        try {
            final FileInputStream fileInputStream = new FileInputStream(getDataFile(uuid));
            final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            data.readExternal(objectInputStream);       
            objectInputStream.close();
            fileInputStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    public static File getDataFile(final UUID uuid) {
        return new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/PlayerData/" + uuid);
    }
    
}
