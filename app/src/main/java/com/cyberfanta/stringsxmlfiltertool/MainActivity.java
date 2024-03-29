package com.cyberfanta.stringsxmlfiltertool;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;

import lib.folderpicker.FolderPicker;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

@SuppressWarnings("RedundantSuppression")
public class MainActivity extends AppCompatActivity {

    final HashMap <Integer, String> names = new HashMap<>(0);
    final HashMap <Integer, String> labels = new HashMap<>(0);
    final HashMap <Integer, String> extras = new HashMap<>(0);
    final Vector <String> rows = new Vector<>(0);
    String contents = "";

    private AdView adView;
    private InterstitialAd interstitialAd;
    int adCounter = 0;

    @SuppressWarnings("FieldCanBeLocal")
    private FirebaseAnalytics mFirebaseAnalytics;

    final int FOLDERPICKER_CODE_SAVE = 1;
    final int FOLDERPICKER_CODE_OPEN = 2;
    String fileLocation = "";

    ReviewManager reviewManager;
    ReviewInfo reviewInfo;

    // Device Metrics
    Integer deviceWidth;

    // Logic for UI Version 2
    int currentComponent = 1;
    final String[] textTextBox = new String[]{"", "", "", "", ""};
    private boolean authorOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        // Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "test");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        // Device Metrics
        calculateDeviceWidth();

        // Keep keyboard hidden
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Interstitial Ads
        loadInterstitialAds();

        // Banner Ads
        final AdView adView1 = findViewById(R.id.adView_2);
        adView1.post(new Runnable() {
            @Override
            public void run() {
                loadBannerAds(adView1);
            }
        });

        // Setting widths for components
        int width = (deviceWidth - DeviceUtils.convertDpToPx(this, 12)) / 5;
        ViewGroup.LayoutParams layoutParams;
        View view;

        view = findViewById(R.id.view14);
        layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);

        view = findViewById(R.id.view15);
        layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);

        view = findViewById(R.id.view16);
        layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);

        view = findViewById(R.id.view17);
        layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);

        view = findViewById(R.id.view18);
        layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        ConstraintLayout constraintLayout = findViewById(R.id.author);
        if (authorOpened) {
            authorSelected(constraintLayout);
            authorOpened = false;
            return;
        }
        super.onBackPressed();
    }

    //    ---

    /**
     * Manage the loading of Interstitial Ads
     */
    public void showInterstitialAds() {
        if (adCounter > 1) {
            if (interstitialAd != null) {
                interstitialAd.show(this);
                adCounter = 0;
                return;
            }
            else
                Log.i("","****** AD NOT LOADED YET");
        }
        adCounter++;
    }

    /**
     * Manage the loading of Banner Ads
     */
    public void loadBannerAds (AdView adView1) {
        adView = new AdView(this);
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, DeviceUtils.convertPxToDp(this, deviceWidth)));
        adView.setAdUnitId(getString(R.string.ads_banner));
        adView1.addView(adView);
        adView.setAdListener(
                new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        // The previous banner ad loaded successfully, call this method again to
                        // load the next ad in the items list.
                        Log.i("MainActivity", "Banner Ad Loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // The previous banner ad failed to load. Call this method again to load
                        // the next ad in the items list.
                        @SuppressLint("DefaultLocale")
                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                        Log.e(
                                "MainActivity",
                                "The previous banner ad failed to load with error: "
                                        + error
                                        + ". Attempting to"
                                        + " load the next banner ad in the items list.");
                    }
                });
        adView.loadAd(new AdRequest.Builder().build());
    }

    /**
     * Manage the loading of Banner Ads
     */
    public void loadInterstitialAds(){
        //noinspection NullableProblems
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                AdRequest adRequest = new AdRequest.Builder().build();
                // Interstitial
                InterstitialAd.load(MainActivity.this, getString(R.string.ads_interstitial), adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd mInterstitialAd) {
                                interstitialAd = mInterstitialAd;
                                //noinspection NullableProblems
                                interstitialAd.setFullScreenContentCallback(
                                        new FullScreenContentCallback() {
                                            @Override
                                            public void onAdDismissedFullScreenContent() {
                                                interstitialAd = null;
                                            }

                                            @Override
                                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                                interstitialAd = null;
                                            }

                                            @Override
                                            public void onAdShowedFullScreenContent() {
                                            }
                                        }
                                );
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                // Handle the error
                                Log.i("MainActivity", loadAdError.getMessage());
                                interstitialAd = null;

                                @SuppressLint("DefaultLocale")
                                String error =
                                        String.format(
                                                "domain: %s, code: %d, message: %s",
                                                loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                                Log.e(
                                        "MainActivity",
                                        "The previous interstitial ad failed to load with error: "
                                                + error
                                                + ". Attempting to"
                                                + " load the next interstitial ad in the items list.");

                            }
                        }
                );
            }
        });
    }

    //    -----

    public void calculateDeviceWidth(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            deviceWidth =  windowMetrics.getBounds().width() - insets.left - insets.right;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            deviceWidth =  displayMetrics.widthPixels;
        }
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
    public void pastePlainText (TextView textView) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboard.getPrimaryClipDescription() != null) {
            if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                Toast.makeText(getApplicationContext(), R.string.noPlainText, Toast.LENGTH_SHORT).show();
            } else {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                textView.setText(item.getText().toString());
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
        if (view.getId() == R.id.ll1) {
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
        if (view.getId() == R.id.buttonAutoTranslate) {
            EditText editText = findViewById(R.id.editTextTextMultiLine2);
            String string = editText.getText().toString();
            if (!string.isEmpty()) {
                if (autoTranslate(string)) {
                    editText = findViewById(R.id.editTextTextMultiLine3);
                    editText.setText(contents);
                }
            }
        }
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
        if (view.getId() == R.id.ll3) {
            EditText editText = findViewById(R.id.editTextTextMultiLine3);
            String string = editText.getText().toString();
            editText.clearFocus();
            if (!string.isEmpty()) {
                returnXml(string);
                editText = findViewById(R.id.editTextTextMultiLine4);
                editText.setText(contents);
            }
            showInterstitialAds();
        }
        horizontalScroll(View.FOCUS_RIGHT);
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
     *  Filter the structure from strings.xml file
     */
    public void filterXml (String string) {
        String[] lines = string.split("\n");

        if (lines.length < 2) {
            Toast.makeText(getApplicationContext(), R.string.stringxmlFormatWrong, Toast.LENGTH_SHORT).show();
            return;
        } else {
            contents = "";
            rows.clear();
            names.clear();

            boolean commentary = false;
            for (String line : lines ) {
                if (line.contains("<!--") && !line.contains("-->")) {
                    rows.add(line);
                    commentary=true;
                    continue;
                }

                if (commentary) {
                    rows.add(line);
                    if (line.contains("-->"))
                        commentary=false;
                    continue;
                }

                if ((line.contains("<!--") && line.contains("-->")) || line.contains("resources") || line.contains("encoding") || !line.contains("<")) {
                    rows.add(line);
                    continue;
                }

                if (line.contains("plurals")) {
                    labels.put(rows.size(), "plurals");
                    rows.add(line);
                    continue;
                }

                if (line.contains("translatable=\"false\""))
                    continue;

                if (line.contains("formatted=\"true\"")) {
                    extras.put(rows.size(), " formatted=\"true\"");
                    String[] split = line.split(" formatted=\"true\"");
                    line = split[0].concat(split[1]);
                }

                String[] name;
                String[] split = null;
                if (line.contains("\\\"")) {
                    split = line.split("\\\\\"");
                    name = split[0].split("\"");
                } else
                    name = line.split("\"");

                if (name.length < 2)
                    continue;

                if (name[0].contains("<item"))
                    labels.put(rows.size(), "item");

                if (split != null)
                    for (int i = 1; i < split.length; i++)
                        name[2] = name[2].concat("\\\"").concat(split[i]);

                names.put(rows.size(), name[1]);

                String[] content = name[2].split("<");
                if (content.length < 2)
                    continue;

                content[0] = content[0].substring(1);

                if (content[0].contains("\\n")){
                    String[] contentSplits = content[0].split("\\\\n");
                    for (String contentSplit: contentSplits) {
                        if (contentSplit.equals(contentSplits[contentSplits.length - 1]))
                            rows.add("endsplit");
                        else
                            rows.add("split");
                        contents = contents.concat(contentSplit.concat("\n"));
                    }
                } else {
                    rows.add("filled");
                    contents = contents.concat(content[0]).concat("\n");
                }
            }
        }

        Toast.makeText(getApplicationContext(), R.string.processDone, Toast.LENGTH_SHORT).show();
    }

    /**
     *  Return the structure from strings.xml file
     */
    public void returnXml (String string) {
        String[] lines = string.split("\n");

        if (lines.length < 1) {
            Toast.makeText(getApplicationContext(), R.string.stringxmlTranslatedFormatWrong, Toast.LENGTH_SHORT).show();
            return;
        } else if (names.size() < 1) {
            Toast.makeText(getApplicationContext(), R.string.stringxmlFormatEmpty, Toast.LENGTH_LONG).show();
            horizontalScroll(View.FOCUS_LEFT);
            return;
        } else {
            contents = "";
            int j = 0;
            String label, extra, name, indentation;
            for (int i = 0; i < rows.size(); i++)
                if (names.containsKey(i)) {
                    if (lines[j].contains("'")) {
                        if (lines[j].contains("\\'"))
                            lines[j] = lines[j].replaceAll("\\\\'", "'");
                        lines[j] = lines[j].replaceAll("'", "\\\\'");
                    }
                    if (labels.containsKey(i))
                        label = labels.get(i);
                    else
                        label = "string";

                    indentation = "";
                    name = "name";
                    if (label != null && label.equals("item")) {
                        indentation = "    ";
                        name = "quantity";
                    }

                    extra = "";
                    if (extras.containsKey(i))
                        extra = extras.get(i);

                    if (label != null && extra != null) {
                        if (!rows.elementAt(i).equals("split"))
                        contents = contents.concat(indentation).concat("    <").concat(label).concat(" ").concat(name).concat("=\"")
                                .concat(Objects.requireNonNull(names.get(i))).concat("\"").concat(extra).concat(">").concat(lines[j])
                                .concat("</").concat(label).concat(">\n");
                        else {
                            String concat = indentation.concat("    <").concat(label)
                                    .concat(" ").concat(name).concat("=\"")
                                    .concat(Objects.requireNonNull(names.get(i)))
                                    .concat("\"").concat(extra).concat(">");
                            for ( ; rows.elementAt(i).equals("split"); i++) {
                                concat = concat.concat(lines[j]).concat("\\n");
                                j++;
                            }
//                            i--;
//                            j--;
                            concat = concat.concat(lines[j]).concat("\\n");
                            contents = contents.concat(concat.substring(0, concat.length() - 2)).concat("</").concat(label).concat(">\n");
                        }
                    }

                    j++;
                } else {
                    contents = contents.concat(rows.elementAt(i).concat("\n"));
                }
        }

        Toast.makeText(getApplicationContext(), R.string.processDone, Toast.LENGTH_SHORT).show();
    }

    /**
     *  Save the structure from strings.xml to a file
     */
    public void saveXml (View view) {
        if (!((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                & (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        )) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            }, 1500);

            if (!((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    & (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            )) {
                Toast.makeText(getApplicationContext(), R.string.permissionNotGranted, Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(getApplicationContext(), R.string.permissionGranted, Toast.LENGTH_SHORT).show();
            }
        }

        //Checking text edit is not empty
        TextView textView = findViewById(R.id.textBox);
        String string = textView.getText().toString();
        if (!string.isEmpty()) {
            Intent intent = new Intent(this, FolderPicker.class);
            intent.putExtra("title", getString(R.string.fileSave));
            if (fileLocation.equals(""))
                intent.putExtra("location", "");
            else
                intent.putExtra("location", fileLocation);
            startActivityForResult(intent, FOLDERPICKER_CODE_SAVE);
        }
        showInterstitialAds();
    }

    /**
     *  Open the structure from strings.xml to a file
     */
    public void openXml (View view) {
        if (!((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                & (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        )) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            }, 1500);

            if (!((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    & (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            )) {
                Toast.makeText(getApplicationContext(), R.string.permissionNotGranted, Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(getApplicationContext(), R.string.permissionGranted, Toast.LENGTH_SHORT).show();
            }
        }

        Intent intent = new Intent(this, FolderPicker.class);//"/sdcard/stringsxmlfiltertool"
        intent.putExtra("title", getString(R.string.fileOpen));
        if (fileLocation.equals(""))
//            intent.putExtra("location", Environment.getExternalStorageDirectory().getPath());
            intent.putExtra("location", "");
        else
            intent.putExtra("location", fileLocation);
        intent.putExtra("pickFiles", true);
        startActivityForResult(intent, FOLDERPICKER_CODE_OPEN);
    }

//    -----

    /**
     *  Perform the activities when open or save file from intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == FOLDERPICKER_CODE_SAVE && resultCode == Activity.RESULT_OK) {
            fileLocation = Objects.requireNonNull(intent.getExtras()).getString("data");

            TextView textView = findViewById(R.id.textBox);
            String string = textView.getText().toString();
            if (!string.isEmpty()) {
                File file = new File(fileLocation, "strings.xml");
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                    bufferedWriter.write(string);
                    bufferedWriter.close();
                } catch (IOException ignored) {
                }
            }
            Toast.makeText(getApplicationContext(), R.string.processDone, Toast.LENGTH_SHORT).show();
        } else if (requestCode == FOLDERPICKER_CODE_OPEN && resultCode == Activity.RESULT_OK) {
            String path = Objects.requireNonNull(intent.getExtras()).getString("data");

            File file = null;
            if (path != null) {
                fileLocation = path.substring(0, path.lastIndexOf(File.separator) + 1);
                file = new File(path);
            }

            if (file != null){
                String lines = "", line;
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    line = bufferedReader.readLine();
                    while (line != null) {
                        lines = lines.concat(line).concat("\n");
                        line = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                } catch (IOException ignored) {
                }

                textTextBox[0] = lines;
                TextView textView = findViewById(R.id.textBox);
                textView.setText(textTextBox[0]);
            }
            Toast.makeText(getApplicationContext(), R.string.processDone, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *  Auto translate the structure from strings.xml using Google Translate Services
     */
    public boolean autoTranslate (String string){
        if (!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.INTERNET
            }, 1000);

            if (!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(), R.string.permissionNotGranted, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), R.string.permissionGranted, Toast.LENGTH_SHORT).show();
            }
        }

        // Modify from here
        Toast.makeText(getApplicationContext(), R.string.notImplemented, Toast.LENGTH_LONG).show();
        return string.equals("false");
    }

    /**
     *  Create the setting menu of the application
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     *  Handle the setting menu of the application
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_policy:
                Uri uri = Uri.parse("https://docs.google.com/document/d/1jO9nnmsjG2ZO0Si1rEBTAP2kmTin5h-qIb4nhuAN5H0/edit?usp=sharing");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            case R.id.item_rate:
                Task<Void> flow = reviewManager.launchReviewFlow(MainActivity.this, reviewInfo);
                //noinspection NullableProblems
                flow.addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Task<Void> task) {
                            }
                        }
                );
                return true;
            case R.id.item_about:
                ConstraintLayout constraintLayout = findViewById(R.id.author);
                constraintLayout.setVisibility(View.VISIBLE);
                setAnimation(constraintLayout, "translationX", 300L, false, deviceWidth.floatValue(), 0f);
                authorOpened = true;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Show the developer info
     */
    public void authorSelected(View view) {
        ConstraintLayout constraintLayout = findViewById(R.id.author);
        setAnimation(constraintLayout, "translationX", 300L, false, 0f, deviceWidth.floatValue());
    }

    //    ---

    /**
     * Set animation on view
     */
    @SuppressWarnings("SameParameterValue")
    private void setAnimation(View view, String propertyName, Long duration, Boolean repeat, Float value1, Float value2) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, propertyName, value1, value2);
        final AnimatorSet animator = new AnimatorSet();
        animator.play(objectAnimator);
        animator.setDuration(duration);
        if (repeat)
            animator.addListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            animator.start();
                        }
                    }
            );
        animator.start();
    }

    //    ---

    //    New functions for UI version 2
    //    Tools 1
    /**
     * Process the actions from tool A1
     */
    public void toolA1 (View view) {
        if (currentComponent == 1){
            openXml(view);
        } else if (currentComponent == 2){
            currentComponent = 1;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine1_hint));
            // Updating Text for textBox
            textView.setText(textTextBox[0]);

            // Updating Image for ivA1
            ImageView imageView = findViewById(R.id.ivA1);
            imageView.setImageResource(R.drawable.ic_open);
            // Updating Text for ivA1
            textView = findViewById(R.id.tvA1);
            textView.setText(getString(R.string.tvA1));

            // Updating Image for ivB1
            imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_paste);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB2));

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);
        } else if (currentComponent == 3){
            currentComponent = 2;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine2_hint));
            // Updating Text for textBox
            textView.setText(textTextBox[1]);
            textView.setEnabled(true);

            // Updating Image for ivB1
            ImageView imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_copy);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB2));

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);
        } else if (currentComponent == 4){
            currentComponent = 3;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine_google_hint));
            // Updating Text for textBox
            textView.setText(textTextBox[2]);
            textView.setEnabled(false);

            // Updating Image for ivB1
            ImageView imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_translate_tool);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB3));

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);
        } else {
            currentComponent = 4;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine3_hint));
            // Updating Text for textBox
            textView.setText(textTextBox[3]);

            // Updating Image for ivA1
            ImageView imageView = findViewById(R.id.ivA1);
            imageView.setImageResource(R.drawable.ic_back);
            // Updating Text for ivA1
            textView = findViewById(R.id.tvA1);
            textView.setText(getString(R.string.tvA2));

            // Updating Image for ivB1
            imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_paste);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB1));

            // Activating tools1
            ConstraintLayout constraintLayout = findViewById(R.id.tool2);
            constraintLayout.setVisibility(View.GONE);
            constraintLayout = findViewById(R.id.tool1);
            constraintLayout.setVisibility(View.VISIBLE);

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);
        }
    }

    /**
     * Process the actions from tool B1
     */
    public void toolB1 (View view) {
        TextView textView = findViewById(R.id.textBox);
        if (currentComponent == 1){
            pastePlainText (textView);
            textTextBox[0] = textView.getText().toString();
        } else if (currentComponent == 2){
            copyPlainText(textView.getText().toString());
        } else if (currentComponent == 3){
            //todo: must be changed when activate the auto-translate
            autoTranslate(textView.getText().toString());
            toolC1(textView);
        } else if (currentComponent == 4){
            pastePlainText (textView);
            textTextBox[3] = textView.getText().toString();
        } else {
            copyPlainText(textView.getText().toString());
        }
    }

    /**
     * Process the actions from tool C1
     */
    public void toolC1 (View view) {
        if (currentComponent == 1){
            currentComponent = 2;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine2_hint));

            // Updating Image for ivA1
            ImageView imageView = findViewById(R.id.ivA1);
            imageView.setImageResource(R.drawable.ic_back);
            // Updating Text for ivA1
            textView = findViewById(R.id.tvA1);
            textView.setText(getString(R.string.tvA2));

            // Updating Image for ivB1
            imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_copy);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB2));

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);

            // Enabling Components
//            View viewComponent = findViewById(R.id.view15);
//            viewComponent.setEnabled(true);

            // Executing command
            textView = findViewById(R.id.textBox);
            String string = textView.getText().toString();
            if (!string.isEmpty()) {
                filterXml(string);
                textTextBox[1] = contents;

                // Updating Text for textBox
                textView.setText(textTextBox[1]);
            }
        } else if (currentComponent == 2){
            currentComponent = 3;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine_google_hint));
            textView.setEnabled(false);

            // Updating Image for ivB1
            ImageView imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_translate_tool);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB3));

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);

            // Enabling Components
//            View viewComponent = findViewById(R.id.view16);
//            viewComponent.setEnabled(true);

            // Executing Command
            // todo: when auto-translate is ready
            // Updating Text for textBox
            textView = findViewById(R.id.textBox);
            textView.setText(textTextBox[2]);
        } else if (currentComponent == 3){
            currentComponent = 4;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine3_hint));
            textView.setEnabled(true);

            // Updating Image for ivB1
            ImageView imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_paste);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB1));

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);

            // Enabling Components
//            View viewComponent = findViewById(R.id.view17);
//            viewComponent.setEnabled(true);

            // Executing Command
            textView = findViewById(R.id.textBox);
//            String string = textView.getText().toString();
//            if (!string.isEmpty()) {
//                if (autoTranslate(string)) {
//                    // todo: auto-translate
////                    textTextBox[3] = contents;
//
//                    // Updating Text for textBox
//                }
//            }
            textView.setText(textTextBox[3]);
        } else if (currentComponent == 4){
            currentComponent = 5;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine4_hint));

            // Activating tools2
            ConstraintLayout constraintLayout = findViewById(R.id.tool1);
            constraintLayout.setVisibility(View.GONE);
            constraintLayout = findViewById(R.id.tool2);
            constraintLayout.setVisibility(View.VISIBLE);

            // Updating Image for components
            ImageView imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.VISIBLE);

            // Enabling Components
