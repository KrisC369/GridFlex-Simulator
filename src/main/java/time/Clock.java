package time;

public class Clock {
	private int timecount;
	
	public Clock(){
		this.timecount = 0;
	}
	
	public int getTimeCount() {
		return timecount;
	}

	public void addTimeStep(int step) {
		this.timecount += step;
	}

	public void resetTime() {
		timecount = 0;
		
	}

}
