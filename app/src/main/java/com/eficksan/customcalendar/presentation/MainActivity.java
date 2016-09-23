package com.eficksan.customcalendar.presentation;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.domain.routing.Router;
import com.eficksan.customcalendar.presentation.addingevent.AddEventFragment;
import com.eficksan.customcalendar.presentation.calendar.CalendarFragment;
import com.eficksan.customcalendar.presentation.splash.SplashFragment;

import org.joda.time.DateTime;

public class MainActivity extends AppCompatActivity implements Router {

    private static final String KEY_CALENDAR_ID = "KEY_CALENDAR_ID";
    private long mCalendarId;
    private DateTime mTargetDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, SplashFragment.newInstance(), SplashFragment.TAG)
                    .commit();
        } else {
            mCalendarId = savedInstanceState.getLong(KEY_CALENDAR_ID, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_CALENDAR_ID, mCalendarId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_event:
                if (mCalendarId > 0) {
                    AddEventFragment.newInstance(mCalendarId, mTargetDate).show(getSupportFragmentManager(), AddEventFragment.TAG);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void goBack() {
        Fragment addEventFragment = getSupportFragmentManager().findFragmentByTag(AddEventFragment.TAG);
        if (addEventFragment != null && addEventFragment instanceof DialogFragment) {
            ((DialogFragment) addEventFragment).dismiss();
            return;
        }
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void updateCalendarIdAndShowCalendar(long calendarId) {
        mCalendarId = calendarId;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, CalendarFragment.newInstance(calendarId), CalendarFragment.TAG)
                .commit();
    }

    @Override
    public void setSelectedDate(DateTime dateTime) {
        mTargetDate = dateTime;
    }
}
