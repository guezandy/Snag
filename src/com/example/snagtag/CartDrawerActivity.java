package com.example.snagtag;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import android.app.SearchManager;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;

public class CartDrawerActivity extends ActionBarActivity {
	private final String TAG = "Base Activity";
    public ActionBar actionBar;
	public FrameLayout cartDrawerFrame = null;
    public DrawerLayout cartDrawerLayout = null;
    public ActionBarDrawerToggle cartDrawerToggle = null;
    public CharSequence activityTitle = null;
    public CharSequence cartDrawerTitle = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_drawer);
        actionBar = getSupportActionBar();
        handleSearchIntent(getIntent());
        // Check if there is a currently logged in user
        // and they are linked to a Facebook account.
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
         // Fetch Facebook user info if the session is active
            Session session = ParseFacebookUtils.getSession();
            if (session != null && session.isOpened()) {
                makeMeRequest();
        }else{
            // If the user is not logged in, go to the
            // activity showing the login view.
        	System.out.println("User is not logged in!");
            startLoginActivity();
            Log.i(TAG, "User was not logged in.");
            }
        }
        
        //Set up cart drawer, in progress
        activityTitle = actionBar.getTitle();
        cartDrawerTitle = "Cart";
        cartDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        cartDrawerFrame = (FrameLayout) findViewById(R.id.right_drawer);
     // create a new fragment and specify the planet to show based on position
        Fragment fragment = new CartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                       .replace(R.id.right_drawer, fragment)
                       .commit();
     // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        cartDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                cartDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                actionBar.setTitle(activityTitle);
            }

            public void onDrawerOpened(View view) {
                actionBar.setTitle(cartDrawerTitle);
            }
        };
        if (savedInstanceState == null) {
            selectItem(0);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        
        //Configure the search info and add any event listener
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = cartDrawerLayout.isDrawerOpen(cartDrawerFrame);
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
   
    
    
    @Override   
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (cartDrawerToggle.onOptionsItemSelected(item)) {
        return true;}
        
        
        switch(item.getItemId())
        {
            case R.id.action_settings:
                Log.i(TAG,"Settings Item Clicked");
                return true;
            case R.id.action_cart:
                Log.i(TAG,"Cart Item Clicked");
                if(cartDrawerLayout.isDrawerOpen(cartDrawerFrame)){
                    cartDrawerLayout.closeDrawer(cartDrawerFrame);
                }else{
                    cartDrawerLayout.openDrawer(cartDrawerFrame);
                }
                return true;
            case R.id.action_search:
                Log.i(TAG,"Search Item Clicked");
                return true;
            case R.id.action_profile:
                Log.i(TAG,"Profile Item Clicked");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }     
    }
    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.activity_frame, fragment).commit();
        cartDrawerLayout.closeDrawer(cartDrawerFrame);
    }
    @Override
    public void setTitle(CharSequence title) {
        actionBar.setTitle(title);
    }
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        cartDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        cartDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // Check if the user is currently logged
            // and show any cached content
        } else {
            // If the user is not logged in, go to the
            // activity showing the login view.
            startLoginActivity();
        }
    }
    
    public void startLoginActivity() {
	        Intent intent = new Intent(this, LoginActivity.class);
	        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        startActivity(intent);
	    }
	
	
	 


  
    /** Facebook request for user info.
     * Stores facebook graph data into Parse user.
     */
    private void makeMeRequest() {
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) { 
                            // Create a JSON object to hold the profile info
                            JSONObject userProfile = new JSONObject();
                            try {                   
                                // Populate the JSON object 
                                userProfile.put("facebookId", user.getId());
                                userProfile.put("name", user.getName());
                                if (user.getLocation().getProperty("name") != null) {
                                    userProfile.put("location", (String) user
                                            .getLocation().getProperty("name"));    
                                }                           
                                if (user.getProperty("gender") != null) {
                                    userProfile.put("gender",       
                                            (String) user.getProperty("gender"));   
                                }                           
                                if (user.getBirthday() != null) {
                                    userProfile.put("birthday",     
                                            user.getBirthday());                    
                                }                           
                                if (user.getProperty("relationship_status") != null) {
                                    userProfile                     
                                        .put("relationship_status",                 
                                            (String) user                                           
                                                .getProperty("relationship_status"));                               
                                }                           
                                // Now add the data to the UI elements
                                // ...
        
                            } catch (JSONException e) {
                                Log.d(TAG, "Error parsing returned user data.");
                            }
        
                        } else if (response.getError() != null) {
                            // handle error
                        }                  
                    }               
                });
        request.executeAsync();
     
    }
      
    /**
     * Handles a search intent
     * @param intent
     *      the intent that initialized the activity
     * 
     */
    private void handleSearchIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
        }
    }
      
}
