package finalgame;

// One relative difficulty step above the previous prototype; player stats stay unchanged.
public final class CombatTuning {
    // Temporary quick-playtest HP. Set to 0 to restore each boss's original HP.
    public static final int TEST_BOSS_HP = 100;
    public static final double ENEMY_SPEED_MULTIPLIER = 1.15;
    public static final double ATTACK_INTERVAL_MULTIPLIER = 0.88;
    public static final double WARNING_TIME_MULTIPLIER = 0.90;

    private CombatTuning() {
    }
}
