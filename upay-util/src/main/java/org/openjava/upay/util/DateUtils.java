package org.openjava.upay.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils
{
    public final static String YYYY_MM_DD_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";

    public final static String YYYY_MM_DD = "yyyy-MM-dd";

    public final static String YYYYMMDD = "yyyyMMdd";

    public final static String YYMMDD = "yyMMdd";

    public static String formatNow(String format)
    {
        return format(new Date(), format);
    }

    public static String format(Date date)
    {
        return format(date, YYYY_MM_DD_HH_mm_ss);
    }

    public static String format(Date date, String format)
    {
        if (ObjectUtils.isNull(date)) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);

    }

    public static String formatMillis(long time)
    {
        return format(new Date(time), YYYY_MM_DD_HH_mm_ss);
    }

    public static String convertFormat(String dateStr, String oldFromat, String newFormat)
    {
        if (ObjectUtils.isEmpty(dateStr)) {
            return null;
        }

        Date date;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(oldFromat);
            date = sdf.parse(dateStr);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid date format", ex);
        }

        return format(date, newFormat);
    }

    public static Date parseDate(String dateStr)
    {
        return parseDate(dateStr, YYYY_MM_DD_HH_mm_ss);
    }

    public static Date parseDate(String dateStr, String format)
    {
        if (ObjectUtils.isEmpty(dateStr)) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid date format", ex);
        }
    }

    public static Date addDays(Date date, int days)
    {
        return add(date, Calendar.DAY_OF_MONTH, days);
    }

    public static Date addHours(Date date, int hours)
    {
        return add(date, Calendar.HOUR_OF_DAY, hours);
    }

    public static Date addSeconds(Date date, int seconds)
    {
        return add(date, Calendar.SECOND, seconds);
    }

    public static boolean dayBefore(Date day1, Date day2)
    {
        if (day1 == null || day2 == day2) {
            throw new IllegalArgumentException("Invalid day input");
        }

        Calendar c1 = Calendar.getInstance();
        c1.setTime(day1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(day2);

        return c1.before(c2);
    }

    private static Date add(Date date, int field, int amount)
    {
        if (null == date) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, amount);
        return cal.getTime();
    }
}
