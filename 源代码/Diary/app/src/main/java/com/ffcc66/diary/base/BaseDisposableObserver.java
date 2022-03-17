package com.ffcc66.diary.base;

import android.util.Log;

import io.reactivex.observers.DisposableObserver;

public abstract class BaseDisposableObserver extends DisposableObserver {
    private static final String TAG = "BaseDisposableObserver";

    @Override
    public void onNext(Object o) {

    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "onError: ",e);
    }

    @Override
    public void onComplete() {

    }
}
