package com.eficksan.customcalendar.presentation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.presentation.calendar.CalendarFragment;

public class MainActivity extends AppCompatActivity {

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
}
