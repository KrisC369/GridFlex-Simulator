package be.kuleuven.cs.gridflex.domain.workstation;

import be.kuleuven.cs.gridflex.domain.resource.Resource;

/**
 * A decorator for decorating workstation instances introducing Steerability.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class SteerableStationDecorator
        extends ForwardingStationDecorator<ConfigurableWorkstation>
        implements TradeofSteerableWorkstation {

    private int speedfactor;

    SteerableStationDecorator(final ConfigurableWorkstation ws) {
        super(ws);
        getDelegate().setProcessor(new SteerableProcessor());
    }

    @Override
    public void favorSpeedOverFixedEConsumption(final int consumptionShift,
            final int speedShift) {
        getDelegate().increaseFixedECons(consumptionShift);
        getDelegate().decreaseRatedMaxVarECons(consumptionShift);
        setProcessingSpeed(getProcessingSpeed() + speedShift);
    }

    @Override
    public void favorFixedEConsumptionOverSpeed(final int consumptionShift,
            final int speedShift) {
        getDelegate().decreaseFixedECons(consumptionShift);
        getDelegate().increaseRatedMaxVarECons(consumptionShift);
        setProcessingSpeed(getProcessingSpeed() - speedShift);
    }

    @Override
    public void acceptVisitor(final WorkstationVisitor subject) {
        subject.register(this);
        super.acceptVisitor(subject);
    }

    void setProcessingSpeed(final int i) {
        this.speedfactor = i;
    }

    protected int getProcessingSpeed() {
        return speedfactor;
    }

    private final class SteerableProcessor implements Processor {
        @Override
        public void doProcessingStep(final Resource r, final int baseSteps) {
            r.process(baseSteps + getProcessingSpeed());
        }
    }
}
