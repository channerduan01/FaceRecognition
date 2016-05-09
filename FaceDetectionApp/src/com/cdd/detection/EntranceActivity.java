package com.cdd.detection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.cdd.detection.utils.FileEnvironment;

import java.io.File;

/**
 * Created by channerduan on 5/8/16.
 */
public class EntranceActivity extends Activity implements View.OnClickListener {

    public static final int REQUEST_CODE_VERIFYING = 100;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrance_layout);
        init();
    }

    private void init() {
        findViewById(R.id.tv_check).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_VERIFYING:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Collecting finish!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Collecting canceled...", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.tv_check:
                startVerify();
                break;
            default:
                break;
        }
    }

    private void launchShooting(String path) {
        Intent intent = new Intent(EntranceActivity.this, ShootActivity.class);
        intent.putExtra(ShootActivity.INTENT_KEY_COLLECT_NUMBER, 20);
        intent.putExtra(ShootActivity.INTENT_KEY_COLLECT_START_NUM, 1);
        intent.putExtra(ShootActivity.INTENT_KEY_COLLECT_DIRECTORY, path);
        startActivityForResult(intent, REQUEST_CODE_VERIFYING);
    }

    private void startVerify() {
        final EditText et = new EditText(this);
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle("User name")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(et)
                .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.equals("")) {
                            Toast.makeText(getApplicationContext(), "Please input the name" + input, Toast.LENGTH_LONG).show();
                        } else {
                            File destDir = new File(FileEnvironment.getTmpImagePath() + input);
                            if (destDir.exists()) {
                                FileEnvironment.delete(destDir);
                            }
                            destDir.mkdir();
                            launchShooting(destDir.getAbsolutePath());
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        ad.show();
    }


}
