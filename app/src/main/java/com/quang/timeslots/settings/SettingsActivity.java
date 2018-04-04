package com.quang.timeslots.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.quang.timeslots.R;
import com.quang.timeslots.TimeSlotsApplication;

public class SettingsActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    private CheckBox _devmodeCheckbox;

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

    @Override
    public void onResume() {
        super.onResume();
        _devmodeCheckbox.setChecked(TimeSlotsApplication.getInstance().isDevMode);
    }

    public void onNewDataFileClick(View view) {
        DialogFragment newFileDialog = new CreateDataFileDialogFragment();
        newFileDialog.show(getSupportFragmentManager(), "CreateDataFileDialogFragment");
    }

    public void onFileNameClick(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

    }
}
