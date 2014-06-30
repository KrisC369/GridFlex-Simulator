package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * This workstation instance is is capable of switching between two consumption
 * modes.
 */
public interface DualModeWorkstation extends Workstation {

    /**
     * Switch high consumption mode on.
     */
    void signalHighConsumption();

    /**
     * Switch low consumption mode on.
     */
    void signalLowConsumption();
}
