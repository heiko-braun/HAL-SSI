package foo.bar;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.gitective.core.filter.commit.CommitMessageFindFilter;

import static java.util.regex.Pattern.MULTILINE;

/**
 * @author Heiko Braun
 * @since 24/04/15
 */
public class JiraFilter  extends CommitMessageFindFilter {

	/**
	 * BUG_REGEX
	 */
	public static final String BUG_REGEX = "^(HAL-\\w+)$";

	/**
	 * Create bug filter
	 */
	public JiraFilter() {
		super(BUG_REGEX, MULTILINE);
	}

	@Override
	public RevFilter clone() {
		return new JiraFilter();
	}

    @Override
    protected CharSequence getText(RevCommit commit) {

        System.out.println(commit.getFullMessage());
        return super.getText(commit);
    }
}
