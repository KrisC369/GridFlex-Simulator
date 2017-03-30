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
 * Data time series to represent grid congestion in energy volumes of kWh.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class DayAheadPriceProfile
        extends AbstractTimeSeriesImplementation<DayAheadPriceProfile> {

    private static final long serialVersionUID = -1307830669782098778L;

    DayAheadPriceProfile() {
        super();
    }

    DayAheadPriceProfile(final DoubleList values) {
        super(values);
    }

    DayAheadPriceProfile(final double[] values) {
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
    public DayAheadPriceProfile transform(Function<Double, Double> function) {
        return new DayAheadPriceProfile(
                DoubleStream.of(values().toDoubleArray()).map(function::apply).toArray());
    }

    @Override
    public DayAheadPriceProfile transformFromIndex(IntToDoubleFunction function) {
        return new DayAheadPriceProfile(IntStream.range(0, length())
                .mapToDouble(function::applyAsDouble).toArray());
    }

    @Override
    public DayAheadPriceProfile subtractValues(TimeSeries ts) {
        checkArgument(ts.length() == length(),
                "Timeseries should be equal in length to this profile.");
        DoubleList dl = new DoubleArrayList(length());
        for (int i = 0; i < length(); i++) {
            dl.add(value(i) - ts.value(i));
        }
        return new DayAheadPriceProfile(dl);
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
    public static DayAheadPriceProfile createFromCSV(final String filename,
            final String column)
            throws IOException {
        final DayAheadPriceProfile cp = new DayAheadPriceProfile();
        cp.load(filename, column);
        return cp;
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
    public static DayAheadPriceProfile extrapolateFromHourlyOneDayData(final String filename,
            final String column, int days)
            throws IOException {
        final DayAheadPriceProfile cp = new DayAheadPriceProfile();
        cp.load(filename, column);
        DoubleList dl = new DoubleArrayList();
        for (int i = 0; i < 4 * 24 * days; i++) {
            int idx = (i / 4) % 24;
            dl.add(cp.value(idx));
        }
        return DayAheadPriceProfile.createFromTimeSeries(() -> dl);
    }

    /**
     * Factory method for building time series from other time series.
     *
     * @param series The series to copy from.
     * @return the time series.
     */
    public static DayAheadPriceProfile createFromTimeSeries(final TimeSeries series) {
        return new DayAheadPriceProfile(series.values());
    }

    /**
     * Create a new empty congestion profile.
     *
     * @return An empty congestion profile.
     */
    public static DayAheadPriceProfile empty() {
        return new DayAheadPriceProfile();
    }
}
