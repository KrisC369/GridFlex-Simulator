package be.kuleuven.cs.gametheory;

import java.util.Arrays;

import javax.annotation.Nullable;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
class PayoffEntry {
    private int[] entries;

    PayoffEntry(int... entries) {
        this.entries = entries;
    }

    int[] getEntries() {
        return Arrays.copyOf(entries, entries.length);
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
        result = prime * result + Arrays.hashCode(entries);
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
        PayoffEntry other = (PayoffEntry) obj;
        if (!Arrays.equals(entries, other.entries))
            return false;
        return true;
    }

    static PayoffEntry from(int... key) {
        return new PayoffEntry(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PayoffEntry ").append(Arrays.toString(entries))
                .append("");
        return builder.toString();
    }
}
