package simulation;

public interface ISimulationComponent {
    public void initialize(ISimulationContext context);

    public void tick();
}
