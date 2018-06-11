package com.quang.timeslots.habitlist;

import android.app.DialogFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.quang.timeslots.R;
import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.common.HabitEditDialogFragment;
import com.quang.timeslots.common.HabitTimer;
import com.quang.timeslots.db.Habit;
import com.quang.timeslots.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity that shows a list of habits
 */
public class HabitListActivity extends AppCompatActivity
        implements HabitEditDialogFragment.HabitEditDialogListener {
    private HabitListAdapter _habitListAdapter;
    private HabitListViewModel _viewModel;
//    private GoogleSignInClient _googleSignInClient;

//    private static int RC_SIGN_IN = 9001;

    /**
     * Callback for when the activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit_list);

        RecyclerView habitListView = findViewById(R.id.habit_list);
        habitListView.setLayoutManager(new LinearLayoutManager(this));
        _habitListAdapter = new HabitListAdapter(this, new ArrayList<Habit>());
        habitListView.setAdapter(_habitListAdapter);
        HabitListItemTouchHelperCallback callback = new HabitListItemTouchHelperCallback(_habitListAdapter);
        (new ItemTouchHelper(callback)).attachToRecyclerView(habitListView);

        _viewModel = ViewModelProviders.of(this).get(HabitListViewModel.class);
        _viewModel.getHabitList().observe(this, new Observer<List<Habit>>() {
            @Override
            public void onChanged(@Nullable List<Habit> habits) {
                _habitListAdapter.setHabitList(habits);
            }
        });

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        FloatingActionButton fab = findViewById(R.id.create_habit_button);
        fab.setOnClickListener(new FABOnClickListener());

//        Initialize Google sign-in client
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.server_client_id))
//                .requestEmail()
//                .build();
//        _googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Callback for when the activity is resumed
     */
    public void onResume() {
        super.onResume();
        HabitTimer.getInstance().subscribeListener(_habitListAdapter);
    }

    /**
     * Callback for when new habit is created
     * @param habit
     */
    @Override
    public void onEditDialogPositiveClick(Habit habit) {
        _viewModel.addHabit(habit);
    }

    /**
     * Callback to create the action bar menu
     * @param menu
     * @return True
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Callback to react to a button press on the action bar
     * @param menuItem - One of the menu items on the action bar
     * @return True
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.settings_button:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }

        return true;
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN) {
//            // [START get_id_token]
//            // This task is always completed immediately, there is no need to attach an
//            // asynchronous listener.
////            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
////            handleSignInResult(task);
//            // [END get_id_token]
//        }
//    }

    /**
     * React to a reordering of habits
     * @param habits - List of habits in the new order
     */
    public void onHabitsReorder(List<Habit> habits) {
        _viewModel.reorderHabits(habits);
    }


    //////////


    /**
     * Listener that reacts to a click on the add habit button
     */
    private class FABOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            DialogFragment editDialog = HabitEditDialogFragment.newInstance(new Habit("", 15));
            editDialog.show(getFragmentManager(), "HabitEditDialogFragment");
        }
    }
}
