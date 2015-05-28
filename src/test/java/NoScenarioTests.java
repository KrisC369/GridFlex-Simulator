import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.flexsim.experimentation.techReport.TimingTest;

@RunWith(Suite.class)
@SuiteClasses({ SimulatorTests.class, DomainTests.class, GameTheoryTests.class,
        IntegrationTests.class, TimingTest.class })
public class NoScenarioTests {
}
