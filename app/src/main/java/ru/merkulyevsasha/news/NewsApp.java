package ru.merkulyevsasha.news;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

import com.evernote.android.job.JobManager;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import ru.merkulyevsasha.news.dagger.components.AppComponent;
import ru.merkulyevsasha.news.dagger.components.DaggerAppComponent;
import ru.merkulyevsasha.news.newsjobs.NewsJobCreator;

/**
 * Created by sasha_merkulev on 21.09.2017.
 */

public class NewsApp extends Application
        implements HasActivityInjector, HasServiceInjector {

    @Inject DispatchingAndroidInjector<Activity> activityInjector;
    @Inject DispatchingAndroidInjector<Service> serviceInjector;

    private static AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        component = DaggerAppComponent
                .builder()
                .context(this)
                .build();

        component.inject(this);

        JobManager.create(this).addJobCreator(new NewsJobCreator());

    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() { return serviceInjector; }

    public static AppComponent getComponent(){
        return component;
    }
}