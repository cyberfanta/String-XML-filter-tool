package com.cyberfanta.stringsxmlfiltertool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;

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

public class MainActivity extends AppCompatActivity {

    HashMap <Integer, String> names = new HashMap<>(0);
    HashMap <Integer, String> labels = new HashMap<>(0);
    HashMap <Integer, String> extras = new HashMap<>(0);
    Vector <String> rows = new Vector<>(0);
    String contents;

    InterstitialAd interstitialAd;
    int adCounter = 0;

    final int FOLDERPICKER_CODE_SAVE = 1;
    final int FOLDERPICKER_CODE_OPEN = 2;
    String fileLocation = "";

    ReviewManager reviewManager;
    ReviewInfo reviewInfo;

    // Device Metrics
    int deviceWidth;

    // Logic for UI Version 2
    int currentComponent = 1;
    String[] textTextBox = new String[]{"", "", "", "", ""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        // Device Metrics
//        int deviceHeight;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceWidth = displayMetrics.widthPixels;
//        deviceHeight = displayMetrics.heightPixels;

        // Keep keyboard hidden
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Loading Ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        // Banner
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, DeviceUtils.convertPxToDp(this, deviceWidth)));
        adView.setAdUnitId(getString(R.string.ads_footer));
        AdView adView1 = findViewById(R.id.adView);
        adView1.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());

        // Instersicial
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.ads_intersticial));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
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

        if (clipboard != null) {
            if (!(Objects.requireNonNull(clipboard.getPrimaryClipDescription()).hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                Toast.makeText(getApplicationContext(), R.string.noPlainText, Toast.LENGTH_SHORT).show();
            } else {
                ClipData.Item item = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0);
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
            loadInterstitialAdd();
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
                } else {
                    name = line.split("\"");
                }

                if (name.length < 2)
                    continue;

                if (line.contains("item")) {
                    labels.put(rows.size(), "item");
                }

                if (split != null)
                    for (int i = 1; i < split.length; i++)
                        name[2] = name[2].concat("\\\"").concat(split[i]);

                names.put(rows.size(), name[1]);

                rows.add("filled");

                String[] content = name[2].split("<");
                if (content.length < 2)
                    continue;

                contents = contents.concat(content[0].substring(1)).concat("\n");
//                int in=content.length;
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
//            if (rows.elementAt(i).equals("filled")) {
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
                    if (label.equals("item")) {
                        indentation = "    ";
                        name = "quantity";
                    }

                    extra = "";
                    if (extras.containsKey(i))
                        extra = extras.get(i);

//                    contents = contents.concat("    <").concat(label).concat(" name=\"").concat(names.get(j)).concat("\">").concat(lines[j]).concat("</").concat(label).concat(">\n");
                    contents = contents.concat(indentation).concat("    <").concat(label).concat(" ").concat(name).concat("=\"").concat(names.get(i)).concat("\"").concat(extra).concat(">").concat(lines[j]).concat("</").concat(label).concat(">\n");
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
            loadInterstitialAdd();
        }
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //    ---

    /**
     * Manage the loading of Interstitial Ads
     */
    public void loadInterstitialAdd () {
        if (adCounter > 1) {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
                adCounter = 0;
                return;
            }
            else
                Log.i("","****** NO ESTA CARGADA LA PUBLICIDAD");
        }
        adCounter++;
    }

    //    ---

    /**
     * Show the developer info
     */
    public void author_selected (View view) {
        ConstraintLayout constraintLayout = findViewById(R.id.author);
        constraintLayout.setVisibility(View.GONE);
    }

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