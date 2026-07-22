package com.arabaoyunu.quest;

import com.arabaoyunu.economy.RewardPenaltySystem;
import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;
import com.arabaoyunu.world.WorldPointType;

/**
 * ArabaOyunu_43: Görev zinciri sistemi.
 *
 * Bağımsız tekil görevler yerine birbirine bağlı kariyer akışı verir:
 * Başlangıç -> ilk drift -> ilk yarış -> ilk polis kaçışı -> ilk yükseltme
 * -> yeni araç kilidi -> yeni bölge.
 */
public final class QuestChainSystem {

    public static final int TOTAL_STEPS = 7;

    private final SaveManager saveManager;
    private final RewardPenaltySystem rewardPenaltySystem;
    private final QuestStep[] steps = new QuestStep[TOTAL_STEPS];

    private String message = "";
    private float messageTimer;

    public QuestChainSystem(SaveManager saveManager) {
        this.saveManager = saveManager;
        this.rewardPenaltySystem = new RewardPenaltySystem(saveManager);
        buildSteps();
        if (saveManager != null) {
            saveManager.ensureQuestChainStarted();
        }
    }

    private void buildSteps() {
        steps[0] = new QuestStep(
                QuestStepType.START,
                "Başlangıç Görevi",
                "Açık dünyada aracı sür ve kariyere başla.",
                "+120 XP +250 coin",
                120, 250, 0,
                32f, -122f,
                WorldPointType.TIME_TRIAL);

        steps[1] = new QuestStep(
                QuestStepType.FIRST_DRIFT,
                "İlk Drift Görevi",
                "Drift noktasına git ve ETK ile drift modunu başlat.",
                "+180 XP +350 coin",
                180, 350, 0,
                -138f, 118f,
                WorldPointType.DRIFT);

        steps[2] = new QuestStep(
                QuestStepType.FIRST_RACE,
                "İlk Yarış Görevi",
                "Yarış noktasına git ve ilk yarışı başlat.",
                "+220 XP +500 coin",
                220, 500, 0,
                0f, -128f,
                WorldPointType.RACE);

        steps[3] = new QuestStep(
                QuestStepType.FIRST_POLICE_ESCAPE,
                "İlk Polis Kaçışı",
                "Polis kaçış noktasına git ve kovalamacayı başlat.",
                "+260 XP +650 coin +1 kasa",
                260, 650, 1,
                124f, -118f,
                WorldPointType.POLICE_ESCAPE);

        steps[4] = new QuestStep(
                QuestStepType.FIRST_UPGRADE,
                "İlk Araç Yükseltme",
                "Garaj veya modifiye noktasında ilk yükseltmeyi yap.",
                "+260 XP +500 coin",
                260, 500, 0,
                -138f, 118f,
                WorldPointType.GARAGE);

        steps[5] = new QuestStep(
                QuestStepType.UNLOCK_VEHICLE,
                "Yeni Araç Kilidi",
                "Seviye 2'ye ulaş veya ikinci aracı satın al.",
                "+300 XP +750 coin",
                300, 750, 0,
                -112f, 86f,
                WorldPointType.VEHICLE_GALLERY);

        steps[6] = new QuestStep(
                QuestStepType.UNLOCK_REGION,
                "Yeni Bölge Açma",
                "Seviye 3'e ulaş ve Büyük Şehir bölgesini aç.",
                "+400 XP +1000 coin +1 kasa",
                400, 1000, 1,
                0f, 220f,
                WorldPointType.SPECIAL_EVENT);
    }

    public void update(float dt, String modeName, String mapName, float speedKmh) {
        if (saveManager == null) return;
        if (dt < 0f) dt = 0f;
        if (dt > 0.12f) dt = 0.12f;

        rewardPenaltySystem.update(dt);
        if (messageTimer > 0f) {
            messageTimer -= dt;
            if (messageTimer <= 0f) message = "";
        }

        int index = getActiveIndex();
        if (index >= TOTAL_STEPS) return;

        QuestStep step = steps[index];
        if (step.type == QuestStepType.START) {
            if (saveManager.isCareerStarted()
                    && (saveManager.getDrivenMeters() >= 120 || Math.max(0f, speedKmh) > 22f)) {
                completeActive("Başlangıç tamamlandı");
            }
        } else if (step.type == QuestStepType.FIRST_UPGRADE) {
            if (getTotalUpgradeLevel() > 0) {
                completeActive("İlk yükseltme tamamlandı");
            }
        } else if (step.type == QuestStepType.UNLOCK_VEHICLE) {
            if (saveManager.getPlayerLevel() >= 2 || getOwnedVehicleCount() >= 2) {
                completeActive("Yeni araç kilidi görevi tamamlandı");
            }
        } else if (step.type == QuestStepType.UNLOCK_REGION) {
            if (saveManager.getPlayerLevel() >= 3 || saveManager.isMapUnlockedByCareer(GameScreenState.MAP_CITY)) {
                completeActive("Yeni bölge görevi tamamlandı");
            }
        }
    }

