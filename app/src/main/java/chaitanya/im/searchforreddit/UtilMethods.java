package chaitanya.im.searchforreddit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chaitanya.im.searchforreddit.DataModel.Child;
import chaitanya.im.searchforreddit.DataModel.Data_;
import chaitanya.im.searchforreddit.DataModel.RecyclerViewItem;

final class UtilMethods {

    private final static int THEME_DEFAULT = 0;
    private final static int THEME_BLACK = 1;
    private final static String TAG = "UtilMethods.java";

    // takes the utc post time as input and generates a string denoting how long ago the post was made
    private static String getTimeString(long utcTime) {
        // Inspired from https://gist.github.com/dmsherazi/5985a093076a8c4e7c38
        long difference;
        long unixTime = System.currentTimeMillis() / 1000L;  //get current time in seconds.
        int j;
        String[] periods = {"s", "m", "h", " day", " month", " year"};
        double[] lengths = {60, 60, 24, 30, 12, 10};
        difference = unixTime - utcTime;
        for (j = 0; difference >= lengths[j] && j < lengths.length-1; j++) {
            difference /= lengths[j];
        }

        if (difference > 1)
            if (j > 2)
                return difference + periods[j] + "s";

        return difference + periods[j];
    }

    // opens the thread when user clicks an item in the recyclerView
    static void resultClicked(AppCompatActivity activity, String url) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(url));
        //Log.d(TAG, url);

        if (browserIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(browserIntent);
        }
    }

    // extracts valid urls from the search string
    static String[] extractLinks(String text) {
        List<String> links = new ArrayList<>();
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String url = m.group();
            Log.d(TAG, "URL extracted: " + url);
            links.add(url);
        }
        return links.toArray(new String[links.size()]);
    }

    // extracts youtube id from a string
    private static String extractYoutubeID(String url) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    static void hideKeyboard(AppCompatActivity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Creates a RecyclerViewItem from a Child object and returns it
    static RecyclerViewItem buildRecyclerViewItem(Child c) {
        Data_ d = c.getData();
        RecyclerViewItem temp = new RecyclerViewItem();

        temp.setTitle(d.getTitle());
        temp.setSubreddit("/r/" + d.getSubreddit());
        temp.setAuthor("/u/" + d.getAuthor());
        temp.setNumComments(d.getNumComments());
        temp.setScore(d.getScore());
        temp.setPermalink("http://m.reddit.com" + d.getPermalink());
        temp.setTimeString(UtilMethods.getTimeString(d.getCreatedUtc()));
        temp.setSelf(d.getSelf());
        temp.setOver18(d.getOver18());
        temp.setUrl(d.getUrl());
        temp.setDomain(d.getDomain());
        temp.setLinkFlairText(d.getLinkFlairText());
        temp.setGilded(d.getGilded());
        temp.setArchived(d.getArchived());
        temp.setLocked(d.getLocked());

        //Log.d(TAG, temp.toString());
        return temp;
    }

    // Checks availability of network
    static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Sets the theme of the based on the savedThemePreference parameter passed to it.
    // savedThemePreference = 0 for light theme
    // savedThemePreference = 1 for dark theme
    static void onActivityCreateSetTheme(AppCompatActivity activity, int savedThemePreference, int source)
    {
        switch (savedThemePreference)
        {
            default:
            case THEME_DEFAULT:
                if (source == 0)
                    activity.setTheme(R.style.shareDialog);
                else
                    activity.setTheme(R.style.AppTheme);
                break;
            case THEME_BLACK:
                if (source == 0)
                    activity.setTheme(R.style.shareDialogDark);
                else
                    activity.setTheme(R.style.AppThemeDark);
                break;
        }
    }

    static void changeToTheme(Activity activity, int theme, SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(activity.getString(R.string.style_pref_key), theme);
        editor.commit();
        activity.recreate();
    }

    static void revealView (final View view) {
        revealView(view, -1, -1);
    }

    static void hideView (final View view) {
        hideView(view, -1, -1);
    }

    @SuppressLint("NewApi")
    static void revealView (final View view, int cx, int cy) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // get the center for the clipping circle
            if(cx == -1) {
                cx = view.getWidth() / 2;
                cy = view.getHeight() / 2;
            }

            //Log.d(TAG, "cx,cy = " + cx + "," + cy);
            // get the final radius for the clipping circle
            final float finalRadius = (float) Math.hypot(cx, cy);
            final int fx = cx;
            final int fy = cy;

            // create the animator for this view (the start radius is zero)
            view.post(new Runnable() {
                @Override
                public void run() {
                    Animator anim =
                            ViewAnimationUtils.createCircularReveal(view, fx, fy, 0, finalRadius);
                    view.setVisibility(View.VISIBLE);
                    anim.start();
                }
            });

        }
        else
            view.post(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.VISIBLE);
                }
            });
    }

    @SuppressLint("NewApi")
    static void hideView (final View view, int cx, int cy) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // get the center for the clipping circle
            if (cx == -1) {
                cx = view.getWidth() / 2;
                cy = view.getHeight() / 2;
            }

            //Log.d(TAG, "cx,cy = " + cx + "," + cy);
            // get the initial radius for the clipping circle
            final float initialRadius = (float) Math.hypot(cx, cy);
            final int fx = cx;
            final int fy = cy;

            view.post(new Runnable() {
                @Override
                public void run() {
                    // create the animation (the final radius is zero)
                    Animator anim =
                            ViewAnimationUtils.createCircularReveal(view, fx, fy, initialRadius, 0);
                    // make the view invisible when the animation is done
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.setVisibility(View.INVISIBLE);
                        }
                    });

                    // start the animation
                    anim.start();
                }
            });

        }
        else
            view.post(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.INVISIBLE);
                }
            });
    }

    static String buildSearchQuery(String[] links) {
        Log.d(TAG, Arrays.toString(links));
        String redditSearchString = "";
        for (String l: links) {
            String youtubeID = extractYoutubeID(l);
            if (youtubeID!=null) {
                while (youtubeID.startsWith("-")) {
                    youtubeID = youtubeID.substring(1);
                }
                redditSearchString = redditSearchString + "url:\"" + youtubeID + "\" OR ";
            }
            else {
                redditSearchString = redditSearchString + "url:\"" + l + "\" OR ";
            }
        }

        if (redditSearchString.endsWith(" OR "))
            return redditSearchString.substring(0, redditSearchString.length() - 4);

        return redditSearchString;
    }
}