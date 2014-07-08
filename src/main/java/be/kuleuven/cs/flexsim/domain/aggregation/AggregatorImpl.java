package be.kuleuven.cs.flexsim.domain.aggregation;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.site.Site;
import be.kuleuven.cs.flexsim.domain.tso.SteeringSignal;

import com.google.common.collect.Lists;

/**
 * Represents an energy aggregator implementation
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public class AggregatorImpl {
    private List<Site> clients;
    private SteeringSignal tso;

    /**
     * Default constructor
     * 
     * @param tso
     *            the tso.
     */
    public AggregatorImpl(SteeringSignal tso) {
        this.clients = Lists.newArrayList();
        this.tso = tso;
    }

    /**
     * @return the clients
     */
    final List<Site> getClients() {
        return clients;
    }

    /**
     * @return the tso
     */
    final SteeringSignal getTso() {
        return tso;
    }

}
