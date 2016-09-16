package be.kuleuven.cs.flexsim.domain.energy.generation.wind;

import com.google.auto.value.AutoValue;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Value class for representing tubine specs.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class TurbineSpecification {
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

    public static TurbineSpecification loadFromResource(final String filename) throws IOException {
        int bladeLKey = -1;
        int powerRKey = -1;
        int hubHeightKey = -1;
        int cutInKey = -1;
        int cutOutKey = -1;
        int windValuesKey = -1;
        int powerValuesKey = -1;
        int powerCoeffHeader = -1;

        //        final List<Double> dataRead = Lists.newArrayList();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = new File(classLoader.getResource(filename).getFile());
        final CSVReader reader = new CSVReaderBuilder(new InputStreamReader(
                new FileInputStream(file), Charset.defaultCharset())).build();
        String[] headers = reader.readNext();

        //set the column keys
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            if (BLADE_LENGTH_HEADER.equalsIgnoreCase(header)) {
                bladeLKey = i;
            } else if (POWER_RATE_HEADER.equalsIgnoreCase(header)) {
                powerRKey = i;
            } else if (HUB_HEIGHT_HEADER.equalsIgnoreCase(header)) {
                hubHeightKey = i;
            } else if (CUT_IN_HEADER.equalsIgnoreCase(header)) {
                cutInKey = i;
            } else if (CUT_OUT_HEADER.equalsIgnoreCase(header)) {
                cutOutKey = i;
            } else if (WIND_VALUES_HEADER.equalsIgnoreCase(header)) {
                windValuesKey = i;
            } else if (POWER_VALUES_HEADER.equalsIgnoreCase(header)) {
                powerValuesKey = i;
            } else if (POWER_COEFF_HEADER.equalsIgnoreCase(header)) {
                powerCoeffHeader = i;
            }
        }

        double bladeLength = 0;
        double powerRate = 0;
        double hubHeight = 0;
        double cutIn = 0;
        double cutOut = 0;
        IntList wind = new IntArrayList();
        DoubleList powerValues = new DoubleArrayList();
        DoubleList powerCoeffValues = new DoubleArrayList();

        //        DoubleList dl = new DoubleArrayList();
        //        Double d = new Double(0);
        //        Integer i = 2;
        //        dl.add(i, d);
        //        dl.add(2, d);

        //fill in data arrays.
        String[] values;
        boolean firstLine = true;
        while ((values = reader.readNext()) != null) {
            if (firstLine) {
                bladeLength = Double.valueOf(values[bladeLKey]);
                powerRate = Double.valueOf(values[powerRKey]);
                hubHeight = Double.valueOf(values[hubHeightKey]);
                cutIn = Double.valueOf(values[cutInKey]);
                cutOut = Double.valueOf(values[cutOutKey]);
                firstLine = false;
            }
            wind.add(Integer.valueOf(values[windValuesKey]));
            powerValues.add(Double.valueOf(values[powerValuesKey]));
            powerCoeffValues.add(Double.valueOf(values[powerCoeffHeader]));
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

        return new AutoValue_TurbineSpecification(bladeLength, powerRate, hubHeight, cutIn,
                cutOut, corrPowerValues, corrPowerCoeffValues);
    }

    /**
     * @return A new empty spec object.
     */
    public static TurbineSpecification empty() {
        return new AutoValue_TurbineSpecification(0, 0, 0, 0,
                0, new DoubleArrayList(), new DoubleArrayList());
    }
}
