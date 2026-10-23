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
    private final SaveStore saveStore=new SaveStore();
    private SaveStore.Profile saved=saveStore.load();
    private SaveStore.Checkpoint checkpoint;
    private boolean runActive;
    private boolean savePending;
    private int menuChoice;
    private boolean rematchActive;
    private int rematchChoice;
    private final boolean[] rematchWins=new boolean[5];
    private final Rectangle rematchButton=new Rectangle();
    private int deathChoice;
    private int pauseChoice;
    private GameState helpReturnState=GameState.MENU;
    private boolean devOverlay;
    private double menuClock;
    private String saveNotice=saveStore.warning();
    private final ExplorationRoom exploration = new ExplorationRoom();
    private final MineRoom mine = new MineRoom();
    private boolean inMine;
    private final ButterflyRoom butterfly=new ButterflyRoom();
    private boolean inButterfly;
    private final TempleRoom temple=new TempleRoom();
    private boolean inTemple;
    private final ThroneRoom throne=new ThroneRoom();
    private boolean inThrone;
    private final Rectangle startButton = new Rectangle(285, 310, 280, 60);
    private final Rectangle continueButton=new Rectangle(285,230,280,60);
    private final Rectangle helpButton=new Rectangle(285,390,280,60);
    private final Rectangle helpBackButton=new Rectangle(285,478,280,48);
    private final Rectangle resumeButton = new Rectangle(340, 270, 280, 60);
    private final Rectangle overlayHelpButton=new Rectangle(340,350,280,60);
    private final Rectangle menuButton = new Rectangle(340, 430, 280, 60);
    private final Rectangle skipStoryButton=new Rectangle(824,74,116,38);
    private final Rectangle battleArea = new Rectangle(300, 300, 360, 260);
    private final Player player;
    private Boss boss;
    private int selectedBoss = 5;
    private final LaserBeam laser = new LaserBeam();
    private final BattleEffects battleEffects=new BattleEffects();
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
    private boolean mineEntrySeen,mineEncounterSeen,mineVictorySeen,mineStoryBattle,skillEUnlocked;
    private boolean mothEntrySeen,mothRootsSeen,mothEncounterSeen,mothVictorySeen,mothStoryBattle,skillQUnlocked;
    private boolean templeEntrySeen,templeEncounterSeen,templeVictorySeen,templeStoryBattle;
    private boolean throneEntrySeen,throneEncounterSeen,throneVictorySeen,throneStoryBattle,thronePhaseSeen;
    private double cinematicFlash,phaseSlash,menuFade;
    private boolean storySkippable;
    private double storyFade,battleReadyTime;

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
        bindScreenAction("released F", "inspect-nearby", this::inspectNearby);
        bindScreenAction("released F3", "collision-view", () -> {
            if (state == GameState.EXPLORATION) { if(inThrone) throne.toggleCollisionView(); else if(inTemple) temple.toggleCollisionView(); else if(inButterfly) butterfly.toggleCollisionView(); else if(inMine) mine.toggleCollisionView(); else exploration.toggleCollisionView(); repaint(); }
        });
        bindScreenAction("released F6", "skip-map", () -> skipMap());
        bindScreenAction("released F7", "replay-map", () -> replayCurrentMap());
        bindScreenAction("released F1", "dev-overlay", () -> {devOverlay=!devOverlay;repaint();});
        bindScreenAction("released ESCAPE", "pause", () -> togglePause());
        bindScreenAction("released ENTER", "confirm", () -> confirmScreen());
        bindScreenAction("released UP", "menu-up", () -> moveMenu(-1));
        bindScreenAction("released DOWN", "menu-down", () -> moveMenu(1));
        bindScreenAction("released SPACE", "dialogue-next", () -> {
            if(state!=GameState.PAUSED && story.active())story.advance();
        });
        bindScreenAction("released TAB", "skip-seen-scene", () -> {
            skipStory();
        });
        bindScreenAction("released M", "menu", () -> {
            if (state == GameState.PAUSED||state==GameState.GAME_OVER) returnToMenu();
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("released R"), "restart-battle");
        getActionMap().put("restart-battle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if(state==GameState.GAME_OVER){retryFight();return;}
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
                if (canActInBattle() && canUseSkillE() && !eHeld) {
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
                if (canActInBattle() && canUseSkillQ() && !qHeld) {
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

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter(){
            @Override public void mouseMoved(MouseEvent event){hoverMenu(event.getX(),event.getY());}
        });
        addMouseListener(new MouseAdapter(){
            @Override public void mouseExited(MouseEvent event){setCursor(java.awt.Cursor.getDefaultCursor());}
        });
        timer = new Timer(16, event -> updateGame());
    }

    private void hoverMenu(int x,int y){
        layoutMainMenu();
        boolean over=false;
        if(state==GameState.MENU){
            int index=menuIndexAt(x,y);if(index>=0){menuChoice=index;over=true;}
        }else if(state==GameState.REMATCH){
            int index=RematchScreen.choiceAt(x,y);
            if(index>=0)over=true; // Hover indicates clickability; selection changes on click only.
            if(RematchScreen.FIGHT.contains(x,y)&&rematchChoice<5)over=true;
        }else if(state==GameState.HELP)over=helpBackButton.contains(x,y);
        else if(state==GameState.PAUSED||state==GameState.GAME_OVER){
            int choice=resumeButton.contains(x,y)?0:overlayHelpButton.contains(x,y)?1:menuButton.contains(x,y)?2:-1;
            if(choice>=0){over=true;if(state==GameState.PAUSED)pauseChoice=choice;else deathChoice=choice;}
        }
        if(story.active()&&(state==GameState.EXPLORATION||state==GameState.BATTLE)&&skipStoryButton.contains(x,y))over=true;
        setCursor(java.awt.Cursor.getPredefinedCursor(over?java.awt.Cursor.HAND_CURSOR:java.awt.Cursor.DEFAULT_CURSOR));
        repaint();
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
        setCursor(java.awt.Cursor.getDefaultCursor());
        state = nextState;
        repaint();
    }

    private void resetRun() {
        rematchActive=false;
        story.cancel();storyFade=0;storySkippable=false;
        forestCorpseSeen=false;forestEncounterSeen=false;forestVictorySeen=false;forestStoryBattle=false;
        mineEntrySeen=false;mineEncounterSeen=false;mineVictorySeen=false;mineStoryBattle=false;skillEUnlocked=false;
        mothEntrySeen=false;mothRootsSeen=false;mothEncounterSeen=false;mothVictorySeen=false;mothStoryBattle=false;templeStoryBattle=false;skillQUnlocked=false;
        templeEntrySeen=false;templeEncounterSeen=false;templeVictorySeen=false;templeStoryBattle=false;
        throneEntrySeen=false;throneEncounterSeen=false;throneVictorySeen=false;throneStoryBattle=false;thronePhaseSeen=false;
        cinematicFlash=0;phaseSlash=0;menuFade=0;
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
    }

    private void requestNewGame(){
        if(saved.checkpoint()!=null&&javax.swing.JOptionPane.showConfirmDialog(this,
                "Start a new game and replace the saved run?","New Game",
                javax.swing.JOptionPane.YES_NO_OPTION)!=javax.swing.JOptionPane.YES_OPTION)return;
        startNewGame();
    }

    private void startNewGame() {
        resetRun();runActive=true;checkpoint=captureCheckpoint(1);
        playForestOpening();
        changeState(GameState.EXPLORATION);
    }

    private int currentMap(){return inThrone?5:inTemple?4:inButterfly?3:inMine?2:1;}
    private SaveStore.Checkpoint captureCheckpoint(int map){
        int mask=(exploration.isCleared()?1:0)|(mine.isCleared()?2:0)|(butterfly.isCleared()?4:0)|(temple.isCleared()?8:0);
        return new SaveStore.Checkpoint(map,skillEUnlocked,skillQUnlocked,mask&~(1<<(map-1)));
    }
    private void checkpointMapIfChanged(){
        if(runActive&&(checkpoint==null||checkpoint.map()!=currentMap())){
            checkpoint=captureCheckpoint(currentMap());
            player.reset(468,500,CombatTuning.playerHpForStage(currentMap()));
        }
    }
    private boolean persistCheckpoint(){
        SaveStore.Profile next=new SaveStore.Profile(saved.completed(),checkpoint);
        try{saveStore.save(next);saved=next;savePending=false;saveNotice="";return true;}
        catch(java.io.IOException ex){savePending=true;saveNotice="Autosave failed. Please check disk space and folder permissions.";repaint();return false;}
    }
    public boolean saveBeforeExit(){
        if(!runActive)return !savePending||persistCheckpoint();
        checkpointMapIfChanged();return persistCheckpoint();
    }
    private void returnToMenu(){
        if(!saveBeforeExit())return;
        rematchActive=false;runActive=false;story.cancel();storyFade=0;cinematicFlash=0;phaseSlash=0;menuFade=0;
        menuChoice=0;changeState(GameState.MENU);
    }
    private void finishCampaign(){
        checkpoint=null;saved=new SaveStore.Profile(true,null);persistCheckpoint();runActive=false;
        endThroneScene();throneStoryBattle=false;menuFade=1;menuChoice=0;changeState(GameState.MENU);
    }
    private void continueGame(){
        SaveStore.Checkpoint resume=saved.checkpoint();if(resume==null)return;
        resetRun();checkpoint=resume;runActive=true;skillEUnlocked=resume.skillE();skillQUnlocked=resume.skillQ();
        player.reset(468,500,CombatTuning.playerHpForStage(resume.map()));
        int mask=resume.clearedMask();
        if((mask&1)!=0){exploration.returnFromBattle();forestCorpseSeen=true;forestEncounterSeen=true;forestVictorySeen=true;}
        if((mask&2)!=0){mine.returnFromBattle();mineEntrySeen=true;mineEncounterSeen=true;mineVictorySeen=true;}
        if((mask&4)!=0){butterfly.returnFromBattle();mothEntrySeen=true;mothRootsSeen=true;mothEncounterSeen=true;mothVictorySeen=true;}
        if((mask&8)!=0){temple.returnFromBattle();templeEntrySeen=true;templeEncounterSeen=true;templeVictorySeen=true;}
        switch(resume.map()){
            case 2:enterMine();break;case 3:enterButterfly();break;case 4:enterTemple();break;case 5:enterThrone();break;
            default:playForestOpening();break;
        }
        changeState(GameState.EXPLORATION);
    }

    private void togglePause() {
        if(state==GameState.REMATCH){rematchActive=false;menuFade=0;menuChoice=0;changeState(GameState.MENU);}
        else if(state==GameState.HELP){returnFromHelp();}
        else if (state == GameState.PAUSED) {
            changeState(stateBeforePause);
        } else if (state == GameState.EXPLORATION || state == GameState.BATTLE) {
            stateBeforePause = state;
            pauseChoice=0;
            changeState(GameState.PAUSED);
        }
    }

    private void moveMenu(int delta){
        if(state==GameState.REMATCH){rematchChoice=Math.floorMod(rematchChoice+delta,6);repaint();return;}
        if(state==GameState.GAME_OVER){deathChoice=Math.floorMod(deathChoice+delta,3);repaint();return;}
        if(state==GameState.PAUSED){pauseChoice=Math.floorMod(pauseChoice+delta,3);repaint();return;}
        if(state!=GameState.MENU)return;
        menuChoice=Math.floorMod(menuChoice+delta,2+(saved.checkpoint()!=null?1:0)+(saved.completed()?1:0));repaint();
    }
    private void layoutMainMenu(){MenuScreen.layoutMenu(saved,continueButton,startButton,helpButton,rematchButton);}
    private int menuIndexAt(int x,int y){
        Rectangle[] buttons=saved.checkpoint()!=null
            ?(saved.completed()?new Rectangle[]{continueButton,startButton,helpButton,rematchButton}:new Rectangle[]{continueButton,startButton,helpButton})
            :(saved.completed()?new Rectangle[]{startButton,helpButton,rematchButton}:new Rectangle[]{startButton,helpButton});
        for(int i=0;i<buttons.length;i++)if(buttons[i].contains(x,y))return i;
        return -1;
    }
    private void activateMenu(){
        int index=0;
        if(saved.checkpoint()!=null&&menuChoice==index++)continueGame();
        else if(menuChoice==index++)requestNewGame();
        else if(menuChoice==index++)openHelp();
        else if(saved.completed())openRematches();
    }
    private void openRematches(){
        if(!saved.completed())return;
        story.cancel();storyFade=0;menuFade=0;cinematicFlash=0;phaseSlash=0;
        rematchActive=false;runActive=false;
        projectiles.clear();enemyProjectiles.clear();laser.reset();battleEffects.reset();
        changeState(GameState.REMATCH);
    }
    private void startRematch(int number){
        if(!saved.completed()||number<1||number>5)return;
        story.cancel();storyFade=1;cinematicFlash=0;phaseSlash=0;menuFade=0;
        forestStoryBattle=false;mineStoryBattle=false;mothStoryBattle=false;templeStoryBattle=false;throneStoryBattle=false;
        inMine=false;inButterfly=false;inTemple=false;inThrone=false;
        runActive=false;rematchActive=true;skillEUnlocked=true;skillQUnlocked=true;
        rematchChoice=number-1;selectedBoss=number;boss=RematchScreen.createBoss(number);boss.enableRematch();
        restartBattle();changeState(GameState.BATTLE);
        story.play(()->{storyFade=0;battleReadyTime=1;clearInput();},
            StoryDirector.motion("rematch-fade-in",.55,p->storyFade=1-p));
    }
    private void leaveBattleMenu(){if(rematchActive)openRematches();else returnToMenu();}
    private void finishRematch(){
        if(story.active())return;
        rematchWins[selectedBoss-1]=true;clearInput();projectiles.clear();enemyProjectiles.clear();laser.reset();
        story.play(this::openRematches,StoryDirector.motion("rematch-fade-out",.7,p->storyFade=p));
    }

    private void openHelp(){helpReturnState=state;changeState(GameState.HELP);}
    private void returnFromHelp(){changeState(helpReturnState);}
    private void activateOverlay(int choice,boolean dead){
        if(choice==0){if(dead)retryFight();else togglePause();}
        else if(choice==1)openHelp();
        else leaveBattleMenu();
    }

    private void confirmScreen() {
        if(state==GameState.REMATCH){if(rematchChoice==5)togglePause();else startRematch(rematchChoice+1);}
        else if (state == GameState.MENU) activateMenu();
        else if(state==GameState.GAME_OVER)activateOverlay(deathChoice,true);
        else if(state==GameState.HELP)returnFromHelp();
        else if (state == GameState.PAUSED) activateOverlay(pauseChoice,false);
        else if(story.active()){story.advance();repaint();}
        else if (state == GameState.BATTLE && !boss.isAlive() && player.isAlive()) {
            if(rematchActive){finishRematch();return;}
            if(forestStoryBattle&&!forestVictorySeen){playForestVictory();return;}
            if(mineStoryBattle&&!mineVictorySeen){playMineVictory();return;}
            if(mothStoryBattle&&!mothVictorySeen){playMothVictory();return;}
            if(templeStoryBattle&&!templeVictorySeen){playTempleVictory();return;}
            if(throneStoryBattle&&!throneVictorySeen){playThroneEnding();return;}
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

    private void skipStory(){
        if(state==GameState.PAUSED||state==GameState.HELP||!story.active())return;
        clearInput();story.skip();clearInput();repaint();
    }

    private void handleClick(int x, int y) {
        layoutMainMenu();
        if(state==GameState.REMATCH){
            int index=RematchScreen.choiceAt(x,y);
            if(index==5)togglePause();else if(index>=0){rematchChoice=index;repaint();}
            else if(RematchScreen.FIGHT.contains(x,y)&&rematchChoice<5)startRematch(rematchChoice+1);
            return;
        }
        if(state==GameState.MENU){int index=menuIndexAt(x,y);if(index>=0){menuChoice=index;activateMenu();}return;}

        if(state!=GameState.PAUSED&&story.active()&&skipStoryButton.contains(x,y)){skipStory();return;}
        if(state==GameState.GAME_OVER){
            if(resumeButton.contains(x,y))retryFight();else if(overlayHelpButton.contains(x,y))openHelp();else if(menuButton.contains(x,y))leaveBattleMenu();return;
        }
        if(state==GameState.HELP){if(helpBackButton.contains(x,y))returnFromHelp();return;}
        if(state!=GameState.PAUSED && story.active()){story.advance();repaint();return;}
        if (state == GameState.MENU && startButton.contains(x, y)) {
            requestNewGame();
        } else if(state==GameState.MENU&&continueButton.contains(x,y)&&saved.checkpoint()!=null){
            continueGame();
        } else if(state==GameState.MENU&&helpButton.contains(x,y)){
            openHelp();
        } else if (state == GameState.PAUSED) {
            if (resumeButton.contains(x, y)) togglePause();
            else if(overlayHelpButton.contains(x,y))openHelp();
            else if (menuButton.contains(x, y)) leaveBattleMenu();
        } else if (state == GameState.EXPLORATION && (inThrone || inTemple ? false : inButterfly ? butterfly.interact(x,y) : inMine ? mine.interact(x,y) : exploration.interact(x, y))) {
            if(inButterfly) startButterflyBattle(); else if(inMine) startMineBattle(); else startForestBattle();
        }
        repaint();
    }

    private void skipMap() {
        if(state!=GameState.EXPLORATION || story.active())return;
        exploration.finishStory();mine.finishStory();butterfly.finishStory();temple.finishStory();forestStoryBattle=false;mineStoryBattle=false;mothStoryBattle=false;templeStoryBattle=false;
        if(inThrone) throne.skipToHall();
        else if(inTemple) {
            temple.skipToExit();temple.consumeExitRequest();inTemple=false;enterThrone();
        }
        else if(inButterfly) {
            skillQUnlocked=true;butterfly.returnFromBattle();inButterfly=false;enterTemple();
        }
        else if(inMine) {
            skillEUnlocked=true;
            mine.returnFromBattle(); inMine=false; enterButterfly();
        } else {
            exploration.returnFromBattle(); enterMine();
        }
        checkpointMapIfChanged();clearInput();repaint();
    }

    /** Developer replay resets only the current room and its story flags. */
    private void replayCurrentMap(){
        if(rematchActive||state==GameState.REMATCH||state==GameState.MENU||state==GameState.HELP)return;
        story.cancel();storyFade=0;storySkippable=false;
        cinematicFlash=0;phaseSlash=0;menuFade=0;throneStoryBattle=false;
        exploration.finishStory();mine.finishStory();butterfly.finishStory();temple.finishStory();
        forestStoryBattle=false;mineStoryBattle=false;mothStoryBattle=false;templeStoryBattle=false;
        clearInput();
        if(inThrone){throne.reset();throneEntrySeen=false;throneEncounterSeen=false;throneVictorySeen=false;thronePhaseSeen=false;selectedBoss=5;boss=new MaskedBoss(430,210);}
        else if(inTemple){temple.reset();templeEntrySeen=false;templeEncounterSeen=false;templeVictorySeen=false;selectedBoss=4;boss=new KnightBoss(430,210);}
        else if(inButterfly){butterfly.reset();mothEntrySeen=false;mothRootsSeen=false;mothEncounterSeen=false;mothVictorySeen=false;skillQUnlocked=false;selectedBoss=3;boss=new MothBoss(430,210);}
        else if(inMine){
            mine.reset();mineEntrySeen=false;mineEncounterSeen=false;mineVictorySeen=false;
            skillEUnlocked=false;selectedBoss=2;boss=new ScientistBoss(430,210);
        }else{
            exploration.reset();forestCorpseSeen=false;forestEncounterSeen=false;forestVictorySeen=false;
            selectedBoss=1;boss=new ForestBoss(430,210);
        }
        restartBattle();
        if(inThrone)enterThrone();
        else if(inMine)enterMine();
        else if(inButterfly)enterButterfly();
        else if(inTemple)enterTemple();
        else if(!inThrone&&!inTemple&&!inButterfly)playForestOpening();
        changeState(GameState.EXPLORATION);
    }

    private void startThroneBattle() {
        if(story.active())return;
        forestStoryBattle=false;mineStoryBattle=false;mothStoryBattle=false;templeStoryBattle=false;
        throneStoryBattle=true;
        if(!throneEncounterSeen){playThroneEntrance();return;}
        selectedBoss=5;boss=new MaskedBoss(430,210);
        restartBattle();changeState(GameState.BATTLE);
    }

    private static double ease(double p){return p*p*(3-2*p);}

    private void enterThrone(){
        inThrone=true;throne.enter();throne.finishStory();clearInput();
        checkpointMapIfChanged();
        if(!throneEntrySeen){
            throneEntrySeen=true;storyFade=1;throne.beginStory();
            story.play(this::endThroneScene,
                StoryDirector.motion("throne-arrival",.9,p->storyFade=1-p),
                StoryDirector.waitFor("throne-silence",.6),
                StoryDirector.say(DialogueBox.Face.CONFUSED,"ข้างในนี้ ที่เจ้านั่นปกป้องไว้ อาจจะทำให้ฉันได้รู้อะไรมากขึ้น"));
        }
    }

    private void endThroneScene(){storyFade=0;cinematicFlash=0;phaseSlash=0;throne.finishStory();clearInput();}

    private void playThroneEntrance(){
        clearInput();throne.beginStory();throne.faceHero(SpriteAnimator.RIGHT);
        final double startX=throne.storyX(),startY=throne.storyY(),hx=1312,hy=404;
        final double[] last={0};
        story.play(()->{throneEncounterSeen=true;endThroneScene();battleReadyTime=1;},
            StoryDirector.motion("throne-stage-hero",.7,p->{double e=ease(p);throne.moveHero(startX+(hx-startX)*e,startY+(hy-startY)*e,(p-last[0])*.7);last[0]=p;throne.focus(throne.storyX()+8,throne.storyY()+8,1.18);}),
            StoryDirector.action("throne-lock-entry",()->{throne.faceHero(SpriteAnimator.RIGHT);throne.rememberEntry();last[0]=0;}),
            StoryDirector.waitFor("throne-notice",.7),
            StoryDirector.motion("throne-pan-to-mask",2.0,p->{double e=ease(p);throne.focus(hx+8+(1560-hx-8)*e,hy+8+(397-hy-8)*e,1.18);}),
            StoryDirector.waitFor("throne-back-silence",1.3),
            StoryDirector.motion("throne-two-shot",1.4,p->{double e=ease(p);throne.focus(1560-110*e,397+7*e,1.18);}),
            StoryDirector.say(DialogueBox.Face.NEUTRAL,"แกคือเจ้าของที่นี่สินะ"),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"ช่วยตอบคำถามฉันซักข้อสิ"),
            StoryDirector.waitFor("throne-turn-hesitate",.45),
            StoryDirector.motion("throne-turn-to-hero",.95,p->throne.turnBoss(p)),
            StoryDirector.waitFor("throne-recognition",1.15),
            StoryDirector.action("throne-commit-lunge",()->throne.bossPose(3)),
            StoryDirector.motion("throne-lunge",.38,p->{double e=p*p;throne.moveBoss(1560+(hx+60-1560)*e,420+(hy+16-420)*e);}),
            StoryDirector.action("throne-land-cast",()->throne.bossPose(4)),
            StoryDirector.motion("throne-close-range-bolt",.18,p->throne.bolt(p)),
            StoryDirector.action("throne-bolt-hits",()->throne.bolt(-1)),
            StoryDirector.motion("throne-eye-impact",.32,p->{cinematicFlash=.60*(1-p);throne.impact(1-p);throne.injure();}),
            StoryDirector.motion("throne-recoil",.38,p->{double e=ease(p);throne.moveHero(hx-12*e,hy,(p-last[0])*.38);last[0]=p;throne.heroPose(0);}),
            StoryDirector.say(DialogueBox.Face.HURT,"อ้าก!!"),
            StoryDirector.say(DialogueBox.Face.HURT_ANGRY,"นี่แก!"),
            StoryDirector.motion("throne-battle-fade-out",.55,p->storyFade=p),
            StoryDirector.action("throne-enter-battle",()->{
                selectedBoss=5;boss=new MaskedBoss(430,210);restartBattle();changeState(GameState.BATTLE);storyFade=1;
            }),
            StoryDirector.waitFor("throne-battle-black",.3),
            StoryDirector.motion("throne-battle-fade-in",.85,p->storyFade=1-p),
            StoryDirector.say(DialogueBox.Face.HURT_ANGRY,"ไม่คิดจะคุยกันดีๆซักคนเลยหรือไง!"));
    }

    private boolean tryThronePhase(){
        if(!throneStoryBattle||thronePhaseSeen||!player.isAlive()||!(boss instanceof MaskedBoss)
                ||!((MaskedBoss)boss).awaitingStoryPhase())return false;
        thronePhaseSeen=true;clearInput();projectiles.clear();enemyProjectiles.clear();laser.reset();
        MaskedBoss masked=(MaskedBoss)boss;masked.clearStoryHazards();
        story.play(()->{masked.finishStoryPhase();phaseSlash=0;cinematicFlash=0;clearInput();battleReadyTime=1;},
            StoryDirector.say(DialogueBox.Face.HURT_ANGRY,"ย๊าก!!"),
            StoryDirector.motion("final-mask-strike",.28,p->{phaseSlash=Math.sin(p*Math.PI);cinematicFlash=.24*Math.sin(p*Math.PI);}),
            StoryDirector.say(DialogueBox.Face.MASKED_PAIN,"อึก!"),
            StoryDirector.say(DialogueBox.Face.MASKED_ANGRY,"แก..."),
            StoryDirector.motion("final-hood-falls",1.1,p->masked.showStoryPhase(ease(p))),
            StoryDirector.waitFor("final-red-eye",1.1));
        return true;
    }

    private void playThroneEnding(){
        if(throneVictorySeen)return;
        throneVictorySeen=true;clearInput();projectiles.clear();enemyProjectiles.clear();laser.reset();
        ((MaskedBoss)boss).clearStoryHazards();
        story.play(this::finishCampaign,
            StoryDirector.waitFor("final-last-breath",.7),
            StoryDirector.motion("final-victory-dark",1.0,p->storyFade=p),
            StoryDirector.action("final-return-hall",()->{
                throne.prepareEnding();throne.focus(1555,402,1.48);changeState(GameState.EXPLORATION);storyFade=1;
            }),
            StoryDirector.waitFor("final-after-battle-silence",.6),
            StoryDirector.motion("final-wounded-reveal",1.4,p->storyFade=1-p),
            StoryDirector.waitFor("final-wounded-breath",1.1),
            StoryDirector.motion("final-approach-mask",2.8,p->throne.woundedWalk(1480+66*ease(p),404,p*2.8)),
            StoryDirector.action("final-stop-walking",()->throne.woundedFrame(0)),
            StoryDirector.say(DialogueBox.Face.HURT_ANGRY,"โถ่เอ้ย"),
            StoryDirector.say(DialogueBox.Face.HURT,"สุดท้ายก็ไม่ได้อะไรเลย"),
            StoryDirector.say(DialogueBox.Face.HURT,"ช่างเถอะ..."),
            StoryDirector.say(DialogueBox.Face.HURT,"งั้นขอนี่ก็แล้วกัน"),
            StoryDirector.waitFor("final-look-at-mask",.45),
            StoryDirector.motion("final-kneel",.8,p->throne.woundedFrame(p<.45?3:4)),
            StoryDirector.waitFor("final-reach-mask",.45),
            StoryDirector.action("final-take-mask",()->throne.takeMask()),
            StoryDirector.action("final-hold-mask",()->throne.woundedFrame(5)),
            StoryDirector.waitFor("final-examine-mask",.65),
            StoryDirector.motion("final-raise-mask",1.3,p->throne.woundedFrame(p<.45?6:7)),
            StoryDirector.waitFor("final-mask-to-face",.7),
            StoryDirector.motion("final-change-cloak",.95,p->storyFade=p),
            StoryDirector.action("final-wear-cloak",()->{throne.wearCloak();throne.focus(1560,397,1.48);}),
            StoryDirector.waitFor("final-dressing-black",.8),
            StoryDirector.motion("final-cloaked-reveal",1.3,p->storyFade=1-p),
            StoryDirector.motion("final-recognise-body",1.3,p->{double e=ease(p);throne.revealBody(e);throne.focus(1560+15*e,397,1.48+.42*e);}),
            StoryDirector.waitFor("final-frozen",1.3),
            StoryDirector.action("final-movement-behind",()->throne.showNewcomer()),
            StoryDirector.say(DialogueBox.Face.NONE,"แกคือเจ้าของที่นี่สินะ"),
            StoryDirector.say(DialogueBox.Face.NONE,"นี่! ช่วยตอบคำถามฉันซักข้อสิ"),
            StoryDirector.waitFor("final-hears-himself",.8),
            StoryDirector.motion("final-turns-back",.95,p->throne.turnBoss(p)),
            StoryDirector.motion("final-reverse-pan",2.8,p->{double e=ease(p);throne.approachNewcomer(Math.min(1,p*3));throne.focus(1575+(throne.entryFootX()-1575)*e,397+(throne.entryFootY()-8-397)*e,1.90-.72*e);}),
            StoryDirector.waitFor("final-realisation",.65),
            StoryDirector.waitFor("final-identical-face",2.4),
            StoryDirector.motion("final-loop-black",1.4,p->storyFade=p),
            StoryDirector.waitFor("final-loop-silence",1.0),
            StoryDirector.say(DialogueBox.Face.MASKED,"......"),
            StoryDirector.waitFor("final-end-black",1.1));
    }

    private void startTempleBattle() {
        if(story.active())return;
        throneStoryBattle=false;
        forestStoryBattle=false;mineStoryBattle=false;mothStoryBattle=false;templeStoryBattle=true;
        if(!templeEncounterSeen){playTempleEntrance();return;}
        selectedBoss=4;boss=new KnightBoss(430,210);
        restartBattle();changeState(GameState.BATTLE);
    }

    private void startButterflyBattle() {
        if(story.active())return;
        throneStoryBattle=false;
        forestStoryBattle=false;mineStoryBattle=false;mothStoryBattle=true;templeStoryBattle=false;
        if(!mothEncounterSeen){playMothEntrance();return;}
        selectedBoss=3; boss=new MothBoss(430,210);
        restartBattle(); changeState(GameState.BATTLE);
    }

    private void startMineBattle() {
        if(story.active())return;
        throneStoryBattle=false;
        forestStoryBattle=false;mineStoryBattle=true;mothStoryBattle=false;templeStoryBattle=false;
        if(!mineEncounterSeen){playMineEntrance();return;}
        selectedBoss=2;boss=new ScientistBoss(430,210);
        restartBattle();changeState(GameState.BATTLE);
    }

    private void startForestBattle() {
        if(story.active())return;
        throneStoryBattle=false;
        mineStoryBattle=false;mothStoryBattle=false;templeStoryBattle=false;
        playForestEntrance();
    }

    private boolean canUseSkillE(){return !forestStoryBattle&&(!mineStoryBattle||skillEUnlocked);}
    private boolean canUseSkillQ(){return !forestStoryBattle&&!mineStoryBattle&&(!mothStoryBattle||skillQUnlocked);}

    private void enterTemple(){
        inTemple=true;temple.enter();clearInput();
        checkpointMapIfChanged();
        if(!templeEntrySeen){
            templeEntrySeen=true;storyFade=1;
            story.play(this::endTempleScene,
                StoryDirector.motion("temple-arrival-fade",.6,p->storyFade=1-p),
                StoryDirector.say(DialogueBox.Face.CONFUSED,"ต่อจากป่าก็วิหารร้างงั้นหรอ"),
                StoryDirector.say(DialogueBox.Face.NEUTRAL,"คงต้องเข้าไปดูซักหน่อย"));
        }
    }

    private void endTempleScene(){storyFade=0;temple.finishStory();clearInput();}

    private void playTempleEntrance(){
        clearInput();final double hx=temple.storyX(),hy=temple.storyY();
        story.play(()->{templeEncounterSeen=true;endTempleScene();battleReadyTime=1;},
            StoryDirector.waitFor("temple-hears-voice",.3),
            StoryDirector.say(DialogueBox.Face.NONE,"ท่าน…."),
            StoryDirector.action("temple-hero-turns",()->temple.faceStoryKnight()),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"!!! รูปปั้นงั้นหรอ?"),
            StoryDirector.motion("temple-pan-statue",1.1,p->{
                double e=p*p*(3-2*p);temple.focusStory(hx+8+(1280-hx-8)*e,hy+8+(245-hy-8)*e);
            }),
            StoryDirector.say(DialogueBox.Face.KNIGHT,"ทำไมท่านถึงยังกลับมา…"),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"แกรู้จักฉันงั้นหรอ"),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"งั้นก็ช่วยบอกฉันหน่อยสิ! ที่นี่ที่ไหนแล้วฉันเป็นใคร!"),
            StoryDirector.motion("temple-knight-turns",1.25,p->temple.setKnightTurn(p*p*(3-2*p))),
            StoryDirector.say(DialogueBox.Face.KNIGHT,"….."),
            StoryDirector.say(DialogueBox.Face.KNIGHT,"อา…ท่านผู้เป็นนิรันดรเอ๋ย ถ้าท่านจะผ่านไปข้างหน้าแล้วล่ะก็ ข้าคงให้ไม่ได้"),
            StoryDirector.motion("temple-battle-fade-out",.5,p->storyFade=p),
            StoryDirector.action("temple-enter-battle",()->{
                selectedBoss=4;boss=new KnightBoss(430,210);restartBattle();changeState(GameState.BATTLE);storyFade=1;
            }),
            StoryDirector.waitFor("temple-battle-black",.18),
            StoryDirector.motion("temple-battle-fade-in",.65,p->storyFade=1-p),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"นี่…ไม่คิดจะพูดอะไรหน่อยหรอ"),
            StoryDirector.say(DialogueBox.Face.KNIGHT,"……"));
    }

    private void playTempleVictory(){
        templeVictorySeen=true;clearInput();projectiles.clear();enemyProjectiles.clear();laser.reset();
        story.play(this::endTempleScene,
            StoryDirector.waitFor("temple-victory-breath",.6),
            StoryDirector.motion("temple-victory-fade-out",.6,p->storyFade=p),
            StoryDirector.action("temple-return",()->{temple.returnFromBattle();changeState(GameState.EXPLORATION);storyFade=1;}),
            StoryDirector.motion("temple-victory-fade-in",.7,p->storyFade=1-p),
            StoryDirector.say(DialogueBox.Face.NEUTRAL,"….อ่า…เรื่องมันชักจะน่าโมโหขึ้นมาแล้วสิ"));
    }

    private void enterButterfly(){
        inButterfly=true;butterfly.enter();clearInput();
        checkpointMapIfChanged();
        if(!mothEntrySeen){
            mothEntrySeen=true;storyFade=1;
            story.play(this::endMothScene,
                StoryDirector.motion("moth-arrival-fade",.6,p->storyFade=1-p),
                StoryDirector.say(DialogueBox.Face.CONFUSED,"ที่นี่ที่ไหน"),
                StoryDirector.say(DialogueBox.Face.CONFUSED,"ป่าอีกแล้วงั้นหรอ?"));
        }
    }

    private void endMothScene(){storyFade=0;butterfly.finishStory();temple.finishStory();clearInput();}

    private void playMothRoots(){
        mothRootsSeen=true;clearInput();
        story.play(this::endMothScene,
            StoryDirector.say(DialogueBox.Face.CONFUSED,"นี่มันอะไรกัน"),
            StoryDirector.say(DialogueBox.Face.NEUTRAL,"คงต้องหาทางเอามันออก"));
    }

    private void playMothEntrance(){
        clearInput();
        final double hx=butterfly.storyX(),hy=butterfly.storyY();
        story.play(()->{mothEncounterSeen=true;endMothScene();battleReadyTime=1;},
            StoryDirector.say(DialogueBox.Face.CONFUSED,"ที่นี่มันอะไรเนี่ย น่าขนลุดชะมัด"),
            StoryDirector.say(DialogueBox.Face.NONE,"เจ้าเป็นใคร"),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"นะ..นั่นใคร!?"),
            StoryDirector.motion("moth-camera-pan",1.1,p->{
                double e=p*p*(3-2*p);
                butterfly.focusStory(hx+8+(1200-hx-8)*e,hy+8+(780-hy-8)*e);
            }),
            StoryDirector.action("moth-look",()->butterfly.faceStoryHero()),
            StoryDirector.motion("moth-materializes",1.4,p->{
                butterfly.setStoryFlight(1390,740-Math.sin(p*Math.PI*4)*3,p*1.4,p*p*(3-2*p));
            }),
            StoryDirector.motion("moth-emerges",3.0,p->{
                double e=p*p*(3-2*p);
                butterfly.setStoryFlight(1390-190*e,740+60*e-Math.sin(p*Math.PI*8)*5,1.4+p*3,1);
            }),
            StoryDirector.action("moth-hover",()->butterfly.setStoryFlight(1200,800,.32,1)),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"คุณคือ…."),
            StoryDirector.say(DialogueBox.Face.MOTH,"อืม….."),
            StoryDirector.say(DialogueBox.Face.MOTH,"เจ้า….ช่างมีกลิ่นอายที่คุ้นเคยเสียจริง"),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"คุณรู้จักผม…."),
            StoryDirector.say(DialogueBox.Face.MOTH,"ข้าจำได้แล้ว…"),
            StoryDirector.say(DialogueBox.Face.MOTH,"เจ้า..เจ้าคือหายนะ เจ้าจะนำหายนะมาสู่เรา!"),
            StoryDirector.say(DialogueBox.Face.MOTH,"ข้าต้องกำจัด!"),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"หมายความว่ายังไง-"),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"เดี๋ยวสิ!"),
            StoryDirector.motion("moth-battle-fade-out",.5,p->storyFade=p),
            StoryDirector.action("moth-enter-battle",()->{
                selectedBoss=3;boss=new MothBoss(430,210);restartBattle();changeState(GameState.BATTLE);storyFade=1;
            }),
            StoryDirector.motion("moth-battle-fade-in",.65,p->storyFade=1-p),
            StoryDirector.say(DialogueBox.Face.MOTH,"หายนะ หายนะ ต้องกำจัด"),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"บ้าไปแล้วหรือเปล่าเนี่ย!"));
    }

    private boolean tryUnlockMothQ(){
        if(!mothStoryBattle||skillQUnlocked||!player.isAlive()||!boss.isAlive()
                ||(boss.getHp()*100>boss.getMaxHp()*65&&player.getHp()*100>player.getMaxHp()*60))return false;
        clearInput();projectiles.clear();enemyProjectiles.clear();laser.reset();
        ((MothBoss)boss).clearStoryHazards();
        story.play(()->{player.activateStoryUltimate();battleReadyTime=0;clearInput();((MothBoss)boss).resumeAfterSkillReveal();},
            StoryDirector.say(DialogueBox.Face.SHOCKED,"แฮ่ก…แฮ่ก"),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"เจ้านี่รับมือยากชะมัด"),
            StoryDirector.say(DialogueBox.Face.NEUTRAL,"ถ้าอย่างนั้น"),
            StoryDirector.motion("q-story-aura",.65,p->player.setStoryQAura(p)),
            StoryDirector.waitFor("q-story-aura-hold",.6),
            StoryDirector.say(DialogueBox.Face.MOTH,"เป็นเจ้าจริงด้วย"),
            StoryDirector.say(DialogueBox.Face.MOTH,"ตายซะเถอะ!"),
            StoryDirector.action("unlock-skill-q",()->skillQUnlocked=true),
            StoryDirector.say(DialogueBox.Face.NONE,"ปลดล็อคสกิล Ultimate\nกด Q เพื่อรับพลังที่แข็งแกร่งขึ้น"));
        return true;
    }

    private void playMothVictory(){
        mothVictorySeen=true;clearInput();projectiles.clear();enemyProjectiles.clear();laser.reset();
        story.play(this::endMothScene,
            StoryDirector.waitFor("moth-victory-breath",.6),
            StoryDirector.motion("moth-victory-fade-out",.6,p->storyFade=p),
            StoryDirector.action("moth-return",()->{butterfly.returnFromBattle();changeState(GameState.EXPLORATION);storyFade=1;}),
            StoryDirector.motion("moth-victory-fade-in",.7,p->storyFade=1-p),
            StoryDirector.say(DialogueBox.Face.RELIEVED,"ฮู่ว..เรื่องอะไรกันเนี่ย อยู่ๆก็มาใส่ร้ายกัน"),
            StoryDirector.say(DialogueBox.Face.NEUTRAL,"…ช่างเถอะ ออกไปจากที่นี่ดีกว่า น่าขนลุกชะมัด"));
    }

    private void enterMine(){
        inMine=true;mine.enter();clearInput();
        checkpointMapIfChanged();
        if(!mineEntrySeen){
            mineEntrySeen=true;storyFade=1;
            story.play(this::endMineScene,
                StoryDirector.motion("mine-arrival-fade",.6,p->storyFade=1-p),
                StoryDirector.say(DialogueBox.Face.CONFUSED,"ที่นี่…เหมืองงั้นเหรอ"));
        }
    }

    private void endMineScene(){storyFade=0;mine.finishStory();clearInput();}

    private void playMineEntrance(){
        clearInput();
        final double hx=mine.storyX(),hy=mine.storyY();
        final double[] walkProgress={0};
        mine.faceStoryHero(SpriteAnimator.UP);
        story.play(()->{mineEncounterSeen=true;mine.finishStory();storyFade=0;battleReadyTime=1;clearInput();},
            StoryDirector.waitFor("mine-notice-work",.3),
            StoryDirector.motion("mine-pan-to-work",1.3,p->{
                double ease=p*p*(3-2*p);
                mine.focusStory(hx+8+(910-hx-8)*ease,hy+8+(200-hy-8)*ease,1.45+.15*ease);
                mine.setWorkBob(Math.sin(p*Math.PI*8));
            }),
            StoryDirector.motion("scientist-working",1.2,p->mine.setWorkBob(Math.sin(p*Math.PI*8))),
            StoryDirector.say(DialogueBox.Face.SCIENTIST,"หืม…หนูทดลองตัวใหม่งั้นเหรอ"),
            StoryDirector.say(DialogueBox.Face.NEUTRAL,"…"),
            StoryDirector.motion("careful-walk-past",4.5,p->{
                double ease=p*p*(3-2*p);
                mine.moveStoryHero(hx+(900-hx)*ease,hy+(280-hy)*ease,(p-walkProgress[0])*4.5*.5);
                walkProgress[0]=p;
                mine.focusStory(910-10*ease,200+30*ease,1.6);
            }),
            StoryDirector.action("stop-in-lab",()->mine.faceStoryHero(SpriteAnimator.UP)),
            StoryDirector.say(DialogueBox.Face.SCIENTIST,"…หึ…อย่าเพิ่งไปสิครับ"),
            StoryDirector.say(DialogueBox.Face.SCIENTIST,"ให้ผมได้ชำแหละดูตัวคุณสักนิดก่อนสิ"),
            StoryDirector.action("scientist-turns",()->mine.faceScientistFront()),
            StoryDirector.waitFor("scientist-recognises",.7),
            StoryDirector.say(DialogueBox.Face.SCIENTIST,"…"),
            StoryDirector.say(DialogueBox.Face.SCIENTIST,"อ่า…นี่คุณ\nช่างเป็นหนูทดลองที่คุ้นเคยเสียจริง"),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"หมายความว่ายัง-"),
            StoryDirector.motion("mine-battle-fade-out",.5,p->storyFade=p),
            StoryDirector.action("mine-enter-battle",()->{
                selectedBoss=2;boss=new ScientistBoss(430,210);restartBattle();changeState(GameState.BATTLE);storyFade=1;
            }),
            StoryDirector.waitFor("mine-battle-black",.18),
            StoryDirector.motion("mine-battle-fade-in",.65,p->storyFade=1-p),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"นี่แก…หมายความว่ายังไงที่ว่าคุ้นเคย"),
            StoryDirector.say(DialogueBox.Face.SCIENTIST,"ผมไม่ได้มีหน้าที่ให้คำตอบอะไร\nกับหนูทดลองหรอกนะครับ"));
    }

    private boolean tryUnlockMineE(){
        if(!mineStoryBattle||skillEUnlocked||!boss.isAlive()||!player.isAlive()
                ||(boss.getHp()*5>boss.getMaxHp()*4&&player.getHp()*10>player.getMaxHp()*7))return false;
        clearInput();projectiles.clear();enemyProjectiles.clear();laser.reset();
        if(boss instanceof ScientistBoss)((ScientistBoss)boss).clearStoryHazards();
        final double[] chargeProgress={0},flightProgress={0};
        story.play(()->{
                battleReadyTime=0;player.resetSkillEAfterTutorial();clearInput();
                if(boss instanceof ScientistBoss)((ScientistBoss)boss).resumeAfterSkillReveal();
            },
            StoryDirector.action("skill-e-hit-reaction",player::showHitReaction),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"อ้าก!"),
            StoryDirector.say(DialogueBox.Face.SCIENTIST,"อ่อนแอเสียจริง"),
            StoryDirector.say(DialogueBox.Face.NEUTRAL,"งั้นเจอนี่ ย้าก!!"),
            StoryDirector.action("skill-e-auto-charge",()->{
                projectiles.clear();enemyProjectiles.clear();laser.reset();player.useSkillE();
            }),
            StoryDirector.motion("skill-e-charge",.6,p->{
                player.update((p-chargeProgress[0])*.6);chargeProgress[0]=p;
            }),
            StoryDirector.action("skill-e-auto-fire",()->{
                Projectile charged=player.pollSkillProjectile();
                if(charged!=null){
                    Rectangle heart=player.getBounds(),target=boss.getHitBounds();
                    projectiles.add(new SkillProjectile(heart.getCenterX(),heart.y,target.getCenterX(),target.getCenterY()));
                }
            }),
            StoryDirector.motion("skill-e-flight",1.2,p->{
                double dt=(p-flightProgress[0])*1.2;flightProgress[0]=p;player.update(dt);battleEffects.update(dt);
                java.util.Iterator<Projectile> shots=projectiles.iterator();
                while(shots.hasNext()){
                    Projectile shot=shots.next();shot.update(dt);
                    if(shot.getBounds().intersects(boss.getHitBounds())){showShotImpact(shot);boss.takeDamage(shot.getDamage());shots.remove();}
                }
            }),
            StoryDirector.action("skill-e-clear-arena",()->projectiles.clear()),
            StoryDirector.say(DialogueBox.Face.SCIENTIST,"....น่าสนใจจริงๆ"),
            StoryDirector.action("unlock-skill-e",()->skillEUnlocked=true),
            StoryDirector.say(DialogueBox.Face.NONE,"ปลดล็อคสกิล Heavy Attact\nกด E เพื่อรวบรวมพลังและโจมตี"));
        return true;
    }

    private void playMineVictory(){
        mineVictorySeen=true;clearInput();projectiles.clear();enemyProjectiles.clear();laser.reset();
        story.play(this::endMineScene,
            StoryDirector.waitFor("mine-victory-breath",.6),
            StoryDirector.motion("mine-victory-fade-out",.6,p->storyFade=p),
            StoryDirector.action("mine-return-from-battle",()->{mine.returnFromBattle();changeState(GameState.EXPLORATION);storyFade=1;}),
            StoryDirector.motion("mine-victory-fade-in",.7,p->storyFade=1-p),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"…ไม่เข้าใจอะไรเลยแฮะ"));
    }

    private boolean canActInBattle() {
        return state==GameState.BATTLE && !story.active() && battleReadyTime<=0;
    }

    private void endForestScene() {
        storyFade=0;storySkippable=false;exploration.finishStory();clearInput();
    }

    private void playForestOpening() {
        clearInput();storyFade=0;exploration.setStoryHeroPose(1);
        final double closeZoom=2.15;
        exploration.focusStoryHero(closeZoom);
        story.play(this::endForestScene,
            StoryDirector.motion("fade-from-menu",.7,p->storyFade=p),
            StoryDirector.waitFor("black",.3),
            StoryDirector.motion("wake-fade",1.3,p->storyFade=1-p),
            StoryDirector.waitFor("unconscious",.8),
            StoryDirector.action("wake-up",()->exploration.setStoryHeroPose(0)),
            StoryDirector.say(DialogueBox.Face.NEUTRAL,"อึก…"),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"ที่นี่…ที่ไหนกัน…"),
            StoryDirector.say(DialogueBox.Face.CONFUSED,"แล้วฉัน…อึก…เป็นใครกัน"),
            StoryDirector.waitFor("hold-head",.6),
            StoryDirector.motion("opening-pull-back",1.5,p->{
                double ease=p*p*(3-2*p);
                exploration.focusStoryHero(closeZoom+(ExplorationRoom.explorationZoom()-closeZoom)*ease);
            }));
    }

    private void inspectNearby(){if(inTemple)inspectTemple();else if(inButterfly)inspectButterfly();else if(inMine)inspectMine();else inspectForest();}

    private void inspectTemple(){
        if(state!=GameState.EXPLORATION||story.active()||!inTemple||inThrone||rematchActive)return;
        TempleRoom.InspectPoint point=temple.nearestInspection();if(point==null)return;
        clearInput();temple.faceStoryPoint(point.x,point.y);
        String[] lines=point.lines();StoryDirector.Beat[] beats=new StoryDirector.Beat[lines.length];
        for(int i=0;i<lines.length;i++)beats[i]=StoryDirector.say(DialogueBox.Face.CONFUSED,lines[i]);
        story.play(this::endTempleScene,beats);repaint();
    }

    private void inspectButterfly(){
        if(state!=GameState.EXPLORATION||story.active()||!inButterfly||inTemple||inThrone||rematchActive)return;
        ButterflyRoom.InspectPoint point=butterfly.nearestInspection();if(point==null)return;
        clearInput();butterfly.faceStoryPoint(point.x,point.y);
        String[] lines=point.lines();StoryDirector.Beat[] beats=new StoryDirector.Beat[lines.length];
        for(int i=0;i<lines.length;i++)beats[i]=StoryDirector.say(DialogueBox.Face.CONFUSED,lines[i]);
        story.play(this::endMothScene,beats);repaint();
    }

    private void inspectMine(){
        if(state!=GameState.EXPLORATION||story.active()||!inMine||inButterfly||inTemple||inThrone||rematchActive)return;
        MineRoom.InspectPoint point=mine.nearestInspection();if(point==null)return;
        clearInput();mine.faceStoryPoint(point.x,point.y);
        String[] lines=point.lines();StoryDirector.Beat[] beats=new StoryDirector.Beat[lines.length];
        for(int i=0;i<lines.length;i++){
            DialogueBox.Face face=point==MineRoom.InspectPoint.DOCUMENT&&i==0?DialogueBox.Face.NONE:DialogueBox.Face.CONFUSED;
            beats[i]=StoryDirector.say(face,lines[i]);
        }
        story.play(this::endMineScene,beats);repaint();
    }

    private void inspectForest(){
        if(state!=GameState.EXPLORATION||story.active()||inMine||inButterfly||inTemple||inThrone||rematchActive)return;
        ExplorationRoom.InspectPoint point=exploration.nearestInspection();if(point==null)return;
        clearInput();exploration.faceStoryPoint(point.x,point.y);
        String[] lines=point.lines();StoryDirector.Beat[] beats=new StoryDirector.Beat[lines.length];
        for(int i=0;i<lines.length;i++)beats[i]=StoryDirector.say(DialogueBox.Face.NEUTRAL,lines[i]);
        story.play(this::endForestScene,beats);repaint();
    }

    private void playForestCorpse() {
        forestCorpseSeen=true;clearInput();exploration.faceStoryPoint(390,367);
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
        story.play(()->{forestEncounterSeen=true;storySkippable=false;battleReadyTime=1;clearInput();},
            StoryDirector.waitFor("notice-feeding",.3),
            StoryDirector.motion("pan-to-feeding",1.4,p->{
                double ease=p*p*(3-2*p);
                double zoom=ExplorationRoom.explorationZoom();
                exploration.focusStory(hx+(1465-hx)*ease,hy-8+(390-(hy-8))*ease,zoom+(1.50-zoom)*ease);
            }),
            StoryDirector.motion("beast-strikes-prey",.4,p->{exploration.setStoryStrike(p);exploration.setStoryBoss(1,1465,425,Math.sin(p*Math.PI*2)*4);}),
            StoryDirector.action("strike-ends",()->exploration.setStoryStrike(0)),
            StoryDirector.motion("feeding",2.8,p->exploration.setStoryBoss(1,1465,425,Math.sin(p*Math.PI*12)*3)),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"…"),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"นั่นมัน…"),
            StoryDirector.action("beast-looks-up",()->exploration.setStoryBoss(2,1465,425,0)),
            StoryDirector.waitFor("growl",.8),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"แย่แล้ว…"),
            StoryDirector.motion("beast-approaches",.9,p->{
                double ease=p*p*(3-2*p);
                exploration.setStoryBoss(2,1465+(hx+240-1465)*ease,425+(hy-425)*ease,Math.sin(p*Math.PI*4)*1.5);
                exploration.focusStory(1465+((hx+1465)/2-1465)*ease,390+(hy-25-390)*ease,1.50);
            }),
            StoryDirector.waitFor("beast-poised",.15),
            StoryDirector.motion("beast-leaps",.65,p->{
                exploration.setStoryBoss(3,hx+240-230*p,hy-Math.sin(p*Math.PI)*65,0);
                storyFade=Math.max(0,(p-.65)/.35);
            }),
            StoryDirector.action("enter-battle",()->{
                selectedBoss=1;boss=new ForestBoss(430,210);restartBattle();changeState(GameState.BATTLE);
                exploration.finishStory();storyFade=1;
            }),
            StoryDirector.waitFor("battle-black",.18),
            StoryDirector.motion("battle-fade",.65,p->storyFade=1-p),
            StoryDirector.waitFor("battle-settle",.35),
            StoryDirector.say(DialogueBox.Face.SHOCKED,"ยะ...อย่าเข้ามานะ!"),
            StoryDirector.prompt("battle-controls","Hold right mouse to fire. WASD to dodge."));
    }

    private void finishForestEntrance() {
        story.cancel();selectedBoss=1;boss=new ForestBoss(430,210);forestStoryBattle=true;
        exploration.finishStory();storyFade=0;storySkippable=false;
        restartBattle();changeState(GameState.BATTLE);
    }

    private void returnAfterBattle(){
        if(rematchActive){finishRematch();return;}
        clearInput();projectiles.clear();enemyProjectiles.clear();laser.reset();
        story.play(()->{storyFade=0;clearInput();},
            StoryDirector.motion("return-fade-out",.6,p->storyFade=p),
            StoryDirector.action("return-map",()->{
                if(inThrone)throne.returnFromBattle();else if(inTemple)temple.returnFromBattle();
                else if(inButterfly)butterfly.returnFromBattle();else if(inMine)mine.returnFromBattle();
                else exploration.returnFromBattle();
                changeState(GameState.EXPLORATION);storyFade=1;
            }),
            StoryDirector.motion("return-fade-in",.7,p->storyFade=1-p));
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
                if((state==GameState.MENU||state==GameState.REMATCH||state==GameState.GAME_OVER||state==GameState.PAUSED)&&(index==0||index==2))moveMenu(index==0?-1:1);
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
                if(rematchActive){startRematch(number);return;}
                throneStoryBattle=false;
                forestStoryBattle=false;mineStoryBattle=false;mothStoryBattle=false;templeStoryBattle=false;
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
        if (!canActInBattle() || !canUseSkillE() || !player.isAlive() || !boss.isAlive()) {
            return;
        }
        player.useSkillE();
    }

    private void updateGame() {
        long now = System.nanoTime();
        double seconds = Math.min((now - previousTime) / 1_000_000_000.0, 0.05);
        previousTime = now;

        if(state==GameState.GAME_OVER)return;
        if(state==GameState.REMATCH){menuClock+=seconds;repaint();return;}
        if(state==GameState.HELP){menuClock+=seconds;repaint();return;}
        if(state==GameState.MENU){menuClock+=seconds;if(menuFade>0)menuFade=Math.max(0,menuFade-seconds/1.2);repaint();return;}
        if (state == GameState.PAUSED) return;
        if(story.active()){if(story.currentId().equals("fade-from-menu"))menuClock+=seconds;story.update(seconds);repaint();return;}
        if(state==GameState.BATTLE)battleEffects.update(seconds);
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
                    inTemple=false;enterThrone();clearInput();
                }
            } else if(inButterfly) {
                butterfly.update(dx,dy,seconds,running);
                if(butterfly.consumeBackRequest()) {
                    inButterfly=false; inMine=true; mine.returnFromButterfly(); clearInput();
                } else if(butterfly.consumeExitRequest()) {
                    inButterfly=false;enterTemple();clearInput();
                } else if(butterfly.consumeBattleRequest()) startButterflyBattle();
                else if(!mothRootsSeen&&butterfly.nearRoots())playMothRoots();
            } else if(inMine) {
                mine.update(dx,dy,seconds,running);
                if(mine.consumeBackRequest()) {
                    inMine=false; exploration.returnFromMine(); clearInput();
                } else if(mine.consumeExitRequest()) {
                    inMine=false; enterButterfly(); clearInput();
                } else if(mine.consumeBattleRequest()) startMineBattle();
            } else {
                exploration.update(dx,dy,seconds,running);
                if(!forestCorpseSeen && exploration.storyX()>=290 && exploration.storyX()<1100) {
                    playForestCorpse();
                } else if(!forestEncounterSeen && !exploration.isCleared() && exploration.storyX()>=1090) {
                    startForestBattle();
                } else if(exploration.consumeMineRequest()) {
                    enterMine();
                } else if(exploration.consumeBattleRequest()) startForestBattle();
            }
            checkpointMapIfChanged();repaint();
            return;
        }

        if(!player.isAlive()){showGameOver();return;}
        if (!boss.isAlive()) {
            if(!boss.isAlive() && player.isAlive() && forestStoryBattle && !forestVictorySeen) {
                playForestVictory();repaint();return;
            }
            if(!boss.isAlive()&&player.isAlive()&&mineStoryBattle&&!mineVictorySeen){playMineVictory();repaint();return;}
            if(!boss.isAlive()&&player.isAlive()&&mothStoryBattle&&!mothVictorySeen){playMothVictory();repaint();return;}
            if(!boss.isAlive()&&player.isAlive()&&templeStoryBattle&&!templeVictorySeen){playTempleVictory();repaint();return;}
            if(!boss.isAlive()&&player.isAlive()&&throneStoryBattle&&!throneVictorySeen){playThroneEnding();repaint();return;}
            returnAfterBattle();repaint();return;
        }

        if(tryThronePhase()||tryUnlockMineE()||tryUnlockMothQ()){repaint();return;}

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
                showShotImpact(projectile);
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
        if(laserDamage>0){Rectangle target=boss.getHitBounds();battleEffects.hit((int)player.getLaserBounds().getCenterX(),target.y+target.height,false);}

        if (!boss.isAlive()) {
            projectiles.clear();
            enemyProjectiles.clear();
            laser.reset();
            if(forestStoryBattle&&!forestVictorySeen)playForestVictory();
            else if(mineStoryBattle&&!mineVictorySeen)playMineVictory();
            else if(mothStoryBattle&&!mothVictorySeen)playMothVictory();
            else if(templeStoryBattle&&!templeVictorySeen)playTempleVictory();
            else if(throneStoryBattle&&!throneVictorySeen)playThroneEnding();
            else returnAfterBattle();
            repaint();
            return;
        }

        if(tryThronePhase()||tryUnlockMineE()||tryUnlockMothQ()){repaint();return;}
        double enemySeconds=seconds*(rematchActive?RematchRules.CLOCK_MULTIPLIER:1);
        boss.update(enemySeconds, battleArea, player.getBounds(), enemyProjectiles);
        boss.updateEffects(seconds);

        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            EnemyProjectile projectile = enemyProjectiles.get(i);
            projectile.update(seconds*(rematchActive?RematchRules.PROJECTILE_MULTIPLIER:1));
            if (projectile.shouldSplit()) {
                enemyProjectiles.addAll(projectile.split());
                enemyProjectiles.remove(i);
                continue;
            }
            if (projectile.isDangerous()
                    && projectile.getBounds().intersects(player.getBounds())) {
                player.takeDamage(projectile.getDamage());
                enemyProjectiles.remove(i);
            } else if (projectile.isOutsideScreen(getWidth(), getHeight())) {
                enemyProjectiles.remove(i);
            }
        }

        int hazardDamage=boss.getHazardDamage(player.getBounds());
        if(hazardDamage>0)player.takeDamage(hazardDamage);

        if (!player.isAlive()) {
            showGameOver();return;
        }
        repaint();
    }

    private void showGameOver(){
        projectiles.clear();enemyProjectiles.clear();laser.reset();battleEffects.reset();deathChoice=0;
        changeState(GameState.GAME_OVER);
    }
    private void retryFight(){restartBattle();changeState(GameState.BATTLE);}

    private void restartBattle() {
        clearInput();
        battleEffects.reset();
        laser.reset();
        projectiles.clear();
        enemyProjectiles.clear();
        player.reset(468, 500,CombatTuning.playerHpForStage(selectedBoss));
        boss.reset();
        if(boss instanceof MaskedBoss){((MaskedBoss)boss).setStoryControlled(throneStoryBattle);thronePhaseSeen=false;}
        battleReadyTime=1;
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
        if(state==GameState.HELP){drawHelp(g);return;}
        if(state==GameState.REMATCH){RematchScreen.draw(g,menuClock,rematchChoice,rematchWins);return;}
        if (state == GameState.MENU) {
            drawMenu(g);
            if(menuFade>0){g.setColor(new Color(0,0,0,(int)(255*menuFade)));g.fillRect(0,0,960,640);}
            return;
        }
        GameState visibleState = state == GameState.PAUSED ? stateBeforePause : state;
        if(story.currentId().equals("fade-from-menu"))drawMenu(g);
        else if (visibleState == GameState.EXPLORATION) { if(inThrone) throne.draw(g); else if(inTemple) temple.draw(g); else if(inButterfly) butterfly.draw(g); else if(inMine) mine.draw(g); else exploration.draw(g); }
        else drawBattle(g);
        if(state==GameState.EXPLORATION&&!story.active()&&!inThrone){
            if(inTemple)temple.drawInspectionPrompt(g);else if(inButterfly)butterfly.drawInspectionPrompt(g);else if(inMine)mine.drawInspectionPrompt(g);else exploration.drawInspectionPrompt(g);
        }
        if(story.active()) {
            if(cinematicFlash>.004){Graphics2D f=(Graphics2D)g.create();f.setComposite(java.awt.AlphaComposite.SrcOver);f.setColor(new Color(165,106,112,(int)(Math.min(1,cinematicFlash)*255)));f.fillRect(0,0,960,640);f.dispose();}
            if(phaseSlash>.004){Graphics2D s=(Graphics2D)g.create();s.setComposite(java.awt.AlphaComposite.SrcOver);s.setColor(new Color(230,218,215,(int)(phaseSlash*220)));s.setStroke(new BasicStroke(3));s.drawLine(425,158,540,78);s.dispose();}
            if(storyFade>0) {
                Graphics2D fade=(Graphics2D)g.create();fade.setColor(new Color(0,0,0,(int)(Math.min(1,storyFade)*255)));
                fade.fillRect(0,0,VIEW_WIDTH,VIEW_HEIGHT);fade.dispose();
            }
            if(story.currentId().equals("battle-controls"))MenuScreen.battleTutorial(g);
            else dialogue.draw(g);
            MenuScreen.skipButton(g,skipStoryButton);

        }
        if (state == GameState.PAUSED) drawPause(g);
        if(state==GameState.GAME_OVER)drawGameOver(g);
        if(!saveNotice.isEmpty()){g.setColor(new Color(244,173,156));g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));g.drawString(saveNotice,20,600);}
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
        MenuScreen.menu(g,menuClock,saved,menuChoice,continueButton,startButton,helpButton,rematchButton,saveNotice);
    }


    private void drawHelp(Graphics g){
        MenuScreen.help(g,menuClock,helpBackButton);
    }

    private void drawPause(Graphics g) {
        g.setColor(new Color(0, 0, 0, 215));
        g.fillRect(0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
        g.setColor(Color.WHITE);
        g.drawString("PAUSED", 405, 210);
        drawButton(g, resumeButton, "RESUME");
        drawButton(g,overlayHelpButton,"HOW TO PLAY");
        drawButton(g, menuButton, rematchActive?"BOSS SELECT":"MAIN MENU");
        Rectangle selected=pauseChoice==0?resumeButton:pauseChoice==1?overlayHelpButton:menuButton;
        g.setColor(HeroEffects.LIGHT);g.drawRect(selected.x-3,selected.y-3,selected.width+6,selected.height+6);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        g.drawString("Esc: Resume | Arrows + Enter: Select", 300, 530);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(rematchActive?"Rematch does not change your story save.":"Continue returns to this map's entrance.", 250, 565);
    }

    private void drawGameOver(Graphics graphics){
        Graphics2D g=(Graphics2D)graphics.create();
        g.setColor(new Color(6,8,12,245));g.fillRect(0,0,960,640);
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,60));g.setColor(HeroEffects.RED);
        String title="YOU DIED";g.drawString(title,(960-g.getFontMetrics().stringWidth(title))/2,205);
        drawButton(g,resumeButton,"RETRY");drawButton(g,overlayHelpButton,"HOW TO PLAY");drawButton(g,menuButton,rematchActive?"BOSS SELECT":"MAIN MENU");
        Rectangle selected=deathChoice==0?resumeButton:deathChoice==1?overlayHelpButton:menuButton;
        g.setColor(HeroEffects.LIGHT);g.drawRect(selected.x-3,selected.y-3,selected.width+6,selected.height+6);
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,16));g.setColor(new Color(205,196,188));
        String hint="Retry: Restart this fight";g.drawString(hint,(960-g.getFontMetrics().stringWidth(hint))/2,530);
        hint="W/S or arrows: Select | Enter: Confirm | R: Retry";g.drawString(hint,(960-g.getFontMetrics().stringWidth(hint))/2,565);
        g.dispose();
    }

    private void showShotImpact(Projectile shot){
        Rectangle impact=shot.getBounds().intersection(boss.getHitBounds());
        if(!impact.isEmpty())battleEffects.hit((int)impact.getCenterX(),(int)impact.getCenterY(),shot instanceof SkillProjectile);
    }

    private void drawBattle(Graphics g) {
        BattleHud.boss(g,boss,battleArea);
        if(rematchActive){g.setColor(new Color(163,118,124));g.setFont(new Font(Font.MONOSPACED,Font.BOLD,12));g.drawString("REMATCH +",710,25);}
        Graphics2D border=(Graphics2D)g.create();
        border.setColor(new Color(197,188,181));border.setStroke(new BasicStroke(2));
        border.drawRect(battleArea.x,battleArea.y,battleArea.width,battleArea.height);border.dispose();
        if(player.isAlive()&&boss.isAlive()){boss.drawHazards(g);laser.draw(g);}
        Graphics2D art=(Graphics2D)g.create();
        if(player.isAlive()&&boss.isAlive())boss.drawEffects(art,selectedBoss);
        boss.draw(art);art.dispose();
        player.draw(g);
        for(Projectile shot:projectiles)shot.draw(g);
        battleEffects.draw(g);
        Graphics2D enemy=(Graphics2D)g.create();enemy.clip(battleArea);
        for(EnemyProjectile shot:enemyProjectiles)
            shot.draw(enemy,selectedBoss,selectedBoss==5&&boss.getPhaseLabel().equals("PHASE 2"));
        enemy.dispose();
        BattleHud.player(g,player,battleArea,skillEUnlocked,skillQUnlocked);
        if(devOverlay)drawBattleDebug(g);
    }
    private void drawBattleDebug(Graphics g){
        Rectangle hit=boss.getHitBounds();
        g.setColor(new Color(102,190,181));
        g.drawRect(hit.x,hit.y,hit.width-1,hit.height-1);
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,13));g.setColor(new Color(163,151,158));
        String[] lines={"DEV / F1: Hide","Boss "+selectedBoss+" / 5","HP "+boss.getHp()+"/"+boss.getMaxHp(),
                boss.getAttackName(),boss.getPhaseLabel(),"Target "+boss.getDifficultyLabel(),
                "1-5: Select boss","F6: Skip map","F7: Replay map"};
        for(int i=0;i<lines.length;i++)g.drawString(lines[i],700,320+i*23);
    }
}
