package foo.bar.ui;

import foo.bar.ui.model.VersionDiff;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

/**
 * @author Heiko Braun
 * @since 24/09/15
 */
public class SSIChart extends VBox {

    private final XYChart.Series ssiSeries;
    private final XYChart.Series csiSeries;

    public SSIChart() {


        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> csi = new BarChart<String,Number>(xAxis,yAxis);
        csi.setTitle("CSI");
        xAxis.setLabel("Release");
        yAxis.setLabel("CSI");
        csi.setLegendVisible(false);

        csiSeries = new XYChart.Series();
        csiSeries.setName("KCSI");

        ssiSeries = new XYChart.Series();
        ssiSeries.setName("KSSI");

        csi.getData().addAll(ssiSeries, csiSeries);


        getChildren().add(csi);

    }

    public void updateFrom(ObservableList<VersionDiff> changes) {
        ssiSeries.getData().clear();
        csiSeries.getData().clear();

        for (VersionDiff change : changes) {
            String label = change.getTagTo().getRevName();
            double ssi = Math.log10(change.getSsi());
            double csi = Math.abs(change.getCsi() / 1000); //Math.log10(change.getCsi());
            //ssiSeries.getData().add(new XYChart.Data(label, ssi));
            csiSeries.getData().add(new XYChart.Data(label, csi));
        }
    }
}
