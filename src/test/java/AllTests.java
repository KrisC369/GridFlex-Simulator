import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.flexsim.domain.aggregation.AggregatorImplTest;
import be.kuleuven.cs.flexsim.domain.finance.FinanceTrackerTest;
import be.kuleuven.cs.flexsim.domain.process.ProductionLineTest;
import be.kuleuven.cs.flexsim.domain.resource.ResourceTest;
import be.kuleuven.cs.flexsim.domain.site.FlexTupleTest;
import be.kuleuven.cs.flexsim.domain.site.SiteTest;
import be.kuleuven.cs.flexsim.domain.util.BufferTest;
import be.kuleuven.cs.flexsim.domain.workstation.WorkstationTest;
import be.kuleuven.cs.flexsim.simulation.SimulatorTest;
import be.kuleuven.cs.flexsim.time.ClockTest;

@RunWith(Suite.class)
@SuiteClasses({ SimulatorTest.class, ClockTest.class, BufferTest.class,
        WorkstationTest.class, ProductionLineTest.class, ResourceTest.class,
        FinanceTrackerTest.class, ScenarioTest.class, FlexTupleTest.class,
        SiteTest.class, AggregatorImplTest.class })
public class AllTests {
}
