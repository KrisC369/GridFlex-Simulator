package be.kuleuven.cs.gridflex.experimentation.tosg.data;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;

import java.util.Arrays;
import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class EgtCsvResultWriter {
    //CSV file header
    private static final Object[] FILE_HEADER = { "nAgents", "reps", "price point",
            "median fixed points",
            "lower bound fixed points", "upper bound fixed points", "data file",
            "mean eqn params", "lower bound eqn params", "upper bound eqn params", "CI Level",
            "Allocation efficiency", "Wind error file idx" };

    private EgtCsvResultWriter() {
    }

    public static void writeCsvFile(String fileName,
            List<EgtCsvResultWriter.WgmfDynamicsResults> results,
            boolean append) {
        CsvWriter.writeCsvFile(fileName, results, append, FILE_HEADER);
    }

    @AutoValue
    public static abstract class WgmfDynamicsResults implements Printable {

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
            return new AutoValue_EgtCsvResultWriter_WgmfDynamicsResults(n, r, data, pp, median,
                    lower, upper, medianEqn, lowerEqn, upperEqn, ciLevel, allocEff,
                    windErrorFileNameIdx);
        }
    }
}
