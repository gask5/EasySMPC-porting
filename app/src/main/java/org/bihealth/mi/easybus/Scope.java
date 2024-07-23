package org.bihealth.mi.easybus;

import java.io.Serializable;

/**
 * Identifies the scope of a message
 *
 * @author Felix Wirth
 */
public class Scope implements Serializable {

    /**
     * SVUID
     */
    private static final long serialVersionUID = -2127462409172617852L;
    /**
     * Name of the Scope
     */
    private final String name;

    /**
     * Creates a new instance
     */
    public Scope(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Scope other = (Scope) obj;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Scope [name=" + name + "]";
    }
}
