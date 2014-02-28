package domain;

public class SimpleResource implements IResource {
	private int neededTime;

	public SimpleResource(int i) {
		this.neededTime = i;
	}

	@Override
	public int getNeededProcessTime() {
		return this.neededTime;
	}

	@Override
	public void process(int time) {
		this.neededTime--;
	}

}
