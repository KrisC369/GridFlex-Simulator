package be.kuleuven.cs.gridflex.domain.util.data;

import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract error distribution data file.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class AbstractErrorDistribution {

    private static final double KMH_TO_MS = 3.6d;

    /**
     * @return All means for this distribution instance.
     */
    public abstract List<Double> getMeans();

    /**
     * @return All standard deviations for this distribution instance.
     */
    public abstract List<Double> getStandardDeviations();

    /**
     * @param horizon The forecast horizon.
     * @return The standard deviation for given forecast horizon.
     */
    public double getSdForHorizon(int horizon) {
        return getStandardDeviations().get(horizon);
    }

    /**
     * @param horizon The forecast horizon.
     * @return The mean for given forecast horizon.
     */
    public double getMeanForHorizon(int horizon) {
        return getMeans().get(horizon);
    }

    /**
     * @return The largest/furtest forecast horizon in hours.
     */
    public int getMaxForecastHorizon() {
        return getMeans().size();
    }

    /**
     * Error distribution from csv loader method object.
     */
    static class ErrorDistributions {
        private String filename;
        private List<Double> means;
        private List<Double> sds;

        /**
         * Constructor.
         *
         * @param filename The name of the file to load.
         */
        ErrorDistributions(String filename) {
            this.filename = filename;
        }

        List<Double> getMeans() {
            return means;
        }

        List<Double> getSds() {
            return sds;
        }

        /**
         * Load the file.
         *
         * @return this method object.
         * @throws IOException
         */
        ErrorDistributions invoke() throws IOException {
            means = Lists.newArrayList();
            sds = Lists.newArrayList();
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream(filename);
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
            InputStreamReader fileReader = new InputStreamReader(
                    resourceAsStream, StandardCharsets.UTF_8);
            Iterable<CSVRecord> records = new CSVParser(fileReader, csvFileFormat).getRecords();
            //Assuming formatting of header line is following:
            //"hour.horizon","mean","sd" in km/h
            for (CSVRecord record : records) {
                means.add(Double.valueOf(record.get(1)));
                sds.add(Double.valueOf(record.get(2)));
            }
            //Apply correction: converting from km/h to m/s
            means = means.stream().map(v -> v / KMH_TO_MS).collect(Collectors.toList());
            sds = sds.stream().map(v -> v / KMH_TO_MS).collect(Collectors.toList());
            fileReader.close();
            return this;
        }
    }
}
