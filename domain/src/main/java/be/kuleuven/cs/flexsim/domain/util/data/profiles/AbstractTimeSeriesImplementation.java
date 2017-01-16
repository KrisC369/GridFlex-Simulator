package be.kuleuven.cs.flexsim.domain.util.data.profiles;

import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A time series representation of values in energy domain contexts.
 *
 * @param <R> The concrete implementation type of the series.
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public abstract class AbstractTimeSeriesImplementation<R extends AbstractTimeSeriesImplementation>
        implements TimeSeries, Serializable {

    private static final long serialVersionUID = -7784771631667547974L;
    private DoubleList dValues;

    @Nullable
    private Double maxcache;
    @Nullable
    private Double sumcache;

    AbstractTimeSeriesImplementation() {
        dValues = new DoubleArrayList();
    }

    AbstractTimeSeriesImplementation(final DoubleList values) {
        dValues = new DoubleArrayList(values);
    }

    AbstractTimeSeriesImplementation(final double[] values) {
        dValues = new DoubleArrayList(values);
    }

    /**
     * Load and parse time series from file.
     *
     * @param filename the name of the file to parse and load.
     * @param column   the label of the column to parse and use as time series.
     * @throws IOException          When loading is not possible for whatever reason.
     * @throws NullPointerException when the input file cannot be found.
     */
    public void load(final String filename, final String column) throws IOException {
        final List<Double> dataRead = Lists.newArrayList();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = new File(classLoader.getResource(filename).getFile());
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        InputStreamReader fileReader = new InputStreamReader(new FileInputStream(file),
                StandardCharsets.UTF_8);
        Iterable<CSVRecord> records = new CSVParser(fileReader, csvFileFormat).getRecords();

        for (CSVRecord record : records) {
            dataRead.add(Double.valueOf(record.get(column)));
        }

        dValues = new DoubleArrayList();
        dValues.addAll(dataRead);
        resetCache();
        fileReader.close();
    }

    @Override
    public DoubleList values() {
        return DoubleLists.unmodifiable(dValues);
    }

    @Override
    public double value(final int index) {
        checkArgument(index >= 0 && index < length(), "Index out of bounds: " + index);
        return dValues.getDouble(index);
    }

    @Override
    public int length() {
        return this.dValues.size();
    }

    /**
     * Change the value of a certain element in the time series.
     *
     * @param index the index of the value to change.
     * @param value the new value.
     */
    public void changeValue(final int index, final double value) {
        checkArgument(index >= 0 && index < length(),
                "Index(" + index + ") should be within range of time series.");
        this.dValues.set(index, value);
        resetCache();
    }

    private void resetCache() {
        this.sumcache = null;
        this.maxcache = null;
    }

    @Override
    public double max() {
        if (this.maxcache != null) {
            return maxcache;
        }
        maxcache = TimeSeries.super.max();
        return maxcache;
    }

    @Override
    public double sum() {
        if (this.sumcache != null) {
            return sumcache;
        }
        sumcache = TimeSeries.super.sum();
        return sumcache;
    }

    /**
     * Apply a double to double function to the elements in this profile and return a new profile.
     *
     * @param function The function transformation to apply.
     * @return A new profile instance.
     */
    abstract R transform(Function<Double, Double> function);

    /**
     * Apply a double to double function to the elements in this profile and return a new profile.
     *
     * @param function The function transformation to apply.
     * @return A new profile instance.
     */
    abstract R transformFromIndex(IntToDoubleFunction function);

    /**
     * Subtract argument time series from this profile.
     *
     * @param ts The ts to subtract from this profile.
     * @return A new profile instance.
     */
    abstract R subtractValues(TimeSeries ts);

}
