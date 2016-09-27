package be.kuleuven.cs.flexsim.experimentation.tosg.jppf;

import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.node.protocol.AbstractTask;
import org.jppf.node.protocol.Task;

import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class JppfPoc {

    public static void main(String[] args) {
        try (JPPFClient jppfClient = new JPPFClient()) {
            TemplateApplicationRunner runner = new TemplateApplicationRunner();
            runner.executeBlockingJob(jppfClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JPPFJob createJob(String jobname) throws Exception {
        JPPFJob job = new JPPFJob();
        job.setName(jobname);
        for (int i = 0; i < 10000; i++) {
            job.add(new TemplateJPPFTask());
        }
        return job;
    }

    public static class TemplateJPPFTask extends AbstractTask<String> {
        public void run() {
            System.out.println("Hello, jppf world!");
            setResult("This went well :)");
        }
    }

    public static class TemplateApplicationRunner {
        public void executeBlockingJob(JPPFClient jppfClient) throws Exception {
            JPPFJob job = createJob("Template blocking job");
            job.setBlocking(true);
            List<Task<?>> results = jppfClient.submitJob(job);
            processExecutionResults(job.getName(), results);

        }

        private void processExecutionResults(String name, List<Task<?>> results) {

            for (Task<?> task : results) {
                if (task.getThrowable() != null) {
                    System.err.println(task.getThrowable().toString());
                } else {
                    System.out.println(name + ":" + task.getResult().toString());
                }
            }
        }
    }
}
