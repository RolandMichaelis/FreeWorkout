package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by uwe on 24.09.13.
 */
@Root
public class Unit {
    @Attribute
    private String name;
    @Attribute
    private int quantity;
    @Attribute
    private int exercise;

    public String getName() {
        return name;
    }
    public int getQuantity() {
        return quantity;
    }
    public int getExercise() {
        return exercise;
    }
}
