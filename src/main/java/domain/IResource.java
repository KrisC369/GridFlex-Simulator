package domain;

public interface IResource {
    public int getNeededProcessTime();

    public void process(int time);
}
