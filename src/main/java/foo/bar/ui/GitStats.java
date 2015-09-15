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
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.gitective.core.CommitFinder;
import org.gitective.core.filter.commit.DiffLineCountFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GitStats extends Application {

    private TagList tagList;
    private SSITable ssiTable;
    private Config config = new Config();
    private List<Tag> currentTags;
    private ExecutorService workers;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        workers.shutdown();
    }

    @Override
    public void start(Stage primaryStage) {

        this.workers = Executors.newFixedThreadPool(2);
        this.tagList = new TagList(this);
        this.ssiTable = new SSITable(this);

        primaryStage.setTitle("Git Project Statistics");

        BorderPane grid = new BorderPane();

        // -------

        grid.setLeft(tagList);

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

        // resize handler
        primaryStage.setResizable(true);


        // --- init

        // load tags
        currentTags = loadTags();

        // update view
        ObservableList<Tag> data = FXCollections.observableArrayList(currentTags);
        tagList.setItems(data);


    }

    private List<Tag> loadTags() {

        List<Tag> tags = new LinkedList<>();
        Repository repository = config.getRepository();
        final RevWalk walk = new RevWalk(repository);

        try {
            List<Ref> call = new Git(repository).tagList().call();

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
        finally {
            walk.dispose();
            repository.close();
        }

        Collections.sort(tags);
        return tags;

    }


    private ObjectId getActualRefObjectId(Ref ref) {
        if(ref.isPeeled())
            return config.getRepository().peel(ref).getObjectId();
        else
            return ref.getObjectId();
    }

    private List<VersionDiff> getChangesForTags(List<Tag> tags) {
        Repository repository = config.getRepository();
        List<VersionDiff> changes = new LinkedList<>();

        try {

            int i;
            for(i=0;i<tags.size();i++){
                if(i+1<tags.size())
                {

                    VersionDiff diff = calculateCSI(repository, tags.get(i), tags.get(i + 1));
                    if(diff.getCsi()>0) // bogus tags
                        changes.add(diff);
                }
            }

            // HEAD
                    /*VersionDiff headCsi = calculateCSI(repository, "refs/tags/" + tags.get(i - 1), "HEAD");
                    if(headCsi.getCsi()>0) // in some cases the last tag and head are the same
                        changes.add(headCsi);
                    */


        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            repository.close();
        }

        return changes;
    }

    public void onTagSelection(LinkedList<Tag> tags) {

        // calculate SSI for all changes (baseline)
        // TODO: should be cached


        Task task = new Task<Void>() {
            @Override public Void call() {

                List<VersionDiff> changes = getChangesForTags(currentTags);

                long ssi = 0;
                for (VersionDiff change : changes) {

                    if (0 == ssi) {
                        ssi = change.getCsi();
                    } else
                    {
                        ssi = (ssi + change.getCsi()) - change.getRemoved() - change.getChanged();
                    }

                    change.setSsi(ssi);
                }

                // filter the range of commits
                List<VersionDiff> range = changes
                        .stream()
                        .filter(c -> {
                            return c.getTagFrom().getVersion().greaterThanOrEqualTo(tags.getFirst().getVersion())
                                    && c.getTagFrom().getVersion().lessThanOrEqualTo(tags.getLast().getVersion());
                        })
                        .collect(Collectors.toList());

                ssiTable.updateFrom(FXCollections.observableArrayList(range));


                return null;
            }
        };

        workers.submit(task);
    }

    private VersionDiff calculateCSI(Repository repository, Tag tagFrom, Tag tagTo) throws Exception{

        CommitFinder finder = new CommitFinder(repository);

        DiffLineCountFilter filter = new DiffLineCountFilter();
        finder.setFilter(filter);

        String a = "refs/tags/"+tagFrom.getRevName();
        String b = "refs/tags/"+tagTo.getRevName();

        // git traversal
        ObjectId start = repository.resolve(b);
        ObjectId end = repository.resolve(a);
        finder.findBetween(start, end);

        return new VersionDiff(
                tagFrom, tagTo,
                filter.getAdded(),
                filter.getEdited(),
                filter.getDeleted(),
                (filter.getAdded() + filter.getEdited()) // csi
        );

    }

}

