package com.cdd.detection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.cdd.detection.detect.ShootActivity;
import com.cdd.detection.recognize.MatchEngine;
import com.cdd.detection.utils.FileEnvironment;
import com.cdd.detection.views.dialoglist.ShareSelectDialog;
import com.cdd.detection.views.dialoglist.ShareSelectDialogAdapter;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by channerduan on 5/8/16.
 */
public class EntranceActivity extends Activity implements View.OnClickListener {

    public static final int REQUEST_CODE_SAMPLE = 100;
    public static final int REQUEST_CODE_VERIFY = 101;

    private TextView mManageDataTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrance_layout);
        init();
    }

    private void init() {
        findViewById(R.id.tv_verify).setOnClickListener(this);
        findViewById(R.id.tv_sample).setOnClickListener(this);
        findViewById(R.id.tv_setting).setOnClickListener(this);
        mManageDataTextView = (TextView) findViewById(R.id.tv_manage);
        updateManageText();
        mManageDataTextView.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_VERIFY:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra(ShootActivity.INTENT_KEY_COLLECT_SUBJECT_NAME);
                } else {
//                    Toast.makeText(this, "Verifying canceled...", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_SAMPLE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Sampling success!", Toast.LENGTH_SHORT).show();
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(200);
                    MatchEngine.getInstance().createSubject(
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
            case R.id.tv_setting:
                setVerifyThreshold();
                break;
            default:
                break;
        }
    }

    private void updateManageText() {
        mManageDataTextView.setText("Delete (" + MatchEngine.getInstance().getSubjectNum() + ")");
    }

    private void deleteSample() {
        if (MatchEngine.getInstance().getSubjectNum() == 0) {
            Toast.makeText(this, "You donot have any samples now!\nTry collect some!", Toast.LENGTH_SHORT).show();
            return;
        }
        final ShareSelectDialogAdapter adapter = new ShareSelectDialogAdapter(this, MatchEngine.getInstance().getCoreData());
        new ShareSelectDialog.Builder(this).setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                MatchEngine.getInstance().deleteSubjectByName(
                        MatchEngine.getInstance().getCoreData().get(position).name);
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
        if (MatchEngine.getInstance().getSubjectNum() == 0) {
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

    private void setVerifyThreshold() {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(MatchEngine.getInstance().getThreshold()));
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle("Verify Threshold")
                .setView(et)
                .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Float threshold = Float.valueOf(et.getText().toString());
                        Log.e("channer test", "channer test set threshold: " + threshold);
                        MatchEngine.getInstance().setThreshold(threshold);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        ad.show();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                et.requestFocus();
                InputMethodManager inputManager = (InputMethodManager) et
                        .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(et, 0);
            }
        }, 200);
    }

    private void startSample() {
        final EditText et = new EditText(this);
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle("Subject name")
                .setView(et)
                .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.equals("")) {
                            Toast.makeText(getApplicationContext(), "Please input the name" + input, Toast.LENGTH_LONG).show();
                        } else if (MatchEngine.getInstance().nameExist(input)) {
                            Toast.makeText(getApplicationContext(), "Sorry, this name was used. Please try another name.", Toast.LENGTH_LONG).show();
                        } else {
                            File destDir = new File(FileEnvironment.getTmpImagePath() + input);
//                            if (!destDir.exists()) destDir.mkdir();
                            if (destDir.exists()) {
                                FileEnvironment.delete(destDir);
                            }
                            destDir.mkdir();
                            launchShootForSample(input);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        ad.show();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                et.requestFocus();
                InputMethodManager inputManager = (InputMethodManager) et
                        .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(et, 0);
            }
        }, 200);
    }
}
