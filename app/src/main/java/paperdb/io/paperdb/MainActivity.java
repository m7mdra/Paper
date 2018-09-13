package paperdb.io.paperdb;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_WRITE = 22;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String PERSON = "person";
    public static final int REQUEST_CODE_READ = 342;
    public static final int REQUEST_CODE_CREATE_FILE = 345;

    @Override
    protected void onResume() {
        super.onResume();
        if (hasExternalStoragePermission()) {
            createFileAndInitDB();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_CREATE_FILE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        findViewById(R.id.test_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasExternalStoragePermission())
                    writeValues();
                else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE);
                    }
                }
            }
        });

        final Button btnRead = (Button) findViewById(R.id.test_read);

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasExternalStoragePermission())
                    readValues();
                else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_READ);
                    }
                }
            }
        });
    }

    private void createFileAndInitDB() {
        final File file = new File(Environment.getExternalStorageDirectory(), "paper-db");
        file.mkdir();
        Paper.init(file);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                readValues();
        if (requestCode == REQUEST_CODE_WRITE)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                writeValues();
        if (requestCode == REQUEST_CODE_CREATE_FILE)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                createFileAndInitDB();
    }

    private void readValues() {
        LongHolder o1 = Paper.book().read("o1", new LongHolder(-1L));
        LongListHolder o2 = Paper.book().read("o2", new LongListHolder(Collections.singletonList(-1L)));
        long lastModified = Paper.book().lastModified("o1");
        Log.d(TAG, "lastModified: " + new Date(lastModified));
        ((Button) findViewById(R.id.test_read))
                .setText(String.format(Locale.ENGLISH, "Read: %d : %d", o1.getValue(), o2.getValue().get(0)));
    }

    private void writeValues() {
        LongHolder o1 = new LongHolder(12L);
        LongListHolder o2 = new LongListHolder(Collections.singletonList(23L));
        Paper.book().write("o1", o1);
        Paper.book().write("o2", o2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class Person {
        final String name;

        Person(String name) {
            this.name = name;
        }
    }

    interface Holder<V> {
        V getValue();
    }

    static abstract class AbstractValueHolder<V> implements Holder<V> {
        private final V value;

        AbstractValueHolder(V value) {
            this.value = value;
        }

        @Override
        public V getValue() {
            return value;
        }
    }

    static abstract class AbstractValueListHolder<V> extends AbstractValueHolder<List<V>> {
        AbstractValueListHolder(List<V> value) {
            super(value);
        }
    }

    static class LongHolder extends AbstractValueHolder<Long> {
        LongHolder(Long value) {
            super(value);
        }
    }

    static class LongListHolder extends AbstractValueListHolder<Long> {
        LongListHolder(List<Long> value) {
            super(value);
        }
    }

    public boolean hasExternalStoragePermission() {
        return ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
    }
}
