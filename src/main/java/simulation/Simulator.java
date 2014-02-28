package simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import time.Clock;

public class Simulator implements ISimulationContext {

	private static final int RUNNERTHREADSLEEPDURATION = 500;
	private volatile boolean runflag;
	private int duration;
	private Clock clock;
	private List<ISimulationComponent> components;

	public Simulator() {
		this.runflag = false;
		this.duration = 0;
		this.clock = new Clock();
		this.components = new ArrayList<ISimulationComponent>();
	}

	public boolean isRunning() {
		return this.runflag;
	}

	public void start(boolean immediateReturn) {
		setFlag(true);
		simloop();
		while (!immediateReturn && runflag) {
			sleep(RUNNERTHREADSLEEPDURATION);
		}
	}

	private void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void simloop() {
		new Thread(new Runnable() {
			public void run() {
				while (shouldRun()) {
					getClock().addTimeStep(1);
					tickComponents();
				}
				setFlag(false);
			}
		}).start();

	}

	private synchronized void tickComponents() {
		for (ISimulationComponent c : components) {
			c.tick(1);
		}
	}

	private boolean shouldRun() {
		if (!runflag)
			return false;
		if (getDuration() > 0 && getClock().getTimeCount() >= getDuration())
			return false;
		return true;

	}

	private Clock getClock() {
		return this.clock;
	}

	public void setDuration(int i) {
		this.duration = i;

	}

	public int getDuration() {
		return this.duration;
	}

	@Override
	public void register(ISimulationComponent comp) {
		this.components.add(comp);

	}

	public Collection<ISimulationComponent> getComponents() {
		return Collections.unmodifiableCollection(components);
	}

	public void stop() {
		setFlag(false);
	}

	private void setFlag(boolean flag) {
		this.runflag = flag;
	}

}
