package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * A decorator for decorating workstation instances that are both Steerable and
 * curtailable.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 *         TODO test the decorator hierarchies.
 */
public class SteerableCurtailableStationDecorator extends
        SteerableStationDecorator implements CurtailableWorkstation {

    private final CurtailableWorkstation cs;

    SteerableCurtailableStationDecorator(ConfigurableWorkstation ws) {
        super(ws);
        this.cs = new CurtailableStationDecorator(this);
    }

    @Override
    public void doFullCurtailment() {
        getCurtDelegate().doFullCurtailment();

    }

    @Override
    public void restore() {
        getCurtDelegate().restore();

    }

    @Override
    public boolean isCurtailed() {
        return getCurtDelegate().isCurtailed();
    }

    /**
     * @return the curtailable instance delegate.
     */
    CurtailableWorkstation getCurtDelegate() {
        return cs;
    }

    @Override
    public void registerWith(WorkstationRegisterable subject) {
        subject.register((CurtailableWorkstation) this);
        subject.register((TradeofSteerableWorkstation) this);
    }
}
