package be.kuleuven.cs.flexsim.persistence;

import com.igormaznitsa.jute.annotations.JUteTest;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static be.kuleuven.cs.flexsim.persistence.MapDBMemoizationContextTest.parallelTestImpl;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MapDBMemoizationContextTest_Issues {

    private MapDBMemoizationContext<String, String> target;
    private static final Logger logger = LoggerFactory
            .getLogger(MapDBMemoizationContextTest_Issues.class);
    private static final String NAME_DB = "juteTest.db";

    @Before
    public void setUp() throws Exception {
        target = MapDBMemoizationContext.createDefault(NAME_DB);
    }

    @After
    public void tearDown() throws Exception {
    }

    //    @AfterClass
    //    public static void cleanup() throws Exception {
    //        MapDBMemoizationContext<String, String> target = MapDBMemoizationContext
    // .createDefault();
    //        target.resetStore();
    //    }

    private void reset() {
        target.resetStore();
    }

    @JUteTest(order = 1, jvm = "java", printConsole = true)
    public void parallell_1_JuteTest() throws Exception {
        parallelTestImpl(0, 4, 250, false, NAME_DB);

    }

    @JUteTest(order = 1, jvm = "java", printConsole = true)
    public void parallell_2_JuteTest() throws Exception {
        parallelTestImpl(1000, 4, 250, false, NAME_DB);

    }

    @JUteTest(order = 1, jvm = "java", printConsole = true)
    public void parallell_3_JuteTest() throws Exception {
        parallelTestImpl(2000, 4, 250, false, NAME_DB);
    }

}