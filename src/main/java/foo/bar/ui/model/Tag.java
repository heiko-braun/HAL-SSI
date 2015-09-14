package foo.bar.ui.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Heiko Braun
 * @since 14/09/15
 */
public class Tag {

    private String name;
    private Date date;

    public Tag(String title, Date date) {
        this.name = title;
        this.date = date;
    }

    public Tag(String title, String date) {
        this.name = title;

        try {
            SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
            this.date = fmt.parse( date );
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return name;
    }
}
