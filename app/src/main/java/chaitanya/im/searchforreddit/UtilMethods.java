package chaitanya.im.searchforreddit;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import chaitanya.im.searchforreddit.DataModel.Child;
import chaitanya.im.searchforreddit.DataModel.Data_;
import chaitanya.im.searchforreddit.DataModel.RecyclerViewItem;
import chaitanya.im.searchforreddit.DataModel.Result;

public final class UtilMethods {

    public static String getTimeString(long utcTime) {
        // Inspired from https://gist.github.com/dmsherazi/5985a093076a8c4e7c38
        long difference;
        long unixTime = System.currentTimeMillis() / 1000L;  //get current time in seconds.
        int j;
        String[] periods = {"s", "m", "h", "day", "month", "year"};
        double[] lengths = {60, 60, 24, 30, 12};
        difference = unixTime - utcTime;
        String tense = " ago";
        for (j = 0; difference >= lengths[j] && j < lengths.length - 1; j++) {
            difference /= lengths[j];
        }

        if (difference > 1)
            if (j > 2)
                return difference + " " + periods[j] + "s" + tense;

        return difference + periods[j] + tense;
    }

    public static void resultClicked(AppCompatActivity context, String url) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(url));
        Log.d("resultClicked()", url);

        if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(browserIntent);
        }
    }

    public static String[] extractLinks(String text) {
        List<String> links = new ArrayList<>();
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String url = m.group();
            Log.d("ShareActivity.java", "URL extracted: " + url);
            links.add(url);
        }
        return links.toArray(new String[links.size()]);
    }

}