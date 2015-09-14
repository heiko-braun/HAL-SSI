package foo.bar.ui;

/**
 * @author Heiko Braun
 * @since 14/09/15
 */

import foo.bar.ui.model.Tag;
import foo.bar.ui.model.VersionDiff;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.gitective.core.CommitFinder;
import org.gitective.core.filter.commit.DiffLineCountFilter;

import java.util.LinkedList;
import java.util.List;

public class GitStats extends Application {

    private TagList tags;
    private SSITable ssiTable;
    private Config config = new Config();



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        this.tags = new TagList(this);
        this.ssiTable = new SSITable(this);

        primaryStage.setTitle("Git Project Statistics");

        BorderPane grid = new BorderPane();

        // -------

        grid.setLeft(tags);

        // update tags

        refreshTags();


        // -------

        TabPane tabPane = new TabPane();

        Tab ssiTab = new Tab();
        ssiTab.setText("SSI");
        ssiTab.setContent(new Text("SSI goes here"));

        ssiTable = new SSITable(this);
        ssiTab.setContent(ssiTable);

        Tab issuesTab = new Tab();
        issuesTab.setText("Issues");
        issuesTab.setContent(new Text("SSI goes here"));

        tabPane.getTabs().add(ssiTab);
        tabPane.getTabs().add(issuesTab);

        grid.setCenter(tabPane);

        Scene scene = new Scene(grid, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void refreshTags() {
        ObservableList<Tag> data =
                FXCollections.observableArrayList(
                        new Tag("2.8.7", "04-09-2015"),
                        new Tag("2.8.8.Final", "08-09-2015"),
                        new Tag("2.8.9.Final", "14-09-2015")
                );

        tags.setItems(data);
    }

    private static void commitInfo(Repository repository, String a, String b)  throws Exception {
        RevWalk walk = new RevWalk(repository);

        RevCommit commitA = walk.parseCommit(repository.resolve(a));
        RevCommit commitB = walk.parseCommit(repository.resolve(b));

        System.out.println(commitA.getType());
        walk.dispose();
    }

    public void onTagSelection(List<Tag> tags) {
        Repository repository = config.getRepository();

        try {
            int i;

            List<VersionDiff> changes = new LinkedList<>();
            for(i=0;i<tags.size();i++){
                if(i+1<tags.size())
                {
                    String a = "refs/tags/"+tags.get(i);
                    String b = "refs/tags/"+tags.get(i + 1);


                    changes.add(calculateCSI(repository, a, b));
                }
            }

            // the last chunk
            changes.add(calculateCSI(repository, "refs/tags/" + tags.get(i - 1), "HEAD"));

            ssiTable.updateFrom(FXCollections.observableArrayList(changes));

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            repository.close();
        }
    }

    private VersionDiff calculateCSI(Repository repository, String a, String b) throws Exception{

        System.out.println("Processing "+a+">"+b);

        CommitFinder finder = new CommitFinder(repository);

        DiffLineCountFilter filter = new DiffLineCountFilter();
        finder.setFilter(filter);

        ObjectId start = repository.resolve(b);
        ObjectId end = repository.resolve(a);

        finder.findBetween(start, end);

        /*System.out.println("Added:\t"+filter.getAdded());
        System.out.println("Changed:\t"+filter.getEdited());
        System.out.println("Deleted:\t"+filter.getDeleted());

        System.out.println("CSI:\t" + (filter.getAdded() + filter.getEdited()));*/

        return new VersionDiff(
                b,
                filter.getAdded(),
                filter.getEdited(),
                filter.getDeleted(),
                (filter.getAdded() + filter.getEdited()) // ssi
        );

        //System.out.println(bugCommits.getCount() + " total bugs fixed");
    }

}

