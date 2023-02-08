package data.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import objects.bases.Base;
import objects.bases.BaseInvite;

public class PlayerSessionData {
    
    public static HashMap<UUID, PlayerSessionData> PlayerData = new HashMap<>();
    
    public PlayerSaveData savedata = new PlayerSaveData();
    public ArrayList<UUID> activeTeamInvites = new ArrayList<>(), carPassengers = new ArrayList<>();
    public ArrayList<BaseInvite> activeBaseInvites = new ArrayList<>();
    public ArrayList<Base> activeBaseDisollutionRequests = new ArrayList<>();
    public boolean shouldRespondToInventoryCloseEvents = true, combatLogged, isDrivingCar;
    public Location selectionPosition1, selectionPosition2;
    public float vehicleAcceleration;
    public UUID carDriver, oldCarDriver;
    public BukkitTask previousCombatLogTask;
    
    public PlayerSessionData(final PlayerSaveData data) {
        this.savedata = data;
    }

    public PlayerSessionData() {}
    
}
