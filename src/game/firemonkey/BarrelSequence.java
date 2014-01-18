package game.firemonkey;

import java.util.ArrayList;
import java.util.Random;

import com.bag.lib.math.Vector2;

public class BarrelSequence {

	public final static int STATE_ACTIVE 	= 0;
	public final static int STATE_DEAD 		= 1;
	
	public final static int DIFF_EASY 	= 0;
	public final static int DIFF_MEDIUM = 1;
	public final static int DIFF_HARD 	= 2;
	
	public ArrayList<BarrelToken> tokens;
	public ArrayList<Vector2> anchorPoints;
	
	public float completionBonus;
	public float velocityBonus;
	public int nextIndex;
	
	public int state;
	
	public BarrelSequence(int difficulty, float height)
	{
		state = STATE_ACTIVE;
		tokens = new ArrayList<BarrelToken>();
		anchorPoints = new ArrayList<Vector2>();
		nextIndex = 0;
		completionBonus = 0;
		generate(difficulty, height);
	}
	
	public void update(float deltaTime)
	{
		for (int i = 0; i < tokens.size(); i++) {
			BarrelToken bt = tokens.get(i);
			bt.update(deltaTime);
			
			if(bt.actualTime <= 0)
				bt.success = false;
		}
		
//		if(tokens.size() == 0) {
//			state = STATE_DEAD;
//			finalizeSequence();
//		}
			
	}
	
	private void generate(int difficulty, float height)
	{
		int nbTokens = 0;
		float size = 1.0f;
		float bonus = 1.0f;
		float time = 1.0f;
		
		if (difficulty == DIFF_EASY) {
			nbTokens = 3;
			anchorPoints.add(new Vector2(100, 1000));
			anchorPoints.add(new Vector2(650, 1000));
			anchorPoints.add(new Vector2(760/2, 300));

			size = 140.0f;
			bonus = 20.0f;
			time = 5.0f;
			
		} else if (difficulty == DIFF_MEDIUM) {
			nbTokens = 4;
			anchorPoints.add(new Vector2(200, 800));
			anchorPoints.add(new Vector2(600, 800));
			anchorPoints.add(new Vector2(200, 200));
			anchorPoints.add(new Vector2(600, 200));
			size = 100.0f;
			bonus = 30.0f;
			time = 1.5f;
			
		} else if (difficulty == DIFF_HARD) {
			nbTokens = 5;
			
		}
		
		Random rand = new Random();
		for (int i = 0; i < nbTokens; i++) {
			Vector2 pos = anchorPoints.get(i);			
			BarrelToken bt = new BarrelToken(pos.x, pos.y, size, size, i, time, bonus);
			tokens.add(bt);
		}
	}
	
	public void inputSequence(BarrelToken bt)
	{
		if(bt.index == nextIndex) {
			bt.success = true;
			nextIndex = bt.index + 1;
			// PLAY COOL SOUND
		} else if (bt.index < tokens.size()){
			nextIndex = bt.index + 1;
			// PLAY BAD SOUND
		} else {
			finalizeSequence();
			// PLAY WIN SOUND
		}	
		
		bt.touched = true;
		
		int touched = 0;
		for (BarrelToken t : tokens) {
			touched += (t.touched) ? 1 : 0;
		}
		
		if(touched == tokens.size())
			finalizeSequence();
	}
	
	public void finalizeSequence()
	{
		float bonus = 15.0f;
		for (BarrelToken bt : tokens) {
			
			if(bt.success) {
				velocityBonus += bt.bonus;
			}
		}
		
		state = STATE_DEAD;
		completionBonus = bonus + velocityBonus;
	}
}
