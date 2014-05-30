package be.kuleuven.cs.flexsim.domain.workstation;

import static com.google.common.base.Preconditions.checkArgument;
import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;

/**
 * A steerable station implementation for the steerable workstation interface.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
class SteerableStationImpl extends WorkstationImpl implements
        SteerableWorkstation {

    private int speedfactor;

    /**
     * Constructor for the steerable station
     */
    SteerableStationImpl(Buffer<Resource> bufferIn, Buffer<Resource> bufferOut,
            int idle, int working, int capacity, ConsumptionModel model) {
        super(bufferIn, bufferOut, idle, working, capacity, model);
        this.speedfactor = 0;
    }

    @Override
    public void favorSpeedOverFixedEConsumption(int consumptionShift,
            int speedShift) {
        checkArgument(consumptionShift < getMaxVarECons(),
                "cant shift more towards speed than available.");
        setFixedECons(getFixedECons() + consumptionShift);
        setMaxVarECons(getMaxVarECons() - consumptionShift);
        setProcessingSpeed(getProcessingSpeed() + speedShift);
    }

    @Override
    public void favorFixedEConsumptionOverSpeed(int consumptionShift,
            int speedShift) {
        checkArgument(consumptionShift < getFixedECons(),
                "cant shift more towards low consumption than available.");
        setFixedECons(getFixedECons() - consumptionShift);
        setMaxVarECons(getMaxVarECons() + consumptionShift);
        setProcessingSpeed(getProcessingSpeed() - speedShift);
    }

    @Override
    void doProcessingStep(Resource r, int basesteps) {
        r.process(basesteps + getProcessingSpeed());
    }

    private void setProcessingSpeed(int i) {
        this.speedfactor = i;
    }

    private int getProcessingSpeed() {
        return speedfactor;
    }
}