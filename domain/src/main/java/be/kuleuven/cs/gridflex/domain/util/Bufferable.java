package be.kuleuven.cs.gridflex.domain.util;

import java.io.Serializable;

/**
 * Interface representing a bufferable instance.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */@FunctionalInterface
public interface Bufferable extends Serializable{

    /**
     * Sends a notification to this bufferable instance that it has entered a
     * buffer.
     */
    void notifyOfHasBeenBuffered();
}
