package com.quang.timeslots.habitdetails;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.quang.timeslots.R;

public class HabitDeleteDialogFragment extends DialogFragment {
    public interface HabitDeleteDialogListener {
        void onDeleteDialogPositiveClick();
    }

    private HabitDeleteDialogListener _dialogListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inflater.inflate(R.layout.habit_delete_dialog, null))
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        _dialogListener.onDeleteDialogPositiveClick();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            _dialogListener = (HabitDeleteDialogListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement HabitDeleteDialogListener");
        }
    }
}
