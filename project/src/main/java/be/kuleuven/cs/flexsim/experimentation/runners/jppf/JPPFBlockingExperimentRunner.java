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

    JPPFBlockingExperimentRunner(String jobName, Map<String, Object> dataParams) {
        this.jobName = jobName;
        this.dataParams = dataParams;
        this.blocking = true;
    }

    @Override
    public void runExperiments(Collection<? extends Callable<Object>> experiments) {
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
        JPPFConfiguration.getProperties().setBoolean("jppf.discovery.enabled", false);
        try (JPPFClient jppfClient = new JPPFClient()) {
            jppfClient.submitJob(job);
        }
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
