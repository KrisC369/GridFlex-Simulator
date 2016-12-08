package be.kuleuven.cs.flexsim.experimentation.tosg.data;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class CsvResultWriter {
    private static final Logger logger = LoggerFactory.getLogger(CsvResultWriter.class);
    //Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final char DELIMITER = ';';

    //CSV file header
    private static final Object[] FILE_HEADER = { "nAgents", "reps", "price point",
            "median fixed points",
            "lower bound fixed points", "upper bound fixed points", "data file",
            "median eqn params", "lower bound eqn params", "upper bound eqn params", "CI Level" };

    public static void writeCsvFile(String fileName, List<WgmfDynamicsResults> results) {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR)
                .withDelimiter(DELIMITER);
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            CSVPrinter csvPrinter = new CSVPrinter(fileWriter, csvFileFormat);
            csvPrinter.printRecord(FILE_HEADER);
            for (WgmfDynamicsResults res : results) {
                csvPrinter.printRecord(res.getValues());
            }

            fileWriter.flush();
            csvPrinter.close();

            if (logger.isInfoEnabled()) {
                logger.info("Output written and resources released.");
            }
        } catch (Exception e) {
            logger.error("Error writing to csv file.", e);
        }
    }

    @AutoValue
    public static abstract class WgmfDynamicsResults {
        public abstract int getNAgents();

        public abstract int getRepititions();

        public abstract String getDataFileName();

        public abstract double getPricePoint();

        public abstract double[] getMedianFixedPoints();

        public abstract double[] getLowerBoundCIFixedPoints();

        public abstract double[] getUpperBoundCIFixedPoints();

        public abstract double[] getMedianDynEqnParams();

        public abstract double[] getLowerBoundDynEqnParams();

        public abstract double[] getUpperBoundEqnParams();

        public abstract double ciLevel();

        public List getValues() {
            return Lists.newArrayList(getNAgents(), getRepititions(),
                    getPricePoint(),
                    Arrays.toString(getMedianFixedPoints()),
                    Arrays.toString(getLowerBoundCIFixedPoints()),
                    Arrays.toString(getUpperBoundCIFixedPoints()), getDataFileName());
        }

        public static WgmfDynamicsResults create(int n, int r, String data, double pp,
                double[] median, double[] lower,
                double[] upper, double[] medianEqn, double[] lowerEqn, double[] upperEqn,
                double ciLevel) {
            return new AutoValue_CsvResultWriter_WgmfDynamicsResults(n, r, data, pp, median, lower,
                    upper, medianEqn, lowerEqn, upperEqn, ciLevel);
        }
    }
}
