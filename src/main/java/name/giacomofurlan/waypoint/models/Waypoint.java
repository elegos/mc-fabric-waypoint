package name.giacomofurlan.waypoint.models;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class Waypoint {
    private final String name;
    private final Vec3d position;
    private final Identifier dimension;
    
    public Waypoint(String name, Vec3d position, Identifier dimension) {
        this.name = name;
        this.position = position;
        this.dimension = dimension;
    }

    public String getName() { return name; }
    public Vec3d getPosition() { return position; }
    public Identifier getDimension() { return dimension; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Waypoint)) return false;
        
        return name.equalsIgnoreCase(((Waypoint)o).name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
}
