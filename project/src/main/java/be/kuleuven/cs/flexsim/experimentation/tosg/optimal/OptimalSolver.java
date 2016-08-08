package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import autovalue.shaded.com.google.common.common.collect.Lists;
import be.kuleuven.cs.flexsim.experimentation.tosg.FlexProvider;
import be.kuleuven.cs.flexsim.simulation.SimulationComponent;
import be.kuleuven.cs.flexsim.simulation.SimulationContext;
import net.sf.jmpi.main.MpProblem;
import net.sf.jmpi.main.MpResult;
import net.sf.jmpi.main.MpSolver;

import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class OptimalSolver implements SimulationComponent{
    private final List<FlexProvider> providers;

    public OptimalSolver(){
        providers = Lists.newArrayList();
    }
    public void registerFlexProvider(FlexProvider p) {
        providers.add(p);
    }

    @Override
    public void afterTick(int t) {

    }

    @Override
    public void tick(int t) {
        MpSolver s = getSolver();
        s.add(getProblem());
        MpResult result = s.solve();
        processResults(result);
    }

    protected abstract void processResults(MpResult result);

    @Override
    public List<? extends SimulationComponent> getSimulationSubComponents() {
        return null;
    }

    @Override
    public void initialize(SimulationContext context) {
    }

    public abstract MpSolver getSolver();

    public abstract MpProblem getProblem();

}
