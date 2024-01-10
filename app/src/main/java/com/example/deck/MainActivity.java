package com.example.deck;

import static com.example.deck.CommandSender.sendCommand;
import static com.example.deck.CommandSender.setHost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
        sliderAudio = findViewById(R.id.sliderAudio);
        sliderAudio.setCustomThumbDrawable(Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.baseline_music_note_24)));
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

        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pausePlay();
            }
        });

        btnNextTrack = findViewById(R.id.btnNext);
        btnNextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextTrack();
            }
        });

        btnPrevTrack = findViewById(R.id.btnPrevious);
        btnPrevTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevTrack();
            }
        });

        prep();
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
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_primary90));
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
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_primary90));
        }
        assert volume != null;
        if (!volume.isEmpty())
            animateSliderToValue(sliderAudio, Float.parseFloat(volume));
        text.setText(result);
    }

    public void prep() {
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
            buttonMute.getBackground().setTint(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_primary90));
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the title and layout for the dialog
        builder.setTitle("Enter IPv4 Address");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ip_input, null);
        builder.setView(view);

        // Get the EditText view from the layout
        final EditText ipAddressEditText = view.findViewById(R.id.editTextIpAddress);

        // Set positive button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the entered IPv4 address
                String ipAddress = ipAddressEditText.getText().toString();

                setHost(ipAddress);
                prep();
                // Do something with the entered address
                // For example, you can log it or use it in your application
                // TODO: Add your logic here
            }
        });

        // Set negative button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked cancel, do nothing
            }
        });

        // Show the dialog
        builder.show();
    }
}