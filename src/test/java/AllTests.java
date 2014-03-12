import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import simulation.SimulatorTest;
import time.ClockTest;
import domain.BufferTest;
import domain.ResourceTest;
import domain.factory.ProductionLineTest;
import domain.workstation.WorkstationTest;

@RunWith(Suite.class)
@SuiteClasses({ SimulatorTest.class, ClockTest.class, BufferTest.class,
        WorkstationTest.class, ProductionLineTest.class,ResourceTest.class })
public class AllTests {
}
