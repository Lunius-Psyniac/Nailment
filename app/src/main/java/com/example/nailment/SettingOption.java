package com.example.nailment;

public class SettingOption {
    public enum Type {
        PRIVACY_POLICY,
        NOTIFICATIONS,
        APPEARANCE,
        HELP
    }

    private String name;
    private Type type;

    public SettingOption(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
