package objects.region;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RegionOperator {

    public static double distanceToCenter(final Region region, final Location location) {
        return location.distance(new Location(location.getWorld(), region.lowX + Math.abs(region.lowX - region.highX) / 2, region.lowY + Math.abs(region.lowY - region.highY) / 2, region.lowZ + Math.abs(region.lowZ - region.highZ) / 2));
    }
    
    public static double distanceToCenterInversed(final Region region, final Location location) {
        final int sizeX = Math.abs(region.lowX - region.highX);
        final int sizeY = Math.abs(region.lowY - region.highY);
        final int sizeZ = Math.abs(region.lowZ - region.highZ);
        return Math.abs(sizeX - Math.abs((region.lowX + sizeX / 2) - location.getX())) + Math.abs(sizeY - Math.abs((region.lowY + sizeY / 2) - location.getY())) + Math.abs(sizeZ - Math.abs((region.lowZ + sizeZ / 2) - location.getZ()));
    }
    
    public static double distanceToCenterInversedScaled(final Region region, final Location location, final float max) {
        final int sizeX = Math.abs(region.lowX - region.highX);
        final int sizeY = Math.abs(region.lowY - region.highY);
        final int sizeZ = Math.abs(region.lowZ - region.highZ);
        return (Math.abs(sizeX - Math.abs((region.lowX + sizeX / 2) - location.getX())) / sizeX * max + Math.abs(sizeY - Math.abs((region.lowY + sizeY / 2) - location.getY())) / sizeY * max + Math.abs(sizeZ - Math.abs((region.lowZ + sizeZ / 2) - location.getZ())) / sizeZ * max) / 3;
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
