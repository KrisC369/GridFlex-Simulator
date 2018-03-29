import be.kuleuven.cs.gridflex.event.EventTest;
import be.kuleuven.cs.gridflex.simulation.SimulatorTest;
import be.kuleuven.cs.gridflex.time.ClockTest;
import be.kuleuven.cs.gridflex.util.ArrayUtils;
import be.kuleuven.cs.gridflex.util.CollectionUtils;
import be.kuleuven.cs.gridflex.util.MathUtils;
import be.kuleuven.cs.gridflex.util.NPermuteAndCombiner;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ClockTest.class, EventTest.class, SimulatorTest.class, ArrayUtils.class,
        CollectionUtils.class, MathUtils.class, NPermuteAndCombiner.class})
public class SimulatorTests {
}
