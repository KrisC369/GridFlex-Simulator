package be.kuleuven.cs.flexsim.domain.util.data;

import com.google.auto.value.AutoValue;

/**
 * Tuples representing a power flexibility profile. This tuple is a 6-element
 * tuple consisting of a unique ID, the amount of power represented by this
 * profile, the direction (increase or decrease of consumption), the duration,
 * the lead time needed to activate and the time needed to recover afterwards.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@AutoValue
public abstract class FlexTuple {

    /**
     * Represents no flex.
     */
    public static final FlexTuple NONE = createNONE();

    /**
     * Creation method for a FlexTuple.
     *
     * @param id        The id
     * @param deltaP    the deltaP value
     * @param direction the direction value
     * @param t         the duration time value
     * @param tR        the reaction time value
     * @param tC        the cease time value
     * @return A new Tuple representing these values
     */
    public static FlexTuple create(final long id, final int deltaP, final Direction direction,
            final int t, final int tR, final int tC) {
        return new AutoValue_FlexTuple(id, deltaP, direction, t, tR, tC);
    }

    /**
     * Returns a tuple representing no flex.
     *
     * @return an empty tuple.
     */
    public static FlexTuple createNONE() {
        return new AutoValue_FlexTuple(0, 0, Direction.DOWN, 0, 0, 0);
    }

    /**
     * @return the id
     */
    public abstract long getId();

    /**
     * @return the deltaP value. This value represents the amount of power
     * available in KW.
     */
    public abstract int getDeltaP();

    /**
     * Returns the direction of flexibility. Positive flexibility represents the
     * ability to consume more, while negative flexibility represents the
     * ability to curtail.
     *
     * @return the direction of flexibility. True for upflex (consume more) and
     * False for downflex (curtailment).
     */
    public abstract Direction getDirection();

    /**
     * @return the duration for which the deltaP power amount can be made
     * available.
     */
    public abstract int getT();

    /**
     * @return the lead time needed to activate the full profile.
     */
    public abstract int getTR();

    /**
     * @return the cease time after activation. This is the time this activation
     * needs to recover.
     */
    public abstract int getTC();

    /**
     * Flexbility direction in terms of injection(false) or offtake(up)
     */
    public enum Direction {
        UP(true), DOWN(false);

        private final boolean representation;

        Direction(boolean v) {
            this.representation = v;
        }

        public boolean booleanRepresentation() {
            return representation;
        }

        /**
         * @param rep The boolean representation of the desired direction.
         * @return the direction based on its boolean representation.
         */
        public static Direction fromRepresentation(boolean rep) {
            return rep ? up() : down();
        }

        /**
         * @return Direction.UP
         */
        public static Direction up() {
            return UP;
        }

        /**
         * @return Direction.DOWN
         */
        public static Direction down() {
            return DOWN;
        }

    }

}
