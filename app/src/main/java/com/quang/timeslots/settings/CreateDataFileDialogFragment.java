package com.quang.timeslots.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.quang.timeslots.R;

/**
 * Popup dialog to enter the name for a new data file
 */
public class CreateDataFileDialogFragment extends DialogFragment {
    public interface CreateDataFileDialogListener {
        void onCreateDataFileDialogPositiveClick(String fileName);
    }

    private CreateDataFileDialogListener _listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_data_file_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText nameEditText = dialogView.findViewById(R.id.create_data_file_name_input);
                        _listener.onCreateDataFileDialogPositiveClick(nameEditText.getText().toString());
                    }
                });
        return builder.create();
    }

//    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CreateDataFileDialogListener) {
            _listener = (CreateDataFileDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CreateDataFileDialogListener");
        }
    }
}
