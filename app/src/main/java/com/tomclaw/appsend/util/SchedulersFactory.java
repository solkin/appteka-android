package com.tomclaw.appsend.util;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by solkin on 20/01/2018.
 */
public interface SchedulersFactory {

    Scheduler io();

    Scheduler mainThread();

    class SchedulersFactoryImpl implements SchedulersFactory {

        @Override
        public Scheduler io() {
            return Schedulers.io();
        }

        @Override
        public Scheduler mainThread() {
            return AndroidSchedulers.mainThread();
        }

    }

}
