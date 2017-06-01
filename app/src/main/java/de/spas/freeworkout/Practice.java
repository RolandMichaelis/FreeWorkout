package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by uwe on 24.09.13.
 */
@Root
public class Practice {
    @Attribute(required=false)
    private String name;
    @Attribute(required=false)
    private int quantity;

    public String getName() {
        return name;
    }
    public int getQuantity() {
        return quantity;
    }
}
