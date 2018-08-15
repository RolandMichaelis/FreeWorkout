package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 *
 */
@Root
public class workoutNewPack {
    @Attribute
    private String name;
    @ElementList
    private List<WorkoutNew> workouts;

    public workoutNewPack() {
    }
    public String getName() {
        return name;
    }

    public List<WorkoutNew> getWorkouts() {
        return workouts;
    }
}