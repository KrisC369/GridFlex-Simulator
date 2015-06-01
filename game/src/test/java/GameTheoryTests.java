import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.gametheory.GameDirectorTest;
import be.kuleuven.cs.gametheory.GameResultTest;
import be.kuleuven.cs.gametheory.HeuristicSymmetricPayoffMatrixTest;

@RunWith(Suite.class)
@SuiteClasses({ HeuristicSymmetricPayoffMatrixTest.class,
        GameDirectorTest.class, GameResultTest.class })
public class GameTheoryTests {
}
