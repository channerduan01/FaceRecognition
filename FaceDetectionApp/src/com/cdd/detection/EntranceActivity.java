package com.cdd.detection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.cdd.detection.matching.DataEngine;
import com.cdd.detection.utils.FileEnvironment;
import com.cdd.detection.views.dialoglist.ShareSelectDialog;
import com.cdd.detection.views.dialoglist.ShareSelectDialogAdapter;

import java.io.File;
import java.util.Locale;

/**
 * Created by channerduan on 5/8/16.
 */
public class EntranceActivity extends Activity implements View.OnClickListener {

    public static final int REQUEST_CODE_SAMPLE = 100;
    public static final int REQUEST_CODE_VERIFY = 101;

    private TextView mManageDataTextView;

    private TextToSpeech mSpeechTool;
    private boolean mIsSpeechReady = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrance_layout);
        init();
    }

    private void init() {
        findViewById(R.id.tv_verify).setOnClickListener(this);
        findViewById(R.id.tv_sample).setOnClickListener(this);
        mManageDataTextView = (TextView) findViewById(R.id.tv_manage);
        updateManageText();
        mManageDataTextView.setOnClickListener(this);

        mSpeechTool = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //下面这句代码是主要的，设置语言，如果是英文的话，就用
                    int result = mSpeechTool.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("lanageTag", "not use");
                    } else {
                        mIsSpeechReady = true;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_VERIFY:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra(ShootActivity.INTENT_KEY_COLLECT_SUBJECT_NAME);
                    new AlertDialog.Builder(this).setMessage("Recognized: " + name).create().show();
//                    Toast.makeText(this, "Verifying success!", Toast.LENGTH_SHORT).show();
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(200);
                    if (mIsSpeechReady) {
                        mSpeechTool.stop();
                        mSpeechTool.speak(name, TextToSpeech.QUEUE_FLUSH,
                                null);
                    }
                } else {
                    Toast.makeText(this, "Verifying canceled...", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_SAMPLE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Sampling success!", Toast.LENGTH_SHORT).show();
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(200);
                    DataEngine.getInstance().createSubject(
                            data.getStringExtra(ShootActivity.INTENT_KEY_COLLECT_SUBJECT_NAME));
                    updateManageText();
                } else {
                    Toast.makeText(this, "Sampling canceled...", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_verify:
                launchShootForVerify();
                break;
            case R.id.tv_sample:
                startSample();
                break;
            case R.id.tv_manage:
                deleteSample();
                break;
            default:
                break;
        }
    }

    private void updateManageText() {
        mManageDataTextView.setText("Delete (" + DataEngine.getInstance().getSubjectNum() + ")");
    }

    private void deleteSample() {
        if (DataEngine.getInstance().getSubjectNum() == 0) {
            Toast.makeText(this, "You donot have any samples now!\nTry collect some!", Toast.LENGTH_SHORT).show();
            return;
        }
        final ShareSelectDialogAdapter adapter = new ShareSelectDialogAdapter(this, DataEngine.getInstance().getCoreData());
        new ShareSelectDialog.Builder(this).setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                DataEngine.getInstance().deleteSubjectByName(
                        DataEngine.getInstance().getCoreData().get(position).name);
                updateManageText();
                Toast.makeText(EntranceActivity.this, "deleted", Toast.LENGTH_SHORT).show();
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
            }
        }).show();
    }

    private void launchShootForVerify() {
        if (DataEngine.getInstance().getSubjectNum() == 0) {
            Toast.makeText(this, "You donot have any samples now!\nTry collect some!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(EntranceActivity.this, ShootActivity.class);
        startActivityForResult(intent, REQUEST_CODE_VERIFY);
    }

    private void launchShootForSample(String subjectName) {
        Intent intent = new Intent(EntranceActivity.this, ShootActivity.class);
        intent.putExtra(ShootActivity.INTENT_KEY_COLLECT_NUMBER, 20);
        intent.putExtra(ShootActivity.INTENT_KEY_COLLECT_SUBJECT_NAME, subjectName);
        startActivityForResult(intent, REQUEST_CODE_SAMPLE);
    }

    private void startSample() {
        final EditText et = new EditText(this);
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle("Subject name")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(et)
                .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.equals("")) {
                            Toast.makeText(getApplicationContext(), "Please input the name" + input, Toast.LENGTH_LONG).show();
                        } else if (DataEngine.getInstance().nameExist(input)) {
                            Toast.makeText(getApplicationContext(), "Sorry, this name was used. Please try another name.", Toast.LENGTH_LONG).show();
                        } else {
                            File destDir = new File(FileEnvironment.getTmpImagePath() + input);
                            if (!destDir.exists()) destDir.mkdir();
//                            if (destDir.exists()) {
//                                FileEnvironment.delete(destDir);
//                            }
//                            destDir.mkdir();
                            launchShootForSample(input);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        ad.show();
    }
}
