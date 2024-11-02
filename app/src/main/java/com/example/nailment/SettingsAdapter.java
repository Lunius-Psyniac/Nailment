package com.example.nailment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
    private List<SettingOption> settings;
    private OnSettingChangeListener settingChangeListener;

    public interface OnSettingChangeListener {
        boolean isNotificationsEnabled();
        void toggleNotifications(boolean enable);
        boolean isDarkModeEnabled();
        void toggleDarkMode(boolean enable);
    }

    public SettingsAdapter(List<SettingOption> settings, OnSettingChangeListener settingChangeListener) {
        this.settings = settings;
        this.settingChangeListener = settingChangeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.setting_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingOption setting = settings.get(position);
        holder.titleTextView.setText(setting.getName());

        switch (setting.getType()) {
            case NOTIFICATIONS:
                holder.toggleButton.setVisibility(View.VISIBLE);
                holder.toggleButton.setChecked(settingChangeListener.isNotificationsEnabled());
                holder.toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    settingChangeListener.toggleNotifications(isChecked);
                });
                break;
            case APPEARANCE:
                holder.toggleButton.setVisibility(View.VISIBLE);
                holder.toggleButton.setChecked(settingChangeListener.isDarkModeEnabled());
                holder.toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    settingChangeListener.toggleDarkMode(isChecked);
                });
                break;
            default:
                holder.toggleButton.setVisibility(View.GONE);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (setting.getType() == SettingOption.Type.PRIVACY_POLICY || setting.getType() == SettingOption.Type.HELP) {
                ((SettingsActivity) holder.itemView.getContext()).handleSettingClick(setting);
            }
        });
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ToggleButton toggleButton;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.setting_title);
            toggleButton = itemView.findViewById(R.id.toggle_button);
        }
    }
}
