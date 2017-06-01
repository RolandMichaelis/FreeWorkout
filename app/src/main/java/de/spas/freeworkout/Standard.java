package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by uwe on 24.09.13.
 */
@Root
public class Standard {
    @Attribute
    private int skill;
    @Attribute(required=false)
    private int points;
    @ElementList(required=false,entry="round", inline = true)
    List<Round> round;

    public int getSkill() {
        return skill;
    }
    public int getPoints() {
        return points;
    }
    public List<Round> getRounds() {
        return round;
    }

}
