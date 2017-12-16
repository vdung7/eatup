package vn.momo.eatup.ui;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;

import vn.momo.eatup.R;
import vn.momo.eatup.provider.EatFor;
import vn.momo.eatup.provider.EatUpProviderAPI;

/**
 * @author dungvu
 * @since 12/11/17
 */
public class EatInputDialog extends Dialog implements View.OnClickListener {
    private EditText eatInput;

    public EatInputDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_eat_input);

        eatInput = findViewById(R.id.input_eat);
        Button btnOk = findViewById(R.id.button_input_eat_ok);
        Button btnCancel = findViewById(R.id.button_input_eat_cancel);
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    private void onEatInputEntered(View view) {
        //update to db
        if (eatInput != null) {
            ContentValues v = new ContentValues();
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            EatFor eatfor = EatFor.LUNCH;
            if (hour < 11) {
                eatfor = EatFor.BREAKFAST;
            } else if(hour > 16) {
                eatfor = EatFor.DINNER;
            }

            v.put(EatUpProviderAPI.EatWhatColumn.NAME, eatInput.getText().toString());
            v.put(EatUpProviderAPI.EatWhatColumn.LAST_EAT_DATE, calendar.getTimeInMillis());
            v.put(EatUpProviderAPI.EatWhatColumn.EAT_FOR, eatfor.ordinal());
            getContext().getContentResolver().insert(EatUpProviderAPI.EatWhatColumn.CONTENT_URI, v);
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
}
