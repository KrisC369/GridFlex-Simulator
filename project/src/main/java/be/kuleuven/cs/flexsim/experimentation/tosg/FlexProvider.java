package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.domain.energy.dso.DSMPartner;
import be.kuleuven.cs.flexsim.domain.energy.tso.contractual.ContractualMechanismParticipant;
import be.kuleuven.cs.flexsim.domain.util.data.PowerCapabilityBand;
import be.kuleuven.cs.flexsim.experimentation.tosg.optimal.FlexConstraints;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class FlexProvider {
    private FlexConstraints constraints;

    public FlexProvider(final int powerRate) {
        this.constraints = FlexConstraints.NOFLEX;
    }

//    @Override
//    public void signalTarget(final int timestep, final int target) {
//
//    }
//
//    @Override
//    public PowerCapabilityBand getPowerCapacity() {
//        return null;
//    }

    public FlexConstraints getActivationConstraints(){
        return constraints;
    }


}
