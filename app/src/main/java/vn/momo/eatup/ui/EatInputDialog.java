package vn.momo.eatup.ui;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Calendar;

import vn.momo.eatup.R;
import vn.momo.eatup.provider.EatFor;
import vn.momo.eatup.provider.EatUpProvider;
import vn.momo.eatup.provider.EatUpProviderAPI;

/**
 * @author dungvu
 * @since 12/11/17
 */
public class EatInputDialog extends Dialog implements View.OnClickListener {
    private AutoCompleteTextView eatInput;

    public EatInputDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_eat_input);

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
        Cursor c = getContext().getContentResolver().query(EatUpProviderAPI.EatWhatColumn.CONTENT_URI,
                new String[]{EatUpProviderAPI.EatWhatColumn.NAME},
                null, null, null);
        if (c != null) {
            String[] nameList = new String[c.getCount()];
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToNext();
                nameList[i] = c.getString(0);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, nameList);
            eatInput.setAdapter(adapter);
            c.close();
        }
    }

    private EatUpItem checkNameExist(String name) {
        Cursor c = getContext().getContentResolver().query(EatUpProviderAPI.EatWhatColumn.CONTENT_URI,
                new String[]{EatUpProviderAPI.EatWhatColumn._ID, EatUpProviderAPI.EatWhatColumn.EAT_TIMES},
                EatUpProviderAPI.EatWhatColumn.NAME + "=? COLLATE NOCASE",
                new String[]{name}, null);
        EatUpItem item = new EatUpItem();
        if (c != null) {
            if (c.moveToFirst()) {
                item.id = c.getLong(0);
                item.eatTimes = c.getInt(1);
            }
            c.close();
        }
        return item;
    }

    private void onEatInputEntered(View view) {
        //update to db
        if (eatInput != null) {
            String name = eatInput.getText().toString();
            name = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
            EatUpItem oldItem = checkNameExist(name);

            ContentValues v = new ContentValues();
            v.put(EatUpProviderAPI.EatWhatColumn.NAME, name);
            v.put(EatUpProviderAPI.EatWhatColumn.EAT_TIMES, 1);
            v.put(EatUpProviderAPI.EatWhatColumn.LAST_EAT_DATE, System.currentTimeMillis());
            v.put(EatUpProviderAPI.EatWhatColumn.EAT_FOR, getSelectedEatFor().ordinal());

            if (oldItem.id == -1) { //insert new record
                getContext().getContentResolver()
                        .insert(EatUpProviderAPI.EatWhatColumn.CONTENT_URI, v);
            } else { //update the existed record
                v.put(EatUpProviderAPI.EatWhatColumn.EAT_TIMES, oldItem.eatTimes + 1);
                getContext().getContentResolver()
                        .update(Uri.withAppendedPath(EatUpProviderAPI.EatWhatColumn.CONTENT_URI, String.valueOf(oldItem.id)), v, null, null);
            }
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
