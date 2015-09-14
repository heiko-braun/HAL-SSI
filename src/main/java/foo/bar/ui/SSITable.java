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
import javafx.scene.layout.VBox;

/**
 * @author Heiko Braun
 * @since 14/09/15
 */
public class SSITable extends VBox {

    private TableView<VersionDiff> table;

     public SSITable(GitStats presenter) {
         super();


         setSpacing(5);
         setPadding(new Insets(10, 0, 0, 10));

         final Label label = new Label("SSI Values");

         this.table = new TableView();
         table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

         TableColumn diff = new TableColumn("Version");
         diff.setCellValueFactory(new PropertyValueFactory<>("version"));

         TableColumn added = new TableColumn("Added");
         added.setCellValueFactory(new PropertyValueFactory<>("added"));

         TableColumn changed = new TableColumn("Changed");
         changed.setCellValueFactory(new PropertyValueFactory<>("changed"));

         TableColumn removed = new TableColumn("Removed");
         removed.setCellValueFactory(new PropertyValueFactory<>("removed"));

         TableColumn ssi = new TableColumn("SSI");
         ssi.setCellValueFactory(new PropertyValueFactory<>("ssi"));

         table.getColumns().addAll(diff, added, changed, removed, ssi);


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
                             System.out.println(tags[0].getName() + " > " + tags[1].getName());
                         }*/
                     }
                 }
         );

         getChildren().addAll(label, table);

     }

    public void updateFrom(ObservableList<VersionDiff> changes) {
        table.setItems(changes);

    }
}
