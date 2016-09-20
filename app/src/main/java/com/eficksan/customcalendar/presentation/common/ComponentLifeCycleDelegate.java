package com.eficksan.customcalendar.presentation.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.eficksan.customcalendar.domain.routing.Router;
import com.eficksan.customcalendar.ioc.IComponent;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public class ComponentLifeCycleDelegate<VIEW extends IView, PRESENTER extends IPresenter<VIEW>, INJECTOR_COMPONENT extends IComponent<VIEW, PRESENTER>> {

    private boolean mIsDestroyBySystem;
    private INJECTOR_COMPONENT mComponent;
    private PRESENTER mPresenter;
    private VIEW mView;

    private final ComponentProvider<INJECTOR_COMPONENT> componentProvider;

    public ComponentLifeCycleDelegate(ComponentProvider<INJECTOR_COMPONENT> componentProvider) {
        this.componentProvider = componentProvider;
    }

    public void onCreate(@Nullable Bundle savedInstanceState, Router router) {
        mComponent = componentProvider.onSetUpComponent();
        mPresenter = mComponent.getPresenter();
        mPresenter.takeRouter(router);
        mPresenter.onCreate(savedInstanceState);
    }

    public void onViewCreated(View view) {
        mView = mComponent.getView();
        mView.takeView(view);
        mPresenter.onViewCreated(mView);
    }

    public void onResume() {
        mIsDestroyBySystem = false;
    }

    public void onSaveInstanceState(Bundle outState) {
        mIsDestroyBySystem = true;
        mPresenter.onSaveInstanceState(outState);
    }


    public void onDestroyView() {
        mView.releaseView();
        mPresenter.onViewDestroyed();
    }

    public void onDestroy() {
        mPresenter.releaseRouter();
        mPresenter.onDestroy();
        if (!mIsDestroyBySystem) {
            componentProvider.onKillComponent();
        }
    }

    public interface ComponentProvider<INJECTOR_COMPONENT> {
        INJECTOR_COMPONENT onSetUpComponent();

        void onKillComponent();
    }
}
