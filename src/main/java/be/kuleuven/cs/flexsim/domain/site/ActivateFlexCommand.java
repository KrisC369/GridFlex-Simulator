package be.kuleuven.cs.flexsim.domain.site;

/**
 * Represent a control schedule for effectuating flexibility steering.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface ActivateFlexCommand {

    /**
     * Returns the reference ID from flex provider to reference a specific flex
     * profile.
     * 
     * @return the ReferenceID.
     */
    long getReferenceID();

    /**
     * Indicates whether this command is meant to increase or decrease
     * consumption.
     * 
     * @return true if curtailment is wanted.
     */
    boolean isDownFlexCommand();
}
