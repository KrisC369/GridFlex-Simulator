package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.domain.util.data.PowerCapabilityBand;
import com.google.auto.value.AutoValue;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */@AutoValue
public abstract class FlexConstraints {

    public static final FlexConstraints NOFLEX = create(0,0,0);

    /**
     * @return The number of time steps allowed between activations.
      */
    public abstract int getInterActivationTime();

    /**
     * @return The maximum number of consecutive time steps activation is allowed.
     */
    public abstract int getActivationDuration();

    /**
     * @return The maximum amount of allowed activations
     */
    public abstract int getMaximumActivations();

    public static FlexConstraints create(int interAct, int activationDuration, int maximumActivations){
        return new AutoValue_FlexConstraints(interAct,activationDuration,maximumActivations);
    }

}
