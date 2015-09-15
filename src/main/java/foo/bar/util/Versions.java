package foo.bar.util;

import com.github.zafarkhaja.semver.Version;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * @author Heiko Braun
 * @since 25/02/15
 */
public class Versions {

    private static final int INDEX_NOT_FOUND = -1;

    public static Optional<Version> parseVersion(String versionString) {
        Version version = null;
        try {
            int defaultIndex = Versions.ordinalIndexOf(versionString, ".", 3);
            if (INDEX_NOT_FOUND == defaultIndex)
                defaultIndex = versionString.length();

            version = Version.valueOf(versionString.substring(0, defaultIndex));
        } catch (Exception e) {
            /*e.printStackTrace();
            System.out.println(versionString);*/
            return Optional.empty();
        }
        return Optional.of(version);
    }

    public static Date parseDate(String dateString) throws Exception {
        SimpleDateFormat parser =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = parser.parse(dateString);
        return date;
    }

    public static int ordinalIndexOf(final CharSequence str, final CharSequence searchStr, final int ordinal) {
        return ordinalIndexOf(str, searchStr, ordinal, false);
    }

    public static int ordinalIndexOf(final CharSequence str, final CharSequence searchStr, final int ordinal, final boolean lastIndex) {
        if (str == null || searchStr == null || ordinal <= 0) {
            return INDEX_NOT_FOUND;
        }
        if (searchStr.length() == 0) {
            return lastIndex ? str.length() : 0;
        }
        int found = 0;
        int index = lastIndex ? str.length() : INDEX_NOT_FOUND;
        do {
            if (lastIndex) {
                index = lastIndexOf(str, searchStr, index - 1);
            } else {
                index = indexOf(str, searchStr, index + 1);
            }
            if (index < 0) {
                return index;
            }
            found++;
        } while (found < ordinal);
        return index;
    }

    public static int lastIndexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
        return cs.toString().lastIndexOf(searchChar.toString(), start);
        //        if (cs instanceof String && searchChar instanceof String) {
        //            // TODO: Do we assume searchChar is usually relatively small;
        //            //       If so then calling toString() on it is better than reverting to
        //            //       the green implementation in the else block
        //            return ((String) cs).lastIndexOf((String) searchChar, start);
        //        } else {
        //            // TODO: Implement rather than convert to String
        //            return cs.toString().lastIndexOf(searchChar.toString(), start);
        //        }
    }

    public static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
        return cs.toString().indexOf(searchChar.toString(), start);
        //        if (cs instanceof String && searchChar instanceof String) {
        //            // TODO: Do we assume searchChar is usually relatively small;
        //            //       If so then calling toString() on it is better than reverting to
        //            //       the green implementation in the else block
        //            return ((String) cs).indexOf((String) searchChar, start);
        //        } else {
        //            // TODO: Implement rather than convert to String
        //            return cs.toString().indexOf(searchChar.toString(), start);
        //        }
    }
}
