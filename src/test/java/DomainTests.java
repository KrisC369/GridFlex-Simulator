import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.flexsim.domain.aggregation.AggregatorImplTest;
import be.kuleuven.cs.flexsim.domain.energy.generation.OutputGeneratorTest;
import be.kuleuven.cs.flexsim.domain.energy.tso.CopperPlateTSOTest;
import be.kuleuven.cs.flexsim.domain.energy.tso.BalancingAuctionTSOTest;
import be.kuleuven.cs.flexsim.domain.energy.tso.RandomTSOTest;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTrackerTest;
import be.kuleuven.cs.flexsim.domain.process.ProductionLineTest;
import be.kuleuven.cs.flexsim.domain.resource.ResourceTest;
import be.kuleuven.cs.flexsim.domain.site.SiteSimulationTest;
import be.kuleuven.cs.flexsim.domain.site.SiteTest;
import be.kuleuven.cs.flexsim.domain.util.BufferTest;
import be.kuleuven.cs.flexsim.domain.util.NPermuteAndCombinerTest;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTupleTest;
import be.kuleuven.cs.flexsim.domain.workstation.WorkstationTest;

@RunWith(Suite.class)
@SuiteClasses({ BufferTest.class, WorkstationTest.class,
        ProductionLineTest.class, ResourceTest.class, FinanceTrackerTest.class,
        FlexTupleTest.class, SiteTest.class, AggregatorImplTest.class,
        RandomTSOTest.class, NPermuteAndCombinerTest.class,
        CopperPlateTSOTest.class, OutputGeneratorTest.class,
        SiteSimulationTest.class, BalancingAuctionTSOTest.class })
public class DomainTests {
}
