package com.eficksan.customcalendar.presentation.common;

import android.view.View;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public interface IView {
    /**
     * Initialize view.
     * @param view android view component
     */
    void takeView(View view);

    /**
     * Release view. Component should release all using dependencies.
     */
    void releaseView();
}
