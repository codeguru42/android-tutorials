package apt.tutorial;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class DetailForm extends Activity {

    EditText name = null;
    EditText address = null;
    EditText notes = null;
    RadioGroup types = null;
    RestaurantHelper helper = null;
    String restaurantId = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_form);

        helper = new RestaurantHelper(this);

        name = (EditText) findViewById(R.id.name);
        address = (EditText) findViewById(R.id.addr);
        notes = (EditText) findViewById(R.id.notes);
        types = (RadioGroup) findViewById(R.id.types);

        Button save = (Button) findViewById(R.id.save);

        save.setOnClickListener(onSave);

        restaurantId = getIntent().getStringExtra(LunchList.ID_EXTRA);

        if (restaurantId != null) {
            load();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        helper.close();
    }

    private void load() {
        Cursor c = helper.getById(restaurantId);

        c.moveToFirst();
        name.setText(helper.getName(c));
        address.setText(helper.getAddress(c));
        notes.setText(helper.getNotes(c));

        if (helper.getType(c).equals("sit_down")) {
            types.check(R.id.sit_down);
        } else if (helper.getType(c).equals("take_out")) {
            types.check(R.id.take_out);
        } else {
            types.check(R.id.delivery);
        }

        c.close();
    }
    private View.OnClickListener onSave = new View.OnClickListener() {
        public void onClick(View v) {
            String type = null;

            switch (types.getCheckedRadioButtonId()) {
                case R.id.sit_down:
                    type = "sit_down";
                    break;

                case R.id.take_out:
                    type = "take_out";
                    break;

                case R.id.delivery:
                    type = "delivery";
                    break;
            }

            if (restaurantId == null) {
                helper.insert(name.getText().toString(), address.getText().toString(), type, notes.getText().toString());
            } else {
                helper.update(restaurantId, name.getText().toString(), address.getText().toString(), type, notes.getText().toString());
            }

            finish();
        }
    };
}
