package com.cyberfanta.stringxmlfiltertool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Objects;
import java.util.Vector;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class MainActivity extends AppCompatActivity {

    Vector <String> names = new Vector<>(0);
    String contents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Device Metrics
        int deviceWidth;//, deviceHeight;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceWidth = displayMetrics.widthPixels;
//        deviceHeight = displayMetrics.heightPixels;

        // Loading Ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, DeviceUtils.convertPxToDp(this, deviceWidth)));
        adView.setAdUnitId(getString(R.string.ads_footer));

        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

        final AdView adView1 = findViewById(R.id.adView);
        adView1.addView(adView);
        adView.loadAd(adRequestBuilder.build());
    }

//    -----

    /**
     * Paste clipboard text into editTextTextMultiLine1
     */
    public void plainText1(View view) {
        EditText editText = findViewById(R.id.editTextTextMultiLine1);
        pastePlainText(editText);
    }

    /**
     *  Copy data from editTextTextMultiLine2 and paste it into clipboard
     */
    public void plainText2(View view) {
        EditText editText = findViewById(R.id.editTextTextMultiLine2);
        copyPlainText(editText.getText().toString());
    }

    /**
     * Paste clipboard text into editTextTextMultiLine3
     */
    public void plainText3(View view) {
        EditText editText = findViewById(R.id.editTextTextMultiLine3);
        pastePlainText(editText);
    }

    /**
     *  Copy data from editTextTextMultiLine2 and paste it into clipboard
     */
    public void plainText4(View view) {
        EditText editText = findViewById(R.id.editTextTextMultiLine4);
        copyPlainText(editText.getText().toString());
    }

    /**
     * Take clipboard information and paste it into the selected view
     */
    public void pastePlainText (EditText editText) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // If it does contain data, decide if you can handle the data.
        if (clipboard != null) {
            if (!(Objects.requireNonNull(clipboard.getPrimaryClipDescription()).hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                Toast.makeText(getApplicationContext(), R.string.noPlainText, Toast.LENGTH_SHORT).show();
            } else {
                ClipData.Item item = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0);
                editText.setText(item.getText().toString());
            }
        }
    }

    /**
     *  Copy data from the selected view and paste it into clipboard
     */
    public void copyPlainText (String string) {
        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.app_name), string);
        Objects.requireNonNull(clipboard).setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), R.string.textCopied, Toast.LENGTH_SHORT).show();
    }

//    -----

    /**
     *  Move horizontalScroll to position 1
     */
    public void horizontalScroll1 (View view) {
        horizontalScroll(DeviceUtils.convertDpToPx(this, getResources().getInteger(R.integer.horizontalScrollView1)));
        if (view.getId() == R.id.buttonNext1) {
            EditText editText = findViewById(R.id.editTextTextMultiLine1);
            String string = editText.getText().toString();
            if (!string.isEmpty()) {
                filterXml(string);
                editText = findViewById(R.id.editTextTextMultiLine2);
                editText.setText(contents);
            }
        }
    }

    /**
     *  Move horizontalScroll to position 2
     */
    public void horizontalScroll2 (View view) {
        horizontalScroll(DeviceUtils.convertDpToPx(this, getResources().getInteger(R.integer.horizontalScrollView2)));
    }

    /**
     *  Move horizontalScroll to position 3
     */
    public void horizontalScroll3 (View view) {
        horizontalScroll(DeviceUtils.convertDpToPx(this, getResources().getInteger(R.integer.horizontalScrollView3)));
    }

    /**
     *  Move horizontalScroll to beginning
     */
    public void horizontalScrollRestart (View view) {
        horizontalScroll(View.FOCUS_LEFT);
    }

    /**
     *  Move horizontalScroll to end
     */
    public void horizontalScrollEnd (View view) {
        horizontalScroll(View.FOCUS_RIGHT);
        if (view.getId() == R.id.buttonNext3) {
            EditText editText = findViewById(R.id.editTextTextMultiLine3);
            String string = editText.getText().toString();
            if (!string.isEmpty()) {
                returnXml(string);
                editText = findViewById(R.id.editTextTextMultiLine4);
                editText.setText(contents);
            }
        }
    }

    /**
     *  Move horizontalScroll to a position
     */
    public void horizontalScroll (final Integer integer) {
        final HorizontalScrollView horizontalScrollView = findViewById(R.id.horizontalScrollView);
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (integer == View.FOCUS_LEFT || integer == View.FOCUS_RIGHT)
                            horizontalScrollView.fullScroll(integer);
                        else
                            horizontalScrollView.scrollTo(integer, 0);
                    }
                });
            }
        }).start();
    }

//    -----

    /**
     *  Filter the structure from string.xml file
     */
    public void filterXml (String string) {
        String[] lines = string.split("\n");

        if (lines.length < 2) {
            Toast.makeText(getApplicationContext(), R.string.stringxmlFormatWrong, Toast.LENGTH_SHORT).show();
            return;
        } else {
            contents = "";
            for (String line : lines ) {
                ProjectUtils.PrintLogI("line: " + line);
                if (line.contains("<!--") || line.contains("resources") || !line.contains("<"))
                    continue;

                String[] name = line.split("\"");
                if (name.length < 2)
                    continue;

                ProjectUtils.PrintLogI("name[0]: " + name[0] + "  ---  " + "name[1]: " + name[1] + "  ---  " + "name[2]: " + name[2]);
                names.add(name[1]);

                String[] content = name[2].split("<");
                if (content.length < 2)
                    continue;

                ProjectUtils.PrintLogI("content[0]: " + content[0] + "  ---  " + "content[1]: " + content[1]);
                contents = contents.concat(content[0].substring(1)).concat("\n");
                ProjectUtils.PrintLogI("contents: " + contents);
            }
        }

        Toast.makeText(getApplicationContext(), R.string.processDone, Toast.LENGTH_SHORT).show();
    }

    /**
     *  Return the structure from string.xml file
     */
    public void returnXml (String string) {
        String[] lines = string.split("\n");

        if (lines.length < 1) {
            Toast.makeText(getApplicationContext(), R.string.stringxmlTranslatedFormatWrong, Toast.LENGTH_SHORT).show();
            return;
        } else {
            contents = "<resources>\n";
            int i=0;
            for (String line : lines ) {
                ProjectUtils.PrintLogI("line: " + line);

                contents = contents.concat("    <string name=\"").concat(names.get(i)).concat("\">").concat(line).concat("</string>\n");
                i++;
            }
            contents = contents.concat("</resources>");
            ProjectUtils.PrintLogI("contents: " + contents);
        }

        Toast.makeText(getApplicationContext(), R.string.processDone, Toast.LENGTH_SHORT).show();
    }

}