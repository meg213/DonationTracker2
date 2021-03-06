package com.length6array.donationtracker2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Donations. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link DonationsDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two vertical panes.
 *
 * <p>Very similar setup to the LocationListActivity, so I won't really explain what's actually
 * going on. What IS really important to know are these crazy things called Intents! An intent is a
 * way to pass information from one Activity another. In this case, we pass the name of a location
 * from LocationDetailActivity (or elsewhere) so that then we can grab that location's donations and
 * fill the RecyclerView.
 */
public class DonationsListActivity extends AppCompatActivity {

    /** Whether or not the activity is in two-pane mode, i.e. running on a tablet device. */
    private boolean mTwoPane;

    // this will be used to grab an intent
    String location;
    Spinner filter;
    Spinner selectLocation;
    TextView noItems;
    String category = "All";
    String[] options = {"All", "Clothing", "Hat", "Kitchen", "Electronics", "Household", "Other"};
    ArrayList<String> categories = new ArrayList<>();
    SimpleItemRecyclerViewAdapter adapter1;

    // used for filtering
    int position1 = 0; // spinner position
    int locationSelection = 0; // location spinner selection
    ArrayList<String> locations = new ArrayList<>();
    ArrayList<Donation> donations = new ArrayList<>();
    String selection = "All";
    String selectedLocation = "All";
    SearchView searchView;
    String search;

