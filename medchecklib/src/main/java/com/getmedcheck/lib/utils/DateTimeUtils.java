package com.getmedcheck.lib.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {

    private static final SimpleDateFormat[] ACCEPTED_TIMESTAMP_FORMATS = {
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.US),
            new SimpleDateFormat("dd,MMM,yyyy HH:mm:ss aa", Locale.US),
            new SimpleDateFormat("dd MMM,yyyy HH:mm:ss aa", Locale.US),
            new SimpleDateFormat("dd-MMM-yyyy", Locale.US),
            new SimpleDateFormat("dd MMM, yyyy", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd", Locale.US)
    };
    private static final long SEC = 1000;
    private static final long MIN = SEC * 60;
    private static final long HOUR = MIN * 60;
    private static final long DAY = HOUR * 24;
    public static final long YESTERDAY = DAY * 2;
    private static final long YEAR = DAY * 365;
    private static final long MONTH;
    private static final int[] DAY_IN_MONTH;

    static {
        int dayInFebruary = 28;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if (year % 4 == 0 && year % 100 == 0 && year % 400 == 0) {
            dayInFebruary = 29;
        }
        DAY_IN_MONTH = new int[]{31, dayInFebruary, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        MONTH = DAY * (DAY_IN_MONTH[Calendar.getInstance().get(Calendar.MONTH)]);
    }

    private DateTimeUtils() {
    }

    public static long getTimeFromStringDate(String format, String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        try {
            return sdf.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getFormattedDate(String format, long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        return sdf.format(time);
    }

    public static String getFormattedDate(String format, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        return sdf.format(date);
    }

    public static int getDayOdWeek(String day) {
        switch (day.toLowerCase()) {
            case "sun":
            case "sunday":
                return Calendar.SUNDAY;
            case "mon":
            case "monday":
                return Calendar.MONDAY;
            case "tue":
            case "tuesday":
                return Calendar.TUESDAY;
            case "wed":
            case "wednesday":
                return Calendar.WEDNESDAY;
            case "thu":
            case "thursday":
                return Calendar.THURSDAY;
            case "fri":
            case "friday":
                return Calendar.FRIDAY;
            case "sat":
            case "saturday":
                return Calendar.SATURDAY;
        }
        return 0;
    }

    public static int getDayInMonth(int month) {
        if (month > DAY_IN_MONTH.length) {
            return 30;
        }
        return DAY_IN_MONTH[month];
    }

    public static Date parseTimestamp(String timestamp) {
        for (SimpleDateFormat format : ACCEPTED_TIMESTAMP_FORMATS) {
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                return format.parse(timestamp);
            } catch (ParseException ex) {
                continue;
            }
        }

        // All attempts to parse have failed
        return null;
    }

    /**
     * Returns the GMT offset in seconds as a String.
     *
     * @return GMT offset in seconds
     */
    public static String getGMTOffsetInSeconds() {
        return String.valueOf(getGMTOffsetInSecondsLong());
    }

    public static long getGMTOffsetInSecondsLong() {
        final Calendar calendar = new GregorianCalendar();
        final TimeZone timeZone = calendar.getTimeZone();
        return TimeUnit.SECONDS.convert(timeZone.getRawOffset() + (timeZone.inDaylightTime(new Date()) ? timeZone.getDSTSavings() : 0), TimeUnit.MILLISECONDS);
    }

    /**
     * @param format date format
     * @param time   date time in long
     * @return formatted date time
     */
    public static String format(String format, long time) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date(time));
    }

    /**
     * show formatted time from now
     *
     * @param dateTime date time in long
     * @return return formatted time
     */
    public static String fromNow(long dateTime) {
        String formatted = "";
        long current = System.currentTimeMillis();
        long diff = current - dateTime;

        int min = (int) (diff / MIN);
        int hour = (int) (diff / HOUR);
        int day = (int) (diff / DAY);
        int month = (int) (diff / MONTH);
        int year = (int) (diff / YEAR);

        //Log.e("TAG", "Month : " + month);

        if (diff < MIN) {
            formatted = "Just Now";
        } else if (diff > MIN && diff < HOUR) {
            formatted = min + (min > 1 ? " minutes ago" : " minute ago");
        } else if (diff > HOUR && diff < DAY) {
            formatted = hour + (hour > 1 ? " hours ago" : " hour ago");
        } else if (diff > DAY && diff < YESTERDAY) {
            formatted = "Yesterday";
        } else if (diff > DAY && diff < MONTH) {
            formatted = day + (day > 1 ? " days ago" : " day ago");
        } else if (diff > MONTH && diff < YEAR) {
            formatted = month + (month > 1 ? " months ago" : " month ago");
        } else if (diff > YEAR) {
            formatted = year + (year > 1 ? " years ago" : " year ago");
        }
        return formatted;
    }

    /**
     * show formatted time from now
     *
     * @param dateTime            date time in string format
     * @param inputDateTimeFormat format for display time if max recent time is over
     * @return return formatted time
     */
    public static String fromNow(String dateTime, String inputDateTimeFormat) {
        String formatted = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(inputDateTimeFormat, Locale.getDefault());
            Date date = sdf.parse(dateTime);
            formatted = fromNow(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formatted;
    }

    /**
     * show formatted time from now
     *
     * @param dateTime      date time in long
     * @param maxRecentTime showing time from now until max recent time
     * @param format        format for display time if max recent time is over
     * @return return formatted time
     */
    public static String fromNow(long dateTime, long maxRecentTime, String format) {
        String formatted = "";
        try {
            long current = System.currentTimeMillis();
            long diff = current - dateTime;

            if (diff < maxRecentTime) {
                formatted = fromNow(dateTime);
            } else {
                formatted = new SimpleDateFormat(format, Locale.getDefault()).format(dateTime);
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return formatted;
    }
}
