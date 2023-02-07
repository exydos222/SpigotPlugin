package objects.region;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RegionOperator {

    public static double distanceToCenter(final Region region, final Player player) {
        return player.getLocation().distance(new Location(player.getWorld(), region.lowX + Math.abs(region.lowX - region.highX) / 2, region.lowY + Math.abs(region.lowY - region.highY) / 2, region.lowZ + Math.abs(region.lowZ - region.highZ) / 2));
    }
    
    public static double distanceToCenterInversed(final Region region, final Player player) {
        return (Math.abs(region.highX - region.lowX) - Math.abs((region.lowX + Math.abs(region.lowX - region.highX) / 2) - player.getLocation().getX())) + (Math.abs(region.highY - region.lowY) - Math.abs((region.lowY + Math.abs(region.lowY - region.highY) / 2) - player.getLocation().getY())) + (Math.abs(region.highZ - region.lowZ) - Math.abs((region.lowZ + Math.abs(region.lowZ - region.highZ) / 2) - player.getLocation().getZ()));
    }
    
    // TODO
    
    public static double distanceToCenterScaled(final Region region, final Player player, final float max) {
        return player.getLocation().distance(new Location(player.getWorld(), region.lowX + Math.abs(region.lowX - region.highX) / 2, region.lowY + Math.abs(region.lowY - region.highY) / 2, region.lowZ + Math.abs(region.lowZ - region.highZ) / 2));
    }
    
    public static double distanceToCenterInversedScaled(final Region region, final Player player, final float max) {
        return (Math.abs(region.highX - region.lowX) - Math.abs((region.lowX + Math.abs(region.lowX - region.highX) / 2) - player.getLocation().getX())) + (Math.abs(region.highY - region.lowY) - Math.abs((region.lowY + Math.abs(region.lowY - region.highY) / 2) - player.getLocation().getY())) + (Math.abs(region.highZ - region.lowZ) - Math.abs((region.lowZ + Math.abs(region.lowZ - region.highZ) / 2) - player.getLocation().getZ()));
    }
    
    public static Region isInsideAnyRegion(final Player player) {
        for (final Region region : Region.regions)
            if (player.getLocation().getX() < region.lowX || player.getLocation().getY() < region.lowY || player.getLocation().getZ() < region.lowZ || player.getLocation().getX() > region.highX || player.getLocation().getY() < region.highY || player.getLocation().getZ() > region.highZ)
                continue;
            else
                return region;
        return null;
    }
    
    public static Region isInsideAnyRegionWithANameStartingWith(final Player player, final String name) {
        for (final Region region : Region.regions)
            if (!region.name.startsWith(name))
                continue;
            else if (player.getLocation().getX() < region.lowX || player.getLocation().getY() < region.lowY || player.getLocation().getZ() < region.lowZ || player.getLocation().getX() > region.highX || player.getLocation().getY() > region.highY || player.getLocation().getZ() > region.highZ)
                continue;
            else
                return region;
        return null;
    }
    
}
