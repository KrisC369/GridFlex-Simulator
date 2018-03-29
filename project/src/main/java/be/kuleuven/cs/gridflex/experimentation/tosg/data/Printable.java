package be.kuleuven.cs.gridflex.experimentation.tosg.data;

import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@FunctionalInterface
public interface Printable {

    List getValues();
}
