package be.kuleuven.cs.flexsim.domain.util.data.profiles;

import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Data time series to represent current values in cable infrastructure in Amps.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class WindSpeedProfile extends AbstractTimeSeriesImplementation<WindSpeedProfile> {

    WindSpeedProfile() {
        super();
    }

    WindSpeedProfile(final DoubleList values) {
        super(values);
    }

    WindSpeedProfile(final double[] values) {
        super(values);
    }

    /**
     * Apply a double to double function to the elements in this profile and return a new profile.
     *
     * @param function The function transformation to apply.
     * @return A new Congestion profile instance.
     */
    @Override
    public WindSpeedProfile transform(Function<Double, Double> function) {
        return new WindSpeedProfile(
                DoubleStream.of(values().toDoubleArray()).map(y -> function.apply(y)).toArray());
    }

    @Override
    public WindSpeedProfile transformFromIndex(IntToDoubleFunction function) {
        return new WindSpeedProfile(IntStream.range(0, length())
                .mapToDouble(i -> function.applyAsDouble(i)).toArray());
    }

    @Override
    public WindSpeedProfile subtractValues(TimeSeries ts) {
        checkArgument(ts.length() == length(),
                "Timeseries should be equal in length to this profile.");
        DoubleList dl = new DoubleArrayList(length());
        for (int i = 0; i < length(); i++) {
            dl.add(value(i) - ts.value(i));
        }
        return new WindSpeedProfile(dl);
    }

    /**
     * Factory method for building a time series from a csv file.
     *
     * @param filename The filename.
     * @param column   The column label to use as data.
     * @return the time series.
     * @throws IOException           If reading from the file is not possible.
     * @throws FileNotFoundException If the file with that name cannot be found.
     */
    public static WindSpeedProfile createFromCSV(final String filename,
            final String column)
            throws IOException {
        final WindSpeedProfile cp = new WindSpeedProfile();
        cp.load(filename, column);
        return cp;
    }

    /**
     * Factory method for building time series from other time series.
     *
     * @param series The series to copy from.
     * @return the time series.
     */
    public static WindSpeedProfile createFromTimeSeries(final TimeSeries series) {
        return new WindSpeedProfile(series.values());
    }

    /**
     * Create a new empty congestion profile.
     *
     * @return
     */
    public static WindSpeedProfile empty() {
        return new WindSpeedProfile();
    }
}
