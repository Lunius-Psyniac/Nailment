package com.example.nailment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {

    private final List<SettingOption> settings;
    private final OnSettingChangeListener listener;

    public interface OnSettingChangeListener {
        boolean isNotificationsEnabled();
        void toggleNotifications(boolean enable);
        boolean isDarkModeEnabled();
        void toggleDarkMode(boolean enable);
    }

    public SettingsAdapter(List<SettingOption> settings, OnSettingChangeListener listener) {
        this.settings = settings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_setting, parent, false);
        return new SettingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        SettingOption setting = settings.get(position);
        Log.d("SettingsAdapter", "Binding setting at position " + position + ": " + setting.getTitle());
        holder.titleTextView.setText(setting.getTitle());

        // Show/hide switch based on setting type
        if (setting.getType() == SettingOption.Type.NOTIFICATIONS ||
            setting.getType() == SettingOption.Type.APPEARANCE) {
            holder.switchView.setVisibility(View.VISIBLE);
            Log.d("SettingsAdapter", "Showing switch for: " + setting.getTitle());
            
            // Set initial switch state
            boolean isEnabled = setting.getType() == SettingOption.Type.NOTIFICATIONS ?
                    listener.isNotificationsEnabled() :
                    listener.isDarkModeEnabled();
            holder.switchView.setChecked(isEnabled);

            // Set switch listener
            holder.switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (setting.getType() == SettingOption.Type.NOTIFICATIONS) {
                    listener.toggleNotifications(isChecked);
                } else if (setting.getType() == SettingOption.Type.APPEARANCE) {
                    listener.toggleDarkMode(isChecked);
                }
            });
        } else {
            holder.switchView.setVisibility(View.GONE);
            Log.d("SettingsAdapter", "Hiding switch for: " + setting.getTitle());
        }

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            Log.d("SettingsAdapter", "Item clicked: " + setting.getTitle());
            if (setting.getType() != SettingOption.Type.NOTIFICATIONS &&
                setting.getType() != SettingOption.Type.APPEARANCE) {
                ((SettingsActivity) holder.itemView.getContext()).handleSettingClick(setting);
            }
        });
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    static class SettingViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        Switch switchView;

        SettingViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.settingTitle);
            switchView = itemView.findViewById(R.id.settingSwitch);
        }
    }
}
