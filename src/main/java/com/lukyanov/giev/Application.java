package com.lukyanov.giev;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.lukyanov.giev.graphics.SimpleGievGraphics;
import com.lukyanov.giev.util.CoordinatesExecutor;

//import com.lukyanov.giev.graphics.SimpleGievGraphics;


public class Application {

   private static final String TITLE = "y=(x1^2+x2^2)^0.25*(sin^2(50*(x1^2+x2^2)^0.1)+1), x \u2208[-100,100]";
   private static final int FROM = -100;
   private static final int TO = 100;

   private JButton runAlgorithm;
   private JPanel panelMain;
   private JTextField populationSize;
   private JTextField crossingOverP;
   private JTextField mutationP;
   private JTextField generationsCount;
   private JPanel chartPanelWrapper;
   private JLabel averageLabel;
   private JLabel minLabel;

   private XYSeriesCollection dataset;

   private SimpleGievGraphics simpleGievGraphics;


   public Application() {
      runAlgorithm.addActionListener(e -> {
         simpleGievGraphics
               .run(Integer.valueOf(generationsCount.getText()),
                     Integer.valueOf(populationSize.getText()),
                     Double.valueOf(crossingOverP.getText()),
                     Double.valueOf(mutationP.getText()),
                     FROM,
                     TO);
      });
   }


   public static void main(String[] args) throws InterruptedException {
      JFrame frame = new JFrame("Giev1");
      Application app = new Application();

      frame.setContentPane(app.panelMain);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);

      while (true) {
         if (app.simpleGievGraphics.isState()) {
            int i = 0;
            for (XYSeries series : app.simpleGievGraphics.getSeriesList()) {
               app.dataset.getSeries(0).clear();

               for (Object item : series.getItems()) {
                  app.dataset.getSeries(0).add((XYDataItem) item);
               }

               app.averageLabel.setText(String.valueOf(app.simpleGievGraphics.getAverageList().get(i)));
               app.minLabel.setText(String.valueOf(app.simpleGievGraphics.getMinList().get(i)));
               i++;
               Thread.sleep(1000);
            }
            app.simpleGievGraphics.setState(false);
            app.simpleGievGraphics.setSeriesList(new ArrayList<>());
            app.simpleGievGraphics.setAverageList(new ArrayList<>());
            app.simpleGievGraphics.setMinList(new ArrayList<>());
         }
         else {
            Thread.sleep(1000);
         }
      }
   }

   private XYSeriesCollection createDataset() {
      XYSeriesCollection dataset = new XYSeriesCollection();

      XYSeries chromosomes = new XYSeries("chromosomes");
      dataset.addSeries(chromosomes);

      int i = 0;
      for (java.util.List<Pair<Double, Double>> pairs : CoordinatesExecutor.generateXYPairs(FROM, TO, 1)) {
         XYSeries series = new XYSeries(i++, true, true);
         for (Pair<Double, Double> pair : pairs) {
            series.add(pair.getLeft(), pair.getRight());
         }

         dataset.addSeries(series);
      }

      return dataset;
   }

   private JFreeChart createChart(XYDataset dataset) {

      JFreeChart chart = ChartFactory.createXYLineChart(
            TITLE,
            "x2",
            "x1",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
      );

      XYPlot plot = chart.getXYPlot();

      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
      renderer.setSeriesShape(0, new Rectangle(10, 10));
      renderer.setSeriesPaint(0, Color.BLACK);

      plot.setRenderer(renderer);
      plot.setBackgroundPaint(Color.white);

      plot.setRangeGridlinesVisible(true);
      plot.setRangeGridlinePaint(Color.BLACK);

      plot.setDomainGridlinesVisible(true);
      plot.setDomainGridlinePaint(Color.BLACK);

      chart.getLegend().setFrame(BlockBorder.NONE);

      chart.setTitle(new TextTitle(TITLE,
                  new Font("Serif", java.awt.Font.BOLD, 18)
            )
      );

      return chart;

   }

   private void createUIComponents() {
      dataset = createDataset();


      JFreeChart chart = createChart(dataset);

      ChartPanel chartPanel = new ChartPanel(chart);
      chartPanel.setChart(chart);
      chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
      chartPanel.setBackground(Color.white);
      chartPanel.setSize(300, 300);
      chartPanel.setRefreshBuffer(true);

      simpleGievGraphics = new SimpleGievGraphics(dataset, chartPanel);

      chartPanelWrapper = chartPanel;
      // TODO: place custom component creation code here
   }
}
