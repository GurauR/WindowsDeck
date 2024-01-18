package com.example.deck;

import static com.example.deck.CommandSender.sendCommand;
import static com.example.deck.CommandSender.setHost;
import static com.example.deck.CommandSender.isConnectionEstablished;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    MaterialCardView buttonMute;
    MaterialCardView buttonAudioDevice;
    Slider sliderAudio;
    Slider sliderBrightness;
    ImageButton btnPlayPause;
    ImageButton btnNextTrack;
    ImageButton btnPrevTrack;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_main);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        showIpInputDialog();
        buttonMute = findViewById(R.id.buttonMute);
        buttonMute.setOnClickListener(view -> mute());
        buttonAudioDevice = findViewById(R.id.buttonAudioDevice);
        buttonAudioDevice.setOnClickListener(view -> headphones());
        sliderAudio = findViewById(R.id.sliderAudio);
        Drawable thumbAudioDrawable = Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.baseline_music_note_24));
        thumbAudioDrawable.mutate().setTint(getResources().getColor(R.color.colorForeground));
        sliderAudio.setCustomThumbDrawable(thumbAudioDrawable);
        sliderAudio.setLabelBehavior(LabelFormatter.LABEL_GONE);
        sliderAudio.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sendCommand("nircmd setsysvolume " + (int)(slider.getValue() * 65535));
            }

        });

        sliderBrightness = findViewById(R.id.sliderBrightness);
        Drawable thumbBrightnessDrawable = Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.rounded_brightness_high_24));
        thumbBrightnessDrawable.mutate().setTint(getResources().getColor(R.color.colorForeground));
        thumbBrightnessDrawable.setColorFilter(new BlendModeColorFilter(getResources().getColor(R.color.colorForeground), BlendMode.SRC_ATOP));
        sliderBrightness.setCustomThumbDrawable(thumbBrightnessDrawable);
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

        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPlayPause.setColorFilter(ContextCompat.getColor(this, R.color.colorForeground));
        btnPlayPause.setOnClickListener(view -> pausePlay());

        btnNextTrack = findViewById(R.id.btnNext);
        btnNextTrack.setOnClickListener(view -> nextTrack());

        btnPrevTrack = findViewById(R.id.btnPrevious);
        btnPrevTrack.setOnClickListener(view -> prevTrack());

    }

    public void mute() {
        String result = sendCommand("nircmd mutesysvolume 2");
        String volume = sendCommand("deviceVolume");
        TextView text = buttonMute.findViewById(R.id.textMute);
        ImageView image = buttonMute.findViewById(R.id.iconMute);
        if (Objects.equals(result, "1")) {
            text.setText("Unmute");
            image.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.rounded_volume_off_24));
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral90));
        } else if (Objects.equals(result, "0")) {
            text.setText("Mute");
            image.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.rounded_volume_up_24));
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, R.color.colorPrimary));
        } else text.setText("fuck");
        assert volume != null;
        if (!volume.isEmpty())
            animateSliderToValue(sliderAudio, Float.parseFloat(volume));
    }

    public void headphones() {
        String result = sendCommand("nircmd setdefaultsounddevice");
        String muted = sendCommand("isMuted");
        String volume = sendCommand("deviceVolume");
        TextView text = buttonAudioDevice.findViewById(R.id.textAudioDevice);
        TextView textMute = buttonMute.findViewById(R.id.textMute);
        ImageView imageMute = buttonMute.findViewById(R.id.iconMute);
        if (Objects.equals(muted, "1")) {
            textMute.setText("Unmute");
            imageMute.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.rounded_volume_off_24));
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral90));
        } else {
            textMute.setText("Mute");
            imageMute.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.rounded_volume_up_24));
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        assert volume != null;
        if (!volume.isEmpty())
            animateSliderToValue(sliderAudio, Float.parseFloat(volume));
        text.setText(result);
    }

    public void prep() {
        String hostname = sendCommand("hostname");
        String currentDevice = sendCommand("currentDevice");
        String muted = sendCommand("isMuted");
        String volume = sendCommand("deviceVolume");
        String brightness = sendCommand("currentBrightness");
        TextView text = buttonAudioDevice.findViewById(R.id.textAudioDevice);
        TextView textMute = buttonMute.findViewById(R.id.textMute);
        ImageView imageMute = buttonMute.findViewById(R.id.iconMute);
        if (Objects.equals(muted, "1")) {
            textMute.setText("Unmute");
            imageMute.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.rounded_volume_off_24));
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral90));
        } else {
            textMute.setText("Mute");
            imageMute.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.rounded_volume_up_24));
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        text.setText(currentDevice);
        assert brightness != null;
        if (!brightness.isEmpty())
            animateSliderToValue(sliderBrightness, Float.parseFloat(brightness));
        assert volume != null;
        if (!volume.isEmpty())
            animateSliderToValue(sliderAudio, Float.parseFloat(volume));
    }

    public void pausePlay() {
        isPlaying = !isPlaying;
        Drawable[] drawables = new Drawable[2];
        GradientDrawable drawable = (GradientDrawable) btnPlayPause.getBackground();

        float newCornerRadius = getResources().getDimension(R.dimen.circle);
        float originalCornerRadius = getResources().getDimension(R.dimen.round);
        ObjectAnimator cornerRadiusAnimator;
        if (isPlaying) {
            cornerRadiusAnimator = ObjectAnimator.ofFloat(drawable, "cornerRadius", originalCornerRadius);
            cornerRadiusAnimator.start();
            sendCommand("pauseMedia");
        } else {
            cornerRadiusAnimator = ObjectAnimator.ofFloat(drawable, "cornerRadius", newCornerRadius);
            cornerRadiusAnimator.start();
            sendCommand("playMedia");
        }


        // Change the button icon based on the play/pause state
        btnPlayPause.setImageResource(isPlaying ? R.drawable.round_pause_24 : R.drawable.round_play_arrow_24);
    }

    public void nextTrack() {
        sendCommand("nextTrack");
    }

    public void prevTrack() {
        sendCommand("prevTrack");
    }

    private void animateSliderToValue(final Slider slider, final float targetValue) {
        ValueAnimator animator = ValueAnimator.ofFloat(slider.getValue(), targetValue);
        animator.setDuration(500); // Set the duration of the animation in milliseconds

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                slider.setValue(animatedValue);
            }
        });

        animator.start();
    }

    private void showIpInputDialog() {
        // Create an alert dialog builder
        SharedPreferences sharedPreferences = getSharedPreferences("ips", Context.MODE_PRIVATE);
        HashMap<String, String> ips = (HashMap<String, String>) sharedPreferences.getAll();
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton(null, null);
        // Set the title and layout for the dialog
        dialogBuilder.setTitle("Select Connection");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ip_input, null);
        ImageView iconAdd = view.findViewById(R.id.iconAdd);
        TableLayout ipList = view.findViewById(R.id.ipList);
        for (Map.Entry<String, String> set :
                ips.entrySet()) {
            MaterialCardView ipCard = (MaterialCardView) LayoutInflater.from(this).inflate(R.layout.ip_item, null);
            TextView title = ipCard.findViewById(R.id.title);
            TextView ipText = ipCard.findViewById(R.id.ip);
            title.setText(set.getKey());
            ipText.setText(set.getValue());

            TableRow tableRow = new TableRow(this);
            tableRow.addView(ipCard);
            ipList.addView(tableRow);
        }
        iconAdd.setColorFilter(R.color.colorPrimary);
        dialogBuilder.setView(view);
        //Objects.requireNonNull(dialogBuilder.getBackground()).setTint(getResources().getColor(R.color.colorBackground));
        // Get the EditText view from the layout

        // Set positive button
        /*dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
            // Get the entered IPv4 address
            String ipAddress = ipAddressEditText.getText().toString();

            setHost(ipAddress);
            if (isConnectionEstablished()) {
                prep();
            }
        });*/
        EditText ipAddressEditText = view.findViewById(R.id.editTextIpAddress);
        AlertDialog alertDialog = dialogBuilder.show();

        iconAdd.setOnClickListener(view1 -> {
            View ipView = LayoutInflater.from(this).inflate(R.layout.ip_item, null);
            MaterialCardView card = (MaterialCardView) ipView;
            String ipAddress = ipAddressEditText.getText().toString();
            ipAddressEditText.setText("");
            setHost(ipAddress);
            if (isConnectionEstablished()) {
                String hostname = sendCommand("hostname");
                TextView title = card.findViewById(R.id.title);
                TextView ip = card.findViewById(R.id.ip);
                title.setText(hostname);
                ip.setText(ipAddress);
                sharedPreferences.edit().putString(hostname, ipAddress).apply();
                TableRow row = new TableRow(this);
                card.setOnClickListener(view12 -> {
                    if (isConnectionEstablished()) {
                        prep();
                    }
                    alertDialog.dismiss();
                });
                row.addView(card);
                ipList.addView(row);
            }
        });

        // Show the dialog




        for(int i = 0, j = ipList.getChildCount(); i < j; i++) {
            View child = ipList.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;
                for(int i1 = 0, j1 = row.getChildCount(); i1 < j1; i1++) {
                    View child1 = row.getChildAt(i);
                    if (child1 instanceof MaterialCardView) {
                        MaterialCardView card = (MaterialCardView) child1;
                        card.setOnClickListener(view12 -> {
                            TextView ip = card.findViewById(R.id.ip);
                            String ipAddress = ip.getText().toString();

                            setHost(ipAddress);
                            if (isConnectionEstablished()) {
                                prep();
                            }
                            alertDialog.dismiss();
                        });
                    }
                }
            }
        }
    }
}