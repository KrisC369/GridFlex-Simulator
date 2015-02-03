import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.gametheory.HeuristicSymmetricPayoffMatrixTest;
import be.kuleuven.cs.gametheory.experimentation.runners.MultiThreadedExperimentRunnerTest;

@RunWith(Suite.class)
@SuiteClasses({ HeuristicSymmetricPayoffMatrixTest.class,
        MultiThreadedExperimentRunnerTest.class })
public class GameTheoryTests {
}
