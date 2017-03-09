package be.kuleuven.cs.flexsim.experimentation.tosg.utils;

import be.kuleuven.cs.flexsim.persistence.MapDBMemoizationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WriteFileCreator {

    private static final int SLEEP_TIME = 200;
    private static final Logger logger = LoggerFactory.getLogger(WriteFileCreator.class);

    public static void main(String[] args) {
        for (String file : args) {
            logger.info("Writing ouput caching files for {}", file);
            MapDBMemoizationContext.builder().setFileName(file).ensureFileExists().build();
            sleep(SLEEP_TIME);
        }
    }

    private static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
