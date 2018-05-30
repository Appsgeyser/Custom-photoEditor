package org.fossasia.phimpme.accounts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.fossasia.phimpme.R;
import org.fossasia.phimpme.base.PhimpmeProgressBarHandler;
import org.fossasia.phimpme.base.RecyclerItemClickListner;
import org.fossasia.phimpme.base.ThemedActivity;
import org.fossasia.phimpme.data.local.AccountDatabase;
import org.fossasia.phimpme.data.local.DatabaseHelper;
import org.fossasia.phimpme.gallery.activities.SettingsActivity;
import org.fossasia.phimpme.gallery.util.AlertDialogsHelper;
import org.fossasia.phimpme.gallery.util.ThemeHelper;
import org.fossasia.phimpme.share.nextcloud.NextCloudAuth;
import org.fossasia.phimpme.share.owncloud.OwnCloudActivity;
import org.fossasia.phimpme.utilities.ActivitySwitchHelper;
import org.fossasia.phimpme.utilities.SnackBarHandler;
import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmQuery;

import static org.fossasia.phimpme.R.string.no_account_signed_in;
import static org.fossasia.phimpme.data.local.AccountDatabase.AccountName.NEXTCLOUD;
import static org.fossasia.phimpme.data.local.AccountDatabase.AccountName.OWNCLOUD;
import static org.fossasia.phimpme.utilities.Utils.checkNetwork;

/**
 * Created by pa1pal on 13/6/17.
 */

