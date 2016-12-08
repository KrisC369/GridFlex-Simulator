import be.kuleuven.cs.flexsim.domain.DomainScenarioTests;
import be.kuleuven.cs.gametheory.GameScenarioTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ GameScenarioTest.class, DomainScenarioTests.class })
public class ScenarioTests {
}