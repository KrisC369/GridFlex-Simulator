package be.kuleuven.cs.flexsim.domain.util.data;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Value class for Error distributions for different forecast horizons.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class ForecastHorizonErrorDistribution {

    ForecastHorizonErrorDistribution() {
    }

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
     * Load forecast horizon error distribution from csv resource.
     *
     * @param filename The name of the resource to load from.
     * @return A fully instantiated value object.
     * @throws IOException e.g. If the file cannot be found.
     */
    public static ForecastHorizonErrorDistribution loadFromCSV(String filename)
            throws IOException {
        List<Double> means = Lists.newArrayList();
        List<Double> sds = Lists.newArrayList();

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = new File(classLoader.getResource(filename).getFile());
        final CSVReader reader = new CSVReaderBuilder(new InputStreamReader(
                new FileInputStream(file), Charset.defaultCharset())).build();
        String[] nextLine = reader.readNext();

        //Assuming formatting of header line is following:
        //"hour.horizon","mean","sd" in km/h
        while ((nextLine = reader.readNext()) != null) {
            means.add(Double.valueOf(nextLine[1]));
            sds.add(Double.valueOf(nextLine[2]));
        }
        //Apply correction: converting from km/h to m/s
        means = means.stream().map(v -> v / 3.6d).collect(Collectors.toList());
        sds = sds.stream().map(v -> v / 3.6d).collect(Collectors.toList());

        return create(means, sds);
    }

    /**
     * Default factory method.
     *
     * @param means The means.
     * @param sds   The standard deviations.
     * @return A fully instantiated value object.
     */
    static ForecastHorizonErrorDistribution create(List<Double> means, List<Double> sds) {
        checkArgument(means.size() == sds.size(), "Should have as many mean values as SD values.");
        return new AutoValue_ForecastHorizonErrorDistribution(means, sds);
    }
}
