package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;


@Root
public class WorkoutNew {

    @Attribute
    private String name;

    @Attribute
    private int duration;

    @Attribute
    private int difficulty;

    @Attribute
    private int type;

    @Attribute
    private int skill;

    @Attribute
    private int points;
    @ElementList(required=false,entry="round", inline = true)
    List<Round> round;

    public List<Round> getRounds() {
        return round;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getType() {
        return type;
    }

    public int getSkill() {
        return skill;
    }

    public int getPoints() {
        return points;
    }



}
