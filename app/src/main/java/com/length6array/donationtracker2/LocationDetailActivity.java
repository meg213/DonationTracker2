package com.length6array.donationtracker2;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

/**
 * An activity representing a single Location detail screen. This activity is only used on narrow
 * width devices. On tablet-size devices, item details are presented side-by-side with a list of
 * items in a {@link LocationListActivity}.
 *
 * <p>Heyo okay so like this one is used to contain the LocationDetailFragment. Has two buttons, add
 * Donation and editLocation ALSO, you can click on a textEdit called Donations (its in the
 * Fragment) that will take you to the list of donations for that specific location.
 */
public class LocationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // TODO make this go to edit location
        FloatingActionButton editLocation = (FloatingActionButton) findViewById(R.id.editLocation);
        editLocation.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(
                                (new Intent(LocationDetailActivity.this, DonationActivity.class)));
                    }
                });

        FloatingActionButton addDonation = (FloatingActionButton) findViewById(R.id.addDonation);
        addDonation.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(
                                (new Intent(LocationDetailActivity.this, DonationActivity.class)));
                    }
                });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(
                    LocationDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(LocationDetailFragment.ARG_ITEM_ID));
            LocationDetailFragment fragment = new LocationDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.location_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, LocationListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