//            View viewComponent = findViewById(R.id.view18);
//            viewComponent.setEnabled(true);

            // Executing Command
            textView = findViewById(R.id.textBox);
            String string = textView.getText().toString();
            if (!string.isEmpty()) {
                returnXml(string);
                textTextBox[4] = contents;

                // Updating Text for textBox
                textView.setText(textTextBox[4]);
            }
        } else {
            currentComponent = 1;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine1_hint));
            // Updating Text for textBox
            textView.setText(textTextBox[0]);

            // Updating Image for ivA1
            ImageView imageView = findViewById(R.id.ivA1);
            imageView.setImageResource(R.drawable.ic_open);
            // Updating Text for ivA1
            textView = findViewById(R.id.tvA1);
            textView.setText(getString(R.string.tvA1));

            // Updating Image for ivB1
            imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_paste);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB1));

            // Activating tools1
            ConstraintLayout constraintLayout = findViewById(R.id.tool2);
            constraintLayout.setVisibility(View.GONE);
            constraintLayout = findViewById(R.id.tool1);
            constraintLayout.setVisibility(View.VISIBLE);

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);
        }
    }

    //    Tools 2
    /**
     * Process the actions from tool C2
     */
    public void toolB2(View view) {
        saveXml(view);
    }

    //    Components
    /**
     * Process the actions from Components M1
     */
    public void componentsM1 (View view) {
        if (currentComponent != 1) {
            currentComponent = 1;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine1_hint));
            // Updating Text for textBox
            textView.setText(textTextBox[0]);

            // Updating Image for ivA1
            ImageView imageView = findViewById(R.id.ivA1);
            imageView.setImageResource(R.drawable.ic_open);
            // Updating Text for ivA1
            textView = findViewById(R.id.tvA1);
            textView.setText(getString(R.string.tvA1));

            // Updating Image for ivB1
            imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_paste);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB1));

            // Activating tools1
            ConstraintLayout constraintLayout = findViewById(R.id.tool1);
            constraintLayout.setVisibility(View.VISIBLE);
            constraintLayout = findViewById(R.id.tool2);
            constraintLayout.setVisibility(View.GONE);

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);
        }
    }

    /**
     * Process the actions from Components M2
     */
    public void componentsM2 (View view) {
        if (currentComponent != 2) {
            currentComponent = 2;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine2_hint));
            // Updating Text for textBox
            textView.setText(textTextBox[1]);

            // Updating Image for ivA1
            ImageView imageView = findViewById(R.id.ivA1);
            imageView.setImageResource(R.drawable.ic_back);
            // Updating Text for ivA1
            textView = findViewById(R.id.tvA1);
            textView.setText(getString(R.string.tvA2));

            // Updating Image for ivB1
            imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_copy);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB2));

            // Activating tools1
            ConstraintLayout constraintLayout = findViewById(R.id.tool1);
            constraintLayout.setVisibility(View.VISIBLE);
            constraintLayout = findViewById(R.id.tool2);
            constraintLayout.setVisibility(View.GONE);

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);
        }
    }

    /**
     * Process the actions from Components M3
     */
    public void componentsM3 (View view) {
        if (currentComponent != 3) {
            currentComponent = 3;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine_google_hint));
            // Updating Text for textBox
            textView.setText(textTextBox[2]);

            // Updating Image for ivA1
            ImageView imageView = findViewById(R.id.ivA1);
            imageView.setImageResource(R.drawable.ic_back);
            // Updating Text for ivA1
            textView = findViewById(R.id.tvA1);
            textView.setText(getString(R.string.tvA2));

            // Updating Image for ivB1
            imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_translate_tool);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB3));

            // Activating tools1
            ConstraintLayout constraintLayout = findViewById(R.id.tool1);
            constraintLayout.setVisibility(View.VISIBLE);
            constraintLayout = findViewById(R.id.tool2);
            constraintLayout.setVisibility(View.GONE);

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);
        }
    }

    /**
     * Process the actions from Components M4
     */
    public void componentsM4 (View view) {
        if (currentComponent != 4) {
            currentComponent = 4;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine3_hint));
            // Updating Text for textBox
            textView.setText(textTextBox[3]);

            // Updating Image for ivA1
            ImageView imageView = findViewById(R.id.ivA1);
            imageView.setImageResource(R.drawable.ic_back);
            // Updating Text for ivA1
            textView = findViewById(R.id.tvA1);
            textView.setText(getString(R.string.tvA2));

            // Updating Image for ivB1
            imageView = findViewById(R.id.ivB1);
            imageView.setImageResource(R.drawable.ic_paste);
            // Updating Text for ivB1
            textView = findViewById(R.id.tvB1);
            textView.setText(getString(R.string.tvB1));

            // Activating tools1
            ConstraintLayout constraintLayout = findViewById(R.id.tool1);
            constraintLayout.setVisibility(View.VISIBLE);
            constraintLayout = findViewById(R.id.tool2);
            constraintLayout.setVisibility(View.GONE);

            // Updating Image for components
            imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.VISIBLE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.GONE);
        }
    }

    /**
     * Process the actions from Components M5
     */
    public void componentsM5 (View view) {
        if (currentComponent != 5) {
            currentComponent = 5;
            // Updating Hint for textBox
            TextView textView = findViewById(R.id.textBox);
            textView.setHint(getString(R.string.editTextTextMultiLine4_hint));
            // Updating Text for textBox
            textView.setText(textTextBox[4]);

            // Activating tools2
            ConstraintLayout constraintLayout = findViewById(R.id.tool1);
            constraintLayout.setVisibility(View.GONE);
            constraintLayout = findViewById(R.id.tool2);
            constraintLayout.setVisibility(View.VISIBLE);

            // Updating Image for components
            ImageView imageView = findViewById(R.id.iv2M1);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M2);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M3);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M4);
            imageView.setVisibility(View.GONE);
            imageView = findViewById(R.id.iv2M5);
            imageView.setVisibility(View.VISIBLE);
        }
    }
}