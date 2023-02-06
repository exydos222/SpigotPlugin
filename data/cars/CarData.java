package data.cars;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("serial")
public class CarData implements Serializable {

    public UUID model;
    public ArrayList<UUID> passengers = new ArrayList<>();
    
    public CarData(final UUID model) {
        this.model = model;
    }
    
}
