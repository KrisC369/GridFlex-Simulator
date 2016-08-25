package be.kuleuven.cs.flexsim.domain.site;

import be.kuleuven.cs.flexsim.domain.process.FlexProcess;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Builder class for building Site implementations.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface SiteBuilder {

    /**
     * Create a new concrete site with flexible production lines.
     *
     * @return a sitebuilder (Fluent API).
     */
    static SiteBuilder newConcreteSite() {
        return new ConcreteSiteBuilder();
    }

    /**
     * Create a new site simulation object.
     *
     * @return a sitebuilder (Fluent API).
     */
    static SiteBuilder newSiteSimulation() {
        return new SiteSimulationBuilder();
    }

    /**
     * Create a new site simulation object with equidistant flex.
     *
     * @return a sitebuilder (Fluent API).
     */
    static SiteBuilder newEquidistantSiteSimulation() {
        return new EquidistantSiteBuilder();
    }

    /**
     * Add a production line to this site. Only for concrete sites.
     *
     * @param line the line to add.
     * @return a sitebuilder (Fluent API).
     */
    default SiteBuilder addLine(final FlexProcess line) {
        throw new UnsupportedOperationException(
                "You cannot add lines to this site.");
    }

    /**
     * Sets the cease time.
     *
     * @param ceaseTime The cease time.
     * @return a sitebuilder (Fluent API).
     */
    default SiteBuilder withCeaseTime(final int ceaseTime) {
        throw new UnsupportedOperationException(
                "You cannot set a cease time for this site.");
    }

    /**
     * Sets the cease reaction or ramp up time.
     *
     * @param rampTime the ramp up time
     * @return a sitebuilder (Fluent API).
     */
    default SiteBuilder withReactionTime(final int rampTime) {
        throw new UnsupportedOperationException(
                "You cannot set a reaction time for this site.");
    }

    /**
     * Sets the duration of the flex profiles.
     *
     * @param duration The flex duration
     * @return a sitebuilder (Fluent API).
     */
    default SiteBuilder withFlexDuration(final int duration) {
        throw new UnsupportedOperationException(
                "You cannot set a flex duration for this site.");
    }

    /**
     * Sets the minimum consumption.
     *
     * @param min the minimum consumption level.
     * @return a sitebuilder (Fluent API).
     */

    default SiteBuilder withMinConsumption(final int min) {
        throw new UnsupportedOperationException(
                "You cannot set a min consumption time for this site.");
    }

    /**
     * Sets the maximum consumption.
     *
     * @param max The maximum consumption level.
     * @return a sitebuilder (Fluent API).
     */
    default SiteBuilder withMaxConsumption(final int max) {
        throw new UnsupportedOperationException(
                "You cannot set a max consumption time for this site.");
    }

    /**
     * Sets the base consumption.
     *
     * @param base The base consumption level.
     * @return a sitebuilder (Fluent API).
     */
    default SiteBuilder withBaseConsumption(final int base) {
        throw new UnsupportedOperationException(
                "You cannot set a base consumption time for this site.");
    }

    /**
     * Sets the number of flex tuples.
     *
     * @param tuples The number of tuples.
     * @return a sitebuilder (Fluent API).
     */
    default SiteBuilder withTuples(final int tuples) {
        throw new UnsupportedOperationException(
                "You cannot set a a number of tuples for this site.");
    }

    /**
     * Creates a Site instance.
     *
     * @return a sitebuilder (Fluent API).
     */
    public abstract Site create();

    static final class ConcreteSiteBuilder implements SiteBuilder {
        private final List<FlexProcess> lines;

        private ConcreteSiteBuilder() {
            lines = Lists.newArrayList();
        }

        @Override
        public SiteBuilder addLine(final FlexProcess line) {
            throw new UnsupportedOperationException(
                    "You cannot add lines to this site.");
        }

        @Override
        public Site create() {
            return new SiteImpl(lines.toArray(new FlexProcess[lines.size()]));
        }

    }

    static class SiteSimulationBuilder implements SiteBuilder {
        protected int min;
        protected int max;
        protected int base;
        protected int cease;
        protected int ramp;
        protected int duration;
        protected int tuples;

        protected SiteSimulationBuilder() {
            duration = 1;
            tuples = 6;
        }

        @Override
        public SiteBuilder withCeaseTime(final int ceaseTime) {
            this.cease = ceaseTime;
            return this;

        }

        @Override
        public SiteBuilder withReactionTime(final int reaction) {
            this.ramp = reaction;
            return this;

        }

        @Override
        public SiteBuilder withFlexDuration(final int duration) {
            this.duration = duration;
            return this;

        }

        @Override
        public SiteBuilder withMinConsumption(final int min) {
            this.min = min;
            return this;

        }

        @Override
        public SiteBuilder withMaxConsumption(final int max) {
            this.max = max;
            return this;

        }

        @Override
        public SiteBuilder withBaseConsumption(final int base) {
            this.base = base;
            return this;
        }

        @Override
        public SiteBuilder withTuples(final int tuples) {
            this.tuples = tuples;
            return this;
        }

        @Override
        public Site create() {
            return new SiteSimulation(base, min, max, tuples, duration, ramp,
                    cease);
        }

    }

    static final class EquidistantSiteBuilder
            extends SiteSimulationBuilder {
        @Override
        public Site create() {
            return new EquidistantSiteSimulation(base, min, max, tuples,
                    duration, ramp, cease);
        }
    }
}
