package finalgame;

// Story balance. Projectile patterns retain the previous movement tuning.
public final class CombatTuning {
    // Developer override: zero uses the stage-specific story HP.
    public static final int TEST_BOSS_HP = 0;
    public static final double ENEMY_SPEED_MULTIPLIER = 1.15;
    public static final double ATTACK_INTERVAL_MULTIPLIER = 0.88;
    public static final double WARNING_TIME_MULTIPLIER = 0.90;

    public static int playerHpForStage(int stage){
        return switch(stage){case 1,2->150;case 3,4->200;case 5->250;default->throw new IllegalArgumentException("Unknown stage");};
    }

    private CombatTuning() {
    }
}
