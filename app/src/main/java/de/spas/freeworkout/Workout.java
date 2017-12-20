package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;


@Root
public class Workout {

    @Attribute
    private String name;

    @Attribute
    private int duration;

    @Attribute
    private int difficulty;

    @Attribute
    private int type;

    @Element
    private Endurance endurance;
    @Element
    private Standard standard;
    @Element
    private Strength strength;


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

    public Endurance getEndurance() {
        return endurance;
    }
    public Standard getStandard() {
        return standard;
    }
    public Strength getStrength() {
        return strength;
    }

}
