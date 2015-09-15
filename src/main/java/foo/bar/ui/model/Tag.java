package foo.bar.ui.model;

import com.github.zafarkhaja.semver.Version;

import java.util.Date;

/**
 * @author Heiko Braun
 * @since 14/09/15
 */
public class Tag implements Comparable<Tag> {

    private Version version;
    private final String revName;
    private Date date;

    public Tag(Version version, String revName, Date date) {
        this.version = version;
        this.revName = revName;
        this.date = date;
    }

    public String getRevName() {
        return revName;
    }

    public Version getVersion() {
        return version;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return getRevName().toString();
    }

    @Override
    public int compareTo(Tag o) {
        return this.getVersion().compareTo(o.getVersion());
    }
}
