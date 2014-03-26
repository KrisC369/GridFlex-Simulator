/**
 * 
 */
package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 * 
 */
public class CurtailableStationDecorator extends ForwardingStationDecorator
        implements Curtailable {

    private boolean curtailed;

    /**
     * Default constructor for creating this decorator.
     * @param delegate
     */
    CurtailableStationDecorator(Workstation delegate) {
        super(delegate);
        this.curtailed = false;
    }

    @Override
    public void doFullCurtailment() {
        if (isCurtailed()) {
            throw new IllegalStateException();
        }
        curtailed = true;
    }

    @Override
    public void restore() {
        if (!isCurtailed()) {
            throw new IllegalStateException();
        }
        curtailed = false;
    }

    @Override
    public boolean isCurtailed() {
        return curtailed;
    }

    /**
     * Only defers ticks when not in curtailment mode.
     */
    @Override
    public void afterTick() {
        if (!isCurtailed()) {
            super.afterTick();
        }
    }

    /**
     * Only defers ticks when not in curtailment mode.
     */
    @Override
    public void tick() {
        if (!isCurtailed()) {
            super.tick();
        }
    }
}
