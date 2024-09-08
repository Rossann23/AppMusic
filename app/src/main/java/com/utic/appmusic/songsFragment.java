package com.utic.appmusic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class songsFragment extends Fragment {
    // Variables y objetos
    private MediaPlayer mediaPlayer;
    private Button btnPlay, btnAtras, btnSiguiente;
    private RadioGroup radioGroup;
    private SeekBar seekBar;
    private ArrayList<File> audioFiles = new ArrayList<>();
    private int posicion = 0;
    private boolean isPlaying = false;
    private int currentTrackIndex = 0;
    private Handler handler = new Handler();
    private static final int REQUEST_PERMISSION = 1;
    private int cancionActual = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.songs_fragment, container, false);

        // Inicializar elementos UI
        radioGroup = view.findViewById(R.id.radioGrupo1);
        btnPlay = view.findViewById(R.id.btnPlay1);
        btnAtras = view.findViewById(R.id.btnAtras1);
        btnSiguiente = view.findViewById(R.id.btnSiguiente1);
        seekBar = view.findViewById(R.id.seekBar1);

        // Configurar SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Configurar botones
        btnPlay.setOnClickListener(this::togglePlayPause);
        btnAtras.setOnClickListener(this::atras);
        btnSiguiente.setOnClickListener(this::siguiente);

        // Verificar y solicitar permisos de almacenamiento solo si no han sido concedidos
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            initMediaPlayer();
        }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMediaPlayer();
            } else {
                Toast.makeText(getActivity(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initMediaPlayer() {
        loadAudioFiles();
        createRadioButtons();

        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> siguiente(null));
        }
    }

    private void loadAudioFiles() {
        File musicDir = new File(Environment.getExternalStorageDirectory(), "Music");

        if (musicDir.exists() && musicDir.isDirectory()) {
            File[] files = musicDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp3")) {
                        audioFiles.add(file);
                    }
                }
            }
        }

        if (audioFiles.isEmpty()) {
            Toast.makeText(getActivity(), "No se encontraron archivos de música", Toast.LENGTH_SHORT).show();
        }
    }

    private void createRadioButtons() {
        if (radioGroup != null) {
            radioGroup.removeAllViews(); // Limpiar los radio buttons existentes

            for (int i = 0; i < audioFiles.size(); i++) {
                RadioButton radioButton = new RadioButton(getActivity());
                radioButton.setText(audioFiles.get(i).getName());
                radioButton.setId(i);
                radioGroup.addView(radioButton);
            }
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            if (mediaPlayer.isPlaying()) {
                handler.postDelayed(this::updateSeekBar, 1000);
            }
        }
    }

    private void togglePlayPause(View view) {
        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId != -1) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pausar();
                btnPlay.setText("▶");
            } else {
                if (selectedId != cancionActual) {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    cancionActual = selectedId;
                    reproducir();
                    btnPlay.setText("❚❚");
                } else {
                    continuar();
                    btnPlay.setText("❚❚");
                }
            }
        } else {
            Toast.makeText(getActivity(), "Seleccione una canción para reproducir", Toast.LENGTH_SHORT).show();
        }
    }

    public void reproducir() {
        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId != -1) {
            File audioFile = audioFiles.get(selectedId);

            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                seekBar.setMax(mediaPlayer.getDuration());
                updateSeekBar();
                cancionActual = selectedId;

                mediaPlayer.setOnCompletionListener(mp -> siguiente(null));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void pausar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            posicion = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void continuar() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(posicion);
            mediaPlayer.start();
            updateSeekBar();
        }
    }

    public void siguiente(View view) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId < audioFiles.size() - 1) {
            radioGroup.check(selectedId + 1);
            reproducir();
        }
    }

    public void atras(View view) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId > 0) {
            radioGroup.check(selectedId - 1);
            reproducir();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}
