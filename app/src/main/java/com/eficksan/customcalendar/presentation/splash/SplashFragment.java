package com.eficksan.customcalendar.presentation.splash;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eficksan.customcalendar.App;
import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.domain.routing.Router;
import com.eficksan.customcalendar.ioc.common.CalendarModule;
import com.eficksan.customcalendar.ioc.splash.DaggerSplashScreenComponent;
import com.eficksan.customcalendar.ioc.splash.SplashScreenComponent;
import com.eficksan.customcalendar.ioc.splash.SplashScreenModule;
import com.eficksan.customcalendar.presentation.common.PermissionResultListener;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListenerDelegate;

import javax.inject.Inject;

public class SplashFragment extends Fragment implements ISplashView, PermissionsRequestListener {

    public static final String TAG = SplashFragment.class.getSimpleName();
    PermissionsRequestListenerDelegate permissionsRequestListenerDelegate;
    SplashScreenComponent mInjectorComponent;

    @Inject
    SplashPresenter mPresenter;

    public static SplashFragment newInstance() {
        SplashFragment fragment = new SplashFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionsRequestListenerDelegate = new PermissionsRequestListenerDelegate();
        setUpInjectionComponent().inject(this);
        mPresenter.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.takeRouter((Router) getActivity());
        mPresenter.onViewCreated(this);
    }

    @Override
    public void onDestroyView() {
        mPresenter.onViewDestroyed();
        mPresenter.releaseRouter();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.onDestroy();
        removeInjectionComponent();
        super.onDestroy();
    }

    @Override
    public void notifyUser(@StringRes int messageResId) {
        Toast.makeText(getActivity(), messageResId, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionsRequired(String[] permissions, int requestCode) {
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void addListener(PermissionResultListener resultListener) {
        permissionsRequestListenerDelegate.addListener(resultListener);
    }

    @Override
    public void removeListener(PermissionResultListener resultListener) {
        permissionsRequestListenerDelegate.removeListener(resultListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsRequestListenerDelegate
                .onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public SplashScreenComponent setUpInjectionComponent() {
        if (mInjectorComponent == null) {
            mInjectorComponent = DaggerSplashScreenComponent.builder()
                    .appComponent(((App) getActivity().getApplication()).getAppComponent())
                    .calendarModule(new CalendarModule())
                    .splashScreenModule(new SplashScreenModule(this))
                    .build();
        }
        return mInjectorComponent;
    }


    public void removeInjectionComponent() {
        mInjectorComponent = null;
    }
}
