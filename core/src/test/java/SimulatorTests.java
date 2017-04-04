import be.kuleuven.cs.gridflex.event.EventTest;
import be.kuleuven.cs.gridflex.simulation.SimulatorTest;
import be.kuleuven.cs.gridflex.time.ClockTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ClockTest.class, EventTest.class, SimulatorTest.class })
public class SimulatorTests {
}
