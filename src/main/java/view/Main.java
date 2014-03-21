package view;


import simulation.Simulator;
import domain.factory.ProductionLine;
import domain.resource.ResourceFactory;

public class Main{
    
    public static void main(String[] args) {
        Simulator s = Simulator.createSimulator(75);
        ProductionLine p = ProductionLine.createExtendedLayout();
        Grapher g1 = new Grapher.StepConsumptionGrapher();
        Grapher g2 = new Grapher.TotalComsumptionGrapher();
        s.register(p);
        s.register(g1);
        s.register(g2);
        p.deliverResources(ResourceFactory.createBulkMPResource(30, 3,1));
        s.start();
        g1.drawChart();
        g2.drawChart();
    }
}
