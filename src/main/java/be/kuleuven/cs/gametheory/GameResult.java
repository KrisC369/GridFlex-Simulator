/**
 *
 */
package be.kuleuven.cs.gametheory;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public final class GameResult {
    private Map<String, String> description;
    private List<Double> results;

    public GameResult() {
        this.description = Maps.newLinkedHashMap();
        this.results = Lists.newArrayList();
    }

    public void addDescription(String key, String value) {
        this.description.put(key, value);
    }

    public void addResult(List<Double> dynamicEquationFactors) {
        this.results.addAll(dynamicEquationFactors);
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
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((results == null) ? 0 : results.hashCode());
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
        GameResult other = (GameResult) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (results == null) {
            if (other.results != null)
                return false;
        } else if (!results.equals(other.results))
            return false;
        return true;
    }

    /**
     * @return the description
     */
    public final Map<String, String> getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    private final void setDescription(Map<String, String> description) {
        this.description = description;
    }

    /**
     * @return the results
     */
    public final List<Double> getResults() {
        return results;
    }

    /**
     * @param results
     *            the results to set
     */
    private final void setResults(List<Double> results) {
        this.results = results;
    }
}
