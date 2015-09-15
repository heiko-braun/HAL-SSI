package foo.bar.ui;

/**
 * @author Heiko Braun
 * @since 14/09/15
 */

import com.github.zafarkhaja.semver.Version;
import foo.bar.ui.model.Tag;
import foo.bar.ui.model.VersionDiff;
import foo.bar.util.Versions;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.gitective.core.CommitFinder;
import org.gitective.core.filter.commit.DiffLineCountFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class GitStats extends Application {

    private TagList tagList;
    private SSITable ssiTable;
    private Config config = new Config();



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        this.tagList = new TagList(this);
        this.ssiTable = new SSITable(this);

        primaryStage.setTitle("Git Project Statistics");

        BorderPane grid = new BorderPane();

        // -------

        grid.setLeft(tagList);

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
        issuesTab.setContent(new Text("Issues goes here"));

        tabPane.getTabs().add(ssiTab);
        tabPane.getTabs().add(issuesTab);

        grid.setCenter(tabPane);

        Scene scene = new Scene(grid, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void refreshTags() {

        List<Tag> tags = new LinkedList<>();
        try {
            Repository repository = config.getRepository();
            List<Ref> call = new Git(repository).tagList().call();

            final RevWalk walk = new RevWalk(repository);

            for (Ref ref : call) {

                String refName = ref.getName();
                String tagName = refName.substring(refName.lastIndexOf("/") + 1, refName.length());

                Date date = null;
                try {
                    RevCommit commit = walk.parseCommit(getActualRefObjectId(ref));
                    PersonIdent authorIdent = commit.getAuthorIdent();
                    date = authorIdent.getWhen();
                } catch (IOException e) {
                    System.out.println("Failed to parse COMMIT ("+tagName+"): " + e.getMessage());
                }

                Optional<Version> version = Versions.parseVersion(tagName);
                if(version.isPresent())
                tags.add(new Tag(version.get(), tagName, date));
            }
        } catch (GitAPIException e) {
            throw new RuntimeException("Failed to parse TAG's", e);
        }

        Collections.sort(tags);
        ObservableList<Tag> data = FXCollections.observableArrayList(tags);

        tagList.setItems(data);
    }


    private ObjectId getActualRefObjectId(Ref ref) {
        if(ref.isPeeled())
            return config.getRepository().peel(ref).getObjectId();
        else
            return ref.getObjectId();
    }

    private static void commitInfo(Repository repository, String a, String b)  throws Exception {
        RevWalk walk = new RevWalk(repository);

        RevCommit commitA = walk.parseCommit(repository.resolve(a));
        RevCommit commitB = walk.parseCommit(repository.resolve(b));

        System.out.println(commitA.getType());
        walk.dispose();
    }

    public void onTagSelection(List<Tag> tags) {

        tags.forEach(t -> System.out.println(t));

        Repository repository = null;

        try {
            repository = config.getRepository();

            int i;

            List<VersionDiff> changes = new LinkedList<>();
            for(i=0;i<tags.size();i++){
                if(i+1<tags.size())
                {
                    String a = "refs/tags/"+tags.get(i).getRevName();
                    String b = "refs/tags/"+tags.get(i + 1).getRevName();

                    VersionDiff diff = calculateCSI(repository, a, b);
                    if(diff.getCsi()>0) // bogus tags
                        changes.add(diff);
                }
            }

            // HEAD
            /*VersionDiff headCsi = calculateCSI(repository, "refs/tags/" + tags.get(i - 1), "HEAD");
            if(headCsi.getCsi()>0) // in some cases the last tag and head are the same
                changes.add(headCsi);
*/
            ssiTable.updateFrom(FXCollections.observableArrayList(changes));

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            repository.close();
        }
    }

    private VersionDiff calculateCSI(Repository repository, String a, String b) throws Exception{

        //System.out.println("Processing "+a+">"+b);

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
                a+" > "+b,
                filter.getAdded(),
                filter.getEdited(),
                filter.getDeleted(),
                (filter.getAdded() + filter.getEdited()) // csi
        );

        //System.out.println(bugCommits.getCount() + " total bugs fixed");
    }

}

