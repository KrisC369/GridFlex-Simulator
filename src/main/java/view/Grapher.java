package view;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import simulation.InstrumentationComponent;
import simulation.SimulationContext;
import be.kuleuven.cs.gridlock.simulation.events.Event;

import com.google.common.eventbus.Subscribe;

public abstract class Grapher extends ApplicationFrame implements
        InstrumentationComponent {
    private final XYSeries series;
    private final String name;

    public Grapher(String title) {
        super(title);
        series = new XYSeries(title);
        name = title;
    }

    @Override
    public void initialize(SimulationContext context) {
    }
    
    @Subscribe
    public void recordReport(Event e) {
        if (e.getType().contains("report")) {
            record(e);
        }

    }
    
    protected abstract void record(Event e);
    
    protected void addRecord(long x, long y){
        series.add(x, y);
    }
    
    public void drawChart() {
        final XYSeriesCollection data = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                name+ "Graph", "time", name + " of Energy", data,
                PlotOrientation.VERTICAL, true, true, false);

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
        
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }
    
    public static class StepConsumptionGrapher extends Grapher {
        public StepConsumptionGrapher() {
            super("Step consumption");
        }

        protected void record(Event e) {
            int t = e.getAttribute("time", Integer.class);
            long y = e.getAttribute("totalLaststepE", Long.class);
            addRecord(t, y);
        }
    }
    
    public static class TotalComsumptionGrapher extends Grapher {
        public TotalComsumptionGrapher() {
            super("Total consumption");
        }

        protected void record(Event e) {
            int t = e.getAttribute("time", Integer.class);
            long y = e.getAttribute("totalTotalE", Long.class);
            addRecord(t, y);
        }

    }
    
}
