package com.meteos;

import java.util.Random;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Game implements ApplicationListener {
	private OrthographicCamera camera;
	private Mesh testBrick;
	private Texture texture;
	private SpriteBatch spriteBatch;
	
	private boolean isClicking = false;

	//I choose 9 tall and 7 wide because the texture I'm using for each brick is 64x64
	//with my Infuse's screen resolution at 480x800, and giving a 200px padding at the top for the score
	//that gives us 9 high and 7 wide spots for the bricks
	private static int NUMBER_OF_ROWS = 9;//14;
	private static int NUMBER_OF_COLUMNS = 7;//11;
	private Brick[][] mBricks;
	
	private long mTimeOfLastBrickDrop;
	private long mTimeBetweenBrickDrops;
	private long mTimeOfLastBrickMove;
	private long mTimeBetweenBrickMoves;
	
	//the screen resolution
	private int mScreenWidth, mScreenHeight;
	
	//current block you're touching
	private int mi_CurrentBlockRow, mi_CurrentBlockCol;

	@Override
	public void create() {
		
		if ( testBrick == null ) {
			testBrick = new Mesh(true, 4, 4,
					new VertexAttribute(Usage.Position, 3, "a_position"));
			
			testBrick.setVertices(new float[] {
					-0.5f, -0.5f, 0,
					0.5f, -0.5f, 0,
					-0.5f, 0.5f, 0,
					0.5f, 0.5f, 0
			});
			
			testBrick.setIndices(new short[] {0, 1, 2, 3});
		}
		
		texture = new Texture(Gdx.files.internal("test-brick.png"));
		spriteBatch = new SpriteBatch();

		//create the size of the field
		mBricks = new Brick[NUMBER_OF_ROWS][NUMBER_OF_COLUMNS];
		
		//set the time between brick drops
		mTimeBetweenBrickDrops = 10; //4 seconds. this is all handled in milliseconds because that's how java gives me time
		mTimeBetweenBrickMoves = 500;
		
		//I'm just going to set the last brick drop to the time when the game started running.
		//TODO: change this when I have a menu and things, because obviously there aren't bricks dropping during the menu
		mTimeOfLastBrickDrop = System.currentTimeMillis();
		mTimeOfLastBrickMove = System.currentTimeMillis();
		
		mi_CurrentBlockRow = -1;
		mi_CurrentBlockRow = -1;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		camera.update();
		camera.apply(Gdx.gl10);
		spriteBatch.setProjectionMatrix(camera.combined);
		
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		//testBrick.render(GL10.GL_TRIANGLE_STRIP, 0, 4);

		//spriteBatch.begin();
		//spriteBatch.draw(texture, convertNormalXToGLX(0), convertNormalYToGLY(0), 42, 42, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
		//spriteBatch.end();
		
		
		/**
		 * My algorithm to move the bricks down seemlessly on the screen as well as in the code's 2d array:
		 * 
		 * When brick is born, it checks to see if there is a new block below it and if there isn't, it sets moving down to true
		 * 		if there is a block below it right when it's born, well it can't move so set moving down to false because it's not going anywhere
		 * 
		 * Now when you start rendering, it'll be moving down. Call move down.
		 * Now after it has been moved, render it and put it on the screen.
		 * Then after it is moved on the screen, check and see if it's new position is it's ending spot from the current move.
		 * If it is, then set moving down to false and stop, then check if it can move down and start the whole process over again
		 */
		
		spriteBatch.begin();
		//render all of the bricks
		for ( int row = 0; row < NUMBER_OF_ROWS; row++ ) {
			for ( int col = 0; col < NUMBER_OF_COLUMNS; col++ ) {
				//get the current brick
				Brick currentBrick = mBricks[row][col];
				
				//there is nothing to do if there is no brick here
				if ( currentBrick == null )
					continue;
				
				//some blocks are getting stuck at the top row for no reason, so this is a check to see if any top row blocks are stuck when they shouldnt be
				//and to get them moving if they are
				//TODO: Find out the cause of this problem and fix it because this seems inefficient
				if ( row == 0 && mBricks[1][col] == null && !currentBrick.isMovingDown())
					currentBrick.setMoveDown(true);
				
				currentBrick.moveDown();
					
				if ( row == mi_CurrentBlockRow && col == mi_CurrentBlockCol ) {
					spriteBatch.setColor(1, 0, 0, 1);
				} else {
					float[] color = currentBrick.getMf_Color();
					spriteBatch.setColor(color[0], color[1], color[2], color[3]);
				}
				
				spriteBatch.draw(texture, convertNormalXToGLX(currentBrick.getMf_x()), convertNormalYToGLY(currentBrick.getMf_y()),42, 42, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
					
				/*if ( currentBrick.checkIfDoneMoving() ) {
					mBricks[row+1][col] = currentBrick;
					mBricks[row][col] = null;
					if ( row+2 < NUMBER_OF_ROWS ) {
						if ( mBricks[row+2][col] == null ) {
							//it can keep moving down, so move it down more
							currentBrick.setMoveDown(true);
						}
					}
				
				}*/
				
				if ( currentBrick.checkIfDoneMoving() ) {
					mBricks[row+1][col] = currentBrick;
					mBricks[row][col] = null;
					
					if ( row + 2 != NUMBER_OF_ROWS ) {
						if ( mBricks[row+2][col] == null ) {
							mBricks[row+1][col].setMoveDown(true);
						}
					}
				}
			}
		}
		spriteBatch.end();
		processInput();
		//moveBricks();
		addMoreBricks();
		
	}
	
	private void processInput() {		
		if ( Gdx.input.justTouched() ) {
			System.out.println("Started Click: Touched at X: " + Gdx.input.getX() + " - Y: " + Gdx.input.getY());
			isClicking = true;
			
			int x = Gdx.input.getX();
			int r = x / 42;
			mi_CurrentBlockCol = r;
			mi_CurrentBlockRow = (Gdx.input.getY() - 200) / 42;
			System.out.println("Row: " + mi_CurrentBlockRow + " - Col: " + mi_CurrentBlockCol);
		}
		
		if (!Gdx.input.isTouched() && isClicking) {
			System.out.println("Let go of click: at X: " + Gdx.input.getX() + " - Y: " + Gdx.input.getY());
			isClicking = false;
			
			//mi_CurrentBlockRow = -1;
			//mi_CurrentBlockCol = -1;
		}
		
	}
	
	private void addMoreBricks() {
		if ( System.currentTimeMillis() - mTimeOfLastBrickDrop > mTimeBetweenBrickDrops) {
			//time to throw in a new brick!
			
			//pick a random number between 0 and NUMBER_OF_COLUMNS to decide which column to drop it in
			Random generator = new Random();
			int columnToDropIn = generator.nextInt(NUMBER_OF_COLUMNS);
			
			//check if there is already a brick in the top most level in that column
			Brick check = mBricks[0][columnToDropIn];
			if ( check != null ) {
				//end the game or something...
			} else {
				float x,y;
				x = 42*columnToDropIn;
				y = mScreenHeight-42;
				mBricks[0][columnToDropIn] = new Brick(x,y);
				
				//check to see if there is a block below it
				if ( mBricks[1][columnToDropIn] == null) {
					//since there isn't set it to be moving
					mBricks[0][columnToDropIn].setMoveDown(true);
				}
			}
			
			mTimeOfLastBrickDrop = System.currentTimeMillis();
		}
	}
	
	/*private void moveBricks() {
		if ( System.currentTimeMillis() - mTimeOfLastBrickMove > mTimeBetweenBrickMoves) {
			
			//for the first loop, i did NUMBER_OF_ROWS - 2 for two reasons.
			//1. since im starting from the bottom, NUMBER_OF_ROWS is the total number of rows, gotta subtract 1
			//2. the bottom row can't go anywhere, so why bother checking it, gotta subtract 1
			for ( int row = NUMBER_OF_ROWS - 2; row >= 0; row-- ) {
				for ( int col = 0; col < NUMBER_OF_COLUMNS; col++ ) {
					//first check and see if there is even a block to move...
					if ( mBricks[row][col] == null) {
						//if not then there is nothing to do, move on...
						continue;
					}
					
					//check and see if there is a brick below the current brick, and if not then move the current brick there
					if ( mBricks[row+1][col] == null && !mBricks[row][col].isMovingDown() ) {
						mBricks[row+1][col] = mBricks[row][col];
						mBricks[row][col] = null;
						//mBricks[row+1][col].moveDown();
						mBricks[row+1][col].setMoveDown();
					}
				}
			}
			
			mTimeOfLastBrickMove = System.currentTimeMillis();
		}
	}*/
	
	private float convertNormalXToGLX(float x) {
		return -(mScreenWidth/2) + x + 9;
	}
	
	private float convertNormalYToGLY(float y) {
		return -(mScreenHeight/2) + y - 200;
		//return y;
	}

	@Override
	public void resize(int width, int height) {
		//float aspectRatio = (float) width / (float) height;
		//camera = new OrthographicCamera(2f * aspectRatio, 2f);
		camera = new OrthographicCamera(width, height);
		
		mScreenWidth = width;
		mScreenHeight = height;
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

}
