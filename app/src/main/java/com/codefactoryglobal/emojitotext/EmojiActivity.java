package com.codefactoryglobal.emojitotext;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class EmojiActivity extends AppCompatActivity {

    private static final String TAG = "ETAct";
    private static final String ASSETS_BASIC ="basic";
    private static final String ASSETS_DERIVED ="derived";
    private static final String TRANSLATEDXML = "strings-emoji-descriptions.xml";
    private static final String COPYRIGHTMESSAGE = "<!-- Copyright © 1991-2018 Unicode, Inc. \nFor terms of use, see http://www.unicode.org/copyright.html \nUnicode and the Unicode Logo are registered trademarks of Unicode, Inc. in the U.S. and other countries. \nCLDR data files are interpreted according to the LDML specification (http://unicode.org/reports/tr35/) \n \n Warnings: All cp values have U+FE0F characters removed. See /annotationsDerived/ for derived annotations.  \n-->\n";

    @TargetApi(23)
    public static final String[] permissionsUsed = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSIONS_REQUEST_INIT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = findViewById(R.id.editText);
                TextView output = findViewById(R.id.resultText);
                String input_text = input.getText().toString();
                String translated_text = EmojiToText.translateEmoji(EmojiActivity.this, input_text);
                output.setText(translated_text);

                /* Current version CLDR common 34
                Given the annotations xml files from CLDR (in assets folder, basic and derived)
                will generate a bundle xml strings foreach language and
                place them at EmojiTranslations folder

                if (hasPermissionsOrAsk(PERMISSIONS_REQUEST_INIT)) {
                    generateTranslations();
                }*/
            }
        });
    }

    private void generateTranslations() {
        ArrayList<Translation> translation_file, translation_file_derived;
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "EmojiTranslations");
            if (!root.exists()) {
                root.mkdirs();
            }
            AssetManager a = getAssets();
            String[] languages = a.list(ASSETS_BASIC);
            if (languages == null) {
                return;
            }
            for (String lang : languages) {
                InputStream in = getAssets().open(ASSETS_BASIC + "/" + lang);
                XMLParser xml = new XMLParser(in);
                translation_file = xml.parse();
                in.close();
                try {
                    in = getAssets().open(ASSETS_DERIVED + "/" + lang);
                    xml = new XMLParser(in);
                    translation_file_derived = xml.parse();
                    translation_file.addAll(translation_file_derived);
                    in.close();
                } catch (IOException e) {
                    Log.i(TAG, "Has no derived strings");
                }
                try {
                    File folder = new File(root, lang.substring(0, lang.length() - 4));
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    File tFile = new File(folder, TRANSLATEDXML);
                    FileWriter writer = new FileWriter(tFile);
                    writer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?> \n");
                    writer.append(COPYRIGHTMESSAGE);
                    writer.append("<resources> \n");
                    for (Translation t :
                            translation_file) {
                        writer.append("\t");
                        writer.append(t.toString());
                        writer.append("\n");
                    }
                    writer.append("</resources> \n");
                    writer.flush();
                    writer.close();
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                translation_file.clear();
            }
        } catch (Exception id) {
            Log.e("TEST", "Error opening voices XML file: " + id.toString());
        }
    }

    public static boolean isPermissionDialogNeeded(Context context) {
        return !(Build.VERSION.SDK_INT < 23 || haveBasicPermissions(context));
    }

    @TargetApi(23)
    public static boolean haveBasicPermissions(Context context) {
        boolean permissions_granted = true;
        try {
            for (String permission : permissionsUsed) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissions_granted = false;
                    break;
                }
            }
        } catch (Exception e) {
            // Could not check all permissions
            permissions_granted = false;
        }
        return permissions_granted;
    }

    @TargetApi(23)
    public static boolean shouldRationaleAnyPermissions(Activity context) {
        boolean show_rationale = false;
        try {
            for (String permission : permissionsUsed) {
                if (context.shouldShowRequestPermissionRationale(permission)) {
                    show_rationale = true;
                    break;
                }
            }
        } catch (Exception e) {
            // Could not check all permissions
            show_rationale = true;
        }
        return show_rationale;
    }

    private boolean hasPermissionsOrAsk(int requestID) {
        if (Build.VERSION.SDK_INT < 23 || haveBasicPermissions(this)) {
            return true;
        }
        if (hasBlockedPermission()) {
            manageBlockedPermission();
            return false;
        }
        if (!shouldRationaleAnyPermissions(this)) {
            requestPermissions(permissionsUsed, requestID);
        } else {
            final int q_requestID = requestID;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.setTitle(getString(R.string.app_name));
            builder.setMessage(getString(R.string.app_alert_permissions_rationale));
            builder.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(android.content.DialogInterface arg0, int arg1) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        requestPermissions(permissionsUsed, q_requestID);
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return false;
    }

    private boolean hasBlockedPermission() {
        try {
            return getPreferences(MODE_PRIVATE).getBoolean("app_dont_ask_again_permissions", false);
        } catch (Exception e) {
            return false;
        }
    }

    private void manageBlockedPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage(getString(R.string.app_alert_permissions_denied));
        builder.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(android.content.DialogInterface arg0, int arg1) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.create().show();
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_INIT) {
            boolean permissions_granted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permissions_granted = false;
                    if (!shouldShowRequestPermissionRationale(permissions[i])) {
                        // We do this to detect if user blocked our permission
                        if (!hasBlockedPermission()) {
                            SharedPreferences.Editor prefsEdit = getPreferences(MODE_PRIVATE).edit();
                            prefsEdit.putBoolean("app_dont_ask_again_permissions", true);
                            prefsEdit.apply();
                            break;
                        }
                    }
                }
            }
            if (permissions_granted) {
                generateTranslations();
            }/* else {
                try {
                    recreate();
                } catch (Exception e) {
                }
                return;
            }*/

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
