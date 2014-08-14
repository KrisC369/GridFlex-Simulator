package be.kuleuven.cs.flexsim.domain.process;

import java.util.List;

import be.kuleuven.cs.flexsim.domain.resource.Resource;
import be.kuleuven.cs.flexsim.domain.util.Buffer;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;
import be.kuleuven.cs.flexsim.domain.workstation.Workstation;
import be.kuleuven.cs.flexsim.simulation.UIDGenerator;

import com.google.common.collect.LinkedListMultimap;

import edu.uci.ics.jung.graph.Graph;

/**
 * An aspect of flexibility. A certain measure or category of flexibility is
 * represented by this instance. Allows for the calculation of this aspect.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
interface FlexAspect<T extends Workstation> {
    FlexDTO<List<FlexTuple>, LinkedListMultimap<Long, Workstation>> getFlexibility(
            List<T> stations);

    void initialize(UIDGenerator generator,
            Graph<Buffer<Resource>, Workstation> layout);
}
