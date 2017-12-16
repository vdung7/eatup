package vn.momo.eatup.ui;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import vn.momo.eatup.R;
import vn.momo.eatup.provider.EatUpProviderAPI;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onEatInputClicked(View view) {
        EatInputDialog dialog = new EatInputDialog(this);
        dialog.show();
    }

    public void onEatSuggestionClicked(View view) {
        Cursor c = getContentResolver()
                .query(EatUpProviderAPI.EatWhatColumn.CONTENT_URI,
                        new String[]{EatUpProviderAPI.EatWhatColumn.NAME}, null, null,
                        EatUpProviderAPI.EatWhatColumn.LAST_EAT_DATE + " DESC LIMIT 7");
        String lastName = null;
        StringBuilder options = new StringBuilder();
        if (c != null) {
            while (c.moveToNext()) {
                String name = c.getString(0);
                if (c.isLast()) {
                    lastName = name;
                } else {
                    options.append(name);
                    options.append("\n");
                }
            }
            c.close();
        }

        TextView tv = findViewById(R.id.eatup_suggestion);
        if (tv != null) {
            if (lastName == null) {
                tv.setText(R.string.eat_suggestion_result_notfound);
            } else {
                tv.setText(getString(R.string.eat_suggestion_result, lastName));
                if (options.length() > 0) {
                    TextView o = findViewById(R.id.eatup_suggestion_options);
                    if (o != null) {
                        o.setText(getString(R.string.eat_suggestion_result_options, options.toString()));
                    }
                }
            }
        }

    }

    public void onSaveToCloudClicked(View view) {
        //...
    }
}
