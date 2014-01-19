package game.firemonkey;

import java.util.List;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

import com.bag.lib.Game;
import com.bag.lib.Music;
import com.bag.lib.Input.TouchEvent;
import com.bag.lib.gl.Camera2D;
import com.bag.lib.gl.SpriteBatcher;
import com.bag.lib.impl.GLScreen;
import com.bag.lib.math.OverlapTester;
import com.bag.lib.math.Vector2;

import game.firemonkey.World.WorldListener;

public class GameScreen extends GLScreen {
	
	public static GameScreen instance;
	
	// States 
    static final int GAME_READY 	= 0;    
    static final int GAME_RUNNING 	= 1;
    static final int GAME_PAUSED 	= 2;
    static final int GAME_LEVEL_END = 3;
    static final int GAME_OVER 		= 4;
    static final int GAME_ENTERING 	= 5;   
    
    // Touch States
    static final int STATE_TOUCH_STARTING = 10;
    static final int STATE_TOUCH_FLYING = 11;
    static final int STATE_TOUCH_REFUEL = 12;
    
    // Screen size
    static final int SCREEN_WIDTH	= 768;
    static final int SCREEN_HEIGHT	= 1280;

    int 			state;
    int 			touchState;
    
    Camera2D 		guiCam;
    Vector2 		touchPoint;

    SpriteBatcher 	batcher;
    GameUI			gameUI;
    
    World 			world;
    WorldListener 	worldListener;
    WorldRenderer 	renderer;
    
    float 			startTime;
    float 			elapsedTime;
    float 			transitionTime;
    
	public int currentLevelIndex;
	
    public GameScreen(Game game) {
        super(game);
        
        state = GAME_ENTERING;
        touchState = STATE_TOUCH_STARTING;
        
        guiCam = new Camera2D(glGraphics, SCREEN_WIDTH, SCREEN_HEIGHT);
        touchPoint = new Vector2();
        
        // Create a worldListener, to trigger events on the world
        worldListener = new WorldListener() {		
			public int getTime() {
				return (int)elapsedTime;			
			}

			@Override
			public void playBananaHit() {
				Random rand = new Random();
				float r = rand.nextFloat();
				
				if(r > 0.5)
					Assets.bananaSound_1.play(0.5f);		
				else
					Assets.bananaSound_2.play(0.5f);
			}
			
			public void playBarrelOut() {
				Assets.barrelSound_1.play(0.5f);		
			}
			
			public void playBonusAcquired() {
				Assets.bonusSound_1.play(0.5f);		
			}
        };
        
        selectMusic();
        
        batcher = new SpriteBatcher(glGraphics, 5000);
        gameUI = new GameUI(batcher);
        
        world = new World(worldListener, gameUI);
        renderer = new WorldRenderer(glGraphics, batcher, world);
        
        // Variables
        transitionTime = 1.0f;
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
    }
    
	/**************************************************
	 * 
	 * 	GAMESCREEN UPDATE SECTION
	 * 
	 **************************************************/
	@Override
	public void update(float deltaTime) {
	    
/*		if(deltaTime > 0.1f)
	        deltaTime = 0.1f;*/
	    
	    switch(state) {
	    case GAME_ENTERING:
	    	updateEntering(deltaTime);
	        break;
	    case GAME_READY:
	        updateReady();
	        break;        
	    case GAME_RUNNING:
	        updateRunning(deltaTime);
	        break;
	    case GAME_PAUSED:
	        updatePaused();
	        break;
	    case GAME_OVER:
	        updateGameOver();
	        break;
	    }
	}
	
	// Update when state is ENTERING
	private void updateEntering(float deltaTime) {
		
	    if(transitionTime <= 0.0f) { // 1 second wait time
	        state = GAME_READY;
	    }
	    transitionTime -= deltaTime/1.3f;
	}
	
	// Update when state is READY
	private void updateReady() {
		// First touch 
	    if(game.getInput().getTouchEvents().size() > 0) {
	        state = GAME_RUNNING;
	        
	    }
	}
	
