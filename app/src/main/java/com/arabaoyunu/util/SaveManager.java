package com.arabaoyunu.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.arabaoyunu.vehicle.VehicleCatalog;
import com.arabaoyunu.career.CareerLeagueSystem;
import com.arabaoyunu.career.CareerEventSystem;
import com.arabaoyunu.mode.CheckpointRaceSystem;
import com.arabaoyunu.mode.RaceModeSystem;

/** Basit ve AIDE uyumlu kalici kayit yoneticisi. */
public final class SaveManager {
    private static final String KEY_VISUAL_CUSTOMIZATION_SCHEMA = "visual_customization_schema";
    private static final String KEY_GARAGE_SHOWROOM_SCHEMA = "garage_showroom_schema";

    public static final int UPGRADE_ENGINE = 0;
    public static final int UPGRADE_BRAKE = 1;
    public static final int UPGRADE_TIRES = 2;
    public static final int UPGRADE_DRIFT = 3; // Eski kayıtlarla uyumluluk; drift etkisi diferansiyel ile birlikte okunur.
    public static final int UPGRADE_TURBO = 4;
    public static final int UPGRADE_TRANSMISSION = 5;
    public static final int UPGRADE_SUSPENSION = 6;
    public static final int UPGRADE_NITRO = 7;
    public static final int UPGRADE_DIFFERENTIAL = 8;
    public static final int UPGRADE_ECU = 9;
    public static final int UPGRADE_WEIGHT_REDUCTION = 10;
    public static final int UPGRADE_TRACTION = 11;
    public static final int UPGRADE_DURABILITY = 12;
    public static final int PERFORMANCE_UPGRADE_COUNT = 13;
    public static final int MAX_UPGRADE_LEVEL = 5;

    private static final String PREFS = "araba_oyunu_save";
    private static final String KEY_DRIFT_BEST = "drift_best_score";
    private static final String KEY_DRIFT_TOTAL_SCORE = "drift_total_score";
    private static final String KEY_DRIFT_COMPLETED = "drift_completed";
    private static final String KEY_DRIFT_EARNED = "drift_earned";
    private static final String KEY_DRIFT_XP = "drift_xp";
    private static final String KEY_DRIFT_GOLD = "drift_gold";
    private static final String KEY_DRIFT_SILVER = "drift_silver";
    private static final String KEY_DRIFT_BRONZE = "drift_bronze";
    private static final String KEY_DRIFT_LEGEND = "drift_legend";
    private static final String KEY_DRIFT_LONGEST_SECONDS = "drift_longest_seconds";
    private static final String KEY_DRIFT_BEST_COMBO = "drift_best_combo";
    private static final String KEY_DRIFT_LAST_SCORE = "drift_last_score";
    private static final String KEY_DRIFT_LAST_GRADE = "drift_last_grade";
    private static final String KEY_TIME_TRIAL_BEST = "time_trial_best_seconds";
    private static final String KEY_RACE_BEST = "race_best_seconds";
    private static final String KEY_CHECKPOINT_RACE_COMPLETED = "checkpoint_race_completed";
    private static final String KEY_CHECKPOINT_RACE_EARNED = "checkpoint_race_earned";
    private static final String KEY_CHECKPOINT_RACE_GOLD = "checkpoint_race_gold";
    private static final String KEY_CHECKPOINT_RACE_SILVER = "checkpoint_race_silver";
    private static final String KEY_CHECKPOINT_RACE_BRONZE = "checkpoint_race_bronze";
    private static final String KEY_CHECKPOINT_RACE_LAST_TIME = "checkpoint_race_last_time";
    private static final String KEY_CHECKPOINT_RACE_LAST_GRADE = "checkpoint_race_last_grade";
    private static final String KEY_CHECKPOINT_SELECTED_ROUTE = "checkpoint_selected_route";
    private static final String KEY_DRAG_BEST = "drag_best_seconds";
    private static final String KEY_DRAG_COMPLETED = "drag_completed";
    private static final String KEY_DRAG_EARNED = "drag_earned";
    private static final String KEY_DRAG_GOLD = "drag_gold";
    private static final String KEY_DRAG_SILVER = "drag_silver";
    private static final String KEY_DRAG_BRONZE = "drag_bronze";
    private static final String KEY_DRAG_LAST_TIME = "drag_last_time";
    private static final String KEY_DRAG_LAST_GRADE = "drag_last_grade";
    private static final String KEY_DRAG_BEST_SPEED = "drag_best_speed_kmh";
    private static final String KEY_RIVAL_TOTAL_RACES = "rival_total_races";
    private static final String KEY_RIVAL_TOTAL_WINS = "rival_total_wins";
    private static final String KEY_RIVAL_TOTAL_LOSSES = "rival_total_losses";
    private static final String KEY_RIVAL_DRAG_WINS = "rival_drag_wins";
    private static final String KEY_RIVAL_CHECKPOINT_WINS = "rival_checkpoint_wins";
    private static final String KEY_RIVAL_HARD_BEATEN = "rival_hard_beaten";
    private static final String KEY_RIVAL_TOTAL_EARNED = "rival_total_earned";
    private static final String KEY_RIVAL_LAST_RESULT = "rival_last_result";
    private static final String KEY_RIVAL_LAST_DIFFICULTY = "rival_last_difficulty";
    private static final String KEY_RIVAL_LAST_MODE = "rival_last_mode";
    private static final String KEY_POLICE_BEST = "police_best_seconds";
    private static final String KEY_POLICE_TOTAL_CHASES = "police_total_chases";
    private static final String KEY_POLICE_ESCAPES = "police_escapes";
    private static final String KEY_POLICE_CAUGHT = "police_caught";
    private static final String KEY_POLICE_HIGHEST_WANTED = "police_highest_wanted";
    private static final String KEY_POLICE_EARNED_COINS = "police_earned_coins";
    private static final String KEY_POLICE_EARNED_XP = "police_earned_xp";
    private static final String KEY_POLICE_LAST_RESULT = "police_last_result";
    private static final String KEY_POLICE_LAST_WANTED = "police_last_wanted";
    private static final String KEY_POLICE_LAST_SECONDS = "police_last_seconds";
    private static final String KEY_TRAFFIC_NEAR_MISS_TOTAL = "traffic_near_miss_total";
    private static final String KEY_TRAFFIC_BEST_COMBO = "traffic_best_combo";
    private static final String KEY_TRAFFIC_EARNED_COINS = "traffic_earned_coins";
    private static final String KEY_TRAFFIC_COLLISIONS = "traffic_collisions";
    private static final String KEY_TRAFFIC_POLICE_RISK_PASSES = "traffic_police_risk_passes";
    private static final String KEY_TRAFFIC_CLEAN_STREAK = "traffic_clean_streak";
    private static final String KEY_TRAFFIC_LAST_MESSAGE = "traffic_last_message";
    private static final String KEY_COINS = "coins";
    private static final String KEY_PLAYER_LEVEL = "player_level";
    private static final String KEY_PLAYER_XP = "player_xp";
    private static final String KEY_REWARD_CRATES = "reward_crates";
    private static final String KEY_OPENED_CRATES = "opened_crates";
    private static final String KEY_DRIVEN_METERS = "driven_meters";
    private static final String KEY_CAREER_STARTED = "career_started";
    private static final String KEY_CAREER_STARTER_INDEX = "career_starter_vehicle_index";
    private static final String KEY_SELECTED_VEHICLE_INDEX = "selected_vehicle_index";
    private static final String KEY_CAREER_SYNC_VERSION = "career_sync_version";
    private static final String KEY_FIRST_MISSIONS_UNLOCKED = "first_missions_unlocked";
    private static final String KEY_QUEST_CHAIN_STEP = "quest_chain_step";
    private static final String KEY_QUEST_CHAIN_COMPLETED = "quest_chain_completed";
    private static final String KEY_QUEST_CHAIN_MESSAGE = "quest_chain_message";
    private static final String KEY_UNLOCKED_PAINT_COUNT = "unlocked_paint_count";
    private static final String KEY_UNLOCKED_RIM_COUNT = "unlocked_rim_count";
    private static final String KEY_UNLOCKED_PART_TIER = "unlocked_part_tier";
    private static final String KEY_ECONOMY_LAST_MESSAGE = "economy_last_message";
    private static final String KEY_LAST_PENALTY_AMOUNT = "last_penalty_amount";
    private static final String KEY_TEST_DRIVE_CHALLENGE_INDEX = "test_drive_challenge_index";
    private static final String KEY_TEST_DRIVE_LAST_RESULT = "test_drive_last_result";
    private static final String KEY_TEST_DRIVE_LAST_REWARD = "test_drive_last_reward";
    private static final String KEY_TOTAL_EARNED_COINS = "total_earned_coins";
    private static final String KEY_BEST_SPEED_KMH = "best_speed_kmh";
    private static final String KEY_DRIVING_MISSION_INDEX = "driving_mission_index";
    private static final String KEY_DRIVING_MISSION_PROGRESS = "driving_mission_progress";
    private static final String KEY_DRIVING_MISSION_COMPLETED = "driving_mission_completed";
    private static final String KEY_DAILY_REWARD_LAST_DAY = "daily_reward_last_day";
    private static final String KEY_DAILY_REWARD_DAY_INDEX = "daily_reward_day_index";
    private static final String KEY_DAILY_REWARD_TOTAL_CLAIMS = "daily_reward_total_claims";
    private static final String KEY_NITRO_BONUS_PACKS = "nitro_bonus_packs";
    private static final String KEY_GARAGE_DISCOUNT_TOKENS = "garage_discount_tokens";
    private static final String KEY_CAREER_TOTAL_XP = "career_total_xp";
    private static final String KEY_CAREER_TOTAL_RACES = "career_total_races";
    private static final String KEY_CAREER_TOTAL_WINS = "career_total_wins";
    private static final String KEY_CAREER_TOTAL_LOSSES = "career_total_losses";
    private static final String KEY_CAREER_TOTAL_EARNED = "career_total_earned";
    private static final String KEY_CAREER_GOLD = "career_gold";
    private static final String KEY_CAREER_SILVER = "career_silver";
    private static final String KEY_CAREER_BRONZE = "career_bronze";
    private static final String KEY_CAREER_LAST_MESSAGE = "career_last_message";
    private static final String KEY_ACTIVE_CAREER_LEAGUE = "active_career_league";
    private static final String KEY_ACTIVE_CAREER_EVENT = "active_career_event";
    private static final String KEY_ACTIVE_CAREER_MODE = "active_career_mode";
    private static final String KEY_CAREER_LAST_LEVEL_REWARD = "career_last_level_reward";
    private static final String KEY_UI_BALANCE_VERSION = "ui_balance_version";
    private static final String KEY_HUD_COMPACT = "hud_compact";
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    private static final String KEY_CONTROL_SENSITIVITY = "control_sensitivity";
    private static final String KEY_CONTROL_LAYOUT_PRESET = "control_layout_preset";
    private static final String KEY_PEDAL_SIZE_PRESET = "pedal_size_preset";
    private static final String KEY_HUD_PRESET = "hud_preset";
    private static final String KEY_BUTTON_OPACITY = "button_opacity_percent";
    private static final String KEY_LEFT_HANDED = "left_handed_mode";
    private static final String KEY_AUTO_CONTROL = "auto_control_by_mode";
    private static final String KEY_TASK_HUD_ENABLED = "task_achievement_hud_enabled";
    private static final String KEY_TASK_NOTIFICATION_MODE = "task_achievement_notification_mode";
    private static final String KEY_REWARD_POPUP_ENABLED = "task_achievement_reward_popup_enabled";
    private static final String KEY_SAVE_REPAIR_VERSION = "save_repair_version";
    public static final int CAREER_START_COINS = 1250;

    private final SharedPreferences prefs;

