package com.quang.timeslots.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.quang.timeslots.R;
import com.quang.timeslots.TimeSlotsApplication;
import com.quang.timeslots.common.FileIO;

/**
 * Activity that shows various app settings
 */
public class SettingsActivity extends AppCompatActivity
    implements CreateDataFileDialogFragment.CreateDataFileDialogListener,
    FileIO.FileWriteListener,
    FileIO.FileReadListener {

    private static final int CREATE_FILE_REQUEST_CODE = 42;
    private static final int LOAD_FILE_REQUEST_CODE = 43;

    private CheckBox _devmodeCheckbox;
    private Uri _dataFileUri;

    /**
     * Callback for created activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _devmodeCheckbox = findViewById(R.id.settings_devmode_checkbox);
        _devmodeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                TimeSlotsApplication.getInstance().isDevMode = b;
            }
        });
    }

    /**
     * Callback for resumed activity
     */
    @Override
    public void onResume() {
        super.onResume();
        _devmodeCheckbox.setChecked(TimeSlotsApplication.getInstance().isDevMode);
    }

    /**
     * Callback for button to create new data file
     * @param view - Clicked button
     */
    public void onNewDataFileClick(View view) {
        DialogFragment newFileDialog = new CreateDataFileDialogFragment();
        newFileDialog.show(getSupportFragmentManager(), "CreateDataFileDialogFragment");
    }

    /**
     * Callback for button to load existing data file
     * @param view - Clicked button
     */
    public void onLoadDataFileClick(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, LOAD_FILE_REQUEST_CODE);
    }

    /**
     * Callback for the "Create" button in the new file dialog
     * @param fileName - Name of the new file
     */
    @Override
    public void onCreateDataFileDialogPositiveClick(String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    /**
     * Callback for after the file browser has been closed
     * @param requestCode - Code with which the browser was opened
     * @param resultCode - Code returned from the browser
     * @param resultData - Metadata of new/chosen file
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            //If new file was created
            if (requestCode == CREATE_FILE_REQUEST_CODE) {
                if (resultData != null) {
                    findViewById(R.id.settings_progress_bar_holder).setVisibility(View.VISIBLE);
                    Uri uri = resultData.getData();
                    (new FileIO()).writeToFile(uri, this);
                }
            }
            //If existing file was selected
            else if (requestCode == LOAD_FILE_REQUEST_CODE) {
                if (resultData != null) {
                    findViewById(R.id.settings_progress_bar_holder).setVisibility(View.VISIBLE);
                    _dataFileUri = resultData.getData();
                    (new FileIO()).writeToFile(_dataFileUri, this);
                }
            }
        }
    }

    /**
     * Implementation of FileWriteListener's interface
     */
    @Override
    public void onFileWriteComplete() {
        findViewById(R.id.settings_progress_bar_holder).setVisibility(View.GONE);
    }

    /**
     * Implementation of FileReadListener's interface
     */
    @Override
    public void onFileReadComplete() {
        (new FileIO()).writeToFile(_dataFileUri, null);
    }
}
