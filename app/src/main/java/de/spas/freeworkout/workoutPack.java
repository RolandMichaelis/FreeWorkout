package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by uwe on 24.09.13.
 */
@Root
public class workoutPack {
    @Attribute
    private String name;
    @ElementList
    private List<Workout> workouts;

    public List<Workout> getWorkouts() {
        return workouts;
    }
}