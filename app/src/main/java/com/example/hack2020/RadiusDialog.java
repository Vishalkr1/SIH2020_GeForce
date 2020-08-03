package com.example.hack2020;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class RadiusDialog extends AppCompatDialogFragment {
    private ExampleDialogListener listener;
    private EditText editTextRadius;


    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.layout_radius, null);
        builder.setView(v)
                .setTitle("Set radius")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String rad = editTextRadius.getText().toString();
                        listener.applyText(rad);
                    }
                });
        editTextRadius = v.findViewById(R.id.edit_radius);
        return builder.create();

    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (RadiusDialog.ExampleDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ExampleDialogListener");
        }
    }
    public interface ExampleDialogListener {
        void applyText(String rad);

    }
}
