package domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import simulation.ISimulationContext;
import simulation.Simulator;

public class WorkstationTest {

	private Buffer<IResource> in;
	private Buffer<IResource> out;
	private Workstation w;
	private ISimulationContext sim;

	@Before
	public void setUp() throws Exception {
		in = new Buffer<IResource>();
		out = new Buffer<IResource>();
		w = new Workstation(in, out);
		sim = mock(Simulator.class);
		sim.register(w);
	}

	@Test
	public void testInitial() {
		assertEquals(0, w.getProcessedItemsCount());
		assertTrue(w.isIdle());
	}

	@Test
	public void testProcessResourceSingleSteps() {
		int processCount = 3;
		IResource res = new SimpleResource(processCount);
		in.push(res);
		w.tick();
		testStateAfterProces1(res);
		w.tick();
		assertFalse(w.isIdle());
		multiTick(3);
		testStateAfterFinalPush(res);
	}

	@Test
	public void testProcessResourceWithLongerSteps() {
		int processCount = 3;
		IResource res = new SimpleResource(processCount);
		in.push(res);
		w.tick();
		testStateAfterProces1(res);
		w.tick();
		assertFalse(w.isIdle());
		multiTick(3);
		testStateAfterFinalPush(res);
	}

	@Test(expected = IllegalStateException.class)
	public void testDoubleSetResource() {
		IResource res = new SimpleResource(1);
		in.push(res);
		w.tick();
		testStateAfterProces1(res);
		w.setCurrentResource(res);
	}

	private void multiTick(int times) {
		for (; times > 0; times--) {
			w.tick();
		}
	}

	private void testStateAfterFinalPush(IResource res) {
		assertNull(w.getCurrentResource());
		assertTrue(in.isEmpty());
		assertTrue(w.isIdle());
		assertFalse(out.isEmpty());
		assertEquals(res, out.pull());
	}

	private void testStateAfterProces1(IResource res) {
		assertTrue(in.isEmpty());
		assertFalse(w.isIdle());
		assertTrue(out.isEmpty());
		assertEquals(res, w.getCurrentResource());
	}

}
