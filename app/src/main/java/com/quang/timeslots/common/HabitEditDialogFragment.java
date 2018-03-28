package com.quang.timeslots.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.gregacucnik.EditableSeekBar;
import com.quang.timeslots.R;
import com.quang.timeslots.db.Habit;

public class HabitEditDialogFragment extends DialogFragment {
    public interface HabitEditDialogListener {
        void onEditDialogPositiveClick(Habit habit);
    }

    HabitEditDialogListener _dialogListener;

    public static HabitEditDialogFragment newInstance(Habit currHabit) {
        HabitEditDialogFragment dialog = new HabitEditDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("habit", currHabit);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.habit_edit_dialog, null);

        final EditText nameField = dialogView.findViewById(R.id.habit_name_field);
        final EditableSeekBar slotLengthField = dialogView.findViewById(R.id.habit_slot_length_field);
        final Habit currHabit = getArguments().getParcelable("habit");
        nameField.setText(currHabit.getName());
        nameField.requestFocus();
        slotLengthField.setValue(currHabit.getSlotLength());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        _dialogListener.onEditDialogPositiveClick(
                                new Habit(nameField.getText().toString(), slotLengthField.getValue()));
                    }
                });
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            _dialogListener = (HabitEditDialogListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                " must implement HabitEditDialogListener");
        }
    }
}
