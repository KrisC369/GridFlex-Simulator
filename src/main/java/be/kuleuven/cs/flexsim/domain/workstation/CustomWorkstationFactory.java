package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * Factory template for creating custom workstations
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public interface CustomWorkstationFactory {
    /**
     * Create an instance of the station this factory is designed to build.
     * 
     * @return The station T.
     * 
     */
    Workstation createStation();
}
