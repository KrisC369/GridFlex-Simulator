package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import be.kuleuven.cs.gridflex.domain.energy.dso.r3dp.HourlyFlexConstraints;
import com.google.auto.value.AutoValue;

import java.math.BigDecimal;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class OptaExperimentResults {
    public abstract BigDecimal getResultValue();

    public abstract HourlyFlexConstraints getFlexConstraints();

    public static OptaExperimentResults create(BigDecimal d,
            HourlyFlexConstraints constraints) {
        return new AutoValue_OptaExperimentResults(d, constraints);
    }
}