    public SaveManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        ensureStarterState();
        validateAndRepairState();
    }

    private void ensureStarterState() {
        SharedPreferences.Editor e = null;
        if (!prefs.contains(KEY_COINS)) {
            e = prefs.edit();
            e.putInt(KEY_COINS, CAREER_START_COINS);
        }
        if (!prefs.contains(KEY_PLAYER_LEVEL)) {
            if (e == null) e = prefs.edit();
            e.putInt(KEY_PLAYER_LEVEL, 1);
            e.putInt(KEY_PLAYER_XP, 0);
            e.putInt(KEY_REWARD_CRATES, 0);
            e.putInt(KEY_OPENED_CRATES, 0);
            e.putInt(KEY_DRIVEN_METERS, 0);
            e.putBoolean(KEY_FIRST_MISSIONS_UNLOCKED, false);
            e.putInt(KEY_QUEST_CHAIN_STEP, 0);
            e.putInt(KEY_QUEST_CHAIN_COMPLETED, 0);
            e.putString(KEY_QUEST_CHAIN_MESSAGE, "");
            e.putInt(KEY_UNLOCKED_PAINT_COUNT, 2);
            e.putInt(KEY_UNLOCKED_RIM_COUNT, 2);
            e.putInt(KEY_UNLOCKED_PART_TIER, 1);
            e.putString(KEY_ECONOMY_LAST_MESSAGE, "");
            e.putInt(KEY_LAST_PENALTY_AMOUNT, 0);
            e.putInt(KEY_TEST_DRIVE_CHALLENGE_INDEX, 0);
            e.putString(KEY_TEST_DRIVE_LAST_RESULT, "");
            e.putInt(KEY_TEST_DRIVE_LAST_REWARD, 0);
            e.putInt(KEY_TOTAL_EARNED_COINS, 0);
            e.putFloat(KEY_BEST_SPEED_KMH, 0f);
            e.putInt(KEY_DRIVING_MISSION_INDEX, 0);
            e.putFloat(KEY_DRIVING_MISSION_PROGRESS, 0f);
            e.putInt(KEY_DRIVING_MISSION_COMPLETED, 0);
            e.putLong(KEY_DAILY_REWARD_LAST_DAY, -1L);
            e.putInt(KEY_DAILY_REWARD_DAY_INDEX, -1);
            e.putInt(KEY_DAILY_REWARD_TOTAL_CLAIMS, 0);
            e.putInt(KEY_NITRO_BONUS_PACKS, 0);
            e.putInt(KEY_GARAGE_DISCOUNT_TOKENS, 0);
            e.putInt(KEY_CAREER_TOTAL_XP, 0);
            e.putInt(KEY_CAREER_TOTAL_RACES, 0);
            e.putInt(KEY_CAREER_TOTAL_WINS, 0);
            e.putInt(KEY_CAREER_TOTAL_LOSSES, 0);
            e.putInt(KEY_CAREER_TOTAL_EARNED, 0);
            e.putInt(KEY_CAREER_GOLD, 0);
            e.putInt(KEY_CAREER_SILVER, 0);
            e.putInt(KEY_CAREER_BRONZE, 0);
            e.putString(KEY_CAREER_LAST_MESSAGE, "");
            e.putInt(KEY_CAREER_LAST_LEVEL_REWARD, 0);
            e.putInt(KEY_UI_BALANCE_VERSION, 626);
            e.putBoolean(KEY_HUD_COMPACT, true);
            e.putBoolean(KEY_VIBRATION_ENABLED, true);
            e.putInt(KEY_CONTROL_SENSITIVITY, 1);
            e.putInt(KEY_CONTROL_LAYOUT_PRESET, 0);
            e.putInt(KEY_PEDAL_SIZE_PRESET, 1);
            e.putInt(KEY_HUD_PRESET, 0);
            e.putInt(KEY_BUTTON_OPACITY, 80);
            e.putBoolean(KEY_LEFT_HANDED, false);
            e.putBoolean(KEY_AUTO_CONTROL, true);
            e.putInt(KEY_CAREER_STARTER_INDEX, 0);
            e.putInt(KEY_SELECTED_VEHICLE_INDEX, 0);
            e.putInt(KEY_CAREER_SYNC_VERSION, 0);
        }
        // A62_2: başlangıç aracı her zaman sahipli olmalı; eski kayıtlarda
        // kariyer başlamış olsa bile seçili araç/owned bilgisi eksik kalabiliyordu.
        String starterId = VehicleCatalog.id(0);
        if (!prefs.getBoolean(keyOwned(starterId), false)) {
            if (e == null) e = prefs.edit();
            e.putBoolean(keyOwned(starterId), true);
        }
        if (!prefs.contains(KEY_SELECTED_VEHICLE_INDEX)) {
            if (e == null) e = prefs.edit();
            e.putInt(KEY_SELECTED_VEHICLE_INDEX, clamp(prefs.getInt(KEY_CAREER_STARTER_INDEX, 0), 0, VehicleCatalog.count() - 1));
        }
        if (!prefs.contains(KEY_UI_BALANCE_VERSION)) {
            if (e == null) e = prefs.edit();
            e.putInt(KEY_UI_BALANCE_VERSION, 626);
        }
        if (!prefs.contains(KEY_HUD_COMPACT)) {
            if (e == null) e = prefs.edit();
            e.putBoolean(KEY_HUD_COMPACT, true);
        }
        if (!prefs.contains(KEY_VIBRATION_ENABLED)) {
            if (e == null) e = prefs.edit();
            e.putBoolean(KEY_VIBRATION_ENABLED, true);
        }
        if (!prefs.contains(KEY_CONTROL_SENSITIVITY)) {
            if (e == null) e = prefs.edit();
            e.putInt(KEY_CONTROL_SENSITIVITY, 1);
        }
        if (!prefs.contains(KEY_CONTROL_LAYOUT_PRESET)) { if (e == null) e = prefs.edit(); e.putInt(KEY_CONTROL_LAYOUT_PRESET, 0); }
        if (!prefs.contains(KEY_PEDAL_SIZE_PRESET)) { if (e == null) e = prefs.edit(); e.putInt(KEY_PEDAL_SIZE_PRESET, 1); }
        if (!prefs.contains(KEY_HUD_PRESET)) { if (e == null) e = prefs.edit(); e.putInt(KEY_HUD_PRESET, 0); }
        if (!prefs.contains(KEY_BUTTON_OPACITY)) { if (e == null) e = prefs.edit(); e.putInt(KEY_BUTTON_OPACITY, 80); }
        if (!prefs.contains(KEY_LEFT_HANDED)) { if (e == null) e = prefs.edit(); e.putBoolean(KEY_LEFT_HANDED, false); }
        if (!prefs.contains(KEY_AUTO_CONTROL)) { if (e == null) e = prefs.edit(); e.putBoolean(KEY_AUTO_CONTROL, true); }
        if (e != null) e.apply();
    }

    public int getDriftBestScore() {
        return prefs.getInt(KEY_DRIFT_BEST, 0);
    }

    public boolean saveDriftBestScoreIfHigher(int score) {
        int current = getDriftBestScore();
        if (score <= current) return false;
        prefs.edit().putInt(KEY_DRIFT_BEST, score).apply();
        return true;
    }

    public void recordDriftModeResult(int score, int coins, int xp, String grade, float longestDriftSeconds, int bestCombo) {
        int safeScore = Math.max(0, score);
        int safeCoins = Math.max(0, coins);
        int safeXp = Math.max(0, xp);
        String safeGrade = grade == null ? "" : grade;
        SharedPreferences.Editor e = prefs.edit();
        e.putInt(KEY_DRIFT_COMPLETED, getDriftCompletedCount() + 1);
        e.putInt(KEY_DRIFT_TOTAL_SCORE, getDriftTotalScore() + safeScore);
        e.putInt(KEY_DRIFT_EARNED, getDriftEarnedCoins() + safeCoins);
        e.putInt(KEY_DRIFT_XP, getDriftEarnedXp() + safeXp);
        e.putInt(KEY_DRIFT_LAST_SCORE, safeScore);
        e.putString(KEY_DRIFT_LAST_GRADE, safeGrade);
        if (longestDriftSeconds > getDriftLongestSeconds()) {
            e.putFloat(KEY_DRIFT_LONGEST_SECONDS, Math.max(0f, longestDriftSeconds));
        }
        if (bestCombo > getDriftBestCombo()) {
            e.putInt(KEY_DRIFT_BEST_COMBO, Math.max(0, bestCombo));
        }
        if ("EFSANE".equals(safeGrade)) e.putInt(KEY_DRIFT_LEGEND, getDriftLegendCount() + 1);
        else if ("ALTIN".equals(safeGrade)) e.putInt(KEY_DRIFT_GOLD, getDriftGoldCount() + 1);
        else if ("GUMUS".equals(safeGrade)) e.putInt(KEY_DRIFT_SILVER, getDriftSilverCount() + 1);
        else if ("BRONZ".equals(safeGrade)) e.putInt(KEY_DRIFT_BRONZE, getDriftBronzeCount() + 1);
        e.putString(KEY_CAREER_LAST_MESSAGE, "Drift sonucu: " + safeGrade + " +" + safeCoins + " coin +" + safeXp + " XP");
        e.apply();
        recordCareerRaceResult("Drift", safeGrade, true, safeCoins);
    }

    public int getDriftTotalScore() { return Math.max(0, prefs.getInt(KEY_DRIFT_TOTAL_SCORE, 0)); }
    public int getDriftCompletedCount() { return Math.max(0, prefs.getInt(KEY_DRIFT_COMPLETED, 0)); }
    public int getDriftEarnedCoins() { return Math.max(0, prefs.getInt(KEY_DRIFT_EARNED, 0)); }
    public int getDriftEarnedXp() { return Math.max(0, prefs.getInt(KEY_DRIFT_XP, 0)); }
    public int getDriftGoldCount() { return Math.max(0, prefs.getInt(KEY_DRIFT_GOLD, 0)); }
    public int getDriftSilverCount() { return Math.max(0, prefs.getInt(KEY_DRIFT_SILVER, 0)); }
    public int getDriftBronzeCount() { return Math.max(0, prefs.getInt(KEY_DRIFT_BRONZE, 0)); }
    public int getDriftLegendCount() { return Math.max(0, prefs.getInt(KEY_DRIFT_LEGEND, 0)); }
    public float getDriftLongestSeconds() { return Math.max(0f, prefs.getFloat(KEY_DRIFT_LONGEST_SECONDS, 0f)); }
    public int getDriftBestCombo() { return Math.max(0, prefs.getInt(KEY_DRIFT_BEST_COMBO, 0)); }
    public int getDriftLastScore() { return Math.max(0, prefs.getInt(KEY_DRIFT_LAST_SCORE, 0)); }
    public String getDriftLastGrade() { return prefs.getString(KEY_DRIFT_LAST_GRADE, ""); }

    public float getTimeTrialBestSeconds() {
        return prefs.getFloat(KEY_TIME_TRIAL_BEST, 0f);
    }

    public boolean saveTimeTrialBestIfLower(float seconds) {
        if (seconds <= 0f) return false;
        float current = getTimeTrialBestSeconds();
        if (current > 0f && seconds >= current) return false;
        prefs.edit().putFloat(KEY_TIME_TRIAL_BEST, seconds).apply();
        return true;
    }


    public float getRaceBestSeconds() {
        return prefs.getFloat(KEY_RACE_BEST, 0f);
    }

    public boolean saveRaceBestIfLower(float seconds) {
        if (seconds <= 0f) return false;
        float current = getRaceBestSeconds();
        if (current > 0f && seconds >= current) return false;
        prefs.edit().putFloat(KEY_RACE_BEST, seconds).apply();
        return true;
    }

    public int getSelectedCheckpointRoute() {
        return CheckpointRaceSystem.sanitizeRouteId(prefs.getInt(KEY_CHECKPOINT_SELECTED_ROUTE, CheckpointRaceSystem.ROUTE_MEDIUM));
    }

    public void setSelectedCheckpointRoute(int routeId) {
        prefs.edit().putInt(KEY_CHECKPOINT_SELECTED_ROUTE, CheckpointRaceSystem.sanitizeRouteId(routeId)).apply();
    }

    public float getCheckpointRouteBestSeconds(int routeId) {
        return Math.max(0f, prefs.getFloat("checkpoint_route_best_" + CheckpointRaceSystem.sanitizeRouteId(routeId), 0f));
    }

    public boolean saveCheckpointRouteBestIfLower(int routeId, float seconds) {
        if (seconds <= 0f) return false;
        int route = CheckpointRaceSystem.sanitizeRouteId(routeId);
        float current = getCheckpointRouteBestSeconds(route);
        if (current > 0f && seconds >= current) return false;
        prefs.edit().putFloat("checkpoint_route_best_" + route, seconds).apply();
        return true;
    }

    public String getCheckpointRouteBestMedal(int routeId) {
        return prefs.getString("checkpoint_route_best_medal_" + CheckpointRaceSystem.sanitizeRouteId(routeId), "");
    }

    public int getCheckpointRouteCompletedCount(int routeId) {
        return Math.max(0, prefs.getInt("checkpoint_route_completed_" + CheckpointRaceSystem.sanitizeRouteId(routeId), 0));
    }

    public int getCheckpointRouteEarnedCoins(int routeId) {
        return Math.max(0, prefs.getInt("checkpoint_route_earned_" + CheckpointRaceSystem.sanitizeRouteId(routeId), 0));
    }

    public float getCheckpointRouteLastSeconds(int routeId) {
        return Math.max(0f, prefs.getFloat("checkpoint_route_last_time_" + CheckpointRaceSystem.sanitizeRouteId(routeId), 0f));
    }

    public String getCheckpointRouteLastMedal(int routeId) {
        return prefs.getString("checkpoint_route_last_medal_" + CheckpointRaceSystem.sanitizeRouteId(routeId), "");
    }

    public int getCheckpointRouteTotalCompletions() {
        int total = 0;
        for (int i = 0; i < CheckpointRaceSystem.ROUTE_COUNT; i++) total += getCheckpointRouteCompletedCount(i);
        return Math.max(0, total);
    }

    public int getCheckpointRouteTotalEarnedCoins() {
        int total = 0;
        for (int i = 0; i < CheckpointRaceSystem.ROUTE_COUNT; i++) total += getCheckpointRouteEarnedCoins(i);
        return Math.max(0, total);
    }

    public String getCheckpointRouteBestSummary() {
        int bestRoute = -1;
        float bestTime = 0f;
        String bestMedal = "";
        for (int i = 0; i < CheckpointRaceSystem.ROUTE_COUNT; i++) {
            float time = getCheckpointRouteBestSeconds(i);
            int rank = CheckpointRaceSystem.medalRank(getCheckpointRouteBestMedal(i));
            int bestRank = CheckpointRaceSystem.medalRank(bestMedal);
            if (time > 0f && (bestRoute < 0 || rank > bestRank || (rank == bestRank && time < bestTime))) {
                bestRoute = i;
                bestTime = time;
                bestMedal = getCheckpointRouteBestMedal(i);
            }
        }
        if (bestRoute < 0) return "Henüz rota rekoru yok";
        return CheckpointRaceSystem.routeLabel(bestRoute) + " • " + bestMedal + " • " + RaceModeSystem.formatTime(bestTime);
    }

    public int getCheckpointRouteGoldCount(int routeId) {
        return Math.max(0, prefs.getInt("checkpoint_route_gold_" + CheckpointRaceSystem.sanitizeRouteId(routeId), 0));
    }

    public int getCheckpointRouteSilverCount(int routeId) {
        return Math.max(0, prefs.getInt("checkpoint_route_silver_" + CheckpointRaceSystem.sanitizeRouteId(routeId), 0));
    }

    public int getCheckpointRouteBronzeCount(int routeId) {
        return Math.max(0, prefs.getInt("checkpoint_route_bronze_" + CheckpointRaceSystem.sanitizeRouteId(routeId), 0));
    }

    public int getCheckpointRouteMedalRank(int routeId) {
        return Math.max(0, prefs.getInt("checkpoint_route_medal_rank_" + CheckpointRaceSystem.sanitizeRouteId(routeId), 0));
    }

    public void recordCheckpointRouteResult(int routeId, float seconds, int coins, String medal, boolean newBest, boolean newMedal) {
        int route = CheckpointRaceSystem.sanitizeRouteId(routeId);
        SharedPreferences.Editor e = prefs.edit();
        e.putInt("checkpoint_route_completed_" + route, getCheckpointRouteCompletedCount(route) + 1);
        e.putInt("checkpoint_route_earned_" + route, getCheckpointRouteEarnedCoins(route) + Math.max(0, coins));
        e.putFloat("checkpoint_route_last_time_" + route, Math.max(0f, seconds));
        e.putString("checkpoint_route_last_medal_" + route, medal == null ? "" : medal);
        if (newBest && seconds > 0f) e.putFloat("checkpoint_route_best_" + route, seconds);
        int rank = CheckpointRaceSystem.medalRank(medal);
        if (newMedal && rank > getCheckpointRouteMedalRank(route)) {
            e.putInt("checkpoint_route_medal_rank_" + route, rank);
            e.putString("checkpoint_route_best_medal_" + route, medal == null ? "" : medal);
        }
        if (RaceModeSystem.GRADE_GOLD.equals(medal)) e.putInt("checkpoint_route_gold_" + route, getCheckpointRouteGoldCount(route) + 1);
        else if (RaceModeSystem.GRADE_SILVER.equals(medal)) e.putInt("checkpoint_route_silver_" + route, getCheckpointRouteSilverCount(route) + 1);
        else if (RaceModeSystem.GRADE_BRONZE.equals(medal)) e.putInt("checkpoint_route_bronze_" + route, getCheckpointRouteBronzeCount(route) + 1);
        e.apply();
    }

    public void recordCheckpointRaceResult(float seconds, int coins, String grade) {
        SharedPreferences.Editor e = prefs.edit();
        e.putInt(KEY_CHECKPOINT_RACE_COMPLETED, getCheckpointRaceCompletedCount() + 1);
        e.putInt(KEY_CHECKPOINT_RACE_EARNED, getCheckpointRaceEarnedCoins() + Math.max(0, coins));
        e.putFloat(KEY_CHECKPOINT_RACE_LAST_TIME, Math.max(0f, seconds));
        e.putString(KEY_CHECKPOINT_RACE_LAST_GRADE, grade == null ? "" : grade);
        if ("ALTIN".equals(grade)) e.putInt(KEY_CHECKPOINT_RACE_GOLD, getCheckpointRaceGoldCount() + 1);
        else if ("GUMUS".equals(grade)) e.putInt(KEY_CHECKPOINT_RACE_SILVER, getCheckpointRaceSilverCount() + 1);
        else if ("BRONZ".equals(grade)) e.putInt(KEY_CHECKPOINT_RACE_BRONZE, getCheckpointRaceBronzeCount() + 1);
        e.apply();
        recordVehicleCheckpointRaceResult(getSelectedVehicleId(), seconds, coins, grade);
        recordCareerRaceResult("Checkpoint", grade, true, coins);
    }

    public void recordVehicleCheckpointRaceResult(String vehicleId, float seconds, int coins, String grade) {
        if (vehicleId == null || vehicleId.length() == 0) return;
        SharedPreferences.Editor e = prefs.edit();
        e.putInt("vehicle_checkpoint_completed_" + vehicleId, getVehicleCheckpointRaceCompletedCount(vehicleId) + 1);
        e.putInt("vehicle_checkpoint_earned_" + vehicleId, getVehicleCheckpointRaceEarnedCoins(vehicleId) + Math.max(0, coins));
        e.putFloat("vehicle_checkpoint_last_time_" + vehicleId, Math.max(0f, seconds));
        e.putString("vehicle_checkpoint_last_grade_" + vehicleId, grade == null ? "" : grade);
        if (seconds > 0f) {
            float best = getVehicleCheckpointBestSeconds(vehicleId);
            if (best <= 0f || seconds < best) e.putFloat("vehicle_checkpoint_best_" + vehicleId, seconds);
        }
        if ("ALTIN".equals(grade)) e.putInt("vehicle_checkpoint_gold_" + vehicleId, getVehicleCheckpointRaceGoldCount(vehicleId) + 1);
        else if ("GUMUS".equals(grade)) e.putInt("vehicle_checkpoint_silver_" + vehicleId, getVehicleCheckpointRaceSilverCount(vehicleId) + 1);
        else if ("BRONZ".equals(grade)) e.putInt("vehicle_checkpoint_bronze_" + vehicleId, getVehicleCheckpointRaceBronzeCount(vehicleId) + 1);
        e.apply();
    }

    public float getVehicleCheckpointBestSeconds(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0f;
        return Math.max(0f, prefs.getFloat("vehicle_checkpoint_best_" + vehicleId, 0f));
    }

    public int getVehicleCheckpointRaceCompletedCount(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return Math.max(0, prefs.getInt("vehicle_checkpoint_completed_" + vehicleId, 0));
    }

    public int getVehicleCheckpointRaceEarnedCoins(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return Math.max(0, prefs.getInt("vehicle_checkpoint_earned_" + vehicleId, 0));
    }

    public int getVehicleCheckpointRaceGoldCount(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return Math.max(0, prefs.getInt("vehicle_checkpoint_gold_" + vehicleId, 0));
    }

    public int getVehicleCheckpointRaceSilverCount(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return Math.max(0, prefs.getInt("vehicle_checkpoint_silver_" + vehicleId, 0));
    }

    public int getVehicleCheckpointRaceBronzeCount(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return Math.max(0, prefs.getInt("vehicle_checkpoint_bronze_" + vehicleId, 0));
    }

    public int getCheckpointRaceCompletedCount() {
        return Math.max(0, prefs.getInt(KEY_CHECKPOINT_RACE_COMPLETED, 0));
    }

    public int getCheckpointRaceEarnedCoins() {
        return Math.max(0, prefs.getInt(KEY_CHECKPOINT_RACE_EARNED, 0));
    }

    public int getCheckpointRaceGoldCount() {
        return Math.max(0, prefs.getInt(KEY_CHECKPOINT_RACE_GOLD, 0));
    }

    public int getCheckpointRaceSilverCount() {
        return Math.max(0, prefs.getInt(KEY_CHECKPOINT_RACE_SILVER, 0));
    }

    public int getCheckpointRaceBronzeCount() {
        return Math.max(0, prefs.getInt(KEY_CHECKPOINT_RACE_BRONZE, 0));
    }

    public float getCheckpointRaceLastTime() {
        return Math.max(0f, prefs.getFloat(KEY_CHECKPOINT_RACE_LAST_TIME, 0f));
    }

    public String getCheckpointRaceLastGrade() {
        return prefs.getString(KEY_CHECKPOINT_RACE_LAST_GRADE, "");
    }

    public float getDragBestSeconds() {
        return prefs.getFloat(KEY_DRAG_BEST, 0f);
    }

    public boolean saveDragBestIfLower(float seconds) {
        if (seconds <= 0f) return false;
        float current = getDragBestSeconds();
        if (current > 0f && seconds >= current) return false;
        prefs.edit().putFloat(KEY_DRAG_BEST, seconds).apply();
        return true;
    }

    public void recordDragRaceResult(float seconds, int coins, String grade, float topSpeedKmh) {
        SharedPreferences.Editor e = prefs.edit();
        e.putInt(KEY_DRAG_COMPLETED, getDragRaceCompletedCount() + 1);
        e.putInt(KEY_DRAG_EARNED, getDragRaceEarnedCoins() + Math.max(0, coins));
        e.putFloat(KEY_DRAG_LAST_TIME, Math.max(0f, seconds));
        e.putString(KEY_DRAG_LAST_GRADE, grade == null ? "" : grade);
        e.putFloat(KEY_DRAG_BEST_SPEED, Math.max(getDragBestSpeedKmh(), topSpeedKmh));
        if ("ALTIN".equals(grade)) e.putInt(KEY_DRAG_GOLD, getDragRaceGoldCount() + 1);
        else if ("GUMUS".equals(grade)) e.putInt(KEY_DRAG_SILVER, getDragRaceSilverCount() + 1);
        else if ("BRONZ".equals(grade)) e.putInt(KEY_DRAG_BRONZE, getDragRaceBronzeCount() + 1);
        e.apply();
        recordVehicleDragRaceResult(getSelectedVehicleId(), seconds, coins, grade, topSpeedKmh);
        recordCareerRaceResult("Drag", grade, true, coins);
    }

    public void recordVehicleDragRaceResult(String vehicleId, float seconds, int coins, String grade, float topSpeedKmh) {
        if (vehicleId == null || vehicleId.length() == 0) return;
        SharedPreferences.Editor e = prefs.edit();
        e.putInt("vehicle_drag_completed_" + vehicleId, getVehicleDragRaceCompletedCount(vehicleId) + 1);
        e.putInt("vehicle_drag_earned_" + vehicleId, getVehicleDragRaceEarnedCoins(vehicleId) + Math.max(0, coins));
        e.putFloat("vehicle_drag_last_time_" + vehicleId, Math.max(0f, seconds));
        e.putString("vehicle_drag_last_grade_" + vehicleId, grade == null ? "" : grade);
        e.putFloat("vehicle_drag_best_speed_" + vehicleId, Math.max(getVehicleDragBestSpeedKmh(vehicleId), topSpeedKmh));
        if (seconds > 0f) {
            float best = getVehicleDragBestSeconds(vehicleId);
            if (best <= 0f || seconds < best) e.putFloat("vehicle_drag_best_" + vehicleId, seconds);
        }
        if ("ALTIN".equals(grade)) e.putInt("vehicle_drag_gold_" + vehicleId, getVehicleDragRaceGoldCount(vehicleId) + 1);
        else if ("GUMUS".equals(grade)) e.putInt("vehicle_drag_silver_" + vehicleId, getVehicleDragRaceSilverCount(vehicleId) + 1);
        else if ("BRONZ".equals(grade)) e.putInt("vehicle_drag_bronze_" + vehicleId, getVehicleDragRaceBronzeCount(vehicleId) + 1);
        e.apply();
    }

    public float getVehicleDragBestSeconds(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0f;
        return Math.max(0f, prefs.getFloat("vehicle_drag_best_" + vehicleId, 0f));
    }

    public float getVehicleDragBestSpeedKmh(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0f;
        return Math.max(0f, prefs.getFloat("vehicle_drag_best_speed_" + vehicleId, 0f));
    }

    public int getVehicleDragRaceCompletedCount(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return Math.max(0, prefs.getInt("vehicle_drag_completed_" + vehicleId, 0));
    }

    public int getVehicleDragRaceEarnedCoins(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return Math.max(0, prefs.getInt("vehicle_drag_earned_" + vehicleId, 0));
    }

    public int getVehicleDragRaceGoldCount(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return Math.max(0, prefs.getInt("vehicle_drag_gold_" + vehicleId, 0));
    }

    public int getVehicleDragRaceSilverCount(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return Math.max(0, prefs.getInt("vehicle_drag_silver_" + vehicleId, 0));
    }

    public int getVehicleDragRaceBronzeCount(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return Math.max(0, prefs.getInt("vehicle_drag_bronze_" + vehicleId, 0));
    }

    public int getDragRaceCompletedCount() {
        return Math.max(0, prefs.getInt(KEY_DRAG_COMPLETED, 0));
    }

    public int getDragRaceEarnedCoins() {
        return Math.max(0, prefs.getInt(KEY_DRAG_EARNED, 0));
    }

    public int getDragRaceGoldCount() {
        return Math.max(0, prefs.getInt(KEY_DRAG_GOLD, 0));
    }

    public int getDragRaceSilverCount() {
        return Math.max(0, prefs.getInt(KEY_DRAG_SILVER, 0));
    }

    public int getDragRaceBronzeCount() {
        return Math.max(0, prefs.getInt(KEY_DRAG_BRONZE, 0));
    }

    public float getDragRaceLastTime() {
        return Math.max(0f, prefs.getFloat(KEY_DRAG_LAST_TIME, 0f));
    }

    public String getDragRaceLastGrade() {
        return prefs.getString(KEY_DRAG_LAST_GRADE, "");
    }

    public float getDragBestSpeedKmh() {
        return Math.max(0f, prefs.getFloat(KEY_DRAG_BEST_SPEED, 0f));
    }

    public void recordRivalRaceResult(String mode, int difficulty, boolean won, int coins) {
        String safeMode = mode == null ? "" : mode;
        int safeDifficulty = clamp(difficulty, 0, 2);
        int safeCoins = Math.max(0, coins);
        SharedPreferences.Editor e = prefs.edit();
        e.putInt(KEY_RIVAL_TOTAL_RACES, getRivalTotalRaces() + 1);
        e.putInt(won ? KEY_RIVAL_TOTAL_WINS : KEY_RIVAL_TOTAL_LOSSES,
                won ? getRivalTotalWins() + 1 : getRivalTotalLosses() + 1);
        if (won && safeMode.indexOf("Drag") >= 0) e.putInt(KEY_RIVAL_DRAG_WINS, getRivalDragWins() + 1);
        if (won && safeMode.indexOf("Checkpoint") >= 0) e.putInt(KEY_RIVAL_CHECKPOINT_WINS, getRivalCheckpointWins() + 1);
        if (won && safeDifficulty >= 2) e.putInt(KEY_RIVAL_HARD_BEATEN, getRivalHardBeatenCount() + 1);
        e.putInt(KEY_RIVAL_TOTAL_EARNED, getRivalTotalEarnedCoins() + safeCoins);
        e.putString(KEY_RIVAL_LAST_RESULT, won ? "KAZANDIN" : "KAYBETTIN");
        e.putInt(KEY_RIVAL_LAST_DIFFICULTY, safeDifficulty);
        e.putString(KEY_RIVAL_LAST_MODE, safeMode);
        e.putInt(won ? KEY_CAREER_TOTAL_WINS : KEY_CAREER_TOTAL_LOSSES,
                won ? getCareerTotalWins() + 1 : getCareerTotalLosses() + 1);
        e.apply();
    }

    public int getRivalTotalRaces() {
        return Math.max(0, prefs.getInt(KEY_RIVAL_TOTAL_RACES, 0));
    }

    public int getRivalTotalWins() {
        return Math.max(0, prefs.getInt(KEY_RIVAL_TOTAL_WINS, 0));
    }

    public int getRivalTotalLosses() {
        return Math.max(0, prefs.getInt(KEY_RIVAL_TOTAL_LOSSES, 0));
    }

    public int getRivalDragWins() {
        return Math.max(0, prefs.getInt(KEY_RIVAL_DRAG_WINS, 0));
    }

    public int getRivalCheckpointWins() {
        return Math.max(0, prefs.getInt(KEY_RIVAL_CHECKPOINT_WINS, 0));
    }

    public int getRivalHardBeatenCount() {
        return Math.max(0, prefs.getInt(KEY_RIVAL_HARD_BEATEN, 0));
    }

    public int getRivalTotalEarnedCoins() {
        return Math.max(0, prefs.getInt(KEY_RIVAL_TOTAL_EARNED, 0));
    }

    public String getRivalLastResult() {
        return prefs.getString(KEY_RIVAL_LAST_RESULT, "");
    }

    public int getRivalLastDifficulty() {
        return clamp(prefs.getInt(KEY_RIVAL_LAST_DIFFICULTY, 0), 0, 2);
    }

    public String getRivalLastMode() {
        return prefs.getString(KEY_RIVAL_LAST_MODE, "");
    }



    public void recordCareerRaceResult(String raceType, String grade, boolean completed, int coins) {
        int safeCoins = Math.max(0, coins);
        SharedPreferences.Editor e = prefs.edit();
        e.putInt(KEY_CAREER_TOTAL_RACES, getCareerTotalRaces() + 1);
        e.putInt(KEY_CAREER_TOTAL_EARNED, getCareerTotalEarnedCoins() + safeCoins);
        if ("ALTIN".equals(grade)) e.putInt(KEY_CAREER_GOLD, getCareerGoldCount() + 1);
        else if ("GUMUS".equals(grade)) e.putInt(KEY_CAREER_SILVER, getCareerSilverCount() + 1);
        else if ("BRONZ".equals(grade)) e.putInt(KEY_CAREER_BRONZE, getCareerBronzeCount() + 1);
        e.putString(KEY_CAREER_LAST_MESSAGE, (raceType == null ? "Yaris" : raceType) + " kariyer sonucu: " + (grade == null ? "TAMAM" : grade) + " +" + safeCoins + " coin");
        e.apply();
    }

    public int getCareerTotalXp() { return Math.max(0, prefs.getInt(KEY_CAREER_TOTAL_XP, 0)); }
    public int getCareerTotalRaces() { return Math.max(0, prefs.getInt(KEY_CAREER_TOTAL_RACES, 0)); }
    public int getCareerTotalWins() { return Math.max(0, prefs.getInt(KEY_CAREER_TOTAL_WINS, 0)); }
    public int getCareerTotalLosses() { return Math.max(0, prefs.getInt(KEY_CAREER_TOTAL_LOSSES, 0)); }
    public int getCareerTotalEarnedCoins() { return Math.max(0, prefs.getInt(KEY_CAREER_TOTAL_EARNED, 0)); }
    public int getCareerGoldCount() { return Math.max(0, prefs.getInt(KEY_CAREER_GOLD, 0)); }
    public int getCareerSilverCount() { return Math.max(0, prefs.getInt(KEY_CAREER_SILVER, 0)); }
    public int getCareerBronzeCount() { return Math.max(0, prefs.getInt(KEY_CAREER_BRONZE, 0)); }
    public int getCareerLastLevelReward() { return Math.max(0, prefs.getInt(KEY_CAREER_LAST_LEVEL_REWARD, 0)); }
    public String getCareerLastMessage() { return prefs.getString(KEY_CAREER_LAST_MESSAGE, ""); }
    public int getCareerLeagueIndex() { return CareerLeagueSystem.leagueForLevel(getPlayerLevel()); }
    public String getCareerLeagueName() { return CareerLeagueSystem.leagueName(getCareerLeagueIndex()); }
    public int getXpForNextLevel() { return CareerLeagueSystem.xpForNextLevel(getPlayerLevel()); }
    public int getVehicleRequiredLevel(int vehicleIndex) { return CareerLeagueSystem.vehicleRequiredLevel(vehicleIndex); }
    public boolean isVehicleLevelUnlocked(int vehicleIndex) { return getPlayerLevel() >= getVehicleRequiredLevel(vehicleIndex); }
    public boolean canBuyVehicleByCareer(int vehicleIndex) { return isVehicleLevelUnlocked(vehicleIndex) && getCoins() >= VehicleCatalog.price(vehicleIndex); }

    public int getPoliceBestSeconds() {
        return prefs.getInt(KEY_POLICE_BEST, 0);
    }

    public boolean savePoliceBestIfHigher(int seconds) {
        if (seconds <= 0) return false;
        int current = getPoliceBestSeconds();
        if (seconds <= current) return false;
        prefs.edit().putInt(KEY_POLICE_BEST, seconds).apply();
        return true;
    }

    public void recordPoliceChaseResult(boolean escaped, int wantedLevel, int seconds, int coins, int xp) {
        int safeWanted = clamp(wantedLevel, 1, 5);
        int safeSeconds = Math.max(0, seconds);
        int safeCoins = Math.max(0, coins);
        int safeXp = Math.max(0, xp);
        SharedPreferences.Editor e = prefs.edit();
        e.putInt(KEY_POLICE_TOTAL_CHASES, getPoliceTotalChases() + 1);
        e.putInt(escaped ? KEY_POLICE_ESCAPES : KEY_POLICE_CAUGHT,
                escaped ? getPoliceEscapes() + 1 : getPoliceCaughtCount() + 1);
        e.putInt(KEY_POLICE_HIGHEST_WANTED, Math.max(getPoliceHighestWanted(), safeWanted));
        e.putInt(KEY_POLICE_EARNED_COINS, getPoliceEarnedCoins() + safeCoins);
        e.putInt(KEY_POLICE_EARNED_XP, getPoliceEarnedXp() + safeXp);
        e.putString(KEY_POLICE_LAST_RESULT, escaped ? "KAÇIŞ BAŞARILI" : "YAKALANDIN");
        e.putInt(KEY_POLICE_LAST_WANTED, safeWanted);
        e.putInt(KEY_POLICE_LAST_SECONDS, safeSeconds);
        e.putInt(escaped ? KEY_CAREER_TOTAL_WINS : KEY_CAREER_TOTAL_LOSSES,
                escaped ? getCareerTotalWins() + 1 : getCareerTotalLosses() + 1);
        e.putInt(KEY_CAREER_TOTAL_RACES, getCareerTotalRaces() + 1);
        e.putInt(KEY_CAREER_TOTAL_EARNED, getCareerTotalEarnedCoins() + safeCoins);
        e.putString(KEY_CAREER_LAST_MESSAGE, (escaped ? "Polis kaçışı" : "Polis yakalanma")
                + ": " + safeWanted + " yıldız +" + safeCoins + " coin +" + safeXp + " XP");
        if (escaped && safeSeconds > getPoliceBestSeconds()) e.putInt(KEY_POLICE_BEST, safeSeconds);
        e.apply();
    }

    public int getPoliceTotalChases() { return Math.max(0, prefs.getInt(KEY_POLICE_TOTAL_CHASES, 0)); }
    public int getPoliceEscapes() { return Math.max(0, prefs.getInt(KEY_POLICE_ESCAPES, 0)); }
    public int getPoliceCaughtCount() { return Math.max(0, prefs.getInt(KEY_POLICE_CAUGHT, 0)); }
    public int getPoliceHighestWanted() { return Math.max(0, prefs.getInt(KEY_POLICE_HIGHEST_WANTED, 0)); }
    public int getPoliceEarnedCoins() { return Math.max(0, prefs.getInt(KEY_POLICE_EARNED_COINS, 0)); }
    public int getPoliceEarnedXp() { return Math.max(0, prefs.getInt(KEY_POLICE_EARNED_XP, 0)); }
    public String getPoliceLastResult() { return prefs.getString(KEY_POLICE_LAST_RESULT, ""); }
    public int getPoliceLastWanted() { return Math.max(0, prefs.getInt(KEY_POLICE_LAST_WANTED, 0)); }
    public int getPoliceLastSeconds() { return Math.max(0, prefs.getInt(KEY_POLICE_LAST_SECONDS, 0)); }

    public void recordTrafficNearMissReward(int coins, int combo, boolean policeMode, int nearMissEvents) {
        int safeCoins = Math.max(0, coins);
        int safeCombo = Math.max(1, combo);
        int safeEvents = Math.max(1, nearMissEvents);
        if (safeCoins > 0) addCoins(safeCoins);
        int total = Math.max(0, prefs.getInt(KEY_TRAFFIC_NEAR_MISS_TOTAL, 0)) + safeEvents;
        int clean = Math.max(0, prefs.getInt(KEY_TRAFFIC_CLEAN_STREAK, 0)) + safeEvents;
        int earned = Math.max(0, prefs.getInt(KEY_TRAFFIC_EARNED_COINS, 0)) + safeCoins;
        int best = Math.max(Math.max(0, prefs.getInt(KEY_TRAFFIC_BEST_COMBO, 0)), safeCombo);
        int policeRisk = Math.max(0, prefs.getInt(KEY_TRAFFIC_POLICE_RISK_PASSES, 0)) + (policeMode ? safeEvents : 0);
        String msg = "YAKIN GECIS x" + safeCombo + " +" + safeCoins + " coin" + (policeMode ? " | POLIS RISK" : "");
        prefs.edit()
                .putInt(KEY_TRAFFIC_NEAR_MISS_TOTAL, total)
                .putInt(KEY_TRAFFIC_CLEAN_STREAK, clean)
                .putInt(KEY_TRAFFIC_EARNED_COINS, earned)
                .putInt(KEY_TRAFFIC_BEST_COMBO, best)
                .putInt(KEY_TRAFFIC_POLICE_RISK_PASSES, policeRisk)
                .putString(KEY_TRAFFIC_LAST_MESSAGE, msg)
                .putString(KEY_ECONOMY_LAST_MESSAGE, msg)
                .apply();
    }

    public void recordTrafficCollision(int collisions) {
        int safe = Math.max(0, collisions);
        if (safe <= 0) return;
        int total = Math.max(0, prefs.getInt(KEY_TRAFFIC_COLLISIONS, 0)) + safe;
        prefs.edit()
                .putInt(KEY_TRAFFIC_COLLISIONS, total)
                .putInt(KEY_TRAFFIC_CLEAN_STREAK, 0)
                .putString(KEY_TRAFFIC_LAST_MESSAGE, "TRAFIK CARPISMASI")
                .apply();
    }

    public int getTrafficNearMissTotal() { return Math.max(0, prefs.getInt(KEY_TRAFFIC_NEAR_MISS_TOTAL, 0)); }
    public int getTrafficBestCombo() { return Math.max(0, prefs.getInt(KEY_TRAFFIC_BEST_COMBO, 0)); }
    public int getTrafficEarnedCoins() { return Math.max(0, prefs.getInt(KEY_TRAFFIC_EARNED_COINS, 0)); }
    public int getTrafficCollisions() { return Math.max(0, prefs.getInt(KEY_TRAFFIC_COLLISIONS, 0)); }
    public int getTrafficPoliceRiskPasses() { return Math.max(0, prefs.getInt(KEY_TRAFFIC_POLICE_RISK_PASSES, 0)); }
    public int getTrafficCleanStreak() { return Math.max(0, prefs.getInt(KEY_TRAFFIC_CLEAN_STREAK, 0)); }
    public String getTrafficLastMessage() { return prefs.getString(KEY_TRAFFIC_LAST_MESSAGE, ""); }

    public int getCoins() {
        return prefs.getInt(KEY_COINS, 0);
    }

    public void addCoins(int amount) {
        if (amount <= 0) return;
        prefs.edit()
                .putInt(KEY_COINS, Math.max(0, getCoins() + amount))
                .putInt(KEY_TOTAL_EARNED_COINS, Math.max(0, getTotalEarnedCoins() + amount))
                .apply();
    }

    public void setCoins(int coins) {
        prefs.edit().putInt(KEY_COINS, Math.max(0, coins)).apply();
    }

    public int removeCoinsUpTo(int amount) {
        if (amount <= 0) return 0;
        int coins = getCoins();
        int paid = Math.min(coins, amount);
        prefs.edit().putInt(KEY_COINS, Math.max(0, coins - paid)).apply();
        return paid;
    }

    public boolean spendCoins(int amount) {
        if (amount <= 0) return true;
        int coins = getCoins();
        if (coins < amount) return false;
        prefs.edit().putInt(KEY_COINS, coins - amount).apply();
        return true;
    }

    public int getPlayerLevel() {
        return Math.max(1, prefs.getInt(KEY_PLAYER_LEVEL, 1));
    }

    public void setPlayerLevel(int level) {
        prefs.edit().putInt(KEY_PLAYER_LEVEL, Math.max(1, level)).apply();
    }

    public int getPlayerXp() {
        return Math.max(0, prefs.getInt(KEY_PLAYER_XP, 0));
    }

    public void setPlayerXp(int xp) {
        prefs.edit().putInt(KEY_PLAYER_XP, Math.max(0, xp)).apply();
    }

    public void addXp(int amount) {
        if (amount <= 0) return;
        int level = getPlayerLevel();
        int xp = Math.max(0, getPlayerXp() + amount);
        int totalXp = Math.max(0, getCareerTotalXp() + amount);
        int coinReward = 0;
        int crates = getRewardCrates();
        int lastRewardLevel = getCareerLastLevelReward();
        String message = "";
        while (xp >= CareerLeagueSystem.xpForNextLevel(level)) {
            xp -= CareerLeagueSystem.xpForNextLevel(level);
            level++;
            int reward = CareerLeagueSystem.levelRewardCoins(level);
            coinReward += reward;
            crates++;
            lastRewardLevel = level;
            message = "SEVIYE " + level + "! +" + reward + " coin | " + CareerLeagueSystem.leagueProgressText(this);
        }
        SharedPreferences.Editor e = prefs.edit()
                .putInt(KEY_PLAYER_LEVEL, Math.max(1, level))
                .putInt(KEY_PLAYER_XP, Math.max(0, xp))
                .putInt(KEY_CAREER_TOTAL_XP, totalXp)
                .putInt(KEY_REWARD_CRATES, Math.max(0, crates))
                .putInt(KEY_CAREER_LAST_LEVEL_REWARD, Math.max(0, lastRewardLevel));
        if (coinReward > 0) {
            e.putInt(KEY_COINS, Math.max(0, getCoins() + coinReward));
            e.putInt(KEY_TOTAL_EARNED_COINS, Math.max(0, getTotalEarnedCoins() + coinReward));
            e.putString(KEY_CAREER_LAST_MESSAGE, message);
            e.putString(KEY_ECONOMY_LAST_MESSAGE, message);
        }
        e.apply();
    }

    public int getRewardCrates() {
        return Math.max(0, prefs.getInt(KEY_REWARD_CRATES, 0));
    }

    public void setRewardCrates(int crates) {
        prefs.edit().putInt(KEY_REWARD_CRATES, Math.max(0, crates)).apply();
    }

    public int getOpenedCrates() {
        return Math.max(0, prefs.getInt(KEY_OPENED_CRATES, 0));
    }

    public void setOpenedCrates(int opened) {
        prefs.edit().putInt(KEY_OPENED_CRATES, Math.max(0, opened)).apply();
    }

    public boolean openRewardCrate(int level) {
        if (getRewardCrates() <= 0) return false;
        int safeLevel = Math.max(1, level);
        int opened = getOpenedCrates() + 1;
        setRewardCrates(getRewardCrates() - 1);
        setOpenedCrates(opened);
        addCoins(450 + safeLevel * 75 + (opened % 4) * 120);
        addXp(110 + safeLevel * 12);
        return true;
    }

    public int getDrivenMeters() {
        return Math.max(0, prefs.getInt(KEY_DRIVEN_METERS, 0));
    }

    public void addDrivenMeters(int meters) {
        if (meters <= 0) return;
        prefs.edit().putInt(KEY_DRIVEN_METERS, Math.max(0, getDrivenMeters() + meters)).apply();
    }

    public int getProgressDailyCount() {
        resetProgressDailyIfNeeded();
        return prefs.getInt("progress_daily_count", 0);
    }

    public void incrementProgressDaily() {
        resetProgressDailyIfNeeded();
        int current = prefs.getInt("progress_daily_count", 0);
        prefs.edit().putInt("progress_daily_count", Math.min(99, current + 1)).apply();
    }

    private void resetProgressDailyIfNeeded() {
        long today = System.currentTimeMillis() / 86400000L;
        long saved = prefs.getLong("progress_daily_day", -1L);
        if (saved != today) {
            prefs.edit()
                    .putLong("progress_daily_day", today)
                    .putInt("progress_daily_count", 0)
                    .apply();
        }
    }

    public int getProgressWeeklyCount() {
        resetProgressWeeklyIfNeeded();
        return prefs.getInt("progress_weekly_count", 0);
    }

    public void incrementProgressWeekly() {
        resetProgressWeeklyIfNeeded();
        int current = prefs.getInt("progress_weekly_count", 0);
        prefs.edit().putInt("progress_weekly_count", Math.min(999, current + 1)).apply();
    }

    private void resetProgressWeeklyIfNeeded() {
        long week = (System.currentTimeMillis() / 86400000L) / 7L;
        long saved = prefs.getLong("progress_weekly_week", -1L);
        if (saved != week) {
            prefs.edit()
                    .putLong("progress_weekly_week", week)
                    .putInt("progress_weekly_count", 0)
                    .apply();
        }
    }

    public boolean isAchievementUnlocked(int achievementId) {
        return prefs.getBoolean("achievement_unlocked_" + achievementId, false);
    }

    public void setAchievementUnlocked(int achievementId, boolean unlocked) {
        prefs.edit().putBoolean("achievement_unlocked_" + achievementId, unlocked).apply();
    }

    public int getAchievementUnlockedCount(int maxCount) {
        int count = 0;
        for (int i = 0; i < maxCount; i++) {
            if (isAchievementUnlocked(i)) count++;
        }
        return count;
    }

    public boolean isCareerStarted() {
        return prefs.getBoolean(KEY_CAREER_STARTED, false);
    }

    public int getCareerStarterVehicleIndex() {
        return clamp(prefs.getInt(KEY_CAREER_STARTER_INDEX, 0), 0, 7);
    }

    public void setCareerStarterVehicleIndex(int index) {
        prefs.edit().putInt(KEY_CAREER_STARTER_INDEX, Math.max(0, index)).apply();
    }

    public int getSelectedVehicleIndex() {
        int index = clamp(prefs.getInt(KEY_SELECTED_VEHICLE_INDEX, getCareerStarterVehicleIndex()), 0, VehicleCatalog.count() - 1);
        String id = VehicleCatalog.id(index);
        if (!isVehicleOwned(id)) {
            int starter = clamp(getCareerStarterVehicleIndex(), 0, VehicleCatalog.count() - 1);
            if (isVehicleOwned(VehicleCatalog.id(starter))) return starter;
            return 0;
        }
        return index;
    }

    public void setSelectedVehicleIndex(int index) {
        int safe = clamp(index, 0, VehicleCatalog.count() - 1);
        String id = VehicleCatalog.id(safe);
        if (!isVehicleOwned(id)) return;
        prefs.edit().putInt(KEY_SELECTED_VEHICLE_INDEX, safe).apply();
    }

    public String getSelectedVehicleId() {
        return VehicleCatalog.id(getSelectedVehicleIndex());
    }

    public int getCareerSyncVersion() {
        return prefs.getInt(KEY_CAREER_SYNC_VERSION, 0);
    }

    public void setCareerSyncVersion(int version) {
        prefs.edit().putInt(KEY_CAREER_SYNC_VERSION, Math.max(0, version)).apply();
    }

    public boolean areFirstMissionsUnlocked() {
        return prefs.getBoolean(KEY_FIRST_MISSIONS_UNLOCKED, false);
    }

    public int getUnlockedMapTierByCareer() {
        int level = getPlayerLevel();
        if (level >= 8) return 4;
        if (level >= 5) return 3;
        if (level >= 3) return 2;
        return 1;
    }

    public boolean isMapUnlockedByCareer(int map) {
        int tier = getUnlockedMapTierByCareer();
        if (map <= 0) return true;
        if (map == 1) return tier >= 2;
        if (map == 2) return tier >= 3;
        if (map == 3) return tier >= 4;
        // A62.8: Açık dünya GLB dosyası kaldırıldığı için kariyer seviyesi olsa bile kapalı kalır.
        if (map == 4) return false;
        // 2. yeni harita dosya beklediği için pasif kalır.
        if (map == 5) return false;
        return false;
    }

    public int getMapRequiredLevel(int map) {
        if (map == 4) return 1;
        if (map == 1) return 3;
        if (map == 2) return 5;
        if (map == 3) return 8;
        if (map == 4) return 1;
        if (map == 5) return 1;
        return 1;
    }

    public boolean completeCareerStart(int starterIndex) {
        int safeIndex = clamp(starterIndex, 0, 7);
        SharedPreferences.Editor e = prefs.edit();
        e.putBoolean(KEY_CAREER_STARTED, true);
        e.putInt(KEY_CAREER_STARTER_INDEX, safeIndex);
        e.putInt(KEY_SELECTED_VEHICLE_INDEX, safeIndex);
        e.putInt(KEY_CAREER_SYNC_VERSION, 1);
        e.putInt(KEY_PLAYER_LEVEL, 1);
        e.putInt(KEY_PLAYER_XP, 0);
        e.putInt(KEY_COINS, CAREER_START_COINS);
        e.putInt(KEY_CAREER_TOTAL_XP, 0);
        e.putInt(KEY_CAREER_TOTAL_RACES, 0);
        e.putInt(KEY_CAREER_TOTAL_WINS, 0);
        e.putInt(KEY_CAREER_TOTAL_LOSSES, 0);
        e.putInt(KEY_CAREER_TOTAL_EARNED, 0);
        e.putInt(KEY_CAREER_GOLD, 0);
        e.putInt(KEY_CAREER_SILVER, 0);
        e.putInt(KEY_CAREER_BRONZE, 0);
        e.putString(KEY_CAREER_LAST_MESSAGE, "Kariyer basladi: " + CareerLeagueSystem.leagueName(0));
        e.putInt(KEY_CAREER_LAST_LEVEL_REWARD, 0);
        e.putInt(KEY_REWARD_CRATES, 0);
        e.putInt(KEY_OPENED_CRATES, 0);
        e.putBoolean(KEY_FIRST_MISSIONS_UNLOCKED, true);
        e.putInt(KEY_QUEST_CHAIN_STEP, 0);
        e.putInt(KEY_QUEST_CHAIN_COMPLETED, 0);
        e.putString(KEY_QUEST_CHAIN_MESSAGE, "");
        e.putInt(KEY_UNLOCKED_PAINT_COUNT, 2);
        e.putInt(KEY_UNLOCKED_RIM_COUNT, 2);
        e.putInt(KEY_UNLOCKED_PART_TIER, 1);
        e.putString(KEY_ECONOMY_LAST_MESSAGE, "");
        e.putInt(KEY_LAST_PENALTY_AMOUNT, 0);
        e.putInt(KEY_TEST_DRIVE_CHALLENGE_INDEX, 0);
        e.putString(KEY_TEST_DRIVE_LAST_RESULT, "");
        e.putInt(KEY_TEST_DRIVE_LAST_REWARD, 0);
        e.putInt("active_mission", 0);
        e.putInt("selected_map", 0);
        e.putBoolean(keyOwned(VehicleCatalog.id(safeIndex)), true);
        e.apply();
        return true;
    }

    public void ensureQuestChainStarted() {
        if (!prefs.contains(KEY_QUEST_CHAIN_STEP)) {
            prefs.edit()
                    .putInt(KEY_QUEST_CHAIN_STEP, 0)
                    .putInt(KEY_QUEST_CHAIN_COMPLETED, 0)
                    .putString(KEY_QUEST_CHAIN_MESSAGE, "")
                    .apply();
        }
    }

    public int getQuestChainStep() {
        ensureQuestChainStarted();
        return clamp(prefs.getInt(KEY_QUEST_CHAIN_STEP, 0), 0, 7);
    }

    public void setQuestChainStep(int step) {
        prefs.edit().putInt(KEY_QUEST_CHAIN_STEP, clamp(step, 0, 7)).apply();
    }

    public int getQuestChainCompletedCount() {
        ensureQuestChainStarted();
        return clamp(prefs.getInt(KEY_QUEST_CHAIN_COMPLETED, 0), 0, 7);
    }

    public void setQuestChainCompletedCount(int count) {
        prefs.edit().putInt(KEY_QUEST_CHAIN_COMPLETED, clamp(count, 0, 7)).apply();
    }

    public String getQuestChainLastMessage() {
        ensureQuestChainStarted();
        return prefs.getString(KEY_QUEST_CHAIN_MESSAGE, "");
    }

    public void setQuestChainLastMessage(String message) {
        prefs.edit().putString(KEY_QUEST_CHAIN_MESSAGE, message == null ? "" : message).apply();
    }

    public boolean isVehicleOwned(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return false;
        return prefs.getBoolean(keyOwned(vehicleId), false);
    }

    public void setVehicleOwned(String vehicleId, boolean owned) {
        if (vehicleId == null || vehicleId.length() == 0) return;
        prefs.edit().putBoolean(keyOwned(vehicleId), owned).apply();
    }

    public boolean buyVehicle(String vehicleId, int price) {
        if (vehicleId == null || vehicleId.length() == 0) return false;
        if (isVehicleOwned(vehicleId)) return true;
        int index = findVehicleIndex(vehicleId);
        if (index >= 0 && !isVehicleLevelUnlocked(index)) {
            setEconomyLastMessage("Araç için LVL " + getVehicleRequiredLevel(index) + " gerekli");
            return false;
        }
        if (!spendCoins(Math.max(0, price))) return false;
        setVehicleOwned(vehicleId, true);
        if (index >= 0) prefs.edit().putInt(KEY_SELECTED_VEHICLE_INDEX, index).apply();
        return true;
    }

    public int getUpgradeLevel(String vehicleId, int type) {
        if (vehicleId == null) return 0;
        return clamp(prefs.getInt(keyUpgrade(vehicleId, type), 0), 0, MAX_UPGRADE_LEVEL);
    }

    public int getUpgradeCost(String vehicleId, int type) {
        int level = getUpgradeLevel(vehicleId, type);
        // A66.6: fiyat dengesi merkezi performans denge sisteminden okunur.
        // Eski kayıt seviyesi korunur; sadece sonraki satın alma maliyeti profesyonel dengeye çekilir.
        return com.arabaoyunu.performance.PerformanceUpgradeBalanceSystem.balancedCost(type, level);
    }

    public int getTotalPerformanceUpgradeLevel(String vehicleId) {
        if (vehicleId == null) return 0;
        int total = 0;
        for (int i = 0; i < PERFORMANCE_UPGRADE_COUNT; i++) {
            total += getUpgradeLevel(vehicleId, i);
        }
        return total;
    }

    public int getTotalPerformanceUpgradeLevelAllVehicles() {
        int total = 0;
        for (int v = 0; v < VehicleCatalog.count(); v++) {
            total += getTotalPerformanceUpgradeLevel(VehicleCatalog.id(v));
        }
        return Math.max(0, total);
    }

    public boolean upgradeVehicle(String vehicleId, int type) {
        if (vehicleId == null || vehicleId.length() == 0) return false;
        if (!isVehicleOwned(vehicleId)) return false;
        int level = getUpgradeLevel(vehicleId, type);
        if (level >= MAX_UPGRADE_LEVEL) return false;
        if (level >= getUnlockedPartTier()) return false;
        int cost = getUpgradeCost(vehicleId, type);
        if (!spendCoins(cost)) return false;
        prefs.edit().putInt(keyUpgrade(vehicleId, type), level + 1).apply();
        return true;
    }


    public int getPaintPreset(String vehicleId) {
        if (vehicleId == null) return 0;
        return clamp(prefs.getInt("vehicle_paint_" + vehicleId, 0), 0, 5);
    }

    public int cyclePaintPreset(String vehicleId) {
        if (vehicleId == null) return 0;
        int max = Math.max(1, getUnlockedPaintCount());
        int next = (getPaintPreset(vehicleId) + 1) % max;
        prefs.edit().putInt("vehicle_paint_" + vehicleId, next).apply();
        return next;
    }

    public int getRimPreset(String vehicleId) {
        if (vehicleId == null) return 0;
        return clamp(prefs.getInt("vehicle_rim_" + vehicleId, 0), 0, 4);
    }

    public int cycleRimPreset(String vehicleId) {
        if (vehicleId == null) return 0;
        int max = Math.max(1, getUnlockedRimCount());
        int next = (getRimPreset(vehicleId) + 1) % max;
        prefs.edit().putInt("vehicle_rim_" + vehicleId, next).apply();
        return next;
    }

    public void setRimPreset(String vehicleId, int value) {
        if (vehicleId == null) return;
        prefs.edit().putInt("vehicle_rim_" + vehicleId, clamp(value, 0, 4)).apply();
    }

    public void setPaintPreset(String vehicleId, int value) {
        if (vehicleId == null) return;
        prefs.edit().putInt("vehicle_paint_" + vehicleId, clamp(value, 0, 5)).apply();
    }

    public String getPlateCode(String vehicleId) {
        if (vehicleId == null) return "RAYZ-000";
        return prefs.getString("vehicle_plate_" + vehicleId, "RAYZ-" + Math.abs(vehicleId.hashCode() % 900 + 100));
    }

    public String cyclePlateCode(String vehicleId) {
        if (vehicleId == null) return "RAYZ-000";
        int current = prefs.getInt("vehicle_plate_i_" + vehicleId, 0);
        int next = (current + 1) % 900;
        String code = "RAYZ-" + (100 + next);
        prefs.edit()
                .putInt("vehicle_plate_i_" + vehicleId, next)
                .putString("vehicle_plate_" + vehicleId, code)
                .apply();
        return code;
    }




    public int getUnlockedPaintCount() {
        return clamp(prefs.getInt(KEY_UNLOCKED_PAINT_COUNT, 2), 1, 6);
    }

    public int unlockNextPaintPreset() {
        int next = Math.min(6, getUnlockedPaintCount() + 1);
        prefs.edit().putInt(KEY_UNLOCKED_PAINT_COUNT, next).apply();
        return next;
    }

    public int getUnlockedRimCount() {
        return clamp(prefs.getInt(KEY_UNLOCKED_RIM_COUNT, 2), 1, 5);
    }

    public int unlockNextRimPreset() {
        int next = Math.min(5, getUnlockedRimCount() + 1);
        prefs.edit().putInt(KEY_UNLOCKED_RIM_COUNT, next).apply();
        return next;
    }

    public int getUnlockedPartTier() {
        return clamp(prefs.getInt(KEY_UNLOCKED_PART_TIER, 1), 1, MAX_UPGRADE_LEVEL);
    }

    public int unlockNextPartTier() {
        int next = Math.min(MAX_UPGRADE_LEVEL, getUnlockedPartTier() + 1);
        prefs.edit().putInt(KEY_UNLOCKED_PART_TIER, next).apply();
        return next;
    }

    public boolean isVehicleRewardUnlocked(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return false;
        return prefs.getBoolean("vehicle_reward_unlocked_" + vehicleId, false);
    }

    public void setVehicleRewardUnlocked(String vehicleId, boolean unlocked) {
        if (vehicleId == null || vehicleId.length() == 0) return;
        prefs.edit().putBoolean("vehicle_reward_unlocked_" + vehicleId, unlocked).apply();
    }

    public String getEconomyLastMessage() {
        return prefs.getString(KEY_ECONOMY_LAST_MESSAGE, "");
    }

    public void setEconomyLastMessage(String message) {
        prefs.edit().putString(KEY_ECONOMY_LAST_MESSAGE, message == null ? "" : message).apply();
    }

    public int getLastPenaltyAmount() {
        return Math.max(0, prefs.getInt(KEY_LAST_PENALTY_AMOUNT, 0));
    }

    public void setLastPenaltyAmount(int amount) {
        prefs.edit().putInt(KEY_LAST_PENALTY_AMOUNT, Math.max(0, amount)).apply();
    }

    public int getDetailedTuningValue(String vehicleId, int tuningType) {
        if (vehicleId == null || vehicleId.length() == 0) return getDefaultDetailedTuningValue(tuningType);
        int def = getDefaultDetailedTuningValue(tuningType);
        int max = tuningType >= 9 ? 1 : 100;
        return clamp(prefs.getInt(keyDetailedTuning(vehicleId, tuningType), def), 0, max);
    }

    public void setDetailedTuningValue(String vehicleId, int tuningType, int value) {
        if (vehicleId == null || vehicleId.length() == 0) return;
        int max = tuningType >= 9 ? 1 : 100;
        prefs.edit().putInt(keyDetailedTuning(vehicleId, tuningType), clamp(value, 0, max)).apply();
    }

    public int cycleDetailedTuningValue(String vehicleId, int tuningType) {
        int current = getDetailedTuningValue(vehicleId, tuningType);
        if (tuningType >= 9) {
            int next = current > 0 ? 0 : 1;
            setDetailedTuningValue(vehicleId, tuningType, next);
            return next;
        }
        int next = current + 10;
        if (next > 100) next = 0;
        setDetailedTuningValue(vehicleId, tuningType, next);
        return next;
    }

    public int getTuningPreset(String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return clamp(prefs.getInt("vehicle_tuning_preset_" + vehicleId, 0), 0, 3);
    }

    public void setTuningPreset(String vehicleId, int preset) {
        if (vehicleId == null || vehicleId.length() == 0) return;
        prefs.edit().putInt("vehicle_tuning_preset_" + vehicleId, clamp(preset, 0, 3)).apply();
    }

    private int getDefaultDetailedTuningValue(int tuningType) {
        if (tuningType == 2) return 55; // fren dengesi
        if (tuningType == 9 || tuningType == 10 || tuningType == 11) return 1; // TCS/ABS/ESP açık
        return 50;
    }

    private static String keyDetailedTuning(String vehicleId, int tuningType) {
        return "vehicle_detail_tuning_" + vehicleId + "_" + tuningType;
    }

    public int getVisualCustomizationSchemaVersion() {
        return Math.max(0, prefs.getInt(KEY_VISUAL_CUSTOMIZATION_SCHEMA, 0));
    }

    public void markVisualCustomizationSchemaCurrent(int version) {
        prefs.edit().putInt(KEY_VISUAL_CUSTOMIZATION_SCHEMA, Math.max(0, version)).apply();
    }

    public void markGarageShowroomSchemaCurrent(int version) {
        prefs.edit().putInt(KEY_GARAGE_SHOWROOM_SCHEMA, Math.max(0, version)).apply();
    }

    public int getGarageShowroomSchemaVersion() {
        return prefs.getInt(KEY_GARAGE_SHOWROOM_SCHEMA, 0);
    }


    public int getVisualModValue(String vehicleId, int visualType) {
        if (vehicleId == null || vehicleId.length() == 0) return 0;
        return clamp(prefs.getInt(keyVisualMod(vehicleId, visualType), 0), 0, getVisualModMax(visualType));
    }

    public void setVisualModValue(String vehicleId, int visualType, int value) {
        if (vehicleId == null || vehicleId.length() == 0) return;
        int safe = clamp(value, 0, getVisualModMax(visualType));
        prefs.edit()
                .putInt(keyVisualMod(vehicleId, visualType), safe)
                .putBoolean(keyVisualOptionOwned(vehicleId, visualType, safe), true)
                .apply();
    }

    public boolean isVisualModOptionOwned(String vehicleId, int visualType, int value) {
        if (vehicleId == null || vehicleId.length() == 0) return value == 0;
        int safe = clamp(value, 0, getVisualModMax(visualType));
        if (safe == 0) return true;
        if (getVisualModValue(vehicleId, visualType) == safe) return true; // Eski kayıtlarla uyumluluk.
        return prefs.getBoolean(keyVisualOptionOwned(vehicleId, visualType, safe), false);
    }

    public boolean buyVisualModOption(String vehicleId, int visualType, int value, int cost) {
        if (vehicleId == null || vehicleId.length() == 0) return false;
        int safe = clamp(value, 0, getVisualModMax(visualType));
        if (safe == 0 || isVisualModOptionOwned(vehicleId, visualType, safe)) return true;
        if (!spendCoins(Math.max(0, cost))) return false;
        prefs.edit().putBoolean(keyVisualOptionOwned(vehicleId, visualType, safe), true).apply();
        return true;
    }

    public int getOwnedVisualOptionCount(String vehicleId, int visualType) {
        int max = getVisualModMax(visualType);
        int count = 1;
        for (int i = 1; i <= max; i++) {
            if (isVisualModOptionOwned(vehicleId, visualType, i)) count++;
        }
        return count;
    }

    public int cycleVisualModValue(String vehicleId, int visualType) {
        int max = getVisualModMax(visualType);
        int next = getVisualModValue(vehicleId, visualType) + 1;
        if (next > max) next = 0;
        setVisualModValue(vehicleId, visualType, next);
        return next;
    }

    public boolean isRedeemCodeUsed(String normalizedCode) {
        if (normalizedCode == null || normalizedCode.length() == 0) return false;
        return prefs.getBoolean("redeem_used_" + normalizedCode, false);
    }

    public void setRedeemCodeUsed(String normalizedCode, boolean used) {
        if (normalizedCode == null || normalizedCode.length() == 0) return;
        prefs.edit().putBoolean("redeem_used_" + normalizedCode, used).apply();
    }

    private int getVisualModMax(int visualType) {
        switch (visualType) {
            case 0: return 2;
            case 1: return 5;
            case 2: return 6;
            case 3: return 5;
            case 4: return 5;
            case 5: return 4;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12: return 4;
            case 13: return 5;
            case 14: return 5;
            case 15: return 5;
            case 16: return 6;
            case 17: return 4;
            case 18: return 4;
            case 19: return 9;
            default: return 3;
        }
    }

    private static String keyVisualMod(String vehicleId, int visualType) {
        return "vehicle_visual_mod_" + vehicleId + "_" + visualType;
    }

    private static String keyVisualOptionOwned(String vehicleId, int visualType, int value) {
        return "vehicle_visual_mod_owned_" + vehicleId + "_" + visualType + "_" + value;
    }

    public int getTestDriveChallengeIndex() {
        return clamp(prefs.getInt(KEY_TEST_DRIVE_CHALLENGE_INDEX, 0), 0, 6);
    }

    public void setTestDriveChallengeIndex(int index) {
        prefs.edit().putInt(KEY_TEST_DRIVE_CHALLENGE_INDEX, clamp(index, 0, 6)).apply();
    }

    public String getLastTestDriveResult() {
        return prefs.getString(KEY_TEST_DRIVE_LAST_RESULT, "");
    }

    public int getLastTestDriveReward() {
        return Math.max(0, prefs.getInt(KEY_TEST_DRIVE_LAST_REWARD, 0));
    }

    public void setLastTestDriveResult(String result, int reward) {
        prefs.edit()
                .putString(KEY_TEST_DRIVE_LAST_RESULT, result == null ? "" : result)
                .putInt(KEY_TEST_DRIVE_LAST_REWARD, Math.max(0, reward))
                .apply();
    }

    public int getTotalEarnedCoins() {
        return Math.max(0, prefs.getInt(KEY_TOTAL_EARNED_COINS, 0));
    }

    public void addTotalEarnedCoins(int amount) {
        if (amount <= 0) return;
        prefs.edit().putInt(KEY_TOTAL_EARNED_COINS, Math.max(0, getTotalEarnedCoins() + amount)).apply();
    }

    public float getBestSpeedKmh() {
        return Math.max(0f, prefs.getFloat(KEY_BEST_SPEED_KMH, 0f));
    }

    public void updateBestSpeedKmh(float speedKmh) {
        if (speedKmh <= getBestSpeedKmh()) return;
        prefs.edit().putFloat(KEY_BEST_SPEED_KMH, Math.max(0f, speedKmh)).apply();
    }

    public int getDrivingMissionIndex() {
        return Math.max(0, prefs.getInt(KEY_DRIVING_MISSION_INDEX, 0));
    }

    public void setDrivingMissionIndex(int index) {
        prefs.edit().putInt(KEY_DRIVING_MISSION_INDEX, Math.max(0, index)).apply();
    }

    public float getDrivingMissionProgress() {
        return Math.max(0f, prefs.getFloat(KEY_DRIVING_MISSION_PROGRESS, 0f));
    }

    public void setDrivingMissionProgress(float progress) {
        prefs.edit().putFloat(KEY_DRIVING_MISSION_PROGRESS, Math.max(0f, progress)).apply();
    }

    public int getDrivingMissionCompletedCount() {
        return Math.max(0, prefs.getInt(KEY_DRIVING_MISSION_COMPLETED, 0));
    }

    public void incrementDrivingMissionCompleted() {
        prefs.edit().putInt(KEY_DRIVING_MISSION_COMPLETED, Math.max(0, getDrivingMissionCompletedCount() + 1)).apply();
    }

    public int getNitroBonusPacks() {
        return Math.max(0, prefs.getInt(KEY_NITRO_BONUS_PACKS, 0));
    }

    public void addNitroBonusPacks(int amount) {
        if (amount <= 0) return;
        prefs.edit().putInt(KEY_NITRO_BONUS_PACKS, Math.max(0, getNitroBonusPacks() + amount)).apply();
    }

    public int getGarageDiscountTokens() {
        return Math.max(0, prefs.getInt(KEY_GARAGE_DISCOUNT_TOKENS, 0));
    }

    public void addGarageDiscountTokens(int amount) {
        if (amount <= 0) return;
        prefs.edit().putInt(KEY_GARAGE_DISCOUNT_TOKENS, Math.max(0, getGarageDiscountTokens() + amount)).apply();
    }

    public boolean isDailyRewardAvailable() {
        return prefs.getLong(KEY_DAILY_REWARD_LAST_DAY, -1L) != currentEpochDay();
    }

    public int getDailyRewardDayIndex() {
        long today = currentEpochDay();
        long last = prefs.getLong(KEY_DAILY_REWARD_LAST_DAY, -1L);
        int lastIndex = clamp(prefs.getInt(KEY_DAILY_REWARD_DAY_INDEX, -1), -1, 6);
        if (last == today) return clamp(lastIndex, 0, 6);
        if (last == today - 1L && lastIndex >= 0) return (lastIndex + 1) % 7;
        return 0;
    }

    public int getDailyRewardTotalClaims() {
        return Math.max(0, prefs.getInt(KEY_DAILY_REWARD_TOTAL_CLAIMS, 0));
    }

    public long getDailyRewardLastDay() {
        return prefs.getLong(KEY_DAILY_REWARD_LAST_DAY, -1L);
    }

    public boolean claimDailyReward(int rewardDayIndex, int coins, int nitroPacks, int discountTokens) {
        if (!isDailyRewardAvailable()) return false;
        int safeDay = clamp(rewardDayIndex, 0, 6);
        SharedPreferences.Editor editor = prefs.edit()
                .putLong(KEY_DAILY_REWARD_LAST_DAY, currentEpochDay())
                .putInt(KEY_DAILY_REWARD_DAY_INDEX, safeDay)
                .putInt(KEY_DAILY_REWARD_TOTAL_CLAIMS, getDailyRewardTotalClaims() + 1);
        if (coins > 0) {
            editor.putInt(KEY_COINS, Math.max(0, getCoins() + coins));
            editor.putInt(KEY_TOTAL_EARNED_COINS, Math.max(0, getTotalEarnedCoins() + coins));
        }
        if (nitroPacks > 0) editor.putInt(KEY_NITRO_BONUS_PACKS, Math.max(0, getNitroBonusPacks() + nitroPacks));
        if (discountTokens > 0) editor.putInt(KEY_GARAGE_DISCOUNT_TOKENS, Math.max(0, getGarageDiscountTokens() + discountTokens));
        editor.apply();
        return true;
    }


    public int getUiBalanceVersion() {
        return Math.max(0, prefs.getInt(KEY_UI_BALANCE_VERSION, 626));
    }

    public boolean isHudCompactEnabled() {
        return prefs.getBoolean(KEY_HUD_COMPACT, true);
    }

    public void setHudCompactEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HUD_COMPACT, enabled)
                .putString(KEY_ECONOMY_LAST_MESSAGE, enabled ? "HUD sade moda alındı" : "HUD gelişmiş moda alındı")
                .apply();
    }

    public void toggleHudCompact() {
        setHudCompactEnabled(!isHudCompactEnabled());
    }

    public boolean isVibrationEnabled() {
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled)
                .putString(KEY_ECONOMY_LAST_MESSAGE, enabled ? "Titreşim açıldı" : "Titreşim kapatıldı")
                .apply();
    }

    public void toggleVibrationEnabled() {
        setVibrationEnabled(!isVibrationEnabled());
    }

    public int getControlSensitivityPreset() {
        return clamp(prefs.getInt(KEY_CONTROL_SENSITIVITY, 1), 0, 4);
    }

    public void cycleControlSensitivityPreset() {
        int next = getControlSensitivityPreset() + 1;
        if (next > 4) next = 0;
        prefs.edit().putInt(KEY_CONTROL_SENSITIVITY, next)
                .putString(KEY_ECONOMY_LAST_MESSAGE, "Kontrol hassasiyeti: " + controlSensitivityLabel(next))
                .apply();
    }

    public String getControlSensitivityLabel() {
        return controlSensitivityLabel(getControlSensitivityPreset());
    }

    private static String controlSensitivityLabel(int preset) {
        if (preset <= 0) return "Yumuşak";
        if (preset == 2) return "Sport";
        if (preset == 3) return "Drift";
        if (preset >= 4) return "Drag";
        return "Dengeli";
    }

    public int getControlLayoutPreset() { return clamp(prefs.getInt(KEY_CONTROL_LAYOUT_PRESET, 0), 0, 3); }
    public void cycleControlLayoutPreset() {
        int next = (getControlLayoutPreset() + 1) % 4;
        prefs.edit().putInt(KEY_CONTROL_LAYOUT_PRESET, next)
                .putString(KEY_ECONOMY_LAST_MESSAGE, "Kontrol düzeni: " + getControlLayoutLabel(next))
                .apply();
    }
    public String getControlLayoutLabel() { return getControlLayoutLabel(getControlLayoutPreset()); }
    private static String getControlLayoutLabel(int preset) {
        if (preset == 1) return "Sol direksiyon";
        if (preset == 2) return "Sağ direksiyon";
        if (preset == 3) return "Tablet geniş";
        return "Klasik";
    }

    public int getPedalSizePreset() { return clamp(prefs.getInt(KEY_PEDAL_SIZE_PRESET, 1), 0, 3); }
    public void cyclePedalSizePreset() {
        int next = (getPedalSizePreset() + 1) % 4;
        prefs.edit().putInt(KEY_PEDAL_SIZE_PRESET, next)
                .putString(KEY_ECONOMY_LAST_MESSAGE, "Pedal boyutu: " + getPedalSizeLabel(next))
                .apply();
    }
    public String getPedalSizeLabel() { return getPedalSizeLabel(getPedalSizePreset()); }
    private static String getPedalSizeLabel(int preset) {
        if (preset == 0) return "Küçük";
        if (preset == 2) return "Büyük";
        if (preset == 3) return "Tablet";
        return "Normal";
    }

    public int getHudPreset() { return clamp(prefs.getInt(KEY_HUD_PRESET, 0), 0, 3); }
    public void cycleHudPreset() {
        int next = (getHudPreset() + 1) % 4;
        prefs.edit().putInt(KEY_HUD_PRESET, next)
                .putBoolean(KEY_HUD_COMPACT, next == 0)
                .putString(KEY_ECONOMY_LAST_MESSAGE, "HUD düzeni: " + getHudPresetLabel(next))
                .apply();
    }
    public String getHudPresetLabel() { return getHudPresetLabel(getHudPreset()); }
    private static String getHudPresetLabel(int preset) {
        if (preset == 1) return "Gelişmiş";
        if (preset == 2) return "Yarış";
        if (preset == 3) return "Drift";
        return "Sade";
    }

    public int getButtonOpacityPercent() { return clamp(prefs.getInt(KEY_BUTTON_OPACITY, 80), 40, 100); }
    public void cycleButtonOpacityPercent() {
        int current = getButtonOpacityPercent();
        int next = current < 60 ? 60 : (current < 80 ? 80 : (current < 100 ? 100 : 40));
        prefs.edit().putInt(KEY_BUTTON_OPACITY, next)
                .putString(KEY_ECONOMY_LAST_MESSAGE, "Buton opaklığı: %" + next)
                .apply();
    }

    public boolean isLeftHandedModeEnabled() { return prefs.getBoolean(KEY_LEFT_HANDED, false); }
    public void toggleLeftHandedMode() {
        boolean next = !isLeftHandedModeEnabled();
        prefs.edit().putBoolean(KEY_LEFT_HANDED, next)
                .putString(KEY_ECONOMY_LAST_MESSAGE, next ? "Solak mod açıldı" : "Solak mod kapatıldı")
                .apply();
    }

    public boolean isAutoControlByModeEnabled() { return prefs.getBoolean(KEY_AUTO_CONTROL, true); }
    public void toggleAutoControlByMode() {
        boolean next = !isAutoControlByModeEnabled();
        prefs.edit().putBoolean(KEY_AUTO_CONTROL, next)
                .putString(KEY_ECONOMY_LAST_MESSAGE, next ? "Modlara göre otomatik kontrol açık" : "Otomatik kontrol kapalı")
                .apply();
    }

    public boolean isTaskAchievementHudEnabled() {
        return prefs.getBoolean(KEY_TASK_HUD_ENABLED, true);
    }

    public void toggleTaskAchievementHudEnabled() {
        boolean next = !isTaskAchievementHudEnabled();
        prefs.edit().putBoolean(KEY_TASK_HUD_ENABLED, next)
                .putString(KEY_ECONOMY_LAST_MESSAGE, next ? "Görev HUD açıldı" : "Görev HUD kapatıldı")
                .apply();
    }

    public int getTaskAchievementNotificationMode() {
        return clamp(prefs.getInt(KEY_TASK_NOTIFICATION_MODE, 1), 0, 2);
    }

    public void cycleTaskAchievementNotificationMode() {
        int next = (getTaskAchievementNotificationMode() + 1) % 3;
        prefs.edit().putInt(KEY_TASK_NOTIFICATION_MODE, next)
                .putString(KEY_ECONOMY_LAST_MESSAGE, "Görev bildirimi: " + getTaskAchievementNotificationModeLabel(next))
                .apply();
    }

    public String getTaskAchievementNotificationModeLabel() {
        return getTaskAchievementNotificationModeLabel(getTaskAchievementNotificationMode());
    }

    private static String getTaskAchievementNotificationModeLabel(int mode) {
        if (mode <= 0) return "Minimal";
        if (mode >= 2) return "Detaylı";
        return "Normal";
    }

    public boolean isRewardPopupEnabled() {
        return prefs.getBoolean(KEY_REWARD_POPUP_ENABLED, true);
    }

    public void toggleRewardPopupEnabled() {
        boolean next = !isRewardPopupEnabled();
        prefs.edit().putBoolean(KEY_REWARD_POPUP_ENABLED, next)
                .putString(KEY_ECONOMY_LAST_MESSAGE, next ? "Ödül popup açıldı" : "Ödül popup kapatıldı")
                .apply();
    }


    private static long currentEpochDay() {
        return System.currentTimeMillis() / 86400000L;
    }

    public int getSelectedMap() {
        int map = prefs.getInt("selected_map", 0);
        if (map < 0 || map > 5) return 0;
        // A61_6: Açık Dünya ve 2. yeni harita GLB slotları geçici kapalıdır.
        if (map == 4 || map == 5) return 0;
        return map;
    }

    public void setSelectedMap(int map) {
        if (map < 0) map = 0;
        if (map > 5) map = 5;
        // A61_6: Pasif GLB harita slotları kalıcı seçime yazılmaz.
        if (map == 4 || map == 5) map = 0;
        prefs.edit().putInt("selected_map", map).apply();
    }

    public int getActiveMission() {
        return prefs.getInt("active_mission", 0);
    }

    public void setActiveMission(int mission) {
        if (mission < 0) mission = 0;
        prefs.edit().putInt("active_mission", mission).apply();
    }

    public int getDailyMissionCompletedCount() {
        resetDailyIfNeeded();
        return prefs.getInt("daily_mission_count", 0);
    }

    public void incrementDailyMissionCompleted() {
        resetDailyIfNeeded();
        int current = prefs.getInt("daily_mission_count", 0);
        prefs.edit().putInt("daily_mission_count", Math.min(99, current + 1)).apply();
    }

    private void resetDailyIfNeeded() {
        long today = System.currentTimeMillis() / 86400000L;
        long saved = prefs.getLong("daily_mission_day", -1L);
        if (saved != today) {
            prefs.edit()
                    .putLong("daily_mission_day", today)
                    .putInt("daily_mission_count", 0)
                    .apply();
        }
    }

    public float getVehicleHealth(String vehicleId) {
        if (vehicleId == null) return 1f;
        return clampFloat(prefs.getFloat("vehicle_health_" + vehicleId, 1f), 0f, 1f);
    }

    public float getVehicleMotorDamage(String vehicleId) {
        if (vehicleId == null) return 0f;
        return clampFloat(prefs.getFloat("vehicle_motor_damage_" + vehicleId, 0f), 0f, 1f);
    }

    public float getVehicleTireDamage(String vehicleId) {
        if (vehicleId == null) return 0f;
        return clampFloat(prefs.getFloat("vehicle_tire_damage_" + vehicleId, 0f), 0f, 1f);
    }

    public float getVehicleGlassDamage(String vehicleId) {
        if (vehicleId == null) return 0f;
        return clampFloat(prefs.getFloat("vehicle_glass_damage_" + vehicleId, 0f), 0f, 1f);
    }

    public float getVehicleBodyDamage(String vehicleId) {
        if (vehicleId == null) return 0f;
        return clampFloat(prefs.getFloat("vehicle_body_damage_" + vehicleId, 0f), 0f, 1f);
    }

    public void saveVehicleDamage(String vehicleId, float health, float motor, float tire, float glass, float body) {
        if (vehicleId == null || vehicleId.length() == 0) return;
        prefs.edit()
                .putFloat("vehicle_health_" + vehicleId, clampFloat(health, 0f, 1f))
                .putFloat("vehicle_motor_damage_" + vehicleId, clampFloat(motor, 0f, 1f))
                .putFloat("vehicle_tire_damage_" + vehicleId, clampFloat(tire, 0f, 1f))
                .putFloat("vehicle_glass_damage_" + vehicleId, clampFloat(glass, 0f, 1f))
                .putFloat("vehicle_body_damage_" + vehicleId, clampFloat(body, 0f, 1f))
                .apply();
    }

    public int getRepairCost(String vehicleId) {
        if (vehicleId == null) return 0;
        float damage =
                (1f - getVehicleHealth(vehicleId)) * 1.25f
                        + getVehicleMotorDamage(vehicleId) * 0.95f
                        + getVehicleTireDamage(vehicleId) * 0.70f
                        + getVehicleGlassDamage(vehicleId) * 0.45f
                        + getVehicleBodyDamage(vehicleId) * 0.60f;
        return Math.max(0, Math.round(damage * 1250f));
    }

    public boolean repairVehicle(String vehicleId) {
        int cost = getRepairCost(vehicleId);
        if (cost <= 0) {
            saveVehicleDamage(vehicleId, 1f, 0f, 0f, 0f, 0f);
            return true;
        }
        if (!spendCoins(cost)) return false;
        saveVehicleDamage(vehicleId, 1f, 0f, 0f, 0f, 0f);
        return true;
    }

    /**
     * A62.8: Eski sürümden gelen veya yarım yazılmış kayıtları güvenli hale getirir.
     * load -> validate -> repair -> save akışıyla oyun açılışında çökme/negatif para/
     * sahip olunmayan seçili araç gibi durumları otomatik düzeltir.
     */
    public void validateAndRepairState() {
        SharedPreferences.Editor e = prefs.edit();
        boolean dirty = false;
        int a671RepairCount = 0;
        int a672FlowRepairCount = 0;
        int a673UiRepairCount = 0;
        int vehicleCount = Math.max(1, VehicleCatalog.count());
        int maxVehicle = vehicleCount - 1;

        int rawCoins = prefs.getInt(KEY_COINS, CAREER_START_COINS);
        if (rawCoins < 0) { e.putInt(KEY_COINS, 0); dirty = true; }

        int rawLevel = prefs.getInt(KEY_PLAYER_LEVEL, 1);
        if (rawLevel < 1) { e.putInt(KEY_PLAYER_LEVEL, 1); dirty = true; rawLevel = 1; }
        int nextXp = Math.max(1, CareerLeagueSystem.xpForNextLevel(Math.max(1, rawLevel)));
        int rawXp = prefs.getInt(KEY_PLAYER_XP, 0);
        if (rawXp < 0) { e.putInt(KEY_PLAYER_XP, 0); dirty = true; }
        if (rawXp > nextXp * 4) { e.putInt(KEY_PLAYER_XP, nextXp - 1); dirty = true; }

        int starter = clamp(prefs.getInt(KEY_CAREER_STARTER_INDEX, 0), 0, maxVehicle);
        if (prefs.getInt(KEY_CAREER_STARTER_INDEX, 0) != starter) {
            e.putInt(KEY_CAREER_STARTER_INDEX, starter);
            dirty = true;
        }
        String starterId = VehicleCatalog.id(starter);
        if (!prefs.getBoolean(keyOwned(starterId), false)) {
            e.putBoolean(keyOwned(starterId), true);
            dirty = true;
        }

        int selected = clamp(prefs.getInt(KEY_SELECTED_VEHICLE_INDEX, starter), 0, maxVehicle);
        if (!prefs.getBoolean(keyOwned(VehicleCatalog.id(selected)), false)) selected = starter;
        if (prefs.getInt(KEY_SELECTED_VEHICLE_INDEX, starter) != selected) {
            e.putInt(KEY_SELECTED_VEHICLE_INDEX, selected);
            dirty = true;
            a671RepairCount++;
            a672FlowRepairCount++;
        }

        // A67.1: Garaj/modifiye genel stabilite. Eski kayıtlar kariyer ya da
        // garaj dışı akıştan geldiğinde aktif kariyer/araç indexleri güvenli
        // aralıkta tutulur; kilitli araç sürüşe aktarılmaz.
        int rawActiveLeague = prefs.getInt(KEY_ACTIVE_CAREER_LEAGUE, 0);
        int safeActiveLeague = CareerEventSystem.safeLeague(rawActiveLeague);
        if (rawActiveLeague != safeActiveLeague) { e.putInt(KEY_ACTIVE_CAREER_LEAGUE, safeActiveLeague); dirty = true; a671RepairCount++; }
        int rawActiveEvent = prefs.getInt(KEY_ACTIVE_CAREER_EVENT, 0);
        int safeActiveEvent = CareerEventSystem.safeEvent(rawActiveEvent);
        if (rawActiveEvent != safeActiveEvent) { e.putInt(KEY_ACTIVE_CAREER_EVENT, safeActiveEvent); dirty = true; a671RepairCount++; }
        int rawActiveMode = prefs.getInt(KEY_ACTIVE_CAREER_MODE, 0);
        int safeActiveMode = clamp(rawActiveMode, 0, 5);
        if (rawActiveMode != safeActiveMode) { e.putInt(KEY_ACTIVE_CAREER_MODE, safeActiveMode); dirty = true; a671RepairCount++; a672FlowRepairCount++; }

        int rawMap = prefs.getInt("selected_map", 0);
        if (rawMap < 0 || rawMap > 5 || rawMap == 4 || rawMap == 5) {
            e.putInt("selected_map", 0);
            dirty = true;
            a672FlowRepairCount++;
            a673UiRepairCount++;
        }
        int rawCheckpointRoute = prefs.getInt(KEY_CHECKPOINT_SELECTED_ROUTE, CheckpointRaceSystem.ROUTE_MEDIUM);
        int safeCheckpointRoute = CheckpointRaceSystem.sanitizeRouteId(rawCheckpointRoute);
        if (rawCheckpointRoute != safeCheckpointRoute) {
            e.putInt(KEY_CHECKPOINT_SELECTED_ROUTE, safeCheckpointRoute);
            dirty = true;
            a672FlowRepairCount++;
        }

        // A65.1: rota bazlı checkpoint kayıtları negatif/bozuk kalırsa ödül ve kart ekranı bozulmasın.
        for (int route = 0; route < CheckpointRaceSystem.ROUTE_COUNT; route++) {
            repairNonNegative(e, "checkpoint_route_completed_" + route);
            repairNonNegative(e, "checkpoint_route_earned_" + route);
            repairNonNegative(e, "checkpoint_route_gold_" + route);
            repairNonNegative(e, "checkpoint_route_silver_" + route);
            repairNonNegative(e, "checkpoint_route_bronze_" + route);
            int rawRank = prefs.getInt("checkpoint_route_medal_rank_" + route, 0);
            int fixedRank = clamp(rawRank, 0, CheckpointRaceSystem.medalRank(RaceModeSystem.GRADE_GOLD));
            if (rawRank != fixedRank) { e.putInt("checkpoint_route_medal_rank_" + route, fixedRank); dirty = true; }
            float rawBest = prefs.getFloat("checkpoint_route_best_" + route, 0f);
            if (rawBest < 0f || rawBest > 9999f) { e.putFloat("checkpoint_route_best_" + route, 0f); dirty = true; }
            float rawLast = prefs.getFloat("checkpoint_route_last_time_" + route, 0f);
            if (rawLast < 0f || rawLast > 9999f) { e.putFloat("checkpoint_route_last_time_" + route, 0f); dirty = true; }
        }

        for (int i = 0; i < vehicleCount; i++) {
            String id = VehicleCatalog.id(i);
            for (int t = 0; t < PERFORMANCE_UPGRADE_COUNT; t++) {
                String key = keyUpgrade(id, t);
                int value = prefs.getInt(key, 0);
                int fixed = clamp(value, 0, MAX_UPGRADE_LEVEL);
                if (fixed != value) { e.putInt(key, fixed); dirty = true; a671RepairCount++; }
            }
            int rawPreset = prefs.getInt("vehicle_tuning_preset_" + id, 0);
            int safePreset = clamp(rawPreset, 0, 3);
            if (rawPreset != safePreset) {
                e.putInt("vehicle_tuning_preset_" + id, safePreset);
                dirty = true;
                a671RepairCount++;
            }
            // A66.1: araç/modifiye altyapısı final revizyonu. Bozuk eski kayıtlar
            // görsel/tuning ekranında taşma veya geçersiz seçenek üretmesin.
            for (int visualType = 0; visualType <= 19; visualType++) {
                String visualKey = keyVisualMod(id, visualType);
                int visualMax = getVisualModMax(visualType);
                int rawVisual = prefs.getInt(visualKey, 0);
                int safeVisual = clamp(rawVisual, 0, visualMax);
                if (rawVisual != safeVisual) { e.putInt(visualKey, safeVisual); dirty = true; a671RepairCount++; }
                if (!prefs.getBoolean(keyVisualOptionOwned(id, visualType, 0), false)) {
                    e.putBoolean(keyVisualOptionOwned(id, visualType, 0), true);
                    a671RepairCount++;
                }
                if (safeVisual > 0 && !prefs.getBoolean(keyVisualOptionOwned(id, visualType, safeVisual), false)) {
                    e.putBoolean(keyVisualOptionOwned(id, visualType, safeVisual), true);
                    a671RepairCount++;
                }
                // A66.5: Görsel modifiye kaydet/geri al derin testinde eski renderer
                // alanları ile yeni visual slotları aynı kalmalı. Özellikle jant modeli
                // GL renderer içinde hâlâ legacy rim preset olarak okunuyor.
                if (visualType == 3) {
                    int legacyRim = prefs.getInt("vehicle_rim_" + id, safeVisual);
                    int safeLegacyRim = clamp(legacyRim, 0, 4);
                    if (legacyRim != safeLegacyRim || safeLegacyRim != clamp(safeVisual, 0, 4)) {
                        e.putInt("vehicle_rim_" + id, clamp(safeVisual, 0, 4));
                        dirty = true;
                        a671RepairCount++;
                    }
                }
                if (visualType == 19) {
                    int legacyPaint = prefs.getInt("vehicle_paint_" + id, clamp(safeVisual, 0, 5));
                    int targetLegacyPaint = clamp(safeVisual, 0, 5);
                    int safeLegacyPaint = clamp(legacyPaint, 0, 5);
                    if (legacyPaint != safeLegacyPaint || safeLegacyPaint != targetLegacyPaint) {
                        e.putInt("vehicle_paint_" + id, targetLegacyPaint);
                        dirty = true;
                        a671RepairCount++;
                    }
                }
            }
            for (int tuningType = 0; tuningType <= 11; tuningType++) {
                String tuningKey = keyDetailedTuning(id, tuningType);
                int max = tuningType >= 9 ? 1 : 100;
                int def = getDefaultDetailedTuningValue(tuningType);
                int rawTuning = prefs.getInt(tuningKey, def);
                int safeTuning = clamp(rawTuning, 0, max);
                if (rawTuning != safeTuning) { e.putInt(tuningKey, safeTuning); dirty = true; a671RepairCount++; }
            }
            repairFloat(e, "vehicle_health_" + id, 1f, 0f, 1f);
            repairFloat(e, "vehicle_motor_damage_" + id, 0f, 0f, 1f);
            repairFloat(e, "vehicle_tire_damage_" + id, 0f, 0f, 1f);
            repairFloat(e, "vehicle_glass_damage_" + id, 0f, 0f, 1f);
            repairFloat(e, "vehicle_body_damage_" + id, 0f, 0f, 1f);
        }

        repairNonNegative(e, KEY_TOTAL_EARNED_COINS);
        repairNonNegative(e, KEY_CAREER_TOTAL_XP);
        repairNonNegative(e, KEY_CAREER_TOTAL_RACES);
        repairNonNegative(e, KEY_RIVAL_TOTAL_RACES);
        repairNonNegative(e, KEY_POLICE_TOTAL_CHASES);
        repairNonNegative(e, KEY_POLICE_ESCAPES);
        repairNonNegative(e, KEY_POLICE_CAUGHT);
        repairNonNegative(e, KEY_POLICE_HIGHEST_WANTED);
        repairNonNegative(e, KEY_POLICE_EARNED_COINS);
        repairNonNegative(e, KEY_POLICE_EARNED_XP);
        repairNonNegative(e, KEY_POLICE_BEST);
        repairNonNegative(e, KEY_POLICE_LAST_WANTED);
        repairNonNegative(e, KEY_POLICE_LAST_SECONDS);
        repairNonNegative(e, KEY_TRAFFIC_NEAR_MISS_TOTAL);
        repairNonNegative(e, KEY_TRAFFIC_BEST_COMBO);
        repairNonNegative(e, KEY_TRAFFIC_EARNED_COINS);
        repairNonNegative(e, KEY_TRAFFIC_COLLISIONS);
        repairNonNegative(e, KEY_TRAFFIC_POLICE_RISK_PASSES);
        repairNonNegative(e, KEY_TRAFFIC_CLEAN_STREAK);
        repairNonNegative(e, KEY_CAREER_TOTAL_EARNED);
        repairNonNegative(e, KEY_CAREER_GOLD);
        repairNonNegative(e, KEY_CAREER_SILVER);
        repairNonNegative(e, KEY_CAREER_BRONZE);
        // A66.0: kariyer claim anahtarları sadece desteklenen 5x4 lig/etkinlik aralığında tutulur.
        for (int league = 0; league < CareerEventSystem.SUPPORTED_LEAGUE_COUNT; league++) {
            for (int event = 0; event < CareerEventSystem.EVENT_COUNT; event++) {
                // Eski sürümlerden gelen null/negatif sayaç yok; burada yalnızca anahtar aralığı sabitlenir.
                if (prefs.getBoolean(careerEventClaimKey(league, event), false)
                        && !CareerEventSystem.isEventCompleted(this, league, event)) {
                    // Tamamlanmamış hedefe yazılmış claim, ödül tekrar çoğaltmadan kapalı kalır; kullanıcıya rapor satırı ile izlenir.
                    e.putBoolean("career_event_claim_guarded_" + league + "_" + event, true);
                }
            }
            if (prefs.getBoolean(careerLeagueClaimKey(league), false)
                    && !CareerEventSystem.isLeagueCompleted(this, league)) {
                e.putBoolean("career_league_claim_guarded_" + league, true);
            }
        }
        repairNonNegative(e, KEY_CHECKPOINT_RACE_EARNED);
        repairNonNegative(e, KEY_CHECKPOINT_RACE_GOLD);
        repairNonNegative(e, KEY_CHECKPOINT_RACE_SILVER);
        repairNonNegative(e, KEY_CHECKPOINT_RACE_BRONZE);
        repairNonNegative(e, KEY_DRIFT_BEST);
        repairNonNegative(e, KEY_DRIFT_TOTAL_SCORE);
        repairNonNegative(e, KEY_DRIFT_COMPLETED);
        repairNonNegative(e, KEY_DRIFT_EARNED);
        repairNonNegative(e, KEY_DRIFT_XP);
        repairNonNegative(e, KEY_DRIFT_GOLD);
        repairNonNegative(e, KEY_DRIFT_SILVER);
        repairNonNegative(e, KEY_DRIFT_BRONZE);
        repairNonNegative(e, KEY_DRIFT_LEGEND);
        repairNonNegative(e, KEY_DRIFT_BEST_COMBO);
        repairFloat(e, KEY_DRIFT_LONGEST_SECONDS, 0f, 0f, 9999f);
        repairNonNegative(e, KEY_DRAG_COMPLETED);
        repairNonNegative(e, KEY_CHECKPOINT_RACE_COMPLETED);
        int layoutPreset = clamp(prefs.getInt(KEY_CONTROL_LAYOUT_PRESET, 0), 0, 3);
        int sensPreset = clamp(prefs.getInt(KEY_CONTROL_SENSITIVITY, 1), 0, 4);
        int pedalPreset = clamp(prefs.getInt(KEY_PEDAL_SIZE_PRESET, 1), 0, 3);
        int hudPreset = clamp(prefs.getInt(KEY_HUD_PRESET, 0), 0, 3);
        int opacity = clamp(prefs.getInt(KEY_BUTTON_OPACITY, 80), 40, 100);
        int taskNotificationMode = clamp(prefs.getInt(KEY_TASK_NOTIFICATION_MODE, 1), 0, 2);
        if (!prefs.contains(KEY_TASK_HUD_ENABLED)) e.putBoolean(KEY_TASK_HUD_ENABLED, true);
        if (!prefs.contains(KEY_REWARD_POPUP_ENABLED)) e.putBoolean(KEY_REWARD_POPUP_ENABLED, true);
        if (prefs.getInt(KEY_TASK_NOTIFICATION_MODE, 1) != taskNotificationMode) e.putInt(KEY_TASK_NOTIFICATION_MODE, taskNotificationMode);
        if (prefs.getInt(KEY_CONTROL_LAYOUT_PRESET, 0) != layoutPreset) e.putInt(KEY_CONTROL_LAYOUT_PRESET, layoutPreset);
        if (prefs.getInt(KEY_CONTROL_SENSITIVITY, 1) != sensPreset) e.putInt(KEY_CONTROL_SENSITIVITY, sensPreset);
        if (prefs.getInt(KEY_PEDAL_SIZE_PRESET, 1) != pedalPreset) e.putInt(KEY_PEDAL_SIZE_PRESET, pedalPreset);
        if (prefs.getInt(KEY_HUD_PRESET, 0) != hudPreset) e.putInt(KEY_HUD_PRESET, hudPreset);
        if (prefs.getInt(KEY_BUTTON_OPACITY, 80) != opacity) e.putInt(KEY_BUTTON_OPACITY, opacity);

        if (!prefs.getBoolean("a637_legacy_achievement_claim_migration_done", false)) {
            // A63.7: A63.6/öncesinde eski ProgressionSystem bazı ana başarımları
            // otomatik coin/XP vererek achievement_unlocked_* anahtarına yazmış olabilir.
            // Bu kayıtlar yeni manuel panelde tekrar ödül üretmesin diye tek seferlik
            // yeni claim anahtarına taşınır. Güncellemeden sonra ProgressionSystem artık
            // coin/XP vermediği için bu geçiş yalnızca eski kayıtları korur.
            for (int i = 0; i < 8; i++) {
                if (prefs.getBoolean("achievement_unlocked_" + i, false)) {
                    e.putBoolean("a635_achievement_claimed_" + i, true);
                }
            }
            e.putBoolean("a637_legacy_achievement_claim_migration_done", true);
        }

        int previousA671Repairs = prefs.getInt("a671_garage_mod_repair_count", 0);
        e.putInt("a671_garage_mod_repair_count", Math.max(previousA671Repairs, a671RepairCount));
        e.putInt("a671_last_safe_selected_vehicle", selected);
        e.putString("a671_last_repair_summary", a671RepairCount > 0
                ? ("Garaj/modifiye kayıt onarımı: " + a671RepairCount + " düzeltme")
                : "Garaj/modifiye kayıtları temiz");

        int previousA672Repairs = prefs.getInt("a672_post_10_flow_repair_count", 0);
        e.putInt("a672_post_10_flow_repair_count", Math.max(previousA672Repairs, a672FlowRepairCount));
        e.putString("a672_full_game_flow_summary", a672FlowRepairCount > 0
                ? ("A67.2 genel akış onarımı: " + a672FlowRepairCount + " güvenli düzeltme")
                : "A67.2 genel akış QA temiz");
        e.putBoolean("a672_real_showroom_required", true);
        e.putBoolean("a672_open_world_forbidden_confirmed", true);
        int previousA673Repairs = prefs.getInt("a673_ui_safe_area_repair_count", 0);
        e.putInt("a673_ui_safe_area_repair_count", Math.max(previousA673Repairs, a673UiRepairCount));
        e.putString("a673_ui_safe_area_summary", a673UiRepairCount > 0
                ? ("A67.3 UI safe-area onarımı: " + a673UiRepairCount + " düzeltme")
                : "A67.3 UI safe-area QA temiz");
        e.putBoolean("a673_phone_tablet_safe_area_confirmed", true);
        e.putBoolean("a673_showroom_panel_overlap_guard", true);
        e.putBoolean("a673_hud_compact_card_guard", true);

        e.putInt("a674_driving_hud_minimap_repair_count", 0);
        e.putString("a674_driving_hud_minimap_summary", "A67.4 yuvarlak mini harita ve sürüş HUD altyapısı temiz");
        e.putBoolean("a674_circular_minimap_enabled", true);
        e.putBoolean("a674_big_map_overlay_enabled", true);
        e.putBoolean("a674_driving_button_visual_guard", true);
        e.putBoolean("a674_open_world_forbidden_confirmed", true);

        e.putInt("a675_driving_feel_repair_count", 0);
        e.putString("a675_driving_feel_summary", "A67.5 sürüş hissiyatı ve kontrol düğmeleri final ayarı temiz");
        e.putBoolean("a675_analog_button_pressure_enabled", true);
        e.putBoolean("a675_camera_kick_brake_dive_enabled", true);
        e.putBoolean("a675_vehicle_input_response_schema_enabled", true);
        e.putBoolean("a675_open_world_forbidden_confirmed", true);
        e.putInt(KEY_SAVE_REPAIR_VERSION, 675);
        e.putLong("a636_task_achievement_schema", 636L);
        e.putLong("a637_manual_achievement_claim_schema", 637L);
        e.putLong("a638_task_achievement_card_ui_schema", 638L);
        e.putLong("a639_task_achievement_ingame_tracker_schema", 639L);
        e.putLong("a640_task_achievement_final_qa_settings_schema", 640L);
        e.putLong("a641_garage_modification_finalization_schema", 641L);
        e.putLong("a642_garage_save_undo_test_loop_schema", 642L);
        e.putLong("a645_garage_showroom_device_qa_schema", 645L);
        e.putLong("a646_garage_to_test_drive_integration_schema", 646L);
        e.putLong("a657_police_chase_final_ai_qa_schema", 657L);
        e.putLong("a658_three_mode_progress_economy_schema", 658L);
        e.putLong("a659_career_league_start_schema", 659L);
        e.putLong("a660_career_league_final_qa_schema", 660L);
        e.putLong("a661_vehicle_garage_mod_infrastructure_schema", 661L);
        e.putLong("a662_vehicle_model_dlc_audit_schema", 662L);
        e.putLong("a663_garage_ui_professional_layout_schema", 663L);
        e.putLong("a664_modification_workshop_final_ui_schema", 664L);
        e.putLong("a665_visual_mod_save_undo_reset_schema", 665L);
        e.putLong("a666_performance_upgrade_balance_schema", 666L);
        e.putLong("a667_tuning_preset_final_balance_schema", 667L);
        e.putLong("a668_real_showroom_camera_light_scale_schema", 668L);
        e.putLong("a669_garage_vehicle_carousel_swipe_qa_schema", 669L);
        e.putLong("a670_garage_to_drive_transfer_final_qa_schema", 670L);
        e.putLong("a671_garage_mod_save_stability_repair_schema", 671L);
        e.putLong("a672_post_10_update_stability_qa_schema", 672L);
        e.putLong("a673_ui_safe_area_phone_tablet_qa_schema", 673L);
        e.putLong("a674_driving_hud_minimap_foundation_schema", 674L);
        e.putLong("a675_driving_feel_control_final_schema", 675L);
        e.putLong("a647_driving_feel_camera_control_final_schema", 647L);
        e.putLong("a648_checkpoint_time_trial_race_schema", 648L);
        e.putLong("a649_checkpoint_race_final_polish_schema", 649L);
        e.putLong("a651_checkpoint_route_balance_final_qa_schema", 651L);
        e.putLong("a652_drift_score_mode_combo_result_schema", 652L);
        e.putLong("a653_drift_final_combo_balance_schema", 653L);
        e.putLong("a654_game_mode_hub_career_start_schema", 654L);
        e.putLong("a655_game_mode_hub_final_qa_schema", 655L);
        e.putLong("a657_police_chase_mode_schema", 657L);
        e.apply();
    }

    private void repairNonNegative(SharedPreferences.Editor e, String key) {
        if (prefs.getInt(key, 0) < 0) e.putInt(key, 0);
    }

    private void repairFloat(SharedPreferences.Editor e, String key, float defaultValue, float min, float max) {
        float value = prefs.getFloat(key, defaultValue);
        if (value != value || value < min || value > max) {
            e.putFloat(key, clampFloat(value == value ? value : defaultValue, min, max));
        }
    }


    // A65.9: Kariyer ligi/etkinlik odulleri tek seferlik kayitla korunur.
    private String careerEventClaimKey(int league, int event) {
        return "career_event_claimed_" + Math.max(0, league) + "_" + Math.max(0, event);
    }

    private String careerLeagueClaimKey(int league) {
        return "career_league_reward_claimed_" + Math.max(0, league);
    }

    public boolean isCareerEventRewardClaimed(int league, int event) {
        return prefs.getBoolean(careerEventClaimKey(league, event), false);
    }

    public boolean isCareerLeagueRewardClaimed(int league) {
        return prefs.getBoolean(careerLeagueClaimKey(league), false);
    }

    public int getCareerEventClaimedCount(int league) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (isCareerEventRewardClaimed(league, i)) count++;
        }
        return count;
    }

    public boolean claimCareerEventReward(int league, int event, int coins, int xp, String title) {
        int safeLeague = CareerEventSystem.safeLeague(league);
        int safeEvent = CareerEventSystem.safeEvent(event);
        if (!CareerEventSystem.isEventCompleted(this, safeLeague, safeEvent)) {
            setEconomyLastMessage("Kariyer hedefi tamamlanmadan odul verilmez");
            return false;
        }
        if (isCareerEventRewardClaimed(safeLeague, safeEvent)) {
            setEconomyLastMessage("Kariyer etkinlik odulu zaten alindi");
            return false;
        }
        int safeCoins = Math.max(0, coins);
        int safeXp = Math.max(0, xp);
        if (safeCoins > 0) addCoins(safeCoins);
        if (safeXp > 0) addXp(safeXp);
        SharedPreferences.Editor e = prefs.edit();
        e.putBoolean(careerEventClaimKey(safeLeague, safeEvent), true);
        e.putInt(KEY_CAREER_TOTAL_EARNED, getCareerTotalEarnedCoins() + safeCoins);
        e.putString(KEY_CAREER_LAST_MESSAGE, "Kariyer etkinligi: " + (title == null ? "Etkinlik" : title) + " +" + safeCoins + " coin +" + safeXp + " XP");
        e.putInt(KEY_ACTIVE_CAREER_LEAGUE, safeLeague);
        e.putInt(KEY_ACTIVE_CAREER_EVENT, safeEvent);
        e.apply();
        return true;
    }

    public boolean claimCareerLeagueReward(int league, int coins, int xp, String title) {
        int safeLeague = CareerEventSystem.safeLeague(league);
        if (!CareerEventSystem.isLeagueCompleted(this, safeLeague)) {
            setEconomyLastMessage("Lig odulu için tüm etkinlikleri tamamla");
            return false;
        }
        if (isCareerLeagueRewardClaimed(safeLeague)) {
            setEconomyLastMessage("Lig odulu zaten alindi");
            return false;
        }
        int safeCoins = Math.max(0, coins);
        int safeXp = Math.max(0, xp);
        if (safeCoins > 0) addCoins(safeCoins);
        if (safeXp > 0) addXp(safeXp);
        SharedPreferences.Editor e = prefs.edit();
        e.putBoolean(careerLeagueClaimKey(safeLeague), true);
        e.putInt(KEY_CAREER_TOTAL_EARNED, getCareerTotalEarnedCoins() + safeCoins);
        e.putString(KEY_CAREER_LAST_MESSAGE, "Lig odulu: " + (title == null ? "Lig" : title) + " +" + safeCoins + " coin +" + safeXp + " XP");
        e.putInt(KEY_ACTIVE_CAREER_LEAGUE, safeLeague);
        e.apply();
        return true;
    }

    public void setActiveCareerEvent(int league, int event, int mode) {
        prefs.edit()
                .putInt(KEY_ACTIVE_CAREER_LEAGUE, CareerEventSystem.safeLeague(league))
                .putInt(KEY_ACTIVE_CAREER_EVENT, CareerEventSystem.safeEvent(event))
                .putInt(KEY_ACTIVE_CAREER_MODE, mode)
                .apply();
    }

    public int getActiveCareerLeague() {
        return CareerEventSystem.safeLeague(prefs.getInt(KEY_ACTIVE_CAREER_LEAGUE, 0));
    }

    public int getActiveCareerEvent() {
        return CareerEventSystem.safeEvent(prefs.getInt(KEY_ACTIVE_CAREER_EVENT, 0));
    }

    public int getActiveCareerMode() {
        return prefs.getInt(KEY_ACTIVE_CAREER_MODE, 0);
    }

    public String getActiveCareerEventLabel() {
        return CareerEventSystem.leagueTitle(getActiveCareerLeague()) + " • " + CareerEventSystem.eventTitle(getActiveCareerLeague(), getActiveCareerEvent());
    }

    public int getSaveRepairVersion() {
        return Math.max(0, prefs.getInt(KEY_SAVE_REPAIR_VERSION, 0));
    }

    public int getGarageModRepairCount() {
        return Math.max(0, prefs.getInt("a671_garage_mod_repair_count", 0));
    }

    public String getGarageModRepairSummary() {
        String a672 = prefs.getString("a672_full_game_flow_summary", "");
        if (a672 != null && a672.length() > 0) return a672;
        return prefs.getString("a671_last_repair_summary", "Garaj/modifiye kayıtları temiz");
    }

    public int getPostTenUpdateFlowRepairCount() {
        return Math.max(0, prefs.getInt("a672_post_10_flow_repair_count", 0));
    }

    public String getPostTenUpdateFlowSummary() {
        return prefs.getString("a672_full_game_flow_summary", "A67.2 genel akış QA temiz");
    }

    public int getUiSafeAreaRepairCount() {
        return Math.max(0, prefs.getInt("a673_ui_safe_area_repair_count", 0));
    }

    public String getUiSafeAreaQaSummary() {
        return prefs.getString("a673_ui_safe_area_summary", "A67.3 UI safe-area QA temiz");
    }

    public int getDrivingHudMiniMapRepairCount() {
        return Math.max(0, prefs.getInt("a674_driving_hud_minimap_repair_count", 0));
    }

    public String getDrivingHudMiniMapQaSummary() {
        return prefs.getString("a674_driving_hud_minimap_summary", "A67.4 yuvarlak mini harita ve sürüş HUD altyapısı temiz");
    }

    public int getDrivingFeelRepairCount() {
        return Math.max(0, prefs.getInt("a675_driving_feel_repair_count", 0));
    }

    public String getDrivingFeelQaSummary() {
        return prefs.getString("a675_driving_feel_summary", "A67.5 sürüş hissiyatı ve kontrol düğmeleri final ayarı temiz");
    }

    private static int findVehicleIndex(String vehicleId) {
        if (vehicleId == null) return -1;
        for (int i = 0; i < VehicleCatalog.count(); i++) {
            if (vehicleId.equals(VehicleCatalog.id(i))) return i;
        }
        return -1;
    }


    // A63.5: Basarimlar, gunluk/haftalik gorevler ve istatistik odul korumasi.
    public long getCurrentDayStamp() {
        return System.currentTimeMillis() / 86400000L;
    }

    public long getCurrentWeekStamp() {
        return getCurrentDayStamp() / 7L;
    }

    public void ensureDailyWeeklyTaskWindows() {
        long day = getCurrentDayStamp();
        long week = getCurrentWeekStamp();
        SharedPreferences.Editor e = null;
        if (prefs.getLong("a635_daily_day", -1L) != day) {
            e = prefs.edit();
            e.putLong("a635_daily_day", day);
            writeTaskBaselines(e, false);
        }
        if (prefs.getLong("a635_weekly_week", -1L) != week) {
            if (e == null) e = prefs.edit();
            e.putLong("a635_weekly_week", week);
            writeTaskBaselines(e, true);
        }
        if (e != null) e.apply();
    }

    private void writeTaskBaselines(SharedPreferences.Editor e, boolean weekly) {
        String p = weekly ? "a635_weekly_base_" : "a635_daily_base_";
        e.putInt(p + "races", getCareerTotalRaces());
        e.putInt(p + "near", getTrafficNearMissTotal());
        e.putInt(p + "drag", getDragRaceCompletedCount());
        e.putInt(p + "drift", getDriftTotalScore());
        e.putInt(p + "meters", getDrivenMeters());
        e.putInt(p + "police_chases", getPoliceTotalChases());
        e.putInt(p + "police_escapes", getPoliceEscapes());
        e.putInt(p + "upgrades", getTotalPerformanceUpgradeLevelAllVehicles());
        e.putInt(p + "coins", getTotalEarnedCoins());
    }

    public int getTaskWindowBaseline(boolean weekly, String statKey) {
        ensureDailyWeeklyTaskWindows();
        String prefix = weekly ? "a635_weekly_base_" : "a635_daily_base_";
        return Math.max(0, prefs.getInt(prefix + statKey, 0));
    }

    public boolean isAchievementRewardClaimed(int id) {
        return prefs.getBoolean("a635_achievement_claimed_" + Math.max(0, id), false);
    }

    public boolean claimAchievementReward(int id, int coins, int xp, String title) {
        int safeId = Math.max(0, id);
        if (isAchievementRewardClaimed(safeId)) return false;
        SharedPreferences.Editor e = prefs.edit();
        e.putBoolean("a635_achievement_claimed_" + safeId, true);
        // A63.7: Yeni manuel claim tamamlandığında eski başarı anahtarı da
        // senkronlanır; böylece eski ProgressionSystem aynı başarı için yeniden
        // tamamlandı mesajı üretmez. Bu satır coin/XP vermez, sadece durum kaydıdır.
        e.putBoolean("achievement_unlocked_" + safeId, true);
        e.apply();
        if (coins > 0) addCoins(coins);
        if (xp > 0) addXp(xp);
        setEconomyLastMessage("BAŞARIM AÇILDI: " + (title == null ? "Başarım" : title)
                + " +" + Math.max(0, coins) + " coin +" + Math.max(0, xp) + " XP");
        return true;
    }

    public int getAchievementRewardedCount(int maxCount) {
        int count = 0;
        for (int i = 0; i < maxCount; i++) if (isAchievementRewardClaimed(i)) count++;
        return count;
    }

    public boolean isDailyTaskRewardClaimed(int id) {
        ensureDailyWeeklyTaskWindows();
        return prefs.getBoolean("a635_daily_claimed_" + getCurrentDayStamp() + "_" + Math.max(0, id), false);
    }

    public boolean isWeeklyTaskRewardClaimed(int id) {
        ensureDailyWeeklyTaskWindows();
        return prefs.getBoolean("a635_weekly_claimed_" + getCurrentWeekStamp() + "_" + Math.max(0, id), false);
    }

    public boolean claimDailyTaskReward(int id, int coins, int xp, String title) {
        ensureDailyWeeklyTaskWindows();
        int safeId = Math.max(0, id);
        String key = "a635_daily_claimed_" + getCurrentDayStamp() + "_" + safeId;
        if (prefs.getBoolean(key, false)) return false;
        prefs.edit().putBoolean(key, true).apply();
        if (coins > 0) addCoins(coins);
        if (xp > 0) addXp(xp);
        setEconomyLastMessage("GÜNLÜK GÖREV ÖDÜLÜ: " + (title == null ? "Görev" : title)
                + " +" + Math.max(0, coins) + " coin +" + Math.max(0, xp) + " XP");
        return true;
    }

    public boolean claimWeeklyTaskReward(int id, int coins, int xp, String title) {
        ensureDailyWeeklyTaskWindows();
        int safeId = Math.max(0, id);
        String key = "a635_weekly_claimed_" + getCurrentWeekStamp() + "_" + safeId;
        if (prefs.getBoolean(key, false)) return false;
        prefs.edit().putBoolean(key, true).apply();
        if (coins > 0) addCoins(coins);
        if (xp > 0) addXp(xp);
        setEconomyLastMessage("HAFTALIK GÖREV ÖDÜLÜ: " + (title == null ? "Görev" : title)
                + " +" + Math.max(0, coins) + " coin +" + Math.max(0, xp) + " XP");
        return true;
    }

    public int getDailyTaskClaimedCount(int maxCount) {
        int count = 0;
        for (int i = 0; i < maxCount; i++) if (isDailyTaskRewardClaimed(i)) count++;
        return count;
    }

    public int getWeeklyTaskClaimedCount(int maxCount) {
        int count = 0;
        for (int i = 0; i < maxCount; i++) if (isWeeklyTaskRewardClaimed(i)) count++;
        return count;
    }

    private static String keyOwned(String vehicleId) {
        return "vehicle_owned_" + vehicleId;
    }

    private static String keyUpgrade(String vehicleId, int type) {
        return "vehicle_upgrade_" + vehicleId + "_" + type;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float clampFloat(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
