package org.fossasia.phimpme;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import org.fossasia.phimpme.config.Config;
import org.fossasia.phimpme.gallery.data.Album;
import org.fossasia.phimpme.gallery.data.HandlingAlbums;
import org.fossasia.phimpme.utilities.Constants;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by dnld on 28/04/16.
 */

public class MyApplication extends Application {

    private HandlingAlbums albums = null;
    public static Context applicationContext;
    public ImageLoader imageLoader;
    private Boolean isPublished = false; // Set this to true at the time of release
    public Album getAlbum() {
        return albums.dispAlbums.size() > 0 ? albums.getCurrentAlbum() : Album.getEmptyAlbum();
    }

    private Config config;

    @Override
    public void onCreate() {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        albums = new HandlingAlbums(getApplicationContext());
        applicationContext = getApplicationContext();

        MultiDex.install(this);

        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(twitterConfig);

        /**
         * Realm initialization
         */
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("phimpme.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        super.onCreate();


        Config.get().init(this);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public HandlingAlbums getAlbums() {
        return albums;
    }

    public void setAlbums(HandlingAlbums albums) {
        this.albums = albums;
    }

    public void updateAlbums() {
        albums.loadAlbums(getApplicationContext());
    }



}