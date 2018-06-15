package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
/** time: Unterscheidung ob Zeit als so schnell wie möglich (0) oder als so lang wie möglich ausgewertet wird (1)
 *  Hat Auswirkung auf PB und LT
 *  quantHidden: Mengenauswahl (0) ansonsten Zeitwertung (1) -->
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
    private int time;

    @Attribute
    private int quantHidden;

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

    public int getTime() {
        return time;
    }
    public int getQuantHidden() {
        return quantHidden;
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
