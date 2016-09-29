package be.kuleuven.cs.flexsim.experimentation.runners.jppf;

import be.kuleuven.cs.flexsim.experimentation.runners.ExperimentRunner;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.node.protocol.DataProvider;
import org.jppf.node.protocol.MemoryMapDataProvider;

import java.util.Collection;
import java.util.Map;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class JPPFBlockingExperimentRunner implements ExperimentRunner {
    private final String jobName;
    private final Map<String, Object> dataParams;
    private final boolean blocking;

    public JPPFBlockingExperimentRunner(String jobName, Map<String, Object> dataParams) {
        this.jobName = jobName;
        this.dataParams = dataParams;
        this.blocking = true;
    }

    @Override
    public void runExperiments(Collection<? extends Runnable> experiments) {
        DataProvider dP = new MemoryMapDataProvider();
        dataParams.entrySet().forEach(e -> dP.setParameter(e.getKey(), e.getValue()));
        JPPFJob job = new JPPFJob(jobName);
        job.setDataProvider(dP);
        job.setBlocking(blocking);

        try (JPPFClient jppfClient = new JPPFClient()) {
            job.awaitResults()
        }
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
