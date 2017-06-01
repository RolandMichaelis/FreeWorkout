package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by uwe on 24.09.13.
 */
@Root
public class Special {

    @Attribute
    private String name;

    @Attribute
    private int duration;

    @Attribute
    private int difficulty;

    @Element
    private Endurance endurance;
    @Element
    private Standard standard;
    @Element
    private Strength strength;
    @Attribute
    private int type;
    @ElementList
    private List<Unit> units;


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
    public List<Unit> getUnits() {
        return units;
    }

}
