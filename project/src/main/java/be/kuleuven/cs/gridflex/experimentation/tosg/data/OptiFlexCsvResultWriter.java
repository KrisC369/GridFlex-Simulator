package be.kuleuven.cs.gridflex.experimentation.tosg.data;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;

import java.util.List;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class OptiFlexCsvResultWriter {
    //CSV file header
    private static final Object[] FILE_HEADER = { "nAgents", "reps", "price point",
            "data file", "flex duration", "flex Interactivation time", "flex activation count",
            "CI Level",
            "Allocation efficiency", "Wind error file idx" };

    private OptiFlexCsvResultWriter() {
    }

    public static void writeCsvFile(String fileName,
            List<OptiFlexCsvResultWriter.OptiFlexResults> results,
            boolean append) {
        CsvWriter.writeCsvFile(fileName, results, append, FILE_HEADER);
    }

    @AutoValue
    public static abstract class OptiFlexResults implements Printable {

        OptiFlexResults() {
        }

        public abstract int getNAgents();

        public abstract int getRepititions();

        public abstract String getDataFileName();

        public abstract double getFlexDuration();

        public abstract double getFlexInterActivation();

        public abstract int getFlexActivationCount();

        public abstract double getCiLevel();

        public abstract ConfidenceInterval getAllocEff();

        public abstract int getWindErrorFileIdx();

        @Override
        public List getValues() {
            return Lists.newArrayList(getNAgents(), getRepititions(), getFlexDuration(),
                    getFlexInterActivation(), getFlexActivationCount(),
                    getDataFileName(),
                    getCiLevel(),
                    getAllocEff().toString(), getWindErrorFileIdx());
        }

        public static OptiFlexResults create(int n, int r, String data, double dur, double ia,
                int ac,
                double ciLevel, ConfidenceInterval allocEff, int windErrorFileNameIdx) {
            return new AutoValue_OptiFlexCsvResultWriter_OptiFlexResults(n, r, data, dur, ia, ac,
                    ciLevel, allocEff,
                    windErrorFileNameIdx);
        }
    }
}
