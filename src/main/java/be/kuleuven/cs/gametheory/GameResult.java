package be.kuleuven.cs.gametheory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Value class for game results.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
@AutoValue
public abstract class GameResult {
    /**
     * @return the description
     */
    public abstract ImmutableMap<String, String> getDescription();

    /**
     * @return the results
     */
    public abstract ImmutableList<Double> getResults();

    // static GameResultBuilder builder() {
    // return new AutoValue_GameResult2.GameResultBuilder();
    // }

    // static GameResult2 create(Map<String, String> descr, List<Double> res) {
    // // return new AutoValue_GameResult2(ImmutableMap.copyOf(descr),
    // // ImmutableList.copyOf(res));
    // // return new AutoValue_GameResult2
    // }

    public static Builder builder() {
        return create(new LinkedHashMap<String, String>(),
                new ArrayList<Double>()).createBuilder();
    }

    public static Builder from(GameResult target) {
        return target.createBuilder();
    }

    Builder createBuilder() {
        return new Builder();
    }

    static GameResult create(Map<String, String> description,
            List<Double> results) {
        return new AutoValue_GameResult(ImmutableMap.copyOf(description),
                ImmutableList.copyOf(results));
    }

    public class Builder {

        public Builder withDescription(String key, String val) {
            Map<String, String> newMap = Maps.newLinkedHashMap();
            newMap.putAll(getDescription());
            newMap.put(key, val);
            return create(newMap, getResults()).createBuilder();
        }

        public Builder setResult(List<Double> res) {
            return create(getDescription(), res).createBuilder();

        }

        public GameResult build() {
            return create(getDescription(), getResults());
        }
    }
}
