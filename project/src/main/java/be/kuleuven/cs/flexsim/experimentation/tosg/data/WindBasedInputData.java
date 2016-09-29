package be.kuleuven.cs.flexsim.experimentation.tosg.data;

import be.kuleuven.cs.flexsim.domain.util.data.profiles.CableCurrentProfile;
import be.kuleuven.cs.flexsim.domain.util.data.profiles.CongestionProfile;
import com.google.auto.value.AutoValue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

/**
 * Represents a value class for representing input data profiles from one same dataset.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class WindBasedInputData implements Serializable {

    private static final String CONGESTION_COLUMN_KEY = "verlies aan energie";
    private static final String CURRENT_COLUMN_KEY = "startprofiel+extra";

    WindBasedInputData() {
    }

    public abstract CongestionProfile getCongestionProfile();

    public abstract CableCurrentProfile getCableCurrentProfile();

    /**
     * Load both defined profiles from resource file.
     *
     * @param filename the name of the resource (file) to load from.
     * @return
     * @throws IOException           If reading from the file is not possible.
     * @throws FileNotFoundException If the file with that name cannot be found.
     */
    public static WindBasedInputData loadFromResource(String filename) throws IOException {
        CongestionProfile congestion = CongestionProfile
                .createFromCSV(filename, CONGESTION_COLUMN_KEY);
        CableCurrentProfile current = CableCurrentProfile
                .createFromCSV(filename, CURRENT_COLUMN_KEY);
        return new AutoValue_WindBasedInputData(congestion, current);
    }

}
