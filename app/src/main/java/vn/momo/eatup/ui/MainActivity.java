package vn.momo.eatup.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Iterator;
import java.util.Locale;

import vn.momo.eatup.R;
import vn.momo.eatup.constant.EatUpField;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
    }

    public void onEatInputClicked(View view) {
        EatInputDialog dialog = new EatInputDialog(this);
        dialog.show();
    }

    public void onEatSuggestionClicked(View view) {
        Query q = db.collection(EatUpField.EATWHAT_TABLE_NAME)
                .limit(7)
                .orderBy(EatUpField.LAST_EAT_DATE, Query.Direction.ASCENDING);
        q.get().addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                String lastName = null;
                StringBuilder options = new StringBuilder();

                Iterator<DocumentSnapshot> i = documentSnapshots.iterator();
                while (i.hasNext()) {
                    DocumentSnapshot d = i.next();
                    String name = d.getString(EatUpField.NAME);
                    long times = d.getLong(EatUpField.EAT_TIMES);

                    name = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
                    if (!i.hasNext()) {
                        lastName = name;
                    } else {
                        options.append(String.format(Locale.US, "%s (%d)\n", name, times));
                    }
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
        });
    }
}
