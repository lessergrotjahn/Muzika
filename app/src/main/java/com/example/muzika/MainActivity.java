package com.example.muzika;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    String[] items;
    int no_songs;
    ArrayList<File> songs;
    ArrayList<File> selectedSongs;

    Boolean[] note1;
    Boolean[] note2;
    Boolean[] note3;

    Boolean note1default = false;
    Boolean note2default = false;
    Boolean note3default = false;

    Boolean note1enabled = true;
    Boolean note2enabled = true;
    Boolean note3enabled = true;

    static String[] fileExtensions = {
            ".mp3",
            ".wav",
            ".m4a"
    };

    static String[] removableStrings = {
            " (Audio)",
            " (Official Audio)",
            " (Explicit)",
            " (Lyrics)",
            " (Music Video)",
            " (Official Music Video)",
            " (Official)",
            " (Official Lyric Video)",
            " (Lyric Video)",
            " (Official Video)",
            " (Video Oficial)",
            " (HD)",
            " (HD Lyrics)",
            " (Dirty)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.songListView);
        validatePermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveNoteArrays();
        }

        if (id == R.id.action_toggle_note_1) {
            item.setTitle(note1enabled ? "Enable Note 1" : "Disable Note 1");
            note1enabled = !note1enabled;
        }

        if (id == R.id.action_toggle_note_2) {
            item.setTitle(note2enabled ? "Enable Note 2" : "Disable Note 2");
            note2enabled = !note2enabled;
        }

        if (id == R.id.action_toggle_note_3) {
            item.setTitle(note3enabled ? "Enable Note 3" : "Disable Note 3");
            note3enabled = !note3enabled;
        }

        return super.onOptionsItemSelected(item);
    }

    public void validatePermission() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.FOREGROUND_SERVICE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        songs = getSongs(Environment.getExternalStorageDirectory());
                        no_songs = songs.size();

                        initNoteArrays();
                        displaySongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> getSongs (File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files == null) {
            return new ArrayList<>();
        }

        for (File f : files) {
            if (f.isDirectory() && !f.isHidden()) {
                arrayList.addAll(getSongs(f));
            }
            else {
                for (String extension : fileExtensions) {
                    if (f.getName().endsWith(extension) && !f.getName().equals("tone.mp3")) {
                        arrayList.add(f);
                    }
                }
            }
        }
        return arrayList;
    }

    static String cleanSongName(String songName) {
        for (String extension : fileExtensions) {
            songName = songName.replace(extension, "");
        }
        for (String removable : removableStrings) {
            songName = songName.replace(removable, "");
            songName = songName.replace(removable.toUpperCase(), "");
            songName = songName.replace(removable.toLowerCase(), "");
        }
        for (String removable : removableStrings) {
            removable = removable.replace("(", "[");
            removable = removable.replace(")", "]");
            songName = songName.replace(removable, "");
            songName = songName.replace(removable.toUpperCase(), "");
            songName = songName.replace(removable.toLowerCase(), "");
        }
        return songName;
    }

    void initNoteArrays() {
        SharedPreferences prefs1 = getSharedPreferences("note1", 0);
        SharedPreferences prefs2 = getSharedPreferences("note2", 0);
        SharedPreferences prefs3 = getSharedPreferences("note3", 0);
        note1 = new Boolean[no_songs];
        note2 = new Boolean[no_songs];
        note3 = new Boolean[no_songs];
        String uri_string;
        for (int i = 0; i < no_songs; i++) {
            uri_string = Uri.parse(songs.get(i).toString()).toString();
            note1[i] = prefs1.getBoolean(uri_string, note1default);
            note2[i] = prefs2.getBoolean(uri_string, note2default);
            note3[i] = prefs3.getBoolean(uri_string, note3default);
        }
    }

    void saveNoteArrays() {
        SharedPreferences.Editor editor1 = getSharedPreferences("note1", 0).edit();
        SharedPreferences.Editor editor2 = getSharedPreferences("note2", 0).edit();
        SharedPreferences.Editor editor3 = getSharedPreferences("note3", 0).edit();
        String uri_string;
        for (int i = 0; i < no_songs; i++) {
            uri_string = Uri.parse(songs.get(i).toString()).toString();
            editor1.putBoolean(uri_string, note1[i]).commit();
            editor2.putBoolean(uri_string, note2[i]).commit();
            editor3.putBoolean(uri_string, note3[i]).commit();
        }
    }

    void displaySongs() {
        items = new String[no_songs];

        String songName;
        for (int i = 0; i < no_songs; i++) {
            songName = songs.get(i).getName();
            items[i] = cleanSongName(songName);
        }
        ListAdapter listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startPlayerActivity(i);
            }
        });
    }

    void startPlayerActivity(int i) {
        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
        intent.putExtra("songs", songs);
        intent.putExtra("pos", i);
        intent.putExtra("selectedSongs", getSelectedSongs());
        someActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                }
            });

    ArrayList<File> getSelectedSongs() {
        selectedSongs = new ArrayList<>();
        for (int i = 0; i < no_songs; i++) {
            if ((note1enabled && note1[i]) || (note2enabled && note2[i]) || (note3enabled && note3[i])) {
                selectedSongs.add(songs.get(i));
            }
        }
        return selectedSongs;
    }

    class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View itemView = getLayoutInflater().inflate(R.layout.list_item,     null);
            TextView songText = itemView.findViewById(R.id.songTxtName);
            Button note1Button = itemView.findViewById(R.id.note1Button);
            Button note2Button = itemView.findViewById(R.id.note2Button);
            Button note3Button = itemView.findViewById(R.id.note3Button);

            if (note1[i]) {
                note1Button.setBackgroundResource(R.drawable.ic_note_green);
            }
            else {
                note1Button.setBackgroundResource(R.drawable.ic_horizontal_rule);
            }
            if (note2[i]) {
                note2Button.setBackgroundResource(R.drawable.ic_note_red);
            }
            else {
                note2Button.setBackgroundResource(R.drawable.ic_horizontal_rule);
            }
            if (note3[i]) {
                note3Button.setBackgroundResource(R.drawable.ic_note_blue);
            }
            else {
                note3Button.setBackgroundResource(R.drawable.ic_horizontal_rule);
            }


            note1Button.setOnClickListener(view1 -> {
                if (note1[i]) {
                    note1[i] = false;
                    note1Button.setBackgroundResource(R.drawable.ic_horizontal_rule);
                }
                else {
                    note1[i] = true;
                    note1Button.setBackgroundResource(R.drawable.ic_note_green);
                }
            });

            note2Button.setOnClickListener(view1 -> {
                if (note2[i]) {
                    note2[i] = false;
                    note2Button.setBackgroundResource(R.drawable.ic_horizontal_rule);
                }
                else {
                    note2[i] = true;
                    note2Button.setBackgroundResource(R.drawable.ic_note_red);
                }
            });

            note3Button.setOnClickListener(view1 -> {
                if (note3[i]) {
                    note3[i] = false;
                    note3Button.setBackgroundResource(R.drawable.ic_horizontal_rule);
                }
                else {
                    note3[i] = true;
                    note3Button.setBackgroundResource(R.drawable.ic_note_blue);
                }
            });

            songText.setSelected(true);
            songText.setText(items[i]);
            return itemView;
        }
    }

}