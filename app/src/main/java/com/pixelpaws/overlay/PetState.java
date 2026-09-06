package com.pixelpaws.overlay;

import android.content.Context;
import android.content.SharedPreferences;

final class PetState {
    int hunger, happiness, energy, level, coins, xp;
    private final SharedPreferences prefs;

    PetState(Context context) {
        prefs = context.getSharedPreferences("pixel_paws", Context.MODE_PRIVATE);
        hunger = prefs.getInt("hunger", 78);
        happiness = prefs.getInt("happiness", 82);
        energy = prefs.getInt("energy", 70);
        level = prefs.getInt("level", 1);
        coins = prefs.getInt("coins", 20);
        xp = prefs.getInt("xp", 0);
    }

    void feed() {
        if (coins >= 2) { coins -= 2; hunger = clamp(hunger + 24); happiness = clamp(happiness + 3); gainXp(5); }
        save();
    }

    void play() {
        if (energy >= 8) { energy = clamp(energy - 8); happiness = clamp(happiness + 18); coins += 3; gainXp(8); }
        save();
    }

    void pet() { happiness = clamp(happiness + 5); gainXp(2); save(); }
    void sleep() { energy = clamp(energy + 30); hunger = clamp(hunger - 5); save(); }

    void decay() {
        hunger = clamp(hunger - 1);
        happiness = clamp(happiness - 1);
        energy = clamp(energy - 1);
        save();
    }

    private void gainXp(int amount) {
        xp += amount;
        int target = level * 30;
        if (xp >= target) { xp -= target; level++; coins += 10; }
    }

    private int clamp(int value) { return Math.max(0, Math.min(100, value)); }

    private void save() {
        prefs.edit().putInt("hunger", hunger).putInt("happiness", happiness)
                .putInt("energy", energy).putInt("level", level)
                .putInt("coins", coins).putInt("xp", xp).apply();
    }
}
