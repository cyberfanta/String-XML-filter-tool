package com.cyberfanta.stringsxmlfiltertool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Vector;

import lib.folderpicker.FolderPicker;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class MainActivity extends AppCompatActivity {

    Vector <String> names = new Vector<>(0);
    String contents;

    final int FOLDERPICKER_CODE_SAVE = 1;
    final int FOLDERPICKER_CODE_OPEN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Device Metrics
        int deviceWidth;
//        int deviceHeight;
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
        horizontalScroll(View.FOCUS_RIGHT);
        if (view.getId() == R.id.ll3) {
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
     *  Filter the structure from strings.xml file
     */
    public void filterXml (String string) {
        String[] lines = string.split("\n");

        if (lines.length < 2) {
            Toast.makeText(getApplicationContext(), R.string.stringxmlFormatWrong, Toast.LENGTH_SHORT).show();
            return;
        } else {
            contents = "";
            for (String line : lines ) {
                if (line.contains("<!--") || line.contains("resources") || !line.contains("<"))
                    continue;

                String[] name = line.split("\"");
                if (name.length < 2)
                    continue;

                names.add(name[1]);

                String[] content = name[2].split("<");
                if (content.length < 2)
                    continue;

                contents = contents.concat(content[0].substring(1)).concat("\n");
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
            contents = "<resources>\n";
            int i=0;
            for (String line : lines ) {
                contents = contents.concat("    <string name=\"").concat(names.get(i)).concat("\">").concat(line).concat("</string>\n");
                i++;
            }
            contents = contents.concat("</resources>");
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

        Intent intent = new Intent(this, FolderPicker.class);
        intent.putExtra("title", getString(R.string.fileSave));
        intent.putExtra("location", Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)).getAbsolutePath());
        startActivityForResult(intent, FOLDERPICKER_CODE_SAVE);
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

        Intent intent = new Intent(this, FolderPicker.class);
        intent.putExtra("title", getString(R.string.fileOpen));
        intent.putExtra("location", Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)).getAbsolutePath());
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
            String path = Objects.requireNonNull(intent.getExtras()).getString("data");
            ProjectUtils.PrintLogI("path: " + path);

            EditText editText = findViewById(R.id.editTextTextMultiLine4);
            String string = editText.getText().toString();
            if (!string.isEmpty()) {
                File file = new File(path, "strings.xml");
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
            ProjectUtils.PrintLogI("path: " + path);

            File file = null;
            if (path != null)
                file = new File(path);

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

                EditText editText = findViewById(R.id.editTextTextMultiLine1);
                editText.setText(lines);
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

        Toast.makeText(getApplicationContext(), R.string.notImplemented, Toast.LENGTH_LONG).show();
        return false; // Modify from here
    }
}