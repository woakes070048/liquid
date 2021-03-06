package liquid.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

/**
 *  
 * User: tao
 * Date: 10/3/13
 * Time: 10:45 PM
 */
public class DateUtil {
    private DateUtil() { }

    public static String stringOf(Date date, DatePattern datePattern) {
        SimpleDateFormat format = new SimpleDateFormat(datePattern.getPattern());
        return format.format(date);
    }

    public static String stringOf(Date date) {
        if (null == date) return "";
        SimpleDateFormat format = new SimpleDateFormat(DatePattern.UNTIL_MINUTE.getPattern());
        format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("CTT", ZoneId.SHORT_IDS)));
        return format.format(date);
    }

    public static String stringOf() {
        return stringOf(new Date());
    }

    public static Date dateOf(String dateStr) {
        if (null == dateStr || dateStr.trim().length() == 0) return null;

        try {
            SimpleDateFormat format = new SimpleDateFormat(DatePattern.UNTIL_MINUTE.getPattern());
            format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("CTT", ZoneId.SHORT_IDS)));
            return format.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(dateStr + " format is illegal.");
        }
    }

    public static String dayStrOf() {
        return dayStrOf(new Date());
    }

    public static String dayStrOf(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DatePattern.UNTIL_DAY.getPattern());
        return format.format(date);
    }

    public static Date dayOf(String dateStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(DatePattern.UNTIL_DAY.getPattern());
            return format.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(dateStr + " format is illegal.");
        }
    }
}
