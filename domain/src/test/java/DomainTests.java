import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.flexsim.domain.aggregation.AggregatorTest;
import be.kuleuven.cs.flexsim.domain.aggregation.brp.BRPAggregatorTest;
import be.kuleuven.cs.flexsim.domain.aggregation.brp.NominationTest;
import be.kuleuven.cs.flexsim.domain.aggregation.independent.IndependentAggregatorTest;
import be.kuleuven.cs.flexsim.domain.aggregation.reactive.ReactiveMechanismAggregatorTest;
import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.DSMPartnerTest;
import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.DSMProposalTest;
import be.kuleuven.cs.flexsim.domain.energy.dso.contractnet.DSOIntegrationTest;
import be.kuleuven.cs.flexsim.domain.energy.generation.OutputGeneratorTest;
import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingAuctionTSOTest;
import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingTSOTest;
import be.kuleuven.cs.flexsim.domain.energy.tso.CopperPlateTSOTest;
import be.kuleuven.cs.flexsim.domain.energy.tso.RandomTSOTest;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTrackerTest;
import be.kuleuven.cs.flexsim.domain.process.ProductionLineTest;
import be.kuleuven.cs.flexsim.domain.resource.ResourceTest;
import be.kuleuven.cs.flexsim.domain.site.EquidistantSiteSimulationTest;
import be.kuleuven.cs.flexsim.domain.site.SiteSimulationTest;
import be.kuleuven.cs.flexsim.domain.site.SiteTest;
import be.kuleuven.cs.flexsim.domain.util.BufferTest;
import be.kuleuven.cs.flexsim.domain.util.CollectionUtilsTest;
import be.kuleuven.cs.flexsim.domain.util.CongestionProfileTest;
import be.kuleuven.cs.flexsim.domain.util.NPermuteAndCombinerTest;
import be.kuleuven.cs.flexsim.domain.util.TrapzPosTest;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTupleTest;
import be.kuleuven.cs.flexsim.domain.util.listener.ListenerTest;
import be.kuleuven.cs.flexsim.domain.workstation.WorkstationTest;

@RunWith(Suite.class)
@SuiteClasses({ BufferTest.class, WorkstationTest.class,
        ProductionLineTest.class, ResourceTest.class, FinanceTrackerTest.class,
        FlexTupleTest.class, SiteTest.class, AggregatorTest.class,
        BRPAggregatorTest.class, IndependentAggregatorTest.class,
        ReactiveMechanismAggregatorTest.class, RandomTSOTest.class,
        NPermuteAndCombinerTest.class, CopperPlateTSOTest.class,
        OutputGeneratorTest.class, SiteSimulationTest.class,
        BalancingAuctionTSOTest.class, BalancingTSOTest.class,
        EquidistantSiteSimulationTest.class, ListenerTest.class,
        NominationTest.class, CongestionProfileTest.class, TrapzPosTest.class,
        CollectionUtilsTest.class, DSMProposalTest.class, DSMPartnerTest.class,
        DSOIntegrationTest.class })
public class DomainTests {
}
