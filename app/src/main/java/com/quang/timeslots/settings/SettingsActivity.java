package com.quang.timeslots.settings;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.TextView;

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
    private TextView _selectedDataFileTextView;
    private DateFileMenuClickListener _dataFileMenuClickListener;

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
        _selectedDataFileTextView = findViewById(R.id.settings_selected_file_text);
        _dataFileMenuClickListener = new DateFileMenuClickListener();
    }

    /**
     * Callback for resumed activity
     */
    @Override
    public void onResume() {
        super.onResume();
        _devmodeCheckbox.setChecked(TimeSlotsApplication.getInstance().isDevMode);
        String dataFileName =
                _getDataFileName(TimeSlotsApplication.getInstance().getDataFileUri());
        _selectedDataFileTextView.setText(getString(
                R.string.text_selected_data_file,
                (dataFileName.equals("") ? getString(R.string.text_none) : dataFileName)));
    }

    /**
     * Show menu when the data file text view is clicked
     * @param view
     */
    public void showDataFileMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);

        popupMenu.setOnMenuItemClickListener(_dataFileMenuClickListener);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.data_file_menu, popupMenu.getMenu());
        popupMenu.show();
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
        if (resultCode == Activity.RESULT_OK &&
                resultData != null &&
                (requestCode == CREATE_FILE_REQUEST_CODE || requestCode == LOAD_FILE_REQUEST_CODE)) {
            findViewById(R.id.settings_progress_bar_holder).setVisibility(View.VISIBLE);

            _dataFileUri = resultData.getData();

            //If new file was created
            if (requestCode == CREATE_FILE_REQUEST_CODE)
                (new FileIO()).writeToFile(_dataFileUri, this);
            //If existing file was selected
            else
                (new FileIO()).readFromFile(_dataFileUri, this);
        }
    }

    /**
     * Implementation of FileWriteListener's interface
     */
    @Override
    public void onFileWriteComplete() {
        _onFileReadWriteComplete();
    }

    /**
     * Implementation of FileReadListener's interface
     */
    @Override
    public void onFileReadComplete() {
        _onFileReadWriteComplete();
    }


    //////////


    /**
     * Listener for the data file menu
     */
    private class DateFileMenuClickListener implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.create_data_file_button:
                    onNewDataFileClick();
                    return true;
                case R.id.load_data_file_button:
                    onLoadDataFileClick();
                    return true;
                case R.id.disconnect_data_file_button:
                    TimeSlotsApplication.getInstance().deleteDataFileUri();
                    SettingsActivity.this.onResume();
                    return true;
            }
            return false;
        }

        /**
         * Callback for button to create new data file
         */
        private void onNewDataFileClick() {
            DialogFragment newFileDialog = new CreateDataFileDialogFragment();
            newFileDialog.show(
                    SettingsActivity.this.getSupportFragmentManager(),
                    "CreateDataFileDialogFragment");
        }

        /**
         * Callback for button to load existing data file
         */
        private void onLoadDataFileClick() {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            SettingsActivity.this.startActivityForResult(intent, LOAD_FILE_REQUEST_CODE);
        }
    }

    /**
     * Get file name from a URI
     * @param uri
     * @return String file name
     */
    public String _getDataFileName(Uri uri) {
        try {
            String result = null;
            if (uri.getScheme().equals("content")) {
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            }
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
            return result;
        }
        catch (Exception e) {
            return "";
        }
    }

    /**
     * Callback for when the data file read or write operation is finished
     */
    private void _onFileReadWriteComplete() {
        TimeSlotsApplication.getInstance().saveDataFileUri(_dataFileUri);
        String dataFileName = _getDataFileName(_dataFileUri);
        findViewById(R.id.settings_progress_bar_holder).setVisibility(View.GONE);
        _selectedDataFileTextView.setText(getString(R.string.text_selected_data_file, dataFileName));
    }
}
