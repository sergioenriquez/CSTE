package org.fern.rest.android;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Duration implements Comparable<Duration> {

    private static Pattern mDurationPattern = Pattern
            .compile("(-?)P(?:(\\d+)Y)?(?:(\\d+)M)?(?:(\\d+)D)?"
                    + "(?:T(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?)?");
    private int mYears = 0;
    private int mMonths = 0;
    private int mDays = 0;
    private int mHours = 0;
    private int mMinutes = 0;
    private int mSeconds = 0;
    private boolean mPositive = true;

    public Duration(boolean positive, int years, int months, int days,
            int hours, int minutes, int seconds) {
        mPositive = positive;
        setYears(years);
        setMonths(months);
        setDays(days);
        setHours(hours);
        setMinutes(minutes);
        setSeconds(seconds);
        normalize();
    }

    public Duration(long millis) {
        mPositive = millis > 0;
        millis = Math.abs(millis);
        mSeconds = (int) (millis / 1000 % 60);
        mMinutes = (int) (millis / 60000 % 60);
        mHours = (int) (millis / 3600000 % 24);
        mDays = (int) (millis / 86400000);
        normalize();
    }

    public static Duration parse(String duration) {
        if (duration == null) return new Duration(true, 0, 0, 0, 0, 0, 0);
        Matcher matcher = mDurationPattern.matcher(duration);
        if (!matcher.matches()) { throw new IllegalArgumentException(
                "Duration does not match given pattern."); }

        Duration d = new Duration(matcher.group(1).equals(""), 0, 0, 0, 0, 0, 0);
        if (!(matcher.group(2) == null || matcher.group(2).equals(""))) {
            d.setYears(Integer.parseInt(matcher.group(2)));
        }

        if (!(matcher.group(3) == null || matcher.group(3).equals(""))) {
            d.setMonths(Integer.parseInt(matcher.group(3)));
        }

        if (!(matcher.group(4) == null || matcher.group(4).equals(""))) {
            d.setDays(Integer.parseInt(matcher.group(4)));
        }

        if (!(matcher.group(5) == null || matcher.group(5).equals(""))) {
            d.setHours(Integer.parseInt(matcher.group(5)));
        }

        if (!(matcher.group(6) == null || matcher.group(6).equals(""))) {
            d.setMinutes(Integer.parseInt(matcher.group(6)));
        }

        if (!(matcher.group(7) == null || matcher.group(7).equals(""))) {
            d.setSeconds(Integer.parseInt(matcher.group(7)));
        }
        d.normalize();
        return d;
    }

    public String toString() {
        if (mYears + mMonths + mDays + mHours + mMinutes + mSeconds == 0) { return null; }
        StringBuilder builder = new StringBuilder();
        if (!mPositive) builder.append("-");
        builder.append("P");
        if (mYears != 0) builder.append(mYears).append("Y");
        if (mMonths != 0) builder.append(mMonths).append("M");
        if (mDays != 0) builder.append(mDays).append("D");
        if (mHours != 0 || mMinutes != 0 || mSeconds != 0) {
            builder.append("T");
            if (mHours != 0) builder.append(mHours).append("H");
            if (mMinutes != 0) builder.append(mMinutes).append("M");
            if (mSeconds != 0) builder.append(mSeconds).append("S");
        }

        return builder.toString();
    }

    public void normalize() {
        mMinutes += mSeconds / 60;
        mSeconds %= 60;
        mHours += mMinutes / 60;
        mMinutes %= 60;
        mDays += mHours / 24;
        mHours %= 24;

        Calendar cal = Calendar.getInstance();
        int months = 0;
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);
        int maxDays = getMaximumDaysForMonthAndYear(currentYear, currentMonth);
        while (mDays > maxDays) {
            mDays -= maxDays;
            maxDays = getMaximumDaysForMonthAndYear(currentYear, currentMonth);
            months++;
            currentMonth++;
            if (currentMonth > Calendar.DECEMBER) {
                currentMonth = Calendar.JANUARY;
                currentYear++;
            }
        }
        mMonths += months;
        mYears += mMonths / 12;
        mMonths %= 12;
    }

    public Calendar addTo(Calendar calendar) {
        Calendar returnable = Calendar.getInstance();
        returnable.setTime(calendar.getTime());
        int currentDays = returnable.get(Calendar.DATE);
        returnable.set(Calendar.DATE, 1);

        int i = mPositive ? 1 : -1;

        returnable.add(Calendar.YEAR, i * mYears);
        returnable.add(Calendar.MONTH, i * mMonths);
        int maxDays = getMaximumDaysForMonthAndYear(
                returnable.get(Calendar.YEAR), returnable.get(Calendar.MONTH));
        returnable.set(Calendar.DATE,
                NumberUtils.clamp(currentDays, 1, maxDays));
        returnable.add(Calendar.DATE, i * mDays);
        returnable.add(Calendar.HOUR_OF_DAY, i * mHours);
        returnable.add(Calendar.MINUTE, i * mMinutes);
        returnable.add(Calendar.SECOND, i * mSeconds);
        return returnable;
    }

    public Date addTo(Date date) {
        Calendar returnable = Calendar.getInstance();
        returnable.setTime(date);
        return addTo(returnable).getTime();
    }

    public static Duration difference(Calendar end, Calendar start) {
        return new Duration(end.getTimeInMillis() - start.getTimeInMillis());
    }

    public static Duration difference(Date start, Date end) {
        return new Duration(start.getTime() - end.getTime());
    }

    private int getMaximumDaysForMonthAndYear(int year, int month) {
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                return 31;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
            default:
                if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0)) {
                    return 29;
                } else {
                    return 28;
                }
        }
    }

    public boolean getPositive() {
        return mPositive;
    }

    public int getYears() {
        return mYears;
    }

    public int getMonths() {
        return mMonths;
    }

    public int getDays() {
        return mDays;
    }

    public int getHours() {
        return mHours;
    }

    public int getMinutes() {
        return mMinutes;
    }

    public int getSeconds() {
        return mSeconds;
    }

    public void setPositive(boolean positive) {
        mPositive = positive;
    }

    public void setYears(int years) {
        if (years < 0) { throw new IllegalArgumentException(
                "Each value must be greater than 0."); }
        mYears = years;
    }

    public void setMonths(int months) {
        if (months < 0) { throw new IllegalArgumentException(
                "Each value must be greater than 0."); }
        mMonths = months;
    }

    public void setDays(int days) {
        if (days < 0) { throw new IllegalArgumentException(
                "Each value must be greater than 0."); }
        mDays = days;
    }

    public void setHours(int hours) {
        if (hours < 0) { throw new IllegalArgumentException(
                "Each value must be greater than 0."); }
        mHours = hours;
    }

    public void setMinutes(int minutes) {
        if (minutes < 0) { throw new IllegalArgumentException(
                "Each value must be greater than 0."); }
        mMinutes = minutes;
    }

    public void setSeconds(int seconds) {
        if (seconds < 0) { throw new IllegalArgumentException(
                "Each value must be greater than 0."); }
        mSeconds = seconds;
    }

    public int getSign() {
        return mPositive ? 1 : -1;
    }

    public long getTotalSeconds() {
        Calendar today = Calendar.getInstance();
        long seconds = (long) mSeconds + (60L * mMinutes) + (3600L * mHours)
                + (86400L * mDays) + (31556926L * mYears);

        for (int i = 0; i < mMonths; i++) {
            today.add(Calendar.MONTH, 1);
            int days = getMaximumDaysForMonthAndYear(today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH));
            seconds += (86400 * days);
        }

        return seconds;
    }

    @Override
    public int compareTo(Duration another) {
        return new Long(getTotalSeconds()).compareTo(new Long(another
                .getTotalSeconds()));
    }
}
