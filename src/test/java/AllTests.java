import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.cs.flexsim.domain.factory.ProductionLineTest;
import be.kuleuven.cs.flexsim.domain.finances.FinanceTrackerTest;
import be.kuleuven.cs.flexsim.domain.resource.ResourceTest;
import be.kuleuven.cs.flexsim.domain.util.BufferTest;
import be.kuleuven.cs.flexsim.domain.workstation.WorkstationTest;
import be.kuleuven.cs.flexsim.simulation.SimulatorTest;
import be.kuleuven.cs.flexsim.site.FlexTupleTest;
import be.kuleuven.cs.flexsim.site.SiteTest;
import be.kuleuven.cs.flexsim.time.ClockTest;

@RunWith(Suite.class)
@SuiteClasses({ SimulatorTest.class, ClockTest.class, BufferTest.class,
        WorkstationTest.class, ProductionLineTest.class, ResourceTest.class,
        FinanceTrackerTest.class, ScenarioTest.class, FlexTupleTest.class,
        SiteTest.class })
public class AllTests {
}
