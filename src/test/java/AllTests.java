import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.flexsim.simulation.SimulatorTest;

@RunWith(Suite.class)
@SuiteClasses({ SimulatorTest.class, UnitTests.class, ScenarioTest.class, })
public class AllTests {
}
