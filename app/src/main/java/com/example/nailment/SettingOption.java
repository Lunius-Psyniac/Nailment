package com.example.nailment;

public class SettingOption {
    private String title;
    private Type type;

    public enum Type {
        PRIVACY_POLICY,
        NOTIFICATIONS,
        APPEARANCE,
        HELP,
        LOCATION_PERMISSION,
        CAMERA_PERMISSION,
        GALLERY_PERMISSION
    }

    public SettingOption(String title, Type type) {
        this.title = title;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public Type getType() {
        return type;
    }
}
