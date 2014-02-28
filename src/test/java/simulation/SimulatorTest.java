package simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class SimulatorTest {
	private Simulator s;
	private ISimulationComponent comp;

	@Before
	public void setUp() throws Exception {
		s = new Simulator();
		comp = mock(ISimulationComponent.class);
	}

	@Test
	public void testInitialState() {
		testStopped();
	}

	private void testStopped() {
		assertFalse(s.isRunning());
	}

	@Test
	public void testStartedSim() {
		s.start(true);
		sleep(500);
		testRunning();
		s.stop();
	}

	private void testRunning() {
		assertTrue(s.isRunning());
	}

	@Test
	public void testSimDuration() {
		s.setDuration(20);
		assertEquals(20, s.getDuration());
	}

	@Test
	public void testRegisterComp() {
		s.register(comp);
		assertEquals(1, s.getComponents().size());
	}

	@Test
	public void testRunDurationImmediateReturn() {
		s.setDuration(20);
		runSim(true);
		sleep(500);
		verify(comp, times(20)).tick(1);
		testStopped();
	}

	@Test
	public void testRunDurationNoImmediateReturn() {
		s.setDuration(20);
		runSim(false);
		verify(comp, times(20)).tick(1);
		testStopped();
	}
	
	@Test
	public void testRunNoDurationImmediateReturn() {
		runSim(true);
		testRunning();
		s.stop();
		testStopped();
	}

	private void runSim(boolean immediateReturn) {
		s.register(comp);
		s.start(immediateReturn);
		if (immediateReturn) {
			testRunning();
		} else {
			testStopped();
		}
	}

	private void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
