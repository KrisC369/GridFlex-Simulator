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
 * Data time series to represent grid congestion in energy volumes of kWh.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class PositiveImbalancePriceProfile
        extends AbstractTimeSeriesImplementation<PositiveImbalancePriceProfile> {

    PositiveImbalancePriceProfile() {
        super();
    }

    PositiveImbalancePriceProfile(final DoubleList values) {
        super(values);
    }

    PositiveImbalancePriceProfile(final double[] values) {
        super(values);
    }

    /**
     * Apply a double to double function to the elements in this profile and return a new profile.
     * Units are in volumes of energy, KWh.
     *
     * @param function The function transformation to apply.
     * @return A new Congestion profile instance.
     */
    @Override
    public PositiveImbalancePriceProfile transform(Function<Double, Double> function) {
        return new PositiveImbalancePriceProfile(
                DoubleStream.of(values().toDoubleArray()).map(function::apply).toArray());
    }

    @Override
    public PositiveImbalancePriceProfile transformFromIndex(IntToDoubleFunction function) {
        return new PositiveImbalancePriceProfile(IntStream.range(0, length())
                .mapToDouble(function::applyAsDouble).toArray());
    }

    @Override
    public PositiveImbalancePriceProfile subtractValues(TimeSeries ts) {
        checkArgument(ts.length() == length(),
                "Timeseries should be equal in length to this profile.");
        DoubleList dl = new DoubleArrayList(length());
        for (int i = 0; i < length(); i++) {
            dl.add(value(i) - ts.value(i));
        }
        return new PositiveImbalancePriceProfile(dl);
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
    public static PositiveImbalancePriceProfile createFromCSV(final String filename, final String column)
            throws IOException {
        final PositiveImbalancePriceProfile cp = new PositiveImbalancePriceProfile();
        cp.load(filename, column);
        return cp;
    }

    /**
     * Factory method for building time series from other time series.
     *
     * @param series The series to copy from.
     * @return the time series.
     */
    public static PositiveImbalancePriceProfile createFromTimeSeries(final TimeSeries series) {
        return new PositiveImbalancePriceProfile(series.values());
    }

    /**
     * Create a new empty congestion profile.
     *
     * @return An empty congestion profile.
     */
    public static PositiveImbalancePriceProfile empty() {
        return new PositiveImbalancePriceProfile();
    }
}
