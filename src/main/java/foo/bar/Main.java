package foo.bar;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gitective.core.CommitFinder;
import org.gitective.core.filter.commit.DiffLineCountFilter;

import java.io.File;

/**
 * @author Heiko Braun
 * @since 23/04/15
 */
public class Main {

    private static final String TAG_A = "2.6.6.Final";
    private static final String TAG_B = "2.6.7.Final";

    static String[] tags = {
            "2.7.1.Final",
            "2.7.2.Final"
    };

    public static void main(String[] args) throws Exception {

        assert args.length > 0 : "Arguments missing";
        String repositoryPath = args[0] + File.separator + ".git";

        // ----

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File(repositoryPath))
                .readEnvironment()
                .findGitDir()
                .setMustExist(true)
                .build();

        // ----


        try {
            int i;

            for(i=0;i<tags.length;i++){
                if(i+1<tags.length)
                {
                    String a = "refs/tags/"+tags[i];
                    String b = "refs/tags/"+tags[i + 1];
                    //commitInfo(repository, a, b);
                    calculateCSI(repository, a, b);
                }
            }

            // the last chunk
            calculateCSI(repository, "refs/tags/"+tags[i-1], "HEAD");

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            repository.close();
        }


    }

    private static void commitInfo(Repository repository, String a, String b)  throws Exception {
        RevWalk walk = new RevWalk(repository);

        RevCommit commitA = walk.parseCommit(repository.resolve(a));
        RevCommit commitB = walk.parseCommit(repository.resolve(b));

        System.out.println(commitA.getType());
        walk.dispose();
    }

    private static void calculateCSI(Repository repository, String a, String b) throws Exception{

        System.out.println("Processing "+a+">"+b);

        CommitFinder finder = new CommitFinder(repository);

        DiffLineCountFilter filter = new DiffLineCountFilter();
        finder.setFilter(filter);

        ObjectId start = repository.resolve(b);
        ObjectId end = repository.resolve(a);

        finder.findBetween(start, end);

        System.out.println("Added:\t"+filter.getAdded());
        System.out.println("Changed:\t"+filter.getEdited());
        System.out.println("Deleted:\t"+filter.getDeleted());

        System.out.println("CSI:\t"+(filter.getAdded()+filter.getEdited()));

        //System.out.println(bugCommits.getCount() + " total bugs fixed");
    }
}
