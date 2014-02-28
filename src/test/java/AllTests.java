import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import simulation.SimulatorTest;
import time.ClockTest;
import domain.BufferTest;
import domain.WorkstationTest;


@RunWith(Suite.class)
@SuiteClasses({SimulatorTest.class, ClockTest.class, BufferTest.class, WorkstationTest.class})
public class AllTests {

}
