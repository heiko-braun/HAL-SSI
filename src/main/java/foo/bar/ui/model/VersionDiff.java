package foo.bar.ui.model;

/**
 * @author Heiko Braun
 * @since 14/09/15
 */
public class VersionDiff {

    private final String version;
    long added;
    long changed;
    long removed;
    long ssi;

    public VersionDiff(String version, long added, long changed, long removed, long ssi) {
        this.version = version;
        this.added = added;
        this.changed = changed;
        this.removed = removed;
        this.ssi = ssi;
    }

    public String getVersion() {
        return version;
    }

    public long getAdded() {
        return added;
    }

    public long getChanged() {
        return changed;
    }

    public long getRemoved() {
        return removed;
    }

    public long getSsi() {
        return ssi;
    }
}
