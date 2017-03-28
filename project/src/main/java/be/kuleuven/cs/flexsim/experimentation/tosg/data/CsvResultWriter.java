package be.kuleuven.cs.flexsim.experimentation.tosg.data;

import com.google.auto.value.AutoValue;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class CsvResultWriter {
    private static final Logger logger = LoggerFactory.getLogger(CsvResultWriter.class);
    //Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final char DELIMITER = ';';

    private CsvResultWriter() {
    }

    //CSV file header
    private static final Object[] FILE_HEADER = { "nAgents", "reps", "price point",
            "median fixed points",
            "lower bound fixed points", "upper bound fixed points", "data file",
            "mean eqn params", "lower bound eqn params", "upper bound eqn params", "CI Level",
            "Allocation efficiency", "Wind error file idx" };

    public static void writeCsvFile(String fileName, List<WgmfDynamicsResults> results,
            boolean append) {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR)
                .withDelimiter(DELIMITER);
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                new FileOutputStream(fileName, append), Charsets.UTF_8)) {
            CSVPrinter csvPrinter = new CSVPrinter(fileWriter, csvFileFormat);
            if (!append) {
                csvPrinter.printRecord(FILE_HEADER);
            }
            for (WgmfDynamicsResults res : results) {
                csvPrinter.printRecord(res.getValues());
            }

            fileWriter.flush();
            csvPrinter.close();

            if (logger.isInfoEnabled()) {
                logger.info("Output written and resources released.");
            }
        } catch (IOException e) {
            logger.error("Error writing to csv file.", e);
        }
    }

    @AutoValue
    public static abstract class WgmfDynamicsResults {

        WgmfDynamicsResults() {
        }

        public abstract int getNAgents();

        public abstract int getRepititions();

        public abstract String getDataFileName();

        public abstract double getPricePoint();

        public abstract double[] getMeanFixedPoints();

        public abstract double[] getLowerBoundCIFixedPoints();

        public abstract double[] getUpperBoundCIFixedPoints();

        public abstract double[] getMeanDynEqnParams();

        public abstract double[] getLowerBoundDynEqnParams();

        public abstract double[] getUpperBoundDynEqnParams();

        public abstract double getCiLevel();

        public abstract ConfidenceInterval getAllocEff();

        public abstract int getWindErrorFileIdx();

        public List getValues() {
            return Lists.newArrayList(getNAgents(), getRepititions(),
                    getPricePoint(),
                    Arrays.toString(getMeanFixedPoints()),
                    Arrays.toString(getLowerBoundCIFixedPoints()),
                    Arrays.toString(getUpperBoundCIFixedPoints()), getDataFileName(),
                    Arrays.toString(getMeanDynEqnParams()),
                    Arrays.toString(getLowerBoundDynEqnParams()),
                    Arrays.toString(getUpperBoundDynEqnParams()), getCiLevel(),
                    getAllocEff().toString(), getWindErrorFileIdx());
        }

        public static WgmfDynamicsResults create(int n, int r, String data, double pp,
                double[] median, double[] lower,
                double[] upper, double[] medianEqn, double[] lowerEqn, double[] upperEqn,
                double ciLevel, ConfidenceInterval allocEff, int windErrorFileNameIdx) {
            return new AutoValue_CsvResultWriter_WgmfDynamicsResults(n, r, data, pp, median, lower,
                    upper, medianEqn, lowerEqn, upperEqn, ciLevel, allocEff, windErrorFileNameIdx);
        }
    }
}
