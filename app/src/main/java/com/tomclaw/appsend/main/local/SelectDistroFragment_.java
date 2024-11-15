//
// DO NOT EDIT THIS FILE.
// Generated using AndroidAnnotations 4.8.0.
// 
// You can create a larger work that contains this file and distribute that work under terms of your choice.
//

package com.tomclaw.appsend.main.local;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.di.legacy.LegacyInjector_;

import org.androidannotations.api.bean.BeanHolder;
import org.androidannotations.api.builder.FragmentBuilder;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

import java.util.HashMap;
import java.util.Map;

public final class SelectDistroFragment_
    extends SelectDistroFragment
    implements BeanHolder, HasViews, OnViewChangedListener
{
    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();
    private View contentView_;
    private final Map<Class<?> , Object> beans_ = new HashMap<Class<?> , Object>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
    }

    @Override
    public<T extends View> T internalFindViewById(int id) {
        return ((T)((contentView_ == null)?null:contentView_.findViewById(id)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView_ = super.onCreateView(inflater, container, savedInstanceState);
        if (contentView_ == null) {
            contentView_ = inflater.inflate(R.layout.local_apps_fragment, container, false);
        }
        return contentView_;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        contentView_ = null;
        viewFlipper = null;
        swipeRefresh = null;
        recycler = null;
        errorText = null;
        buttonRetry = null;
    }

    private void init_(Bundle savedInstanceState) {
        OnViewChangedNotifier.registerOnViewChangedListener(this);
        this.injector = LegacyInjector_.getInstance_(getActivity());
        restoreSavedInstanceState_(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    public static FragmentBuilder_ builder() {
        return new FragmentBuilder_();
    }

    @Override
    public<T> T getBean(Class<T> key) {
        return ((T) beans_.get(key));
    }

    @Override
    public<T> void putBean(Class<T> key, T value) {
        beans_.put(key, value);
    }

    @Override
    public void onViewChanged(HasViews hasViews) {
        this.viewFlipper = hasViews.internalFindViewById(R.id.view_flipper);
        this.swipeRefresh = hasViews.internalFindViewById(R.id.swipe_refresh);
        this.recycler = hasViews.internalFindViewById(R.id.recycler);
        this.errorText = hasViews.internalFindViewById(R.id.error_text);
        this.buttonRetry = hasViews.internalFindViewById(R.id.button_retry);
        init();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle_) {
        super.onSaveInstanceState(bundle_);
        bundle_.putBoolean("isError", isError);
        bundle_.putBoolean("isLoading", isLoading);
        bundle_.putBoolean("isRefreshOnResume", isRefreshOnResume);
    }

    private void restoreSavedInstanceState_(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        isError = savedInstanceState.getBoolean("isError");
        isLoading = savedInstanceState.getBoolean("isLoading");
        isRefreshOnResume = savedInstanceState.getBoolean("isRefreshOnResume");
    }

    public static class FragmentBuilder_
        extends FragmentBuilder<FragmentBuilder_, SelectDistroFragment>
    {

        @Override
        public SelectDistroFragment build() {
            SelectDistroFragment_ fragment_ = new SelectDistroFragment_();
            fragment_.setArguments(args);
            return fragment_;
        }
    }
}
