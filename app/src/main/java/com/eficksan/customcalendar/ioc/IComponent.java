package com.eficksan.customcalendar.ioc;

import com.eficksan.customcalendar.presentation.common.IPresenter;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public interface IComponent<VIEW extends IView, PRESENTER extends IPresenter<VIEW>> {

    VIEW getView();
    PRESENTER getPresenter();
}
