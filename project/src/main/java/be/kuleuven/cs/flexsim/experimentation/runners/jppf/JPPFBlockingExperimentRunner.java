package be.kuleuven.cs.flexsim.experimentation.runners.jppf;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import org.eclipse.jdt.annotation.Nullable;
import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.node.protocol.DataProvider;
import org.jppf.node.protocol.MemoryMapDataProvider;
import org.jppf.node.protocol.Task;
import org.jppf.utils.JPPFConfiguration;
import org.jppf.utils.configuration.JPPFProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class JPPFBlockingExperimentRunner implements ExperimentRunner {
    private final String jobName;
    private final Map<String, Object> dataParams;
    private final boolean blocking;
    @Nullable
    private JPPFJob job;
    private static final Logger logger = LoggerFactory
            .getLogger(JPPFBlockingExperimentRunner.class);

    JPPFBlockingExperimentRunner(String jobName, Map<String, Object> dataParams) {
        this.jobName = jobName;
        this.dataParams = dataParams;
        this.blocking = true;
    }

    @Override
    public void runExperiments(Collection<? extends Callable<Object>> experiments) {
        if (logger.isInfoEnabled()) {
            logger.info("Running experiments on jppf cluster");
        }
        DataProvider dP = new MemoryMapDataProvider();
        dataParams.entrySet().forEach(e -> dP.setParameter(e.getKey(), e.getValue()));
        job = new JPPFJob(jobName);
        job.setDataProvider(dP);
        job.setBlocking(blocking);
        experiments.forEach(r -> {
            try {
                job.add(r);
            } catch (JPPFException e) {
                e.printStackTrace();
            }
        });

        if (logger.isInfoEnabled()) {
            logger.info("JPPF Client connecting to " + JPPFConfiguration.getProperties()
                    .get(JPPFProperties.SERVER_HOST) + ":" + JPPFConfiguration.getProperties()
                    .get(JPPFProperties.SERVER_PORT));
        }
        try (JPPFClient jppfClient = new JPPFClient()) {
            if (logger.isInfoEnabled()) {
                logger.info("Connected to manager: " + jppfClient.getJobManager().toString());
                logger.info("Submitting job: " + job);
            }
            jppfClient.submitJob(job);
            if (logger.isInfoEnabled()) {
                logger.info("Jobs submitted!");
            }
        }
    }

    private void attemptToReadConfig() {
        JPPFConfiguration.getProperties().setBoolean("jppf.discovery.enabled", false);
        JPPFConfiguration.getProperties().setString("jppf.drivers", "driver-1");
        JPPFConfiguration.getProperties().setString("driver-1.jppf.server.host", "localhost");
        JPPFConfiguration.getProperties().setString("driver-1.jppf.server.port", "12111");
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public List<Task<?>> waitAndGetResults() {
        if (job == null) {
            throw new IllegalStateException(
                    "Run experiments before attempting to get the results.");
        }
        return job.awaitResults();
    }
}