	// Update when state is RUNNING
	private void updateRunning(float deltaTime) {
	    
		// Check Touch Events
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
	    int len = touchEvents.size();
	    for(int i = 0; i < len; i++) {
	        TouchEvent event = touchEvents.get(i);
	        touchPoint.set(event.x, event.y);
	        guiCam.touchToWorld(touchPoint);
	        
	        UITouchHandler(event, touchPoint);
	        worldTouchHandler(event, touchPoint);
	        
	    }    
	    elapsedTime += deltaTime;
	    
	    // Update World
	    world.update(deltaTime, game.getInput().getAccelX());
	    
	    if(world.monkey.state == Monkey.PLAYER_STATE_BONUS)
	    	renderer.cam.position.y = world.monkey.position.y;
	    
	    if(world.state == World.WORLD_STATE_GAME_OVER)
	    	this.state = GAME_OVER;
	}
	
	private void updatePaused() 
	{
		
	}	
	
	private void updateGameOver() {
	    try {
			List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		    int len = touchEvents.size();
		    for(int i = 0; i < len; i++) {      
		        TouchEvent event = touchEvents.get(i);
		        touchPoint.set(event.x, event.y);
		        guiCam.touchToWorld(touchPoint);
		        
		        if(event.type == TouchEvent.TOUCH_UP){
		        	stopMusic();
                    if(World.GAME_MODE == World.GAME_MODE_CAMPAIGN) {
                        game.setScreen(new LevelSelectorScreen(game));
                    } else if(World.GAME_MODE == World.GAME_MODE_QUICKSTART) {
                        game.setScreen(new MainMenuScreen(game));
                    }
		        }
		    }
		    } catch (Exception e){}
	}
	
	private void worldTouchHandler(TouchEvent event, Vector2 point)
	{
		// Convert for world coordinate (ex 16:9 rather than 1280x768)
		point.x = (point.x/SCREEN_WIDTH) * WorldRenderer.FRUSTUM_WIDTH;
		point.y = (point.y/SCREEN_HEIGHT) * WorldRenderer.FRUSTUM_HEIGHT;
		
		
		switch (world.monkey.state) {
		
		// No prior touch states detected
		case Monkey.PLAYER_STATE_BONUS:
			if(event.type == TouchEvent.TOUCH_DRAGGED ||event.type == TouchEvent.TOUCH_DOWN){     
	         	
	        } else if(event.type == TouchEvent.TOUCH_UP){        	
	        	
	        }
			break;

		default:
			if(event.type == TouchEvent.TOUCH_DRAGGED ||event.type == TouchEvent.TOUCH_DOWN){     
	         	
	        } 
			else if(event.type == TouchEvent.TOUCH_UP){
	        	
				if(world.monkey.state != Monkey.PLAYER_STATE_BONUS && world.monkey.immuneTime <= 0){
					if(world.monkey.firstJump == true){
		        		world.monkey.firstJump = false;
		        		world.monkey.velocity.y = 30;
		        	}
		        	else if(world.monkey.jump > 0 && world.monkey.jump <= Monkey.PLAYER_DEFAULT_JUMPS && world.monkey.firstJump == false){
			        		world.monkey.velocity.y = 30;
				        	world.monkey.jump--; 
		        	}
				}	        		
	        } 
			break;
		}
	}
	
	// Defines interaction with UI Elements according to different states
	private void UITouchHandler(TouchEvent event, Vector2 point)
	{
		switch (world.monkey.state) {
		
		// No prior touch states detected
		case Monkey.PLAYER_STATE_BONUS:
			if(event.type == TouchEvent.TOUCH_DRAGGED ||event.type == TouchEvent.TOUCH_DOWN){     
	         	
	        } else if(event.type == TouchEvent.TOUCH_UP){        	
	        	world.touchToken(point);
	        }
			break;

		default:
			if(event.type == TouchEvent.TOUCH_DRAGGED ||event.type == TouchEvent.TOUCH_DOWN){     
	         	
	        } else if(event.type == TouchEvent.TOUCH_UP){

	        }
			break;
		}
	}

	/**************************************************
	 * 
	 * 	GAMESCREEN DRAWING SECTION
	 * 
	 **************************************************/
	
