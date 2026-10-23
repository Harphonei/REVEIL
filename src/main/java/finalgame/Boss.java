package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

public abstract class Boss {
    private final int x;
    private final int y;
    private final int width = 100;
    private final int height = 60;
    private int maxHp;
    private boolean rematchTuned;
    private final String name;
    private int hp;
    private double shotCooldown = 1.0;
    private String visualAttack="";
    private double castTime;

    public void updateEffects(double seconds){
        castTime=Math.max(0,castTime-seconds);
        String attack=getAttackName();
        if(!attack.equals(visualAttack)){
            visualAttack=attack;
            if(!attack.equals("RECOVERY")&&!attack.equals("AWAKENING"))castTime=.35;
        }
    }
    public void drawEffects(Graphics g,int theme){BossEffects.cast(g,getBounds(),theme,castTime);}

    public Boss(int startX, int startY) {
        this(startX, startY, 100, "TRAINING BOSS");
    }

    protected Boss(int startX, int startY, int maxHp, String name) {
        x = startX;
        y = startY;
        this.maxHp = CombatTuning.TEST_BOSS_HP > 0 ? CombatTuning.TEST_BOSS_HP : maxHp;
        this.name = name;
        hp = this.maxHp;
    }

    /** Applies once to a fresh boss; Retry retains this maximum without stacking. */
    final void enableRematch(){
        if(rematchTuned)return;
        maxHp=(int)Math.ceil(maxHp*RematchRules.HP_MULTIPLIER);hp=maxHp;rematchTuned=true;
    }

    public int getMaxHp() { return maxHp; }
    public String getName(){return name;}

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /** Damage target may be smaller than the full sprite (e.g. the moth's wings). */
    public Rectangle getHitBounds() {
        return getBounds();
    }

    final void devDefeat(){hp=0;}

    public void takeDamage(int damage) {
        if (damage > 0) {
            hp = Math.max(0, hp - damage);
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public int getHp() {
        return hp;
    }

    public EnemyProjectile shoot() {
        return new EnemyProjectile(x + width / 2.0
                - EnemyProjectile.NORMAL_DIAMETER / 2.0, y + height);
    }

    public abstract void attack(ArrayList<EnemyProjectile> projectiles);

    public void update(double seconds, Rectangle battleArea,
            Rectangle playerBounds, ArrayList<EnemyProjectile> projectiles) {
        if (!isAlive()) {
            return;
        }
        shotCooldown = Math.max(0.0, shotCooldown - seconds);
        if (shotCooldown <= 0.0) {
            attack(projectiles);
            shotCooldown = 1.0;
        }
    }

    public boolean checkHazards(Rectangle playerBounds) {
        return getHazardDamage(playerBounds)>0;
    }
    public int getHazardDamage(Rectangle playerBounds){return 0;}

    public void drawHazards(Graphics g) {
        // Most bosses have no floor hazards.
    }

    public String getAttackName() {
        return "STRAIGHT / SPREAD";
    }

    public String getDifficultyLabel() {
        return "1/5";
    }

    public String getPhaseLabel() {
        return "";
    }

    protected Color getBodyColor() {
        return Color.RED;
    }

    public void reset() {
        visualAttack="";castTime=0;
        hp = maxHp;
        shotCooldown = 1.0;
    }

    public void draw(Graphics g) {
        if (!isAlive()) {
            return;
        }
        g.setColor(Color.WHITE);
        g.drawString(name, x - 45, y - 60);
        g.setColor(getBodyColor());
        g.fillRect(x, y, width, height);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y - 20, width, 8);
        int hpBarWidth = width * hp / maxHp;
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 20, hpBarWidth, 8);
        g.setColor(Color.WHITE);
        g.drawString("HP: " + hp + "/" + maxHp, x - 10, y - 30);
    }
}
