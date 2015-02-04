import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.flexsim.domain.energy.tso.TSOBackwardsCompatibilityTest;
import be.kuleuven.cs.gametheory.GameTest;

@RunWith(Suite.class)
@SuiteClasses({ TSOBackwardsCompatibilityTest.class, GameTest.class })
public class IntegrationTests {
}
