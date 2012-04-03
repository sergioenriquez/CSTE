/**
 * This is the class for the main application. All these functions should be
 * easily accessible from any location in the application. It will provide all
 * query functions (via an AsyncTask) and also handle any settings changes that
 * may need to be handled.
 */
package org.fern.rest.android;

import java.util.Date;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * This class runs on top of all the other classes, and is the main application
 * thread. It allows anything with a contextual reference to the application to
 * have access to some shared functions for the application. For the most part,
 * however, this class is actually used for handling changes in
 * SharedPreferences.
 * 
 * @author Sergio Enriquez
 * @author Andrew Hays
 */
public class RestApplication extends Application implements
        OnSharedPreferenceChangeListener {

    SharedPreferences prefs;

    
    public static enum Priority {
        NONE, LOWEST, VERYLOW, LOW, MEDIUM, HIGH, VERYHIGH, HIGHEST;
        
        private int mResId;
        Priority() {
            mResId = R.drawable.priority;
        }
        
        public int getResId() {
            return mResId;
        }
    }

    /**
     * Method that's called whenever the application is first started up.
     * 
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences.Editor edit = prefs.edit();

        String serverKey = getString(R.string.serverKey);
        String portKey = getString(R.string.portKey);
        String cacheTimeKey = getString(R.string.cacheTimeKey);

        if (prefs.getString(serverKey, null) == null) {
            edit.putString(serverKey, getString(R.string.prefsServerDefault));
        }

        if (prefs.getString(portKey, null) == null) {
            edit.putString(portKey, getString(R.string.prefsPortDefault));
        }

        if (prefs.getString(cacheTimeKey, null) == null) {
            edit.putString(cacheTimeKey,
                    getString(R.string.prefsCacheTimeDefault));
        }

        edit.commit();
    }

    /**
     * Function called whenever a preference is changed. This function should
     * manage any changes to cache time settings (if data is auto fetched and
     * not fetched on demand).
     * 
     * @param sharedPreferences A reference to the instance of SharedPreferences
     *            being changed.
     * @param key Which key in the SharedPreferences was changed.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        String toast;
        if (key.equals(getString(R.string.cacheTimeKey))) toast = "Cache Time is not implemented yet.";
        else toast = String.format("%s was updated.", key);
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method that's called whenever the application is exited.
     * 
     * @see android.app.Application#onTerminate()
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public String getFuzzyTimeSpan(Date then) {
        return getFuzzyTimeSpan(then.getTime(), System.currentTimeMillis());
    }

    public String getFuzzyTimeSpan(Date then, Date now) {
        return getFuzzyTimeSpan(then.getTime(), now.getTime());
    }

    public String getFuzzyTimeSpan(long then) {
        return getFuzzyTimeSpan(then, System.currentTimeMillis());
    }

    public String getFuzzyTimeSpan(long then, long now) {
        long diff = (then - now) / 1000;
        boolean past = diff < 0;
        diff = Math.abs(diff);
        int seconds = (int) (diff % 60);
        int minutes = (int) ((diff / 60) % 60);
        int hours = (int) ((diff / 3600) % 24); // 60 seconds * 60 minutes
        int days = (int) ((diff / 86400) % 365); // 60 seconds * 60 minutes * 24 hours
        int months = getMonthsDifference(then, now) % 12; // month magic
        int years = (int) ((diff / 31536000) % 10); // 60 seconds * 60 minutes * 60 hours * 365 days

        return getFuzzyTimeSpan(years, months, days, hours, minutes, seconds,
                past);
    }

    public String getFuzzyTimeSpan(Duration duration) {
        return getFuzzyTimeSpan(duration.getYears(), duration.getMonths(),
                duration.getDays(), duration.getHours(), duration.getMinutes(),
                duration.getSeconds(), duration.getSign() == -1);
    }

    public String getFuzzyTimeSpan(int years, int months, int days, int hours,
            int minutes, int seconds, boolean past) {
        StringBuilder out = new StringBuilder();
        Resources res = getResources();

        int decades = (years / 10) % 10;
        int centuries = (years / 100) % 10;
        int millenia = years / 1000;
        years %= 10;

        if (millenia != 0) {
            if (millenia > 1 && centuries > 7) {
                millenia += 1;
            }
            out.append(res.getQuantityString(R.plurals.millenia, millenia,
                    millenia));
            if (millenia == 1 && centuries != 0) {
                out.append(" ").append(getString(R.string.and)).append(" ");
                out.append(res.getQuantityString(R.plurals.centuries,
                        centuries, centuries));
            }
        } else if (centuries != 0) {
            if (centuries > 1 && decades > 7) {
                centuries += 1;
            }
            out.append(res.getQuantityString(R.plurals.centuries, centuries,
                    centuries));
            if (centuries == 1 && decades != 0) {
                out.append(" ").append(getString(R.string.and)).append(" ");
                out.append(res.getQuantityString(R.plurals.decades, decades,
                        decades));
            }
        } else if (decades != 0) {
            if (decades > 1 && years > 7) {
                decades += 1;
            }
            out.append(res.getQuantityString(R.plurals.decades, decades,
                    decades));
            if (decades == 1 && years != 0) {
                out.append(" ").append(getString(R.string.and)).append(" ");
                out.append(res.getQuantityString(R.plurals.years, years, years));
            }
        } else if (years != 0) {
            if (years > 1 && months > 8) {
                years += 1;
            }
            out.append(res.getQuantityString(R.plurals.years, years, years));
            if (years == 1 && months != 0) {
                out.append(" ").append(getString(R.string.and)).append(" ");
                out.append(res.getQuantityString(R.plurals.months, months,
                        months));
            }
        } else if (months != 0) {
            if (months > 1 && days > 23) {
                months += 1;
            }
            out.append(res.getQuantityString(R.plurals.months, months, months));
            if (months == 1 && days != 0) {
                out.append(" ").append(getString(R.string.and)).append(" ");
                out.append(res.getQuantityString(R.plurals.days, days, days));
            }
        } else if (days != 0) {
            if (days > 1 && hours > 18) {
                days += 1;
            }
            if (days == 1 && hours <= 6) {
                return (getString(past ? R.string.yesterday : R.string.tomorrow));
            } else if (days == 1) {
                out.append(res.getQuantityString(R.plurals.days, days, days));
                out.append(" ").append(getString(R.string.and)).append(" ");
                out.append(res.getQuantityString(R.plurals.hours, hours, hours));
            } else {
                out.append(res.getQuantityString(R.plurals.days, days, days));
            }
        } else if (hours != 0) {
            if (hours > 1 && minutes > 45) {
                hours += 1;
            }
            out.append(res.getQuantityString(R.plurals.hours, hours, hours));
            if (hours == 1 && minutes != 0) {
                out.append(" ").append(getString(R.string.and)).append(" ");
                out.append(res.getQuantityString(R.plurals.minutes, minutes,
                        minutes));
            }
        } else if (minutes != 0) {
            if (minutes > 1 && seconds > 45) {
                minutes += 1;
            }
            out.append(res.getQuantityString(R.plurals.minutes, minutes,
                    minutes));
            if (minutes == 1 && seconds != 0) {
                out.append(" ").append(getString(R.string.and)).append(" ");
                out.append(res.getQuantityString(R.plurals.seconds, seconds,
                        seconds));
            }
        } else {
            return getString(R.string.now);
        }
        return out.append(" ")
                .append(getString(past ? R.string.ago : R.string.fromNow))
                .toString();
    }

    private int getMonthsDifference(long then, long now) {
        Date dThen = new Date(then);
        Date dNow = new Date(now);
        return Math.abs((dThen.getYear() - dNow.getYear()) * 12
                + (dThen.getMonth() - dNow.getMonth()));
    }
}
