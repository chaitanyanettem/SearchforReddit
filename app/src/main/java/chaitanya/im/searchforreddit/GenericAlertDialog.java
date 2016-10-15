package chaitanya.im.searchforreddit;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

public class GenericAlertDialog extends DialogFragment {

    private int whichDialog;
    private final String TAG = "GenericAlertDialog.java";
    private int purchaseDialog = -1;
    private Typeface fontAwesome;
    private List<String> allPrices;
    private int skuCode;

    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView;
        setRetainInstance(true);

        switch (whichDialog) {
            case 0:
                dialogView = inflater.inflate(R.layout.dialog_purchase, null);
                builder.setView(dialogView)
                        .setPositiveButton(R.string.ok_purchase, buttonListener);

                final TextView dialogPurchaseTitle = (TextView) dialogView.findViewById(R.id.dialog_purchase_title);
                final TextView purchaseDescription = (TextView) dialogView.findViewById(R.id.purchase_description);
                final TextView priceDisplay = (TextView) dialogView.findViewById(R.id.price_display);
                final TextView coffeePitchText = (TextView) dialogView.findViewById(R.id.coffee_pitch_text);
                coffeePitchText.setTypeface(fontAwesome);
                coffeePitchText.setText(Html.fromHtml(getResources().getString(R.string.donate_coffee_pitch)));

                // SeekBar settings
                AppCompatSeekBar seekBar = (AppCompatSeekBar) dialogView.findViewById(R.id.price_seekbar);
                seekBar.setKeyProgressIncrement(1);
                seekBar.setMax(2);
                seekBar.setProgress(1);

                if (allPrices != null && allPrices.size() != 0) {
                    // We can do a purchase since allPrices isn't null
                    assert priceDisplay != null;
                    priceDisplay.setText(allPrices.get(1));

                    if (purchaseDialog == 1) {
                        dialogPurchaseTitle.setText(Html.fromHtml(getResources().getString(R.string.donate_dialog_title_2)));
                        purchaseDescription.setText(Html.fromHtml(getResources().getString(R.string.donate_features2)));
                    }

                    skuCode = 1;
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                            skuCode = progress;
                            priceDisplay.setText(allPrices.get(progress));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                }
                else {
                    // Aw shucks! We cannot do a purchase.
                    skuCode = 1;
                    seekBar.setEnabled(false);
                    priceDisplay.setText(getResources().getString(R.string.donate_impossible));
                }
                return builder.create();
            case 1:
                skuCode = -1;
                dialogView = inflater.inflate(R.layout.dialog_license, null);
                builder.setView(dialogView)
                        .setTitle("Licenses")
                        .setPositiveButton(R.string.ok_string, buttonListener);
                return builder.create();

            case 2:
                skuCode = -1;
                dialogView = inflater.inflate(R.layout.dialog_about, null);

                builder.setView(dialogView)
                        .setPositiveButton(R.string.ok_string, buttonListener);

                int versionCode = BuildConfig.VERSION_CODE;
                String versionName = BuildConfig.VERSION_NAME;
                String versionText = "Version Code <font color=#FF6F00>" + versionCode + "</font><br/>" +
                        "Version Name <font color=#FF6F00>" + versionName + "</font><br/>";

                TextView aboutText = (TextView) dialogView.findViewById(R.id.about_text);
                TextView aboutVersion = (TextView) dialogView.findViewById(R.id.about_version);
                TextView aboutCopyright = (TextView) dialogView.findViewById(R.id.about_copyright);
                aboutText.setTypeface(fontAwesome);
                aboutCopyright.setTypeface(fontAwesome);
                aboutText.setText(Html.fromHtml(getResources().getString(R.string.about_text)));
                aboutVersion.setText(Html.fromHtml(versionText));
                aboutCopyright.setText(getResources().getString(R.string.about_copyright));
                return builder.create();

            default:
                return builder.create();

        }
    }

    private final DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            ((LauncherActivity)getActivity()).doPositiveClick(skuCode);
        }
    };

    public void setFontAwesome(Typeface fontAwesome) {
        this.fontAwesome = fontAwesome;
    }

    public void setWhichDialog(int whichDialog) {
        this.whichDialog = whichDialog;
    }

    public void setAllPrices(List<String> allPrices) {
        this.allPrices = allPrices;
    }

    public void setPurchaseDialog(int purchaseDialog) {
        this.purchaseDialog = purchaseDialog;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