	@Override
	public void present(float deltaTime) {
		
		deltaTime /= 2; // to adjust framerate

		GL10 gl = glGraphics.getGL();
	    
		// Render Game objects
		for(int i = 0; i < 2; i++){
		    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		    gl.glEnable(GL10.GL_TEXTURE_2D);
			renderer.render();
		}
	    
	    guiCam.setViewportAndMatrices();
	    gl.glEnable(GL10.GL_BLEND);
	    gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	    gl.glColor4f(1, 1, 1, 1);
	    
		// Draw the UI for current State
	    switch(state) {
	    case GAME_ENTERING:
	    	presentEntering(gl);
	        break;
	    case GAME_READY:
	        presentReady();
	        break;
	    case GAME_RUNNING:
	        presentRunning();
	        break;
	    case GAME_PAUSED:
	        presentPaused();
	        break;
	    case GAME_LEVEL_END:
	        presentLevelEnd();
	        break;
	    case GAME_OVER:
	        presentGameOver();
	        break;
	    }

	    gl.glDisable(GL10.GL_BLEND);
		
	}
	
	private void presentEntering(GL10 gl) 
	{
		batcher.beginBatch(Assets.tileMapItems);
		
		gl.glColor4f(1, 1, 1, transitionTime);
  	  	batcher.drawSprite(SCREEN_WIDTH/2, SCREEN_HEIGHT/2, SCREEN_WIDTH, SCREEN_HEIGHT, Assets.whiteTween);
		batcher.endBatch();
		
		gl.glColor4f(1, 1, 1, 1);
	}
	
	private void presentReady() {
         
		 batcher.beginBatch(Assets.fontTex);
         Assets.font.drawText(batcher, "PREPARE FOR LAUNCH!", 262, 520);
         Assets.font.drawText(batcher, "TOUCH TO START!", 300, 560);
         batcher.endBatch();
		
         gameUI.draw();
	}
	
	private void presentRunning() {
		
		 batcher.beginBatch(Assets.fontTex);
         Assets.font.drawText(batcher, "X" + world.score, 100, 30);
         batcher.endBatch();
         
         batcher.beginBatch(Assets.fontTex);
         Assets.font.drawText(batcher, "X" + world.monkey.jump, 700, 30);
         batcher.endBatch();
                  
         gameUI.draw();	
         
        // Touch tokens
     	if(world.activeBarrel == null || world.activeBarrel.sequence == null)
    		return;
    	
     	try{
	     	batcher.beginBatch(Assets.numbersTexture);
	    	if(world.activeBarrel.sequence != null) {
		    	for (BarrelToken bt : world.activeBarrel.sequence.tokens) {
		    		if(!bt.touched) {
						batcher.drawSprite(bt.position.x, bt.position.y, 128, 128, Assets.numbersList.get(bt.index));
		    		}
				}
	    	}
    	batcher.endBatch();
     	} catch (Exception e) {}

	}
	
	private void presentPaused() { 
		gameUI.draw();
	}
	
	private void presentLevelEnd() {
		gameUI.draw();
	}
	
	private void presentGameOver() {
		
		 batcher.beginBatch(Assets.fontTex);
		 Assets.font.drawText(batcher, "Your Monkey ran ", 150, 840);
		 Assets.font.drawText(batcher, "out of bananas!", 150, 800);
		 Assets.font.drawText(batcher, "Game Over!", 230, 700);
         Assets.font.drawText(batcher, "- Final Score -", 140, 600);
         Assets.font.drawText(batcher, "" + world.score, 340, 560);
         batcher.endBatch();
		
		gameUI.draw();
	}

    @Override
    public void pause() {
    	
    }

    @Override
    public void resume() {   
    	
    }

    @Override
    public void dispose() {       
    }
    
    
    // Music
    
    public void selectMusic() 
    {
    	if (World.currentLevel == 1) {
    		
    	} else if (World.currentLevel == 1) {
    		
    	} else {
    		Assets.spaceMusic.play();
    	}
    }
    
    public void stopMusic() 
    {
    	if (World.currentLevel == 1) {
    		
    	} else if (World.currentLevel == 1) {
    		
    	} else {
    		Assets.spaceMusic.stop();
    	}
    }
}


