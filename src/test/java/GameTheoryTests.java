import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.gametheory.GameDirectorTest;
import be.kuleuven.cs.gametheory.HeuristicSymmetricPayoffMatrixTest;
import be.kuleuven.cs.gametheory.experimentation.runners.local.MultiThreadedExperimentRunnerTest;

@RunWith(Suite.class)
@SuiteClasses({ HeuristicSymmetricPayoffMatrixTest.class,
        MultiThreadedExperimentRunnerTest.class, GameDirectorTest.class })
public class GameTheoryTests {
}
