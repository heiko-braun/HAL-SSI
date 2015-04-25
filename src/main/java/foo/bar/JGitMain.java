package foo.bar;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Heiko Braun
 * @since 24/04/15
 */
public class JGitMain {

    private static final String TAG_FROM = "refs/tags/2.2.4.Final";
    private static final String TAG_TO = "refs/tags/2.2.5.Final";

    public static void main(String[] args) throws Exception {

        assert args.length > 0 : "Arguments missing";
        String repositoryPath = args[0] + File.separator + ".git";

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File(repositoryPath))
                .readEnvironment()
                .findGitDir()
                .setMustExist(true)
                .build();


        RevWalk walk = new RevWalk(repository);
        walk.markStart(walk.parseCommit(repository.resolve(TAG_FROM)));
        walk.markUninteresting(walk.parseCommit(repository.resolve(TAG_TO)));
        Iterator<RevCommit> iterator = walk.iterator();
        iterator.forEachRemaining(c -> System.out.println(c));

        //repository.getTags().forEach((key, value) -> System.out.println(key));


       /* AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, TAG_FROM);
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, TAG_TO);

        // diff
        List<DiffEntry> diff = new Git(repository).diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).call();
        for (DiffEntry entry : diff) {
            System.out.println("Entry: " + entry);

            DiffFormatter formatter = new DiffFormatter(System.out);
            formatter.setRepository(repository);
            formatter.format(entry);
        }

        // --------
        */
        // logs
        Iterable<RevCommit> log = new Git(repository).log().addRange(
                repository.resolve(TAG_FROM),
                repository.resolve(TAG_TO)
        ).call();

        log.forEach( l -> {
            System.out.println(l.name());

            try {
                ObjectId hash = repository.resolve(l.getName());
                RevWalk walk2 = new RevWalk(repository);
                RevCommit commit = walk2.parseCommit(hash);

                System.out.println(commit.getFullMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // close the repo
        repository.close();
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String ref)
            throws IOException,
            MissingObjectException,
            IncorrectObjectTypeException {

        // from the commit we can build the tree which allows us to construct the TreeParser
        Ref head = repository.getRef(ref);
        RevWalk walk = new RevWalk(repository);
        RevCommit commit = walk.parseCommit(head.getObjectId());
        RevTree tree = walk.parseTree(commit.getTree().getId());

        CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
        ObjectReader oldReader = repository.newObjectReader();
        try {
            oldTreeParser.reset(oldReader, tree.getId());
        } finally {
            oldReader.release();
        }

        walk.dispose();

        return oldTreeParser;
    }
}

