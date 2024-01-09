package com.example.deck;

import static com.example.deck.CommandSender.sendCommand;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    MaterialCardView buttonMute;
    MaterialCardView buttonAudioDevice;
    Slider slider;
    Slider sliderBrightness;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.activity_main);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        buttonMute = findViewById(R.id.buttonMute);
        buttonMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mute();
            }
        });
        buttonAudioDevice = findViewById(R.id.buttonAudioDevice);
        buttonAudioDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                headphones();
            }
        });
        slider = findViewById(R.id.slider);
        slider.setCustomThumbDrawable(Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.baseline_music_note_24)));
        slider.setLabelBehavior(LabelFormatter.LABEL_GONE);
        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sendCommand("nircmd setsysvolume " + (int)(slider.getValue() * 65535));
            }
        });

        sliderBrightness = findViewById(R.id.sliderBrightness);
        sliderBrightness.setCustomThumbDrawable(Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.rounded_brightness_high_24)));
        sliderBrightness.setLabelBehavior(LabelFormatter.LABEL_GONE);
        sliderBrightness.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sendCommand("ControlMyMonitor.exe /SetValue Primary 10 " + (int)(slider.getValue() * 100));
                sendCommand("ControlMyMonitor.exe /SetValue Secondary 10 " + (int)(slider.getValue() * 100));
            }
        });
    }

    public void mute() {
        String result = sendCommand("nircmd mutesysvolume 2");
        TextView text = buttonMute.findViewById(R.id.textMute);
        ImageView image = buttonMute.findViewById(R.id.iconMute);
        if (Objects.equals(result, "1")) {
            text.setText("Unmute");
            image.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.rounded_volume_off_24));
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral90));
        } else if (Objects.equals(result, "0")) {
            text.setText("Mute");
            image.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.rounded_volume_up_24));
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_primary90));
        } else text.setText("fuck");
    }

    public void headphones() {
        String result = sendCommand("nircmd setdefaultsounddevice");
        String muted = sendCommand("isMuted");
        String volume = sendCommand("deviceVolume");
        TextView text = buttonAudioDevice.findViewById(R.id.textAudioDevice);
        TextView textMute = buttonMute.findViewById(R.id.textMute);
        ImageView imageMute = buttonMute.findViewById(R.id.iconMute);
        if (Objects.equals(muted, "1")) {
            text.setText("Unmute");
            imageMute.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.rounded_volume_off_24));
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral90));
        } else {
            text.setText("Mute");
            imageMute.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.rounded_volume_up_24));
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_primary90));
        }
        text.setText(result);
    }
}