package vn.momo.eatup.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import vn.momo.eatup.R;
import vn.momo.eatup.constant.EatFor;
import vn.momo.eatup.constant.EatUpField;

/**
 * @author dungvu
 * @since 12/11/17
 */
public class EatInputDialog extends Dialog implements View.OnClickListener {
    private ArrayAdapter<String> autocompleteAdapter;
    private AutoCompleteTextView eatInput;
    private FirebaseFirestore db;

    EatInputDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_eat_input);
        db = FirebaseFirestore.getInstance();

        eatInput = findViewById(R.id.input_eat);
        setAutocompleteData();

        Button btnOk = findViewById(R.id.button_input_eat_ok);
        Button btnCancel = findViewById(R.id.button_input_eat_cancel);
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        // set up radio button group of "eat for"
        RadioGroup eatForRadio = findViewById(R.id.input_eat_for);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int eatfor = R.id.input_eat_for_lunch;
        if (hour < 11) {
            eatfor = R.id.input_eat_for_breakfast;
        } else if(hour > 16) {
            eatfor = R.id.input_eat_for_dinner;
        }
        eatForRadio.check(eatfor);
        eatForRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.e("test-check", "checked = " + i);
            }
        });

    }

    private void setAutocompleteData() {
        autocompleteAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line);
        eatInput.setAdapter(autocompleteAdapter);

        db.collection(EatUpField.EATWHAT_TABLE_NAME)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                    String name = documentSnapshot.getString(EatUpField.NAME);
                    name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                    autocompleteAdapter.add(name);
                }
            }
        });
    }

    private void onEatInputEntered(View view) {
        //update to db
        if (eatInput != null  && eatInput.length() > 0) {
            String inputName = eatInput.getText().toString();
            final String name = inputName.toLowerCase();
            db.collection(EatUpField.EATWHAT_TABLE_NAME)
                    .whereEqualTo(EatUpField.NAME, name)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        long currentTimeInMillis = System.currentTimeMillis();

                        if (task.getResult().size() > 0) {
                            Log.e("cloud", "duplicated name");
                            DocumentSnapshot d = task.getResult().getDocuments().get(0);
                            long eatTimes = d.getLong(EatUpField.EAT_TIMES);

                            Map<String, Object> updateValues = new HashMap<>();
                            updateValues.put(EatUpField.EAT_TIMES, eatTimes + 1);
                            updateValues.put(EatUpField.LAST_EAT_DATE, new Date(currentTimeInMillis));
                            updateValues.put(EatUpField.LAST_EAT_DATE_MILLIS, currentTimeInMillis);

                            db.collection(EatUpField.EATWHAT_TABLE_NAME).document(d.getId())
                                    .update(updateValues)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Log.e("cloud", "insert new");
                            Map<String, Object> eat = new HashMap<>();
                            eat.put(EatUpField.NAME, name.toLowerCase());
                            eat.put(EatUpField.EAT_TIMES, 1);
                            eat.put(EatUpField.LAST_EAT_DATE, new Date(currentTimeInMillis));
                            eat.put(EatUpField.LAST_EAT_DATE_MILLIS, currentTimeInMillis);
                            eat.put(EatUpField.EAT_FOR, getSelectedEatFor().ordinal());

                            db.collection(EatUpField.EATWHAT_TABLE_NAME)
                                    .add(eat)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Saved - " + task.getResult().getId(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }

        //dismiss this dialog
        cancel();
    }

    private void onCancelled(View view) {
        cancel();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_input_eat_ok:
                onEatInputEntered(view);
                break;
            case R.id.button_input_eat_cancel:
                onCancelled(view);
                break;
        }
    }

    private EatFor getSelectedEatFor() {
        RadioGroup eatForGroup = findViewById(R.id.input_eat_for);
        switch (eatForGroup.getCheckedRadioButtonId()) {
            case R.id.input_eat_for_dinner:
                return EatFor.DINNER;
            case R.id.input_eat_for_breakfast:
                return EatFor.BREAKFAST;
            case R.id.input_eat_for_lunch:
                return EatFor.LUNCH;
            case R.id.input_eat_for_snack:
                return EatFor.SNACK;
        }
        return EatFor.LUNCH;
    }

    private class EatUpItem {
        long id = -1;
        int eatTimes = 0;
    }
}
