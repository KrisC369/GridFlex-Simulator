package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.solver.Solvers;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.cli.OptionBuilder.withLongOpt;

/**
 * Input parser for who-gets-my-flex games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class WgmfInputParser {
    private static final int NAGENTS_DEFAULT = 2;
    private static final int NREPS_DEFAULT = 1;
    private static final Logger logger = LoggerFactory.getLogger(WgmfInputParser.class);
    private static final Solvers.TYPE SOLVER_DEFAULT = Solvers.TYPE.DUMMY;

    private WgmfInputParser() {
    }

    /**
     * Use --help or -r [repititions] -n [numberOfAgents] -s [GUROBI|CPLEX|DUMMY] -m [LOCAL|REMOTE]
     *
     * @param args The commandline args.
     * @return The experiment params.
     */
    public static ExperimentParams parseInputAndExec(String[] args) {
        Options o = new Options();
        o.addOption("n", true, "The number of participating agents");
        o.addOption("r", true, "The number of repititions");
        o.addOption(withLongOpt("solver")
                .withDescription("Which solver to use. [CPLEX|GUROBI]")
                .hasArg()
                .withArgName("SOLVER")
                .create("s"));
        o.addOption(withLongOpt("mode")
                .withDescription("Which execution mode to use. [LOCAL|REMOTE]")
                .hasArg()
                .withArgName("MODE")
                .create("m"));
        o.addOption("p1start", true, "The starting value of first param");
        o.addOption("p1step", true, "The step size of first param");
        o.addOption("p1end", true, "The end value of first param");

        int nAgents = NAGENTS_DEFAULT;
        int nReps = NREPS_DEFAULT;
        Solvers.TYPE solver = SOLVER_DEFAULT;
        boolean remoteExec = false;
        ExperimentParams.Builder builder = ExperimentParams.builder();
        CommandLineParser parser = new BasicParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(o, args);
            if (line.hasOption("n")) {
                nAgents = Integer.parseInt(line.getOptionValue("n"));
            }
            if (line.hasOption("r")) {
                nReps = Integer.parseInt(line.getOptionValue("r"));
            }
            if (line.hasOption("s")) {
                solver = Solvers.TYPE.valueOf(line.getOptionValue("s"));
            }
            if (line.hasOption("m") && "REMOTE".equals(line.getOptionValue("m"))) {
                remoteExec = true;
            }
            if (line.hasOption("p1start")) {
                builder.setP1Start(Double.parseDouble(line.getOptionValue("p1start")));
            }
            if (line.hasOption("p1step")) {
                builder.setP1Step(Double.parseDouble(line.getOptionValue("p1step")));
            }
            if (line.hasOption("p1end")) {
                builder.setP1End(Double.parseDouble(line.getOptionValue("p1end")));
            }
            if (logger.isWarnEnabled()) {
                String remote = remoteExec ? "REMOTE" : "LOCAL";
                logger.warn(
                        "Performing {} repititions for experiment with {} agents using: {} "
                                + "execution mode: {}",
                        nReps, nAgents, solver.toString(), remote);
            }
            return builder.setNAgents(nAgents).setNRepititions(nReps)
                    .setSolver(solver).setRemoteExecutable(remoteExec).build();

        } catch (ParseException exp) {
            // oops, something went wrong
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("WgmfGameRunner", o);
            if (logger.isErrorEnabled()) {
                logger.error("Parsing failed.  Reason: {}", exp.getMessage(), exp);
            }
            throw new IllegalStateException(exp);
        }
    }
}
