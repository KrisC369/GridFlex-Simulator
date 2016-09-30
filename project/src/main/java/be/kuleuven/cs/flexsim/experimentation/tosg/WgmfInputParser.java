package be.kuleuven.cs.flexsim.experimentation.tosg;

import be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static be.kuleuven.cs.flexsim.solver.optimal.AbstractOptimalSolver.Solver.DUMMY;

/**
 * Input parser for who-gets-my-flex games.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class WgmfInputParser {
    private static final int NAGENTS_DEFAULT = 2;
    private static final int NREPS_DEFAULT = 1;
    private static final Logger logger = LoggerFactory.getLogger(WgmfInputParser.class);
    private static final AbstractOptimalSolver.Solver SOLVER_DEFAULT = DUMMY;

    private WgmfInputParser() {
    }

    /**
     * Use --help or -r [repititions] -n [numberOfAgents] -s [GUROBI|CPLEX|DUMMY]
     *
     * @param args The commandline args.
     * @return The experiment params.
     */
    public static ExperimentParams parseInputAndExec(String[] args) {
        Options o = new Options();
        o.addOption("n", true, "The number of participating agents");
        o.addOption("r", true, "The number of repititions");
        o.addOption(OptionBuilder.withLongOpt("solver")
                .withDescription("Which solver to use. [CPLEX|GUROBI]")
                .hasArg()
                .withArgName("SOLVER")
                .create("s"));

        int nAgents = NAGENTS_DEFAULT;
        int nReps = NREPS_DEFAULT;
        AbstractOptimalSolver.Solver solver = SOLVER_DEFAULT;

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
                solver = AbstractOptimalSolver.Solver.valueOf(line.getOptionValue("s"));
            }
            logger.warn("Performing " + nReps + " repititions for experiment with " + nAgents
                    + " agents using: " + solver.toString());
            return ExperimentParams.create(nReps, nAgents, solver);

        } catch (ParseException exp) {
            // oops, something went wrong
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("WgmfGameRunner", o);
            logger.error("Parsing failed.  Reason: " + exp.getMessage(), exp);
            throw new IllegalStateException(exp);
        }
    }
}