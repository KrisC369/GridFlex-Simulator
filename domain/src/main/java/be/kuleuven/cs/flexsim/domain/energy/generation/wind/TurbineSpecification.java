package be.kuleuven.cs.flexsim.domain.energy.generation.wind;

import com.google.auto.value.AutoValue;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Value class for representing tubine specs.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class TurbineSpecification implements Serializable {
    private static final String BLADE_LENGTH_HEADER = "BladeLength";
    private static final String POWER_RATE_HEADER = "RatedPower";
    private static final String HUB_HEIGHT_HEADER = "HubHeight";
    private static final String CUT_IN_HEADER = "CutIn";
    private static final String CUT_OUT_HEADER = "CutOut";
    private static final String WIND_VALUES_HEADER = "Wind";
    private static final String POWER_VALUES_HEADER = "PowerAtWind";
    private static final String POWER_COEFF_HEADER = "PowerCoefficient";

    TurbineSpecification() {
    }

    /**
     * @return the turbine blade length.
     */
    public abstract double getBladeLength();

    /**
     * @return Returns the square m area swept by the blades.
     */
    public final double getSweptArea() {
        return StrictMath.pow(getBladeLength(), 2) * Math.PI;
    }

    /**
     * @return The power produced in kW at rated nominal wind speeds.
     */
    public abstract double getRatedPower();

    /**
     * @return The hub height.
     */
    public abstract double getHubHeight();

    /**
     * @return The cut-in wind speed.
     */
    public abstract double getCutInWindSpeed();

    /**
     * @return The cut out wind speed.
     */
    public abstract double getCutOutWindSpeed();

    /**
     * @return A Double list with values where indexes correspond to wind speed values (integer
     * samples) and the values are the power output values.
     */
    public abstract List<Double> getPowerValues();

    /**
     * @return A Double list with values where indexes correspond to wind speed   values
     * (integer samples) and the values are the power coefficient values.
     */
    public abstract List<Double> getPowerCoefficientValues();

    /**
     * Default creational method.
     *
     * @param bladeL    Blade length.
     * @param powerR    Rated power.
     * @param hubHeight Hub height.
     * @param cutIn     Cut-in wind speed.
     * @param cutOut    Cut-out wind speed.
     * @return A Turbine spec instance.
     */
    public static TurbineSpecification create(double bladeL, double powerR, double hubHeight,
            double cutIn, double cutOut, List<Double> powerV, List<Double> pcV) {
        return new AutoValue_TurbineSpecification(bladeL, powerR, hubHeight, cutIn, cutOut, powerV,
                pcV);
    }

    /**
     * Load specs file from resource.
     *
     * @param filename The filename
     * @return A Turbine spec instance.
     * @throws IOException If the resource cannot be found.
     */
    public static TurbineSpecification loadFromResource(final String filename) throws IOException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = new File(classLoader.getResource(filename).getFile());
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        InputStreamReader fileReader = new InputStreamReader(
                new FileInputStream(file), Charset.defaultCharset());
        Iterable<CSVRecord> records = new CSVParser(fileReader, csvFileFormat).getRecords();

        double bladeLength = 0;
        double powerRate = 0;
        double hubHeight = 0;
        double cutIn = 0;
        double cutOut = 0;
        IntList wind = new IntArrayList();
        DoubleList powerValues = new DoubleArrayList();
        DoubleList powerCoeffValues = new DoubleArrayList();
        boolean firstLine = true;
        for (CSVRecord record : records) {
            if (firstLine) {
                bladeLength = Double.valueOf(record.get(BLADE_LENGTH_HEADER));
                powerRate = Double.valueOf(record.get(POWER_RATE_HEADER));
                hubHeight = Double.valueOf(record.get(HUB_HEIGHT_HEADER));
                cutIn = Double.valueOf(record.get(CUT_IN_HEADER));
                cutOut = Double.valueOf(record.get(CUT_OUT_HEADER));
                firstLine = false;
            }
            wind.add(Integer.valueOf(record.get(WIND_VALUES_HEADER)));
            powerValues.add(Double.valueOf(record.get(POWER_VALUES_HEADER)));
            powerCoeffValues.add(Double.valueOf(record.get(POWER_COEFF_HEADER)));
        }

        int last = wind.get(wind.size() - 1);
        List<Double> corrPowerValues = new ArrayList(last + 1);
        List<Double> corrPowerCoeffValues = new ArrayList(last + 1);
        for (int i = 0; i < wind.get(wind.size() - 1) + 1; i++) {
            corrPowerValues.add(0d);
            corrPowerCoeffValues.add(0d);
        }

        for (int i = 0; i < wind.size(); i++) {
            corrPowerValues.set(wind.getInt(i), powerValues.get(i));
            corrPowerCoeffValues.set(wind.getInt(i), powerCoeffValues.get(i));
        }

        fileReader.close();
        return new AutoValue_TurbineSpecification(bladeLength, powerRate, hubHeight, cutIn,
                cutOut, corrPowerValues, corrPowerCoeffValues);
    }

    /**
     * @return A new empty spec object.
     */
    public static TurbineSpecification empty() {
        return new AutoValue_TurbineSpecification(0, 0, 0, 0, 0, new DoubleArrayList(),
                new DoubleArrayList());
    }
}
