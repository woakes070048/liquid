package liquid.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TODO: Comments.
 * User: tao
 * Date: 10/3/13
 * Time: 10:45 PM
 */
public class DateUtils {
    private DateUtils() {}

    public static String stringOf(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }

    public static Date dateOf(String dateStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return format.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(dateStr + " format is illegal.");
        }
    }

    public static void main(String[] args) {
        System.out.println(DateUtils.stringOf(new Date()));
        System.out.println(DateUtils.dateOf("2013-10-03 22:50"));
    }
}