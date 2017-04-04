import be.kuleuven.cs.gametheory.GameDirectorTest;
import be.kuleuven.cs.gametheory.GameResultTest;
import be.kuleuven.cs.gametheory.GameTest;
import be.kuleuven.cs.gametheory.HeuristicSymmetricPayoffMatrixTest;
import be.kuleuven.cs.gametheory.configurable.ConfigurableGameDirectorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ HeuristicSymmetricPayoffMatrixTest.class,
        GameDirectorTest.class, GameResultTest.class, GameTest.class,
        ConfigurableGameDirectorTest.class })
public class GameTheoryTests {
}
