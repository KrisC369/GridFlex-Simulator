package be.kuleuven.cs.gridflex.domain.util.data.profiles;

import be.kuleuven.cs.gridflex.domain.util.data.TimeSeries;
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
public class CableCurrentProfile extends AbstractTimeSeriesImplementation<CableCurrentProfile> {

    private static final long serialVersionUID = 8148091227931725197L;

    CableCurrentProfile() {
        super();
    }

    CableCurrentProfile(final DoubleList values) {
        super(values);
    }

    CableCurrentProfile(final double[] values) {
        super(values);
    }

    /**
     * Apply a double to double function to the elements in this profile and return a new profile.
     *
     * @param function The function transformation to apply.
     * @return A new Congestion profile instance.
     */
    @Override
    public CableCurrentProfile transform(Function<Double, Double> function) {
        return new CableCurrentProfile(
                DoubleStream.of(values().toDoubleArray()).map(function::apply).toArray());
    }

    @Override
    public CableCurrentProfile transformFromIndex(IntToDoubleFunction function) {
        return new CableCurrentProfile(IntStream.range(0, length())
                .mapToDouble(function::applyAsDouble).toArray());
    }

    @Override
    public CableCurrentProfile subtractValues(TimeSeries ts) {
        checkArgument(ts.length() == length(),
                "Timeseries should be equal in length to this profile.");
        DoubleList dl = new DoubleArrayList(length());
        for (int i = 0; i < length(); i++) {
            dl.add(value(i) - ts.value(i));
        }
        return new CableCurrentProfile(dl);
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
    public static CableCurrentProfile createFromCSV(final String filename,
            final String column)
            throws IOException {
        final CableCurrentProfile cp = new CableCurrentProfile();
        cp.load(filename, column);
        return cp;
    }

    /**
     * Factory method for building time series from other time series.
     *
     * @param series The series to copy from.
     * @return the time series.
     */
    public static CableCurrentProfile createFromTimeSeries(final TimeSeries series) {
        return new CableCurrentProfile(series.values());
    }

    /**
     * Create a new empty congestion profile.
     *
     * @return An empty cable current profile.
     */
    public static CableCurrentProfile empty() {
        return new CableCurrentProfile();
    }
}