public class AccountActivity extends ThemedActivity implements AccountContract.View,
        RecyclerItemClickListner.OnItemClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int NEXTCLOUD_REQUEST_CODE = 3;
    private static final int OWNCLOUD_REQUEST_CODE = 9;
    private static final int RESULT_OK = 1;
    private static final int RC_SIGN_IN = 9001;
    public static final String BROWSABLE = "android.intent.category.BROWSABLE";
    @BindView(R.id.accounts_parent)
    RelativeLayout parentLayout;
    @BindView(R.id.accounts_recycler_view)
    RecyclerView accountsRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.accounts)
    CoordinatorLayout coordinatorLayout;
    private AccountAdapter accountAdapter;
    private AccountPresenter accountPresenter;
    private Realm realm = Realm.getDefaultInstance();
    private RealmQuery<AccountDatabase> realmResult;
    private PhimpmeProgressBarHandler phimpmeProgressBarHandler;
    private AccountDatabase account;
    private DatabaseHelper databaseHelper;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        ActivitySwitchHelper.setContext(this);
        parentLayout.setBackgroundColor(getBackgroundColor());
        overridePendingTransition(R.anim.right_to_left,
                R.anim.left_to_right);
        parentLayout.setBackgroundColor(getBackgroundColor());
        accountAdapter = new AccountAdapter();
        accountPresenter = new AccountPresenter(realm);
        phimpmeProgressBarHandler = new PhimpmeProgressBarHandler(this);
        accountPresenter.attachView(this);
        databaseHelper = new DatabaseHelper(realm);
        setSupportActionBar(toolbar);
        toolbar.setPopupTheme(getPopupToolbarStyle());
        toolbar.setBackgroundColor(getPrimaryColor());
        setUpRecyclerView();
        accountPresenter.loadFromDatabase();  // Calling presenter function to load data from database
        getSupportActionBar().setTitle(R.string.title_account);
        phimpmeProgressBarHandler.show();
        //  googleApiClient();
    }


    /*    private void googleApiClient(){
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            // Build a GoogleApiClient with access to the Google Sign-In API and the
            // options specified by gso.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, AccountActivity.this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_accounts_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_account_settings:
                startActivity(new Intent(AccountActivity.this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setUpRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        accountsRecyclerView.setLayoutManager(layoutManager);
        accountsRecyclerView.setAdapter(accountAdapter);
        accountsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListner(this, this));
    }

    @Override
    public void setUpAdapter(@NotNull RealmQuery<AccountDatabase> accountDetails) {
        this.realmResult = accountDetails;
        accountAdapter.setResults(realmResult);
    }

    @Override
    public void showError() {
        SnackBarHandler.show(coordinatorLayout, getString(R.string.no_account_signed_in));
    }

    @Override
    public void showComplete() {
        phimpmeProgressBarHandler.hide();
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_accounts;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.navigation_accounts;
    }

    @Override
    public void onItemClick(final View childView, final int position) {
        final SwitchCompat signInSignOut = (SwitchCompat) childView.findViewById(R.id.sign_in_sign_out_switch);
        final String name = AccountDatabase.AccountName.values()[position].toString();

        if (!signInSignOut.isChecked()) {
            if (!checkNetwork(this, parentLayout)) return;
            switch (AccountDatabase.AccountName.values()[position]) {

                case NEXTCLOUD:
                    Intent nextCloudShare = new Intent(getContext(), NextCloudAuth.class);
                    startActivityForResult(nextCloudShare, NEXTCLOUD_REQUEST_CODE);
                    break;

                case OWNCLOUD:
                    Intent ownCloudShare = new Intent(getContext(), OwnCloudActivity.class);
                    startActivityForResult(ownCloudShare, OWNCLOUD_REQUEST_CODE);
                    break;


                default:
                    SnackBarHandler.show(coordinatorLayout, R.string.feature_not_present);
            }
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage(name)
                    .setTitle(getString(R.string.sign_out_dialog_title))
                    .setPositiveButton(R.string.yes_action,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    databaseHelper
                                            .deleteSignedOutAccount(name);
                                    accountAdapter.notifyDataSetChanged();
                                    accountPresenter.loadFromDatabase();
                                    signInSignOut.setChecked(false);
                                }
                            })
                    .setNegativeButton(R.string.no_action,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //TODO: Implement negative button action
                                }
                            })
                    .create();
            alertDialog.show();
            AlertDialogsHelper.setButtonTextColor(new int[]{DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE}, getAccentColor(), alertDialog);
        }
    }


    /*
    Catching the intent of the external browser login and getting that data
     */

    @Override
    protected void onNewIntent(Intent intent) {
        try{

        }catch (Exception e)
        {
            //Nothing is to be done when the BROWSABLE Intent is null
        }
        super.onNewIntent(intent);
    }



    @Override
    public void onItemLongPress(View childView, int position) {
        // TODO: long press to implemented
    }


    @Override
    public Context getContext() {
        this.context = this;
        return context;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivitySwitchHelper.setContext(this);
        setNavigationBarColor(ThemeHelper.getPrimaryColor(this));
        toolbar.setBackgroundColor(getPrimaryColor());
        //dropboxAuthentication();
        setStatusBarColor();
        setNavBarColor();
        accountPresenter.loadFromDatabase();
        accountAdapter.updateTheme();
        accountAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == OWNCLOUD_REQUEST_CODE && resultCode == RESULT_OK) || (requestCode == NEXTCLOUD_REQUEST_CODE && resultCode == RESULT_OK)) {
            realm.beginTransaction();
            if (requestCode == NEXTCLOUD_REQUEST_CODE) {
                account = realm.createObject(AccountDatabase.class, NEXTCLOUD.toString());
            } else {
                account = realm.createObject(AccountDatabase.class, OWNCLOUD.toString());
            }
            account.setServerUrl(data.getStringExtra(getString(R.string.server_url)));
            account.setUsername(data.getStringExtra(getString(R.string.auth_username)));
            account.setPassword(data.getStringExtra(getString(R.string.auth_password)));
            realm.commitTransaction();
        }
     /*   if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }*/
    }

    /*private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();//acct.getDisplayName()
            SnackBarHandler.show(parentLayout,R.string.success);
            realm.beginTransaction();
            account = realm.createObject(AccountDatabase.class, GOOGLEPLUS.name());account.setUsername(acct.getDisplayName());
            account.setUserId(acct.getId());
            realm.commitTransaction();
        } else {
            SnackBarHandler.show(parentLayout,R.string.google_auth_fail);
        }
    }*/

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        SnackBarHandler.show(coordinatorLayout, getApplicationContext().getString(R.string.connection_failed));
    }


}
