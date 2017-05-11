import be.kuleuven.cs.gridflex.domain.aggregation.AggregatorTest;
import be.kuleuven.cs.gridflex.domain.aggregation.brp.BRPAggregatorTest;
import be.kuleuven.cs.gridflex.domain.aggregation.brp.NominationTest;
import be.kuleuven.cs.gridflex.domain.aggregation.independent.IndependentAggregatorTest;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.BudgetTrackerTest;
import be.kuleuven.cs.gridflex.domain.aggregation.r3dp.PortfolioBalanceSolverTest;
import be.kuleuven.cs.gridflex.domain.aggregation.reactive.ReactiveMechanismAggregatorTest;
import be.kuleuven.cs.gridflex.domain.energy.dso.contractnet.DSMPartnerTest;
import be.kuleuven.cs.gridflex.domain.energy.dso.contractnet.DSMProposalTest;
import be.kuleuven.cs.gridflex.domain.energy.dso.contractnet.DSOIntegrationTest;
import be.kuleuven.cs.gridflex.domain.energy.generation.OutputGeneratorTest;
import be.kuleuven.cs.gridflex.domain.energy.generation.wind.TurbineSpecificationTest;
import be.kuleuven.cs.gridflex.domain.energy.tso.BalancingAuctionTSOTest;
import be.kuleuven.cs.gridflex.domain.energy.tso.BalancingTSOTest;
import be.kuleuven.cs.gridflex.domain.energy.tso.CopperPlateTSOTest;
import be.kuleuven.cs.gridflex.domain.energy.tso.RandomTSOTest;
import be.kuleuven.cs.gridflex.domain.finance.FinanceTrackerTest;
import be.kuleuven.cs.gridflex.domain.process.ProductionLineTest;
import be.kuleuven.cs.gridflex.domain.resource.ResourceTest;
import be.kuleuven.cs.gridflex.domain.site.EquidistantSiteSimulationTest;
import be.kuleuven.cs.gridflex.domain.site.SiteSimulationTest;
import be.kuleuven.cs.gridflex.domain.site.SiteTest;
import be.kuleuven.cs.gridflex.domain.util.BufferTest;
import be.kuleuven.cs.gridflex.domain.util.CollectionUtilsTest;
import be.kuleuven.cs.gridflex.domain.util.NPermuteAndCombinerTest;
import be.kuleuven.cs.gridflex.domain.util.TrapzPosTest;
import be.kuleuven.cs.gridflex.domain.util.data.FlexTupleTest;
import be.kuleuven.cs.gridflex.domain.util.data.WindSpeedForecastMultiHorizonErrorDistributionTest;
import be.kuleuven.cs.gridflex.domain.util.data.profiles.CongestionProfileTest;
import be.kuleuven.cs.gridflex.domain.util.listener.ListenerTest;
import be.kuleuven.cs.gridflex.domain.workstation.WorkstationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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
        DSOIntegrationTest.class, TurbineSpecificationTest.class,
        PortfolioBalanceSolverTest.class, WindSpeedForecastMultiHorizonErrorDistributionTest.class,
        BudgetTrackerTest.class })
public class DomainTests {
}
