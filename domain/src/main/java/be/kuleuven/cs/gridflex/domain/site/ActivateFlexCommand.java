package be.kuleuven.cs.gridflex.domain.site;

/**
 * Represent a control schedule for effectuating flexibility steering.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */@FunctionalInterface
public interface ActivateFlexCommand {

    /**
     * Returns the reference ID from flex provider to reference a specific flex
     * profile.
     *
     * @return the ReferenceID.
     */
    long getReferenceID();
}
