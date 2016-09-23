package com.eficksan.customcalendar.domain.events;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.CalendarContract;

import com.eficksan.customcalendar.domain.common.BaseUseCase;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;

/**
 * Created by Aleksei_Ivshin on 9/23/16.
 */

public class EventsChangesUseCase extends BaseUseCase<Uri, Uri> {

    private final Context mContext;
    private ContentObserver mContentObserver;

    public EventsChangesUseCase(Context context, Scheduler jobScheduler, Scheduler uiScheduler) {
        super(jobScheduler, uiScheduler);
        this.mContext = context;
    }

    @Override
    protected Observable<Uri> buildObservable(Uri parameter) {
        return Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(Subscriber<? super Uri> subscriber) {
                mContentObserver = new EventsContentObserver(new Handler(mContext.getMainLooper()), subscriber);
                mContext.getContentResolver().registerContentObserver(CalendarContract.Events.CONTENT_URI, true, mContentObserver);
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                if (mContentObserver != null) {
                    mContext.getContentResolver().unregisterContentObserver(mContentObserver);
                }
            }
        });
    }

    private static class EventsContentObserver extends ContentObserver {

        private final Subscriber<? super Uri> subscriber;

        /**
         * Creates a content observer.
         *
         * @param handler    The handler to run {@link #onChange} on, or null if none.
         * @param subscriber
         */
        public EventsContentObserver(Handler handler, Subscriber<? super Uri> subscriber) {
            super(handler);
            this.subscriber = subscriber;
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (subscriber != null && !subscriber.isUnsubscribed()) {
                subscriber.onNext(uri);
            }
        }
    }
}