    public void onWorldAction(int worldType) {
        if (saveManager == null) return;
        int index = getActiveIndex();
        if (index >= TOTAL_STEPS) return;
        QuestStep step = steps[index];

        if (step.type == QuestStepType.FIRST_DRIFT && worldType == WorldPointType.DRIFT) {
            completeActive("İlk drift görevi tamamlandı");
        } else if (step.type == QuestStepType.FIRST_RACE && worldType == WorldPointType.RACE) {
            completeActive("İlk yarış görevi tamamlandı");
        } else if (step.type == QuestStepType.FIRST_POLICE_ESCAPE && worldType == WorldPointType.POLICE_ESCAPE) {
            completeActive("İlk polis kaçışı görevi tamamlandı");
        } else if (step.type == QuestStepType.FIRST_UPGRADE && worldType == WorldPointType.GARAGE) {
            setMessage("Garajda ilk yükseltmeyi yap");
        } else if (step.type == QuestStepType.UNLOCK_VEHICLE && worldType == WorldPointType.VEHICLE_GALLERY) {
            setMessage("Seviye 2 veya ikinci araç gerekiyor");
        } else if (step.type == QuestStepType.UNLOCK_REGION && worldType == WorldPointType.SPECIAL_EVENT) {
            setMessage("Yeni bölge için seviye 3 gerekiyor");
        }
    }

    public void onUpgradeOrPurchaseChanged() {
        update(0.016f, "", "", 0f);
    }

    private void completeActive(String reason) {
        int index = getActiveIndex();
        if (index >= TOTAL_STEPS) return;
        QuestStep step = steps[index];

        String economyMessage = rewardPenaltySystem.grantQuestReward(
                index,
                step.xpReward,
                step.coinReward,
                step.crateReward,
                step.title);
        saveManager.incrementProgressDaily();
        saveManager.incrementProgressWeekly();
        saveManager.setQuestChainStep(index + 1);
        saveManager.setQuestChainCompletedCount(Math.max(saveManager.getQuestChainCompletedCount(), index + 1));

        if (index + 1 >= TOTAL_STEPS) {
            setMessage("GÖREV ZİNCİRİ TAMAMLANDI: " + step.rewardText + " | " + economyMessage);
        } else {
            setMessage("GÖREV TAMAMLANDI: " + reason + " | Yeni görev: " + steps[index + 1].title + " | " + economyMessage);
        }
    }

    private int getTotalUpgradeLevel() {
        if (saveManager == null) return 0;
        int total = 0;
        for (int i = 0; i < VehicleCatalog.count(); i++) {
            total += saveManager.getTotalPerformanceUpgradeLevel(VehicleCatalog.id(i));
        }
        return total;
    }

    private int getOwnedVehicleCount() {
        if (saveManager == null) return 1;
        int count = 0;
        for (int i = 0; i < VehicleCatalog.count(); i++) {
            if (saveManager.isVehicleOwned(VehicleCatalog.id(i))) count++;
        }
        return count;
    }

    public int getActiveIndex() {
        if (saveManager == null) return 0;
        int index = saveManager.getQuestChainStep();
        if (index < 0) index = 0;
        if (index > TOTAL_STEPS) index = TOTAL_STEPS;
        return index;
    }

    public boolean isCompleted() {
        return getActiveIndex() >= TOTAL_STEPS;
    }

    public QuestStep getActiveStep() {
        int index = getActiveIndex();
        if (index >= TOTAL_STEPS) return steps[TOTAL_STEPS - 1];
        return steps[index];
    }

    public int getStepNumber() {
        return Math.min(TOTAL_STEPS, getActiveIndex() + 1);
    }

    public int getTotalSteps() { return TOTAL_STEPS; }
    public String getTitle() { return isCompleted() ? "Kariyer Zinciri Tamamlandı" : getActiveStep().title; }
    public String getObjective() { return isCompleted() ? "Yeni görev zinciri için hazır." : getActiveStep().objective; }
    public String getRewardText() { return isCompleted() ? "Tamamlandı" : getActiveStep().rewardText; }
    public String getMessage() { return message == null ? "" : message; }
    public float getTargetX() { return isCompleted() ? 0f : getActiveStep().targetX; }
    public float getTargetZ() { return isCompleted() ? 0f : getActiveStep().targetZ; }
    public int getTargetWorldType() { return isCompleted() ? WorldPointType.NONE : getActiveStep().targetWorldType; }
    public int getCompletedCount() { return saveManager == null ? 0 : saveManager.getQuestChainCompletedCount(); }

    private void setMessage(String text) {
        message = text == null ? "" : text;
        messageTimer = 5.0f;
        if (saveManager != null) saveManager.setQuestChainLastMessage(message);
    }
}
