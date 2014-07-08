package be.kuleuven.cs.flexsim.site;

import javax.annotation.Nullable;

/**
 * Tuples representing flexibility.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public class FlexTuple {

    private final int id;
    private final int deltaP;
    private final boolean direction;
    private final int t;
    private final int t_r;
    private final int t_c;

    FlexTuple(int id, int deltaP, boolean direction, int t, int t_r, int t_c) {
        this.id = id;
        this.deltaP = deltaP;
        this.direction = direction;
        this.t = t;
        this.t_r = t_r;
        this.t_c = t_c;
    }

    /**
     * Creation method for a FlexTuple.
     * 
     * @param id
     *            The id
     * @param deltaP
     *            the deltaP value
     * @param direction
     *            the direction value
     * @param t
     *            the duration time value
     * @param t_r
     *            the reaction time value
     * @param t_c
     *            the cease time value
     * @return A new Tuple representing these values
     */
    public static FlexTuple create(int id, int deltaP, boolean direction,
            int t, int t_r, int t_c) {
        return new FlexTuple(id, deltaP, direction, t, t_r, t_c);
    }

    /**
     * Returns a tuple representing no flex.
     * 
     * @return an empty tuple.
     */
    public static FlexTuple createNONE() {
        return new FlexTuple(0, 0, false, 0, 0, 0);
    }

    /**
     * @return the id
     */
    public final int getId() {
        return id;
    }

    /**
     * @return the deltaP
     */
    public final int getDeltaP() {
        return deltaP;
    }

    /**
     * @return the direction
     */
    public final boolean getDirection() {
        return direction;
    }

    /**
     * @return the t
     */
    public final int getT() {
        return t;
    }

    /**
     * @return the t_r
     */
    public final int getT_r() {
        return t_r;
    }

    /**
     * @return the t_c
     */
    public final int getT_c() {
        return t_c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + deltaP;
        result = prime * result + (direction ? 1231 : 1237);
        result = prime * result + id;
        result = prime * result + t;
        result = prime * result + t_c;
        result = prime * result + t_r;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FlexTuple)) {
            return false;
        }
        FlexTuple other = (FlexTuple) obj;
        if (deltaP != other.deltaP) {
            return false;
        }
        if (direction != other.direction) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (t != other.t) {
            return false;
        }
        if (t_c != other.t_c) {
            return false;
        }
        if (t_r != other.t_r) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FlexTuple [id=");
        builder.append(id);
        builder.append(", deltaP=");
        builder.append(deltaP);
        builder.append(", direction=");
        builder.append(direction);
        builder.append(", t=");
        builder.append(t);
        builder.append(", t_r=");
        builder.append(t_r);
        builder.append(", t_c=");
        builder.append(t_c);
        builder.append("]");
        return builder.toString();
    }

}
