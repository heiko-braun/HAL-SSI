package foo.bar.ui;

import foo.bar.ui.model.Tag;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Heiko Braun
 * @since 14/09/15
 */
public class TagList extends VBox {

    private final GitStats presenter;
    private TableView<Tag> table;

    public TagList(GitStats presenter) {
        super();
        this.presenter = presenter;


        setSpacing(5);
        setPadding(new Insets(10, 0, 0, 10));

        final Label label = new Label("TAG's");

        this.table = new TableView();
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn nameCol = new TableColumn("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn dateCol = new TableColumn("Date");
        dateCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
                    @Override
                    public ObservableValue call(TableColumn.CellDataFeatures param) {
                        Tag tag = (Tag)param.getValue();
                        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
                        return new ReadOnlyObjectWrapper<String>(fmt.format(tag.getDate()));
                    }
                }
        );

        table.getColumns().addAll(nameCol, dateCol);

        table.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tag>() {
                    @Override
                    public void changed(ObservableValue observableValue, Tag oldValue, Tag newValue) {

                        ObservableList<TablePosition> selectedCells = table.getSelectionModel().getSelectedCells();
                        if (2==selectedCells.size()) {
                            Iterator<TablePosition> it = selectedCells.iterator();
                            TablePosition from = it.next();
                            TablePosition to = it.next();

                            LinkedList<Tag> slice = getSlice(from.getRow(), to.getRow());
                            presenter.onTagSelection(slice);
                        }
                    }
                }
        );

        getChildren().addAll(label, table);

    }

    public LinkedList<Tag> getSlice(int from, int to) {

        LinkedList<Tag> tags = new LinkedList<>();
        Iterator<Tag> iterator = table.getItems().iterator();
        int i=0;
        while(iterator.hasNext())
        {
            Tag next = iterator.next();
            if(i>=from || i==to) {
                tags.add(next); ;
            }
            else if(i>to) {
                break;
            }

            i++;
        }

        return tags;
    }

    public void setItems(ObservableList<Tag> items){
        table.setItems(items);
    }
}
