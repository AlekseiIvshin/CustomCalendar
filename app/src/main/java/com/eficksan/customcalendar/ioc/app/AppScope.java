package com.eficksan.customcalendar.ioc.app;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;
import javax.inject.Singleton;

@Singleton
@Retention(RetentionPolicy.RUNTIME)
public @interface AppScope {
}
