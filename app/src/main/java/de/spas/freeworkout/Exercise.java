package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by uwe on 24.09.13.
 */
@Root
public class Exercise {

    @Attribute
    private String name;

    @Attribute
    private int duration;

    @Attribute
    private int difficulty;

    @Attribute
    private int nr;

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

    public int getNr() {
        return nr;
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
