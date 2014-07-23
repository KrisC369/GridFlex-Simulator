package be.kuleuven.cs.flexsim.domain.util.data;

import javax.annotation.Nullable;

/**
 * Tuples representing flexibility.
 *
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
// TODO define flex direction semantics clearly.
public class FlexTuple {

    private final long id;
    private final int deltaP;
    private final boolean direction;
    private final int t;
    private final int tR;
    private final int tC;

    FlexTuple(long id, int deltaP, boolean direction, int t, int tR, int tC) {
        this.id = id;
        this.deltaP = deltaP;
        this.direction = direction;
        this.t = t;
        this.tR = tR;
        this.tC = tC;
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
    public static FlexTuple create(long id, int deltaP, boolean direction,
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
    public final long getId() {
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
    public final int getTR() {
        return tR;
    }

    /**
     * @return the t_c
     */
    public final int getTC() {
        return tC;
    }

    /**
     * @param obj
     */
    private boolean varEquals(Object obj) {
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
        if (tC != other.tC) {
            return false;
        }
        if (tR != other.tR) {
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
        builder.append(tR);
        builder.append(", t_c=");
        builder.append(tC);
        builder.append("]");
        return builder.toString();
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
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + t;
        result = prime * result + tC;
        result = prime * result + tR;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return varEquals(obj);
    }

}
