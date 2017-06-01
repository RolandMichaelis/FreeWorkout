package de.spas.freeworkout;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by uwe on 24.09.13.
 */
@Root
public class specialPack {
    @Attribute
    private String name;
    @ElementList
    private List<Special> specials;

    public List<Special> getSpecials() {
        return specials;
    }
}