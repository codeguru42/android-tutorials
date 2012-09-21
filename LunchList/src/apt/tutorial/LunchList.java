package apt.tutorial;

import android.app.TabActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.atomic.AtomicBoolean;

public class LunchList extends TabActivity {

    Cursor model = null;
    RestaurantAdapter adapter = null;
    EditText name = null;
    EditText address = null;
    RadioGroup types = null;
    EditText notes = null;
    Restaurant current = null;
    int progress = 0;
    AtomicBoolean isActive = new AtomicBoolean(true);
    RestaurantHelper helper = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.main);

        helper = new RestaurantHelper(this);

        name = (EditText) findViewById(R.id.name);
        address = (EditText) findViewById(R.id.addr);
        types = (RadioGroup) findViewById(R.id.types);
        notes = (EditText) findViewById(R.id.notes);

        Button save = (Button) findViewById(R.id.save);

        save.setOnClickListener(onSave);

        ListView list = (ListView) findViewById(R.id.restaurants);

        model = helper.getAll();
        startManagingCursor(model);
        adapter = new RestaurantAdapter(model);
        list.setAdapter(adapter);

        TabHost.TabSpec spec = getTabHost().newTabSpec("tag1");

        spec.setContent(R.id.restaurants);
        spec.setIndicator("List", getResources().getDrawable(R.drawable.list));
        getTabHost().addTab(spec);

        spec = getTabHost().newTabSpec("tag2");
        spec.setContent(R.id.details);
        spec.setIndicator("Details", getResources().getDrawable(R.drawable.restaurant));
        getTabHost().addTab(spec);

        getTabHost().setCurrentTab(0);

        list.setOnItemClickListener(onListClick);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        helper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.option, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.toast) {
            String message = "No restaurant selected";

            if (current != null) {
                message = current.getNotes();
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            return true;
        } else if (item.getItemId() == R.id.run) {
            startWork();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        isActive.set(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        isActive.set(true);

        if (progress > 0) {
            startWork();
        }
    }

    private void startWork() {
        setProgressBarVisibility(true);
        new Thread(longTask).start();
    }

    private void doSomeLongWork(final int incr) {
        runOnUiThread(new Runnable() {
            public void run() {
                progress += incr;
                setProgress(progress);
            }
        });

        SystemClock.sleep(250); // should be something more useful
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

            helper.insert(name.getText().toString(), address.getText().toString(), type, notes.getText().toString());
            model.requery();
        }
    };
    private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            model.moveToPosition(position);

            name.setText(helper.getName(model));
            address.setText(helper.getAddress(model));
            notes.setText(helper.getNotes(model));

            if (helper.getType(model).equals("sit_down")) {
                types.check(R.id.sit_down);
            } else if (helper.getType(model).equals("take_out")) {
                types.check(R.id.take_out);
            } else {
                types.check(R.id.delivery);
            }

            getTabHost().setCurrentTab(1);
        }
    };
    private Runnable longTask = new Runnable() {
        public void run() {
            for (int i = progress; i < 10000 && isActive.get(); i += 200) {
                doSomeLongWork(200);
            }

            if (isActive.get()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        setProgressBarVisibility(false);
                        progress = 0;
                    }
                });
            }
        }
    };

    class RestaurantAdapter extends CursorAdapter {

        RestaurantAdapter(Cursor c) {
            super(LunchList.this, c);
        }

        @Override
        public void bindView(View row, Context ctxt, Cursor c) {
            RestaurantHolder holder = (RestaurantHolder) row.getTag();

            holder.populateFrom(c, helper);
        }

        @Override
        public View newView(Context ctxt, Cursor c, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.row, parent, false);
            RestaurantHolder holder = new RestaurantHolder(row);
            
            row.setTag(holder);
            
            return row;
        }
    }

    static class RestaurantHolder {

        private TextView name = null;
        private TextView address = null;
        private ImageView icon = null;
        private View row = null;

        RestaurantHolder(View row) {
            this.row = row;

            name = (TextView) row.findViewById(R.id.title);
            address = (TextView) row.findViewById(R.id.address);
            icon = (ImageView) row.findViewById(R.id.icon);
        }

        void populateFrom(Cursor c, RestaurantHelper helper) {
            name.setText(helper.getName(c));
            address.setText(helper.getAddress(c));

            if (helper.getType(c).equals("sit_down")) {
                icon.setImageResource(R.drawable.ball_red);
            } else if (helper.getType(c).equals("take_out")) {
                icon.setImageResource(R.drawable.ball_yellow);
            } else {
                icon.setImageResource(R.drawable.ball_green);
            }
        }
    }
}
