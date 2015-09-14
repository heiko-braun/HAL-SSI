package foo.bar.ui;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * @author Heiko Braun
 * @since 14/09/15
 */
public class Config {

    private String repoDir = "/Users/hbraun/dev/prj/hal/core";
    private Repository repository;

    public String getRepoDir() {
        return repoDir;
    }

    public void setRepoDir(String repoDir) {
        this.repoDir = repoDir;
    }

    public Repository getRepository() {


        if(null==repository) {
            synchronized (this) {
                assert getRepoDir().length() > 0 : "Arguments missing";
                String repositoryPath = getRepoDir() + File.separator + ".git";

                // ----

                try {
                    FileRepositoryBuilder builder = new FileRepositoryBuilder();
                    repository = builder.setGitDir(new File(repositoryPath))
                            .readEnvironment()
                            .findGitDir()
                            .setMustExist(true)
                            .build();


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return repository;
    }
}
