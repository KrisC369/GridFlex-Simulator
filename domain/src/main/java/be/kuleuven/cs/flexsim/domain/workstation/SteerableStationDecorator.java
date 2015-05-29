package be.kuleuven.cs.flexsim.domain.workstation;

import be.kuleuven.cs.flexsim.domain.resource.Resource;

/**
 * A decorator for decorating workstation instances introducing Steerability.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * 
 */
public class SteerableStationDecorator extends
        ForwardingStationDecorator<ConfigurableWorkstation> implements
        TradeofSteerableWorkstation {

    private int speedfactor;

    SteerableStationDecorator(ConfigurableWorkstation ws) {
        super(ws);
        getDelegate().setProcessor(new SteerableProcessor());
    }

    @Override
    public void favorSpeedOverFixedEConsumption(int consumptionShift,
            int speedShift) {
        getDelegate().increaseFixedECons(consumptionShift);
        getDelegate().decreaseRatedMaxVarECons(consumptionShift);
        setProcessingSpeed(getProcessingSpeed() + speedShift);
    }

    @Override
    public void favorFixedEConsumptionOverSpeed(int consumptionShift,
            int speedShift) {
        getDelegate().decreaseFixedECons(consumptionShift);
        getDelegate().increaseRatedMaxVarECons(consumptionShift);
        setProcessingSpeed(getProcessingSpeed() - speedShift);
    }

    @Override
    public void acceptVisitor(WorkstationVisitor subject) {
        subject.register(this);
        super.acceptVisitor(subject);
    }

    void setProcessingSpeed(int i) {
        this.speedfactor = i;
    }

    protected int getProcessingSpeed() {
        return speedfactor;
    }

    private final class SteerableProcessor implements Processor {
        @Override
        public void doProcessingStep(Resource r, int baseSteps) {
            r.process(baseSteps + getProcessingSpeed());
        }
    }
}
