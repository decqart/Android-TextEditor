package com.deckart.texteditor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    EditText edtTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtTxt = findViewById(R.id.textView);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.create_new) {
            edtTxt.getText().clear();
        } else if (item.getItemId() == R.id.open) {
            filePicker(true);
        } else if (item.getItemId() == R.id.save) {
            filePicker(false);
        }
        return true;
    }

    ActivityResultLauncher<Intent> mLaunchFilePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK && data != null) {
                    handleFileResult(data,true);
                }
            });

    ActivityResultLauncher<Intent> mLaunchFileSaver = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK && data != null) {
                    handleFileResult(data,false);
                }
            });

    void filePicker(boolean readOrWrite) {
        Intent intent;
        if (readOrWrite) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            mLaunchFilePicker.launch(intent);
        } else {
            intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("text/*");
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            mLaunchFileSaver.launch(intent);
        }
    }

    private void handleFileResult(Intent data, boolean readOrWrite) {
        Uri fileUri = data.getData();

        if (fileUri != null) {
            try {
                getContentResolver().takePersistableUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            if (readOrWrite) {
                read(fileUri);
            } else {
                write(fileUri);
            }
        }
    }

    void read(Uri fileUri) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(getContentResolver().openInputStream(fileUri));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder builder = new StringBuilder();

            try {
                String fileContentLine;
                while ((fileContentLine = reader.readLine()) != null) {
                    fileContentLine += "\n";
                    builder.append(fileContentLine);
                }
                String fileContent = builder.toString();
                edtTxt.setText(fileContent);
            } finally {
                reader.close();
                inputStreamReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void write(Uri fileUri) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContentResolver().openOutputStream(fileUri));
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);
            String textContent = edtTxt.getText().toString();

            try {
                writer.write(textContent);
            } finally {
                writer.close();
                outputStreamWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
