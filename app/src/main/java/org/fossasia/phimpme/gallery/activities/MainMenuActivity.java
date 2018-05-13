package org.fossasia.phimpme.gallery.activities;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;



import org.fossasia.phimpme.R;
import org.fossasia.phimpme.accounts.AccountActivity;
import org.fossasia.phimpme.base.ThemedActivity;
import org.fossasia.phimpme.config.Config;
import org.fossasia.phimpme.opencamera.Camera.CameraActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainMenuActivity extends ThemedActivity {

    @BindView(R.id.buttonCamera)
    FrameLayout cameraButton;

    @BindView(R.id.buttonGallery)
    FrameLayout galleryButton;

    @BindView(R.id.buttonAbout)
    FrameLayout aboutButton;

    @BindView(R.id.buttonAccounts)
    FrameLayout accountsButton;

    @BindView(R.id.buttonSettings)
    FrameLayout settingsButton;

    @BindView(R.id.buttonShare)
    FrameLayout shareButton;

    @BindView(R.id.background)
    FrameLayout backgroundPanel;

    @BindView(R.id.logo)
    ImageView logo;

    @BindView(R.id.imageCamera)
    ImageView imageCamera;

    @BindView(R.id.imageGallery)
    ImageView imageGallery;

    @BindView(R.id.imageSettings)
    ImageView imageSettings;

    @BindView(R.id.imageAccount)
    ImageView imageAccount;

    @BindView(R.id.imageShare)
    ImageView imageShare;

    @BindView(R.id.imageAbout)
    ImageView imageAbout;

    boolean showAppsgeyserAbout = false;


    @OnClick(R.id.buttonCamera)
    public void cameraClick(){
        Intent cameraIntent = new Intent(MainMenuActivity.this, CameraActivity.class);
        startActivity(cameraIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @OnClick(R.id.buttonGallery)
    public void galleryClick(){
        Intent homeIntent = new Intent(this, LFMainActivity.class);
        startActivity(homeIntent);
    }

    @OnClick(R.id.buttonAccounts)
    public void accountsClick(){
        Intent accountIntent = new Intent(this, AccountActivity.class);
        startActivity(accountIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @OnClick(R.id.buttonSettings)
    public void settingsClick(){
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @OnClick(R.id.buttonAbout)
    public void aboutClick(){
        if(showAppsgeyserAbout){
            //AppsgeyserSDK.showAboutDialog(this);
        }else {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.buttonShare)
    public void shareClick(){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.install_phimpme) + "\n " + "http://play.google.com/store/apps/details?id="+getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (Config.get().getMenuLayout()){
            case "square":
                setContentView(R.layout.activity_main_menu_square);
                break;
            case "star":
                setContentView(R.layout.activity_main_menu_star);
                break;
            case "simple":
                setContentView(R.layout.activity_main_menu_simple);
                break;
            default:
                setContentView(R.layout.activity_main_menu_square);
                break;
        }

        ButterKnife.bind(this);

        if(Config.get().getMenuLayout().equals("square")){
            cameraButton.setBackgroundColor(Config.get().getCameraColor());
            galleryButton.setBackgroundColor(Config.get().getGalleryColor());
            settingsButton.setBackgroundColor(Config.get().getSettingsColor());
            accountsButton.setBackgroundColor(Config.get().getAccountsColor());
            shareButton.setBackgroundColor(Config.get().getShareColor());
            aboutButton.setBackgroundColor(Config.get().getAboutColor());
        }

        if(!Config.get().getLogoUrl().isEmpty()) {
            logo.setImageDrawable(new BitmapDrawable(getResources(), Config.get().readBitmap(this, Config.get().getLogoUrl())));
        }else {
            logo.setImageDrawable(new BitmapDrawable(getResources(), Config.get().readBitmap(this, Config.get().getIconUrl())));
        }

        if(!Config.get().getBackgroundUrl().isEmpty()){
            backgroundPanel.setBackground(Config.get().createDrawable(this, Config.get().getBackgroundUrl()));
        }

        Config.get().changeIcon(imageCamera, "menu_camera");
        Config.get().changeIcon(imageGallery, "menu_gallery");
        Config.get().changeIcon(imageSettings, "menu_settings");
        Config.get().changeIcon(imageAccount, "menu_accounts");
        Config.get().changeIcon(imageShare, "menu_share");
        Config.get().changeIcon(imageAbout, "menu_about");

        /*AppsgeyserSDK.isAboutDialogEnabled(this, new AppsgeyserSDK.OnAboutDialogEnableListener() {
            @Override
            public void onDialogEnableReceived(boolean enabled) {
                //showAppsgeyserAbout = enabled;
            }
        });*/

    }


}
