import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.flexsim.domain.energy.tso.TSOBackwardsCompatibilityTest;

@RunWith(Suite.class)
@SuiteClasses({ TSOBackwardsCompatibilityTest.class })
public class IntegrationTests {
}
