package com.eficksan.customcalendar.presentation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.domain.routing.Router;
import com.eficksan.customcalendar.presentation.addingevent.AddEventFragment;
import com.eficksan.customcalendar.presentation.calendar.CalendarFragment;

public class MainActivity extends AppCompatActivity implements Router {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, CalendarFragment.newInstance(), CalendarFragment.TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_event:
                AddEventFragment.newInstance().show(getSupportFragmentManager(), AddEventFragment.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void back() {
        getSupportFragmentManager().popBackStack();
    }
}
