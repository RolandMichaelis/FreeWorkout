package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 *
 */
@Root
public class workoutPack {
    @Attribute
    private String name;
    @ElementList
    private List<Workout> workouts;
    public String getName() {
        return name;
    }
    public List<Workout> getWorkouts() {
        return workouts;
    }
}