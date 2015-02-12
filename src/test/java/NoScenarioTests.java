import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.gametheory.experimentation.TimingTest;

@RunWith(Suite.class)
@SuiteClasses({ SimulatorTests.class, DomainTests.class, GameTheoryTests.class,
        IntegrationTests.class, TimingTest.class })
public class NoScenarioTests {
}