    // the database stuff!!!
    myDBHandler myDBHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donations_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        /**
         * THIS IS WHERE I GRAB THE LOCATION name. I basically tell the program, "hey lemme get that
         * thing i sent ya earlier" then I go and grab it via a key
         */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            location = extras.getString("Location");
        }

        FloatingActionButton back = findViewById(R.id.DonationGoBack);
        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(
                                new Intent(DonationsListActivity.this, LocationListActivity.class));
                    }
                });

        FloatingActionButton addDonation = (FloatingActionButton) findViewById(R.id.addDonation);
        addDonation.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(
                                new Intent(DonationsListActivity.this, DonationActivity.class));
                    }
                });

        // THIS IS THE DATABASE STUFF!!!!!
        myDBHandler = new myDBHandler(this, null, null, 2);
        getUpdatedDatabase();

        ArrayList<String> test = new ArrayList<>();
        Cursor cursor = myDBHandler.getAllDonations();
        if (cursor.moveToFirst()) {
            do {
                test.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // this is just making a new locations string to include "all"
        for (int i = 0; i < Location.locations.size(); i++) {
            if (i == 0) {
                locations.add(location); // make the default being the current location
            } else {
                if (i == 1) {
                    locations.add("All"); // option to view donations from all locations
                }
                if (!Location.locations.get(i - 1).getName().equals(location)) {
                    locations.add(Location.locations.get(i - 1).getName());
                }
            }
        }

        setupLocationSpinner();

        setupFilterSpinner();

        noItems = findViewById(R.id.noResultsFoundView);

        if (findViewById(R.id.donations_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.donations_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(getSelectedCategory(position1, locationSelection));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final DonationsListActivity mParentActivity;
        private final List<Donation> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener =
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Donation donation = (Donation) view.getTag();
                        if (mTwoPane) {
                            Bundle arguments = new Bundle();
                            arguments.putString(
                                    DonationsDetailFragment.ARG_ITEM_ID, donation.getName());
                            DonationsDetailFragment fragment = new DonationsDetailFragment();
                            fragment.setArguments(arguments);
                            mParentActivity
                                    .getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.donations_detail_container, fragment)
                                    .commit();
                        } else {
                            Context context = view.getContext();
                            Intent intent = new Intent(context, DonationsDetailActivity.class);
                            intent.putExtra(
                                    DonationsDetailFragment.ARG_ITEM_ID, donation.getName());
                            context.startActivity(intent);
                        }
                    }
                };

        SimpleItemRecyclerViewAdapter(
                DonationsListActivity parent, List<Donation> donations, boolean twoPane) {
            mValues = donations;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.donations_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).getName());
            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }

    private SimpleItemRecyclerViewAdapter getSelectedCategory(int categoryID, int locationID) {
        selection = categories.get(categoryID); // grabs whatever category the user selected
        selectedLocation = locations.get(locationID); // grabs the location the user selected

        donations = sort(selectedLocation, selection);
        adapter1 = new SimpleItemRecyclerViewAdapter(this, donations, mTwoPane);
        return adapter1;
    }

    // sets up the different categories spinner
    private void setupFilterSpinner() {
        categories.add("All");
        categories.add("Clothing");
        categories.add("Hat");
        categories.add("Kitchen");
        categories.add("Electronics");
        categories.add("Household");
        categories.add("Other");
        filter = findViewById(R.id.spinnerFilter);
        ArrayAdapter<String> adapter =
                new ArrayAdapter(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filter.setAdapter(adapter);
        filter.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> adapterView, View view, int position, long id) {
                        if (position >= 0 && position < options.length) {
                            position1 = position;
                            View recyclerView = findViewById(R.id.donations_list);
                            assert recyclerView != null;
                            setupRecyclerView((RecyclerView) recyclerView);
                        } else {
                            Toast.makeText(
                                            DonationsListActivity.this,
                                            "Selected Category DNE",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
    }

    // sets up categories for location spinner
    private void setupLocationSpinner() {
        selectLocation = findViewById(R.id.spinnerLocation);
        ArrayAdapter<String> adapter =
                new ArrayAdapter(this, android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectLocation.setAdapter(adapter);
        selectLocation.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> adapterView, View view, int position, long id) {
                        if (position >= 0 && position < options.length) {
                            locationSelection = position;
                            View recyclerView = findViewById(R.id.donations_list);
                            assert recyclerView != null;
                            setupRecyclerView((RecyclerView) recyclerView);
                        } else {
                            Toast.makeText(
                                            DonationsListActivity.this,
                                            "Selected Category DNE",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.search_bar);
        searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        search = newText.toLowerCase();
                        View recyclerView = findViewById(R.id.donations_list);
                        assert recyclerView != null;
                        setupRecyclerView((RecyclerView) recyclerView);
                        return false;
                    }
                });

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This is the lengthy way of filtering through the donations uses all 3 classes: sort,
     * sortDonations, and search. Sort: handles location sorting sortDonations: handles sorting by
     * donation category search: handles search bar sorting
     *
     * <p>I decided to make the sort method call the other two on return just so that if any of the
     * params are changed, it will go through and check everything.
     *
     * @param selectedLocation the location specified by the user with spinner
     * @param selectedType the donation type specified by the user with spinner
     * @return
     */
    public ArrayList<Donation> sort(String selectedLocation, String selectedType) {
        ArrayList<Donation> sort = new ArrayList<>();
        Log.i("SelectedLocation", selectedLocation);
        if (selectedLocation.equals("All")) { // all locations
            sort = Donation.donations;

        } else { // else find specific location & add those donations
            for (int i = 0; i < Donation.donations.size(); i++) {
                if (Donation.donations.get(i).getLocation().equals(selectedLocation)) {
                    sort.add(Donation.donations.get(i));
                    Log.i("sortLocations", "added " + Donation.donations.get(i).getName());
                }
            }
        }
        return sortDonations(sort, selectedType);
    }

    private ArrayList<Donation> sortDonations(
            ArrayList<Donation> donations, String selectedCategory) {
        ArrayList<Donation> sorted = new ArrayList<>();
        if (selectedCategory.equals("All")) {
            sorted = donations;
        } else {
            for (int i = 0; i < donations.size(); i++) {
                if (donations.get(i).getType().equals(selectedCategory)) {
                    sorted.add(donations.get(i));
                    Log.i("sortDonations", "added " + donations.get(i).getName());
                }
            }
        }
        return search(sorted, search);
    }

    public ArrayList<Donation> search(ArrayList<Donation> donation, String search) {

        // basically for if the user backspaces all the way so that there's no characters
        // will just return the donations as have been sorted up to this point
        // if by filtering it causes no results, this is shown to the user
        if (search == null || search.length() == 0) {
   //         Log.i("Search", "search is empty or null");
            if (donation.size() == 0) { // say "no items matching search"
                noItems.setVisibility(TextView.VISIBLE);
            } else {
                noItems.setVisibility(TextView.INVISIBLE);
            }
            return donation;
        } else {
            ArrayList<Donation> sorted = new ArrayList<>();
            for (int i = 0; i < donation.size(); i++) {
            }
            for (int i = 0; i < donation.size(); i++) {
                if (donation.get(i).getName().toLowerCase().contains(search)) {
                    sorted.add(donation.get(i));
                }
            }
            if (sorted.size() == 0) { // say "no items matching search"
                noItems.setVisibility(TextView.VISIBLE);
            } else {
                noItems.setVisibility(TextView.INVISIBLE);
            }
            return sorted;
        }
    }

    /**
     * This method pulls from the database and puts all the donations in the Donation class's
     * Arraylist: donations and Map: DONATION_MAP to be used while the app is running
     *
     * <p>Tip: Just put the values that you get into the maps and lists that have already been
     * defined so that you don't have to change anything else in the program. Idk why but I was
     * really dumb and originally created like an arraylist "databaseDonations" and decided to use
     * that but it was more of a hassle bc other classes reference those maps and arraylists defined
     * in Location, Donation, and Person so it would cause compile errors bc those would be null and
     * only "databaseDonations", which was local to this class, only had the data. Pls don't be dumb
     * like me. //
     */
    public void getUpdatedDatabase() {
        Cursor cursor = myDBHandler.getAllDonations();
        Log.i("DonationListActivity", "In getUpdatedDatabase");
        Log.i("DonationsListActivity", "Count = " + myDBHandler.getAllDonations().getCount());
        Log.i("DonationListActivity", "Donation.size = " + Donation.donations.size() + " ");

        if (cursor.moveToFirst()) { // if there/s a line to be read
            do {
                Donation d = new Donation();
                d.setName(cursor.getString(1));
                d.setLocation(cursor.getString(2));
                d.setDateAdded(cursor.getString(3));
                d.setType(cursor.getString(4));
                d.setDescription(cursor.getString(5));
                d.setValue(cursor.getString(6));

                boolean matched = false;
                for (int i = 0; i < Donation.donations.size(); i++) {
                    if (d.getName().equals(Donation.donations.get(i).getName())) {
                        if (d.getType().equals(Donation.donations.get(i).getType())) {
                            matched = true;
                            Log.i("DonationListActivity", "Matched = True");
                            Log.i(
                                    "DonationListActivity",
                                    d.getName() + " " + Donation.donations.get(i).getName());
                        }
                    }
                }

                if (!matched) {
                    Donation.donations.add(d); // putting into an arraylist to be used now
                    Donation.DONATION_MAP.put(
                            d.getName(), d); // into a map to be used for other activities
                    Log.i("DonationListActivity", "Added " + d.getName());
                }

            } while (cursor
                    .moveToNext()); // this line basically just says "do while there's lines to read
        }
    }
}
