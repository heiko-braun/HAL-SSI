package foo.bar.ui;

import foo.bar.ui.model.VersionDiff;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * @author Heiko Braun
 * @since 14/09/15
 */
public class SSITable extends VBox {

    private final GitStats presenter;
    private final SSIChart chart;
    private TableView<VersionDiff> table;

    public SSITable(GitStats presenter) {
        super();
        this.presenter = presenter;
        setFillWidth(true);

        setPadding(new Insets(10, 5, 10, 5));

        final Label label = new Label("SSI Values");

        table = new TableView();

        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn diff = new TableColumn("Version");
        diff.setCellValueFactory(new PropertyValueFactory<>("range"));

        TableColumn added = new TableColumn("Added");
        added.setCellValueFactory(new PropertyValueFactory<>("added"));

        TableColumn changed = new TableColumn("Changed");
        changed.setCellValueFactory(new PropertyValueFactory<>("changed"));

        TableColumn removed = new TableColumn("Removed");
        removed.setCellValueFactory(new PropertyValueFactory<>("removed"));

        TableColumn csi = new TableColumn("CSI");
        csi.setCellValueFactory(new PropertyValueFactory<>("csi"));

        TableColumn ssi = new TableColumn("SSI");
        ssi.setCellValueFactory(new PropertyValueFactory<>("ssi"));

        table.getColumns().addAll(diff, added, changed, removed, csi, ssi);

        table.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<VersionDiff>() {
                    @Override
                    public void changed(ObservableValue observableValue, VersionDiff oldValue, VersionDiff newValue) {

                        /* ObservableList<TablePosition> selectedCells = table.getSelectionModel().getSelectedCells();
                         if (2==selectedCells.size()) {
                             Iterator<TablePosition> it = selectedCells.iterator();
                             TablePosition from = it.next();
                             TablePosition to = it.next();

                             Tag[] tags = retrieve(from.getRow(), to.getRow());
                             System.out.println(tags[0].getVersion() + " > " + tags[1].getVersion());
                         }*/
                    }
                }
        );

        getChildren().addAll(label, table);


        chart = new SSIChart();
        getChildren().add(chart);

        setVgrow(table, Priority.ALWAYS);
    }

    public void updateFrom(ObservableList<VersionDiff> changes) {

        table.setItems(changes);

        chart.updateFrom(changes);
    }

}
