package data.cars;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import main.Main;

@SuppressWarnings("serial")
public class Cars implements Serializable {

	public static HashMap<UUID, CarData> CarData;
	
	public static void saveCars() {
		try {
		    final FileOutputStream fileOutputStream = new FileOutputStream(new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/CarData"));
		    final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		    objectOutputStream.writeObject(CarData);
		    objectOutputStream.flush();
		    objectOutputStream.close();
		    fileOutputStream.close();
		} catch (final IOException e) {
		    e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void loadCars() {
		final File dataFile = new File(JavaPlugin.getPlugin(Main.class).getDataFolder() + "/CarData");
		if (!dataFile.exists()) {
			CarData = new HashMap<>();
			return;
		}
		HashMap<UUID, CarData> cars = null;
		try {
		    final FileInputStream fileInputStream = new FileInputStream(dataFile);
		    final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		    cars = (HashMap<UUID, CarData>)objectInputStream.readObject();
		    objectInputStream.close();
		    fileInputStream.close();
		} catch (final IOException e) {
		    e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
		CarData = cars;
	}

}