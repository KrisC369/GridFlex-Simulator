package be.kuleuven.cs.flexsim.solvers.memoization.immutableViews;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.FlexibilityProvider;
import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.domain.util.data.DoublePowerCapabilityBand;
import com.google.auto.value.AutoValue;

import java.io.Serializable;

/**
 * Immutable view of a flexibility provider to be used in memoization context.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class FlexibilityProviderView implements Serializable {

    private static final long serialVersionUID = -7705253266099469069L;

    /**
     * @return The amount of flexible power that this entity is capable of delivering in both
     * injection and offtake directions.
     */
    public abstract DoublePowerCapabilityBand getFlexibilityActivationRate();

    /**
     * @return The constraints regarding the activation of flexibility present on site.
     */
    public abstract HourlyFlexConstraints getFlexibilityActivationConstraints();

    /**
     * Static factory method
     *
     * @param f The modifiable provider.
     * @return An unmodifiable view.
     */
    static FlexibilityProviderView from(FlexibilityProvider f) {
        return new AutoValue_FlexibilityProviderView(f.getFlexibilityActivationRate(),
                f.getFlexibilityActivationConstraints());
    }

    /**
     * @param provider The provider object to test.
     * @return true if this view is possible view of given provider.
     */
    public boolean isViewOf(FlexibilityProvider provider) {
        return this.getFlexibilityActivationRate().equals(provider.getFlexibilityActivationRate())
                && this.getFlexibilityActivationConstraints()
                .equals(provider.getFlexibilityActivationConstraints());
    }
}
