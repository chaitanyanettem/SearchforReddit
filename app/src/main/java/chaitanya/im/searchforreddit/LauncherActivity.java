package chaitanya.im.searchforreddit;

import android.content.res.Resources;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // dp -> px : http://stackoverflow.com/a/9563438/1055475
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = 4 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(px);
        }
        else {
            findViewById(R.id.shadow).setVisibility(View.VISIBLE);
        }
    }
}
