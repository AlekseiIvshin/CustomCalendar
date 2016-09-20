package com.eficksan.customcalendar.presentation.common;

import android.os.Bundle;

import com.eficksan.customcalendar.domain.routing.Router;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */
public interface IPresenter<VIEW_TYPE> {

    void onCreate(Bundle savedInstanceStates);

    void takeRouter(Router router);

    void onViewCreated(VIEW_TYPE view);

    void onSaveInstanceState(Bundle states);

    /**
     * Releases view component.
     * Callback mirrors fragment or activity callback.
     */
    void onViewDestroyed();

    void releaseRouter();

    void onDestroy();

}
