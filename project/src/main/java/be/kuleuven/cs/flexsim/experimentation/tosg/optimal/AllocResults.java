package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.experimentation.tosg.FlexProvider;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ListMultimap;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class AllocResults {
    public abstract ListMultimap<FlexProvider, Boolean> getAllocationResults();

    public abstract double getObjective();

    public static AllocResults create(ListMultimap<FlexProvider, Boolean> allocs,
            double objective) {
        return new AutoValue_AllocResults(allocs, objective);
    }

}