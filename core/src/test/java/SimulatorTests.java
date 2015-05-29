import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.flexsim.event.EventTest;
import be.kuleuven.cs.flexsim.simulation.SimulatorTest;
import be.kuleuven.cs.flexsim.time.ClockTest;

@RunWith(Suite.class)
@SuiteClasses({ ClockTest.class, EventTest.class, SimulatorTest.class })
public class SimulatorTests {
}
