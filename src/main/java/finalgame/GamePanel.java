package finalgame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GamePanel extends JPanel {
    private static final int VIEW_WIDTH = 960;
    private static final int VIEW_HEIGHT = 640;
    private GameState state = GameState.MENU;
    private GameState stateBeforePause = GameState.EXPLORATION;
    private final ExplorationRoom exploration = new ExplorationRoom();
    private final MineRoom mine = new MineRoom();
    private boolean inMine;
    private final ButterflyRoom butterfly=new ButterflyRoom();
    private boolean inButterfly;
    private final TempleRoom temple=new TempleRoom();
    private boolean inTemple;
    private final ThroneRoom throne=new ThroneRoom();
    private boolean inThrone;
    private final Rectangle startButton = new Rectangle(340, 280, 280, 60);
    private final Rectangle resumeButton = new Rectangle(340, 270, 280, 60);
    private final Rectangle menuButton = new Rectangle(340, 350, 280, 60);
    private final Rectangle battleArea = new Rectangle(300, 300, 360, 260);
    private final Player player;
    private Boss boss;
    private int selectedBoss = 5;
    private final LaserBeam laser = new LaserBeam();
    private final boolean[] pressed = new boolean[4];
    private final Timer timer;
    private long previousTime;

    private final ArrayList<Projectile> projectiles = new ArrayList<>();
    private final ArrayList<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private boolean running = false;
    private boolean shooting = false;
    private boolean eHeld = false;
    private boolean qHeld = false;
    private double shotCooldown = 0.0;
    private static final double SHOT_INTERVAL = 0.5;
    private final DialogueBox dialogue=new DialogueBox();
    private final StoryDirector story=new StoryDirector(dialogue);
    private boolean forestCorpseSeen,forestEncounterSeen,forestVictorySeen,forestStoryBattle;
    private boolean storySkippable;
    private double storyFade,battleSlide=1,battleReadyTime;

    public GamePanel() {
        setPreferredSize(new Dimension(VIEW_WIDTH, VIEW_HEIGHT));
        setBackground(Color.BLACK);
        player = new Player(468, 500);
        boss = new MaskedBoss(430, 210);

        bindScreenAction("pressed SHIFT", "run-press", () -> {
            if (state == GameState.EXPLORATION) running = true;
        });
        bindScreenAction("released SHIFT", "run-release", () -> running = false);
        bindKey("W", 0);
        bindKey("A", 1);
        bindKey("S", 2);
        bindKey("D", 3);
        bindBossSelection("1", 1);
        bindBossSelection("2", 2);
        bindBossSelection("3", 3);
        bindBossSelection("4", 4);
        bindBossSelection("5", 5);
        bindScreenAction("released F3", "collision-view", () -> {
            if (state == GameState.EXPLORATION) { if(inThrone) throne.toggleCollisionView(); else if(inTemple) temple.toggleCollisionView(); else if(inButterfly) butterfly.toggleCollisionView(); else if(inMine) mine.toggleCollisionView(); else exploration.toggleCollisionView(); repaint(); }
        });
        bindScreenAction("released F6", "skip-map", () -> skipMap());
        bindScreenAction("released ESCAPE", "pause", () -> togglePause());
        bindScreenAction("released ENTER", "confirm", () -> confirmScreen());
        bindScreenAction("released SPACE", "dialogue-next", () -> {
            if(state!=GameState.PAUSED && story.active())story.advance();
        });
        bindScreenAction("released TAB", "skip-seen-scene", () -> {
            if(state!=GameState.PAUSED && story.active() && storySkippable)finishForestEntrance();
        });
        bindScreenAction("released M", "menu", () -> {
            if (state == GameState.PAUSED) changeState(GameState.MENU);
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("released R"), "restart-battle");
        getActionMap().put("restart-battle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (state == GameState.BATTLE
                        && !story.active()
                        && (!player.isAlive() || !boss.isAlive())) {
                    restartBattle();
                }
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("pressed E"), "skill-e-press");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("released E"), "skill-e-release");
        getActionMap().put("skill-e-press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (canActInBattle() && !forestStoryBattle && !eHeld) {
                    eHeld = true;
                    useSkillE();
                }
            }
        });
        getActionMap().put("skill-e-release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                eHeld = false;
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("pressed Q"), "skill-q-press");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("released Q"), "skill-q-release");
        getActionMap().put("skill-q-press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (canActInBattle() && !forestStoryBattle && !qHeld) {
                    qHeld = true;
                    if (player.isAlive() && boss.isAlive()) {
                        player.useSkillQ();
                    }
                }
            }
        });
        getActionMap().put("skill-q-release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                qHeld = false;
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    handleClick(event.getX(), event.getY());
                }
                if (canActInBattle()
                        && SwingUtilities.isRightMouseButton(event)) {
                    shooting = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    shooting = false;
                }
            }
        });

        timer = new Timer(16, event -> updateGame());
    }

    private void bindScreenAction(String key, String name, Runnable command) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(key), name);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("shift " + key), name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                command.run();
            }
        });
    }

    private void changeState(GameState nextState) {
        clearInput();
        state = nextState;
        repaint();
    }

    private void startNewGame() {
        story.cancel();storyFade=0;battleSlide=1;storySkippable=false;
        forestCorpseSeen=false;forestEncounterSeen=false;forestVictorySeen=false;forestStoryBattle=false;
        exploration.reset();
        mine.reset();
        butterfly.reset();
        temple.reset();
        throne.reset();
        inThrone=false;
        inTemple=false;
        inMine=false;
        inButterfly=false;
        selectedBoss = 1;
        boss = new ForestBoss(430, 210);
        restartBattle();
        changeState(GameState.EXPLORATION);
        playForestOpening();
    }

    private void togglePause() {
        if (state == GameState.PAUSED) {
            changeState(stateBeforePause);
        } else if (state == GameState.EXPLORATION || state == GameState.BATTLE) {
            stateBeforePause = state;
            changeState(GameState.PAUSED);
        }
    }

    private void confirmScreen() {
        if (state == GameState.MENU) startNewGame();
        else if (state == GameState.PAUSED) togglePause();
        else if(story.active()){story.advance();repaint();}
        else if (state == GameState.BATTLE && !boss.isAlive() && player.isAlive()) {
            if(forestStoryBattle&&!forestVictorySeen){playForestVictory();return;}
            if (!inMine && !inButterfly && !inTemple && !inThrone && selectedBoss == 1) exploration.returnFromBattle();
            if (inMine && selectedBoss == 2) mine.returnFromBattle();
            if (inButterfly && selectedBoss == 3) butterfly.returnFromBattle();
            if (inTemple && selectedBoss == 4) temple.returnFromBattle();
            if (inThrone && selectedBoss == 5) throne.returnFromBattle();
            projectiles.clear();
            enemyProjectiles.clear();
            laser.reset();
            changeState(GameState.EXPLORATION);
        }
    }

    private void handleClick(int x, int y) {
        if(state!=GameState.PAUSED && story.active()){story.advance();repaint();return;}
        if (state == GameState.MENU && startButton.contains(x, y)) {
            startNewGame();
        } else if (state == GameState.PAUSED) {
            if (resumeButton.contains(x, y)) togglePause();
            else if (menuButton.contains(x, y)) changeState(GameState.MENU);
        } else if (state == GameState.EXPLORATION && (inThrone || inTemple ? false : inButterfly ? butterfly.interact(x,y) : inMine ? mine.interact(x,y) : exploration.interact(x, y))) {
            if(inButterfly) startButterflyBattle(); else if(inMine) startMineBattle(); else startForestBattle();
        }
        repaint();
    }

    private void skipMap() {
        if(state!=GameState.EXPLORATION || story.active())return;
        exploration.finishStory();forestStoryBattle=false;
        if(inThrone) throne.skipToHall();
        else if(inTemple) {
            temple.skipToExit();temple.consumeExitRequest();inTemple=false;inThrone=true;throne.enter();
        }
        else if(inButterfly) {
            butterfly.returnFromBattle();inButterfly=false;inTemple=true;temple.enter();
        }
        else if(inMine) {
            mine.returnFromBattle(); inMine=false; inButterfly=true; butterfly.enter();
        } else {
            exploration.returnFromBattle(); inMine=true; mine.enter();
        }
        clearInput();repaint();
    }

    private void startThroneBattle() {
        forestStoryBattle=false;
        selectedBoss=5;boss=new MaskedBoss(430,210);
        restartBattle();changeState(GameState.BATTLE);
    }

    private void startTempleBattle() {
        forestStoryBattle=false;
        selectedBoss=4;boss=new KnightBoss(430,210);
        restartBattle();changeState(GameState.BATTLE);
    }

    private void startButterflyBattle() {
        forestStoryBattle=false;
        selectedBoss=3; boss=new MothBoss(430,210);
        restartBattle(); changeState(GameState.BATTLE);
    }

    private void startMineBattle() {
        forestStoryBattle=false;
        selectedBoss=2;
        boss=new ScientistBoss(430,210);
        restartBattle();
        changeState(GameState.BATTLE);
    }

    private void startForestBattle() {
        if(story.active())return;
        playForestEntrance();
    }

    private boolean canActInBattle() {
        return state==GameState.BATTLE && !story.active() && battleReadyTime<=0;
    }

    private void endForestScene() {
        storyFade=0;storySkippable=false;exploration.finishStory();clearInput();
    }

    private void playForestOpening() {
        clearInput();storyFade=0;exploration.setStoryHeroPose(1);
        story.play(this::endForestScene,
            StoryDirector.motion("fade-from-menu",.7,p->storyFade=p),
            StoryDirector.waitFor("black",.3),
            StoryDirector.motion("wake-fade",1.3,p->storyFade=1-p),
            StoryDirector.waitFor("unconscious",.8),
            StoryDirector.action("sit-up",()->exploration.setStoryHeroPose(2)),
            StoryDirector.say(DialogueBox.Face.NEUTRAL,"อึก…"),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"ที่นี่…ที่ไหนกัน…"),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"แล้วฉัน…อึก…เป็นใครกัน"),
            StoryDirector.waitFor("hold-head",.6),
            StoryDirector.motion("rise-to-feet",.7,p->exploration.setStoryRise(p)),
            StoryDirector.action("stand-up",()->exploration.setStoryHeroPose(0)),
            StoryDirector.waitFor("standing",.5));
    }

    private void playForestCorpse() {
        forestCorpseSeen=true;clearInput();exploration.faceStoryHero(SpriteAnimator.DOWN_RIGHT);
        story.play(this::endForestScene,
            StoryDirector.waitFor("notice-corpse",.35),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"นี่มันตัวอะไรกันเนี่ย…"),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"ศพ…งั้นเหรอ?"));
    }

    private void playForestEntrance() {
        clearInput();storySkippable=forestEncounterSeen;forestStoryBattle=true;
        final double hx=exploration.storyX()+8,hy=exploration.storyY()+16;
        exploration.faceStoryHero(SpriteAnimator.RIGHT);
        exploration.setStoryBoss(1,1465,425,0);
        story.play(()->{forestEncounterSeen=true;storySkippable=false;battleReadyTime=3;clearInput();},
            StoryDirector.motion("pan-to-feeding",1.2,p->exploration.focusStory(hx+(1465-hx)*p,hy+(390-hy)*p,1.35+.25*p)),
            StoryDirector.motion("beast-strikes-prey",.4,p->{exploration.setStoryStrike(p);exploration.setStoryBoss(1,1465,425,Math.sin(p*Math.PI*2)*4);}),
            StoryDirector.action("strike-ends",()->exploration.setStoryStrike(0)),
            StoryDirector.motion("feeding",2.8,p->exploration.setStoryBoss(1,1465,425,Math.sin(p*Math.PI*12)*3)),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"…"),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"นั่นมัน…"),
            StoryDirector.action("beast-looks-up",()->exploration.setStoryBoss(2,1465,425,0)),
            StoryDirector.waitFor("growl",.8),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"แย่แล้ว…"),
            StoryDirector.motion("beast-approaches",1.5,p->{
                exploration.setStoryBoss(2,1465+(hx+95-1465)*p,425+(hy-425)*p,Math.sin(p*Math.PI*8)*2);
                exploration.focusStory(1465+((hx+1465)/2-1465)*p,390+(hy-25-390)*p,1.6);
            }),
            StoryDirector.motion("beast-leaps",.42,p->{
                exploration.setStoryBoss(3,hx+95-85*p,hy-Math.sin(p*Math.PI)*50,0);
                storyFade=Math.max(0,(p-.35)/.65);
            }),
            StoryDirector.action("enter-battle",()->{
                selectedBoss=1;boss=new ForestBoss(430,210);restartBattle();changeState(GameState.BATTLE);
                exploration.finishStory();storyFade=1;battleSlide=0;
            }),
            StoryDirector.motion("battle-fade",.45,p->storyFade=1-p),
            StoryDirector.motion("battle-slide",.8,p->battleSlide=1-Math.pow(1-p,3)),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"อึก…แย่แล้ว ฉันต้องสู้กับมันเหรอเนี่ย"));
    }

    private void finishForestEntrance() {
        story.cancel();selectedBoss=1;boss=new ForestBoss(430,210);forestStoryBattle=true;
        exploration.finishStory();storyFade=0;battleSlide=1;storySkippable=false;
        restartBattle();changeState(GameState.BATTLE);
    }

    private void playForestVictory() {
        forestVictorySeen=true;clearInput();projectiles.clear();enemyProjectiles.clear();laser.reset();
        story.play(this::endForestScene,
            StoryDirector.waitFor("victory-breath",.65),
            StoryDirector.motion("victory-fade-out",.65,p->storyFade=p),
            StoryDirector.action("back-to-forest",()->{
                exploration.returnFromBattle();changeState(GameState.EXPLORATION);storyFade=1;
            }),
            StoryDirector.motion("victory-fade-in",.8,p->storyFade=1-p),
            StoryDirector.say(DialogueBox.Face.RELIEVED,"เฮือก!…เกือบไป"),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"ตกลงที่นี่มันที่ไหนกันเนี่ย…"));
    }

    public void onFocusLost() {
        clearInput();
        if (state == GameState.EXPLORATION || state == GameState.BATTLE) {
            togglePause();
        }
    }

    private void bindKey(String key, int index) {
        String pressAction = "press-" + key;
        String releaseAction = "release-" + key;
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("pressed " + key), pressAction);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("released " + key), releaseAction);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("shift pressed " + key), pressAction);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("shift released " + key), releaseAction);
        getActionMap().put(pressAction, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (state == GameState.EXPLORATION || state == GameState.BATTLE) {
                    pressed[index] = true;
                }
            }
        });
        getActionMap().put(releaseAction, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                pressed[index] = false;
            }
        });
    }

    private void bindBossSelection(String key, int number) {
        String action = "select-boss-" + number;
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("released " + key), action);
        getActionMap().put(action, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (state != GameState.BATTLE || story.active()) return;
                forestStoryBattle=false;
                selectedBoss = number;
                switch (number) {
                    case 1: boss = new ForestBoss(430, 210); break;
                    case 2: boss = new ScientistBoss(430, 210); break;
                    case 3: boss = new MothBoss(430, 210); break;
                    case 4: boss = new KnightBoss(430, 210); break;
                    default: boss = new MaskedBoss(430, 210); break;
                }
                restartBattle();
            }
        });
    }

    private void useSkillE() {
        if (!canActInBattle() || forestStoryBattle || !player.isAlive() || !boss.isAlive()) {
            return;
        }
        player.useSkillE();
    }

    private void updateGame() {
        long now = System.nanoTime();
        double seconds = Math.min((now - previousTime) / 1_000_000_000.0, 0.05);
        previousTime = now;

        if (state == GameState.MENU || state == GameState.PAUSED) return;
        if(story.active()){story.update(seconds);repaint();return;}
        if (state == GameState.EXPLORATION) {
            int dx = (pressed[3] ? 1 : 0) - (pressed[1] ? 1 : 0);
            int dy = (pressed[2] ? 1 : 0) - (pressed[0] ? 1 : 0);
            if(inThrone) {
                throne.update(dx,dy,seconds,running);
                if(throne.consumeBackRequest()) {
                    inThrone=false;inTemple=true;temple.returnFromThrone();clearInput();
                } else if(throne.consumeBattleRequest()) startThroneBattle();
            } else if(inTemple) {
                temple.update(dx,dy,seconds,running);
                if(temple.consumeBackRequest()) {
                    inTemple=false;inButterfly=true;butterfly.returnFromTemple();clearInput();
                } else if(temple.consumeBattleRequest()) startTempleBattle();
                else if(temple.consumeExitRequest()) {
                    inTemple=false;inThrone=true;throne.enter();clearInput();
                }
            } else if(inButterfly) {
                butterfly.update(dx,dy,seconds,running);
                if(butterfly.consumeBackRequest()) {
                    inButterfly=false; inMine=true; mine.returnFromButterfly(); clearInput();
                } else if(butterfly.consumeExitRequest()) {
                    inButterfly=false;inTemple=true;temple.enter();clearInput();
                } else if(butterfly.consumeBattleRequest()) startButterflyBattle();
            } else if(inMine) {
                mine.update(dx,dy,seconds,running);
                if(mine.consumeBackRequest()) {
                    inMine=false; exploration.returnFromMine(); clearInput();
                } else if(mine.consumeExitRequest()) {
                    inMine=false; inButterfly=true; butterfly.enter(); clearInput();
                } else if(mine.consumeBattleRequest()) startMineBattle();
            } else {
                exploration.update(dx,dy,seconds,running);
                if(!forestCorpseSeen && exploration.storyX()>=290 && exploration.storyX()<1100) {
                    playForestCorpse();
                } else if(!forestEncounterSeen && !exploration.isCleared() && exploration.storyX()>=1330) {
                    startForestBattle();
                } else if(exploration.consumeMineRequest()) {
                    inMine=true; mine.enter(); clearInput();
                } else if(exploration.consumeBattleRequest()) startForestBattle();
            }
            repaint();
            return;
        }

        if (!player.isAlive() || !boss.isAlive()) {
            if(!boss.isAlive() && player.isAlive() && forestStoryBattle && !forestVictorySeen) {
                playForestVictory();repaint();return;
            }
            projectiles.clear();
            enemyProjectiles.clear();
            laser.reset();
            repaint();
            return;
        }

        if(battleReadyTime>0) {
            battleReadyTime=Math.max(0,battleReadyTime-seconds);
            player.move((pressed[3]?1:0)-(pressed[1]?1:0),(pressed[2]?1:0)-(pressed[0]?1:0),seconds,battleArea);
            shooting=false;repaint();return;
        }

        player.update(seconds);
        int directionX = 0;
        int directionY = 0;
        if (pressed[0]) directionY--;
        if (pressed[1]) directionX--;
        if (pressed[2]) directionY++;
        if (pressed[3]) directionX++;
        player.move(directionX, directionY, seconds, battleArea);

        Projectile chargedSkill = player.pollSkillProjectile();
        if (chargedSkill != null) {
            projectiles.add(chargedSkill);
        }

        shotCooldown = Math.max(0.0, shotCooldown - seconds);
        if (shooting && !player.isUsingSkill() && shotCooldown <= 0.0) {
            Projectile normalShot = player.shoot();
            if (normalShot != null) {
                projectiles.add(normalShot);
                shotCooldown = SHOT_INTERVAL;
            }
        }

        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(seconds);
            if (boss.isAlive()
                    && projectile.getBounds().intersects(boss.getHitBounds())) {
                boss.takeDamage(projectile.getDamage());
                projectiles.remove(i);
            } else if (projectile.isOutsideScreen()) {
                projectiles.remove(i);
            }
        }

        boolean laserEnabled =
                shooting && player.isUltimateActive() && boss.isAlive();
        int laserDamage = laser.update(
                laserEnabled, player.getLaserBounds(), boss.getHitBounds(), seconds);
        boss.takeDamage(laserDamage);

        if (!boss.isAlive()) {
            projectiles.clear();
            enemyProjectiles.clear();
            laser.reset();
            if(forestStoryBattle&&!forestVictorySeen)playForestVictory();
            repaint();
            return;
        }

        boss.update(seconds, battleArea, player.getBounds(), enemyProjectiles);

        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            EnemyProjectile projectile = enemyProjectiles.get(i);
            projectile.update(seconds);
            if (projectile.shouldSplit()) {
                enemyProjectiles.addAll(projectile.split());
                enemyProjectiles.remove(i);
                continue;
            }
            if (projectile.isDangerous()
                    && projectile.getBounds().intersects(player.getBounds())) {
                player.takeDamage(10);
                enemyProjectiles.remove(i);
            } else if (projectile.isOutsideScreen(getWidth(), getHeight())) {
                enemyProjectiles.remove(i);
            }
        }

        if (boss.checkHazards(player.getBounds())) {
            player.takeDamage(10);
        }

        if (!player.isAlive()) {
            projectiles.clear();
            enemyProjectiles.clear();
            laser.reset();
        }
        repaint();
    }

    private void restartBattle() {
        clearInput();
        laser.reset();
        projectiles.clear();
        enemyProjectiles.clear();
        player.reset(468, 500);
        boss.reset();
        battleReadyTime=3;battleSlide=1;
        shotCooldown = 0.0;
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        previousTime = System.nanoTime();
        timer.start();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    public void clearInput() {
        running = false;
        exploration.stopMovement();
        mine.stopMovement();
        butterfly.stopMovement();
        temple.stopMovement();
        throne.stopMovement();
        for (int i = 0; i < pressed.length; i++) {
            pressed[i] = false;
        }
        shooting = false;
        eHeld = false;
        qHeld = false;
        // Stop the visible beam but retain any pending gap when focus is lost.
        laser.update(false, player.getLaserBounds(), boss.getHitBounds(), 0.0);
        previousTime = System.nanoTime();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == GameState.MENU) {
            drawMenu(g);
            return;
        }
        GameState visibleState = state == GameState.PAUSED ? stateBeforePause : state;
        if (visibleState == GameState.EXPLORATION) { if(inThrone) throne.draw(g); else if(inTemple) temple.draw(g); else if(inButterfly) butterfly.draw(g); else if(inMine) mine.draw(g); else exploration.draw(g); }
        else drawBattle(g);
        if(story.currentId().equals("fade-from-menu"))drawMenu(g);
        if(story.active()) {
            if(storyFade>0) {
                Graphics2D fade=(Graphics2D)g.create();fade.setColor(new Color(0,0,0,(int)(Math.min(1,storyFade)*255)));
                fade.fillRect(0,0,VIEW_WIDTH,VIEW_HEIGHT);fade.dispose();
            }
            dialogue.draw(g);
            if(storySkippable) {
                g.setColor(new Color(200,196,180));g.setFont(DialogueBox.font(16));
                g.drawString("Tab: ข้ามฉากที่เคยดูแล้ว",690,36);
            }
        }
        if (state == GameState.PAUSED) drawPause(g);
    }

    private void drawButton(Graphics g, Rectangle bounds, String text) {
        g.setColor(new Color(30, 34, 42));
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(Color.WHITE);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, bounds.x + (bounds.width - textWidth) / 2,
                bounds.y + 38);
    }

    private void drawMenu(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 72));
        g.drawString("FINAL", 370, 190);
        drawButton(g, startButton, "PLAY");
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Click PLAY or press Enter", 340, 395);
        g.drawString("Forest story preview  |  v3.65", 310, 500);
        g.drawString("No saved progress yet", 365, 535);
    }

    private void drawPause(Graphics g) {
        g.setColor(new Color(0, 0, 0, 215));
        g.fillRect(0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
        g.setColor(Color.WHITE);
        g.drawString("PAUSED", 405, 210);
        drawButton(g, resumeButton, "RESUME");
        drawButton(g, menuButton, "MAIN MENU");
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        g.drawString("Esc / Enter: Resume | M: Main menu", 320, 455);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Returning to menu discards this test run.", 290, 490);
    }

    private void drawBattle(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 48));
        g.drawString("FINAL", 60, 100);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        g.drawString("WASD: Move | Esc: Pause", 60, 130);
        g.drawString("Right mouse: Fire", 60, 155);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("TEST: 1 Forest / 2 Scientist / 3 Moth / 4 Knight / 5 Final", 60, 625);
        g.setColor(Color.WHITE);

        Graphics2D border = (Graphics2D) g.create();
        border.setColor(Color.WHITE);
        border.setStroke(new BasicStroke(3.0f));
        border.drawRect(battleArea.x, battleArea.y,
                battleArea.width, battleArea.height);
        border.dispose();

        if (player.isAlive() && boss.isAlive()) {
            boss.drawHazards(g);
            laser.draw(g);
        }
        Graphics2D bossGraphics=(Graphics2D)g.create();
        bossGraphics.translate((int)Math.round((battleSlide-1)*480),0);boss.draw(bossGraphics);bossGraphics.dispose();
        player.draw(g);
        for (Projectile projectile : projectiles) {
            projectile.draw(g);
        }
        Graphics2D enemyGraphics = (Graphics2D) g.create();
        enemyGraphics.clipRect(battleArea.x, battleArea.y,
                battleArea.width, battleArea.height);
        for (EnemyProjectile projectile : enemyProjectiles) {
            projectile.draw(enemyGraphics);
        }
        enemyGraphics.dispose();

        g.setColor(Color.WHITE);
        g.drawString("Player HP: " + player.getHp() + "/" + player.getMaxHp(),
                battleArea.x, battleArea.y + battleArea.height + 35);

        String skillEText;
        if(forestStoryBattle) {
            skillEText="E: LOCKED";
        } else if (player.getSkillEChargeTime() > 0.0) {
            skillEText = "E: CHARGING";
        } else if (player.getSkillECooldown() <= 0.0) {
            skillEText = "E: READY";
        } else {
            int remaining = (int) Math.ceil(player.getSkillECooldown());
            skillEText = "E: " + remaining + "s";
        }
        g.setColor(Color.MAGENTA);
        g.drawString(skillEText, 700, 335);

        String skillQText;
        if(forestStoryBattle) {
            skillQText="Q: LOCKED";
        } else if (player.getSkillQChargeTime() > 0.0) {
            skillQText = "Q: POWER UP";
        } else if (player.isUltimateActive()) {
            int remaining = (int) Math.ceil(player.getUltimateTime());
            skillQText = "Q ACTIVE: " + remaining + "s";
        } else if (player.getSkillQCooldown() > 0.0) {
            int remaining = (int) Math.ceil(player.getSkillQCooldown());
            skillQText = "Q: " + remaining + "s";
        } else {
            skillQText = "Q: READY";
        }
        g.setColor(Color.YELLOW);
        g.drawString(skillQText, 700, 365);
        g.setColor(Color.ORANGE);
        g.drawString("BOSS: " + boss.getAttackName(), 700, 405);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Target: " + boss.getDifficultyLabel(), 700, 435);
        g.drawString("Boss " + selectedBoss + " / 5", 700, 465);
        g.setColor(Color.PINK);
        g.drawString(boss.getPhaseLabel(), 700, 495);
        g.setColor(Color.ORANGE);
        g.drawString("Tuning: +1 STEP", 700, 525);

        if(!story.active() && battleReadyTime>0 && player.isAlive() && boss.isAlive()) {
            g.setFont(DialogueBox.font(28));g.setColor(new Color(220,212,185));
            String ready="เตรียมตัว  "+(int)Math.ceil(battleReadyTime);
            g.drawString(ready,battleArea.x+(battleArea.width-g.getFontMetrics().stringWidth(ready))/2,battleArea.y+130);
        }

        if (!player.isAlive()) {
            g.setColor(Color.RED);
            g.drawString("YOU DIED", battleArea.x + 120, battleArea.y + 130);
        } else if (!boss.isAlive()) {
            g.setColor(Color.GREEN);
            g.drawString("VICTORY", battleArea.x + 120, battleArea.y + 130);
        }
        if (!player.isAlive() || !boss.isAlive()) {
            g.setColor(Color.WHITE);
            g.drawString("Press R to retry",
                    battleArea.x + 95, battleArea.y + 165);
            if (player.isAlive()) {
                g.drawString("Enter: Return to map", 365, 495);
            }
        }
    }
}
