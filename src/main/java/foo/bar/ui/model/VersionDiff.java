package foo.bar.ui.model;

/**
 * @author Heiko Braun
 * @since 14/09/15
 */
public class VersionDiff {

    private final Tag tagFrom;
    private final Tag tagTo;
    long added;
    long changed;
    long removed;
    long csi;

    long ssi;

    public VersionDiff(Tag tagFrom, Tag tagTo, long added, long changed, long removed, long csi) {
        this.tagFrom = tagFrom;
        this.tagTo = tagTo;
        this.added = added;
        this.changed = changed;
        this.removed = removed;
        this.csi = csi;
    }

    public String getRange() {
        return tagFrom.getRevName()+" > "+tagTo.getRevName();
    }

    public Tag getTagFrom() {
        return tagFrom;
    }

    public Tag getTagTo() {
        return tagTo;
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

    public long getCsi() {
        return csi;
    }

    public long getSsi() {
        return ssi;
    }

    public void setSsi(long ssi) {
        this.ssi = ssi;
    }
}
