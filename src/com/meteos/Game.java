package com.meteos;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public class Game implements ApplicationListener {
	private OrthographicCamera camera;
	private Mesh testBrick;
	private Texture texture;
	private SpriteBatch spriteBatch;

	private boolean isClicking = false;

	// I choose 9 tall and 7 wide because the texture I'm using for each brick
	// is 64x64
	// with my Infuse's screen resolution at 480x800, and giving a 200px padding
	// at the top for the score
	// that gives us 9 high and 7 wide spots for the bricks
	private static int NUMBER_OF_ROWS = 9;// 14;
	private static int NUMBER_OF_COLUMNS = 7;// 11;
	private Brick[][] mBricks;
	
	private ArrayList<Brick> mBlocks[];

	private long mTimeOfLastBrickDrop;
	private long mTimeBetweenBrickDrops;
	private long mTimeOfLastBrickMove;
	private long mTimeBetweenBrickMoves;

	// the screen resolution
	private float mScreenWidth, mScreenHeight;
	private float mHeaderBuffer; //padding at the top of the screen

	// block dimensions
	float mBlockWidth, mBlockHeight;

	// current block you're touching
	private int mi_CurrentBlockRow, mi_CurrentBlockCol;
	
	Random generator;
	
	private boolean mb_BlockIsFalling = false; //only one block can fall at a time. this says whether or not a block is currently falling

	@Override
	public void create() {
		texture = new Texture(Gdx.files.internal("test-brick.png"));
		spriteBatch = new SpriteBatch();

		// create the size of the field
		mBricks = new Brick[NUMBER_OF_ROWS][NUMBER_OF_COLUMNS];
		
		mBlocks = new ArrayList[NUMBER_OF_COLUMNS];
		for ( int i = 0; i < NUMBER_OF_COLUMNS; i++ )
			mBlocks[i] = new ArrayList<Brick>();

		// set the time between brick drops
		mTimeBetweenBrickDrops = 1000; // 4 seconds. this is all handled in
										// milliseconds because that's how java
										// gives me time
		mTimeBetweenBrickMoves = 500;

		// I'm just going to set the last brick drop to the time when the game
		// started running.
		// TODO: change this when I have a menu and things, because obviously
		// there aren't bricks dropping during the menu
		mTimeOfLastBrickDrop = System.currentTimeMillis();
		mTimeOfLastBrickMove = System.currentTimeMillis();

		mi_CurrentBlockRow = -1;
		mi_CurrentBlockRow = -1;
		
		generator = new Random();
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

		addMoreBricks();
		processInput();
		simulate();
		draw();
	}
	
	private void draw() {
		spriteBatch.begin();
		// render all of the bricks

		for ( int col = 0; col < NUMBER_OF_COLUMNS; col++ ) {
			ArrayList currentList = mBlocks[col];
			if ( currentList == null || currentList.size() == 0)
				continue;
			
			for ( int i = 0; i < currentList.size(); i++) {
				Brick currentBrick = (Brick)currentList.get(i);

				float[] color = currentBrick.getColor();
				if ( i == mi_CurrentBlockRow && col == mi_CurrentBlockCol && !getBrickAt(i, col).isMovingDown()) {
					spriteBatch.setColor(0.3f*color[0], 0.3f*color[1], 0.3f*color[2], color[3]);
				} else {
					spriteBatch.setColor(color[0], color[1], color[2], color[3]);
				}
				spriteBatch.draw(texture,
						convertNormalXToGLX(currentBrick.getMf_x()),
						convertNormalYToGLY(currentBrick.getMf_y()),
						mBlockWidth, mBlockHeight, 0, 0, texture.getWidth(),
						texture.getHeight(), false, false);
			}
		}
				
		spriteBatch.end();
	}
	
	private void simulate() {
		float bottomOfScreen = 0.17f;//-(mScreenHeight/2);
		for ( int col = 0; col < NUMBER_OF_COLUMNS; col++ ) {
			ArrayList<Brick> currentList = mBlocks[col];
			if ( currentList == null || currentList.size() == 0)
				continue;
			
			for ( int i = 0; i < currentList.size(); i++ ) {
				Brick currentBrick = (Brick)currentList.get(i);
				float bottomOfCurrentBrick = currentBrick.getMf_y() - mBlockHeight;
				
				if ( bottomOfCurrentBrick <= bottomOfScreen) {
					if ( currentBrick.isMovingDown() ) {
						currentBrick.setMoveDown(false);
						mb_BlockIsFalling = false;
					}
					continue;	
				}
				
				boolean bCanMoveDown = true;
				for ( int j = 0; j < currentList.size(); j++ ) {
					if ( j == i ) continue;
					
					Brick tempBrick = (Brick)currentList.get(j);
					float topOfTempBrick = tempBrick.getMf_y();
					
					if ( bottomOfCurrentBrick <= topOfTempBrick ) 
						bCanMoveDown = false;
					
				}
				
				if ( bCanMoveDown && currentBrick.isMovingDown())
					currentBrick.moveDown();
				else {
					if ( currentBrick.isMovingDown() ) {
						currentBrick.setMoveDown(false);
						mb_BlockIsFalling = false;
						
						//if it's not the bottom brick, set it's y position to be a block above the brick below it. this will fix the overlapping error
						if ( i > 0 ) {
							Brick brickBelow = currentList.get(i-1);
							float yBelow = brickBelow.getMf_y();
							
							currentBrick.setMf_y(yBelow+mBlockHeight);
							
						}
					}
				}
			}
		}
	}

	private void processInput() {

		Vector3 input = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(input);
		
		input.x += (mScreenWidth/2); //this sets the x coordinate on a value from 0 to whatever the screen size is
		input.y += (mScreenHeight/2);
		
		if (Gdx.input.justTouched()) {
			System.out.println("Started Click: Touched at X: "
					+ input.x + " - Y: " + input.y);
			isClicking = true;

			float x = input.x;
			float r =  (x / mBlockWidth);
			mi_CurrentBlockCol = (int)r;
			float yPos = (float)input.y;
			mi_CurrentBlockRow = (int) ((yPos) / mBlockHeight);
			//mi_CurrentBlockRow = NUMBER_OF_ROWS - mi_CurrentBlockRow - 1; //it was upside down so I needed to flip it
			System.out.println("Row: " + mi_CurrentBlockRow + " - Col: "
					+ mi_CurrentBlockCol);
			
			//Brick grabbedBrick = mBricks[mi_CurrentBlockRow][mi_CurrentBlockCol];
			//if ( grabbedBrick != null )
				//grabbedBrick.grab();
		}

		if (!Gdx.input.isTouched() && isClicking) {
			System.out.println("Let go of click: at X: " + Gdx.input.getX()
					+ " - Y: " + Gdx.input.getY());
			isClicking = false;
			
			//Brick grabbedBrick = mBricks[mi_CurrentBlockRow][mi_CurrentBlockCol];
			//if ( grabbedBrick != null )
				//grabbedBrick.setMb_IsBeingGrabbed(false);

			mi_CurrentBlockRow = -1;
			mi_CurrentBlockCol = -1;
		} 
		
		if ( Gdx.input.isTouched() ) {
			int tempCol, tempRow;
			
			float x = input.x;
			float r =  (x / mBlockWidth);
			tempCol = (int)r;
			float yPos = (float)input.y;
			tempRow = (int) ((yPos) / mBlockHeight);
			
			//System.out.println("Dragging. FromRow: " + mi_CurrentBlockRow + " FromCol: " + mi_CurrentBlockCol +
			//				   "          ToRow:   " + tempRow +            " ToCol:   " + tempCol);
			
			//user is trying to drag a block
			if (tempRow != mi_CurrentBlockRow ) {
				System.out.println("Switching rows");
				ArrayList<Brick> list = mBlocks[mi_CurrentBlockCol];
				if (list.size() < 2 || mi_CurrentBlockRow >= list.size())
					return;
				Brick fromBrick = list.get(mi_CurrentBlockRow);
				if ( fromBrick == null )
					return;
				float fromY = fromBrick.getMf_y();
				
				if ( tempRow >= list.size() || tempRow < 0)
					return;
				Brick toBrick = list.get(tempRow);
				
				if ( toBrick.isMovingDown() )
					return; //you don't want to be able to switch with a falling block
				
				fromBrick.setMf_y(toBrick.getMf_y());
				
				toBrick.setMf_y(fromY);
				
				
				list.remove(tempRow);
				list.add(tempRow, fromBrick);
				
				list.remove(mi_CurrentBlockRow);
				list.add(mi_CurrentBlockRow, toBrick);
				
				mi_CurrentBlockRow = tempRow;
			}
			
			/*if ( tempCol != mi_CurrentBlockCol ) {
				System.out.println("Switching Cols");
				ArrayList<Brick> fromList = mBlocks[mi_CurrentBlockCol];
				if (fromList.size() == 0 )
					return;
				Brick fromBrick = fromList.get(mi_CurrentBlockRow);
				if ( fromBrick == null )
					return;
				float fromX = fromBrick.getMf_x();
				float fromY = fromBrick.getMf_y();
				
				ArrayList<Brick> toList = mBlocks[tempCol];
				if ( toList.size() == 0 )
					return;
				Brick toBrick = toList.get(tempRow);
				if ( toBrick == null )
					return;
				
				fromBrick.setMf_x(toBrick.getMf_x());
				fromBrick.setMf_y(toBrick.getMf_y());
				
				toBrick.setMf_x(fromX);
				toBrick.setMf_y(fromY);
				
				mi_CurrentBlockRow = tempRow;
				mi_CurrentBlockCol = tempCol;
			}*/
		}
	}

	private void addMoreBricks() {
		if (System.currentTimeMillis() - mTimeOfLastBrickDrop > mTimeBetweenBrickDrops && !mb_BlockIsFalling) {
			// time to throw in a new brick!

			// pick a random number between 0 and NUMBER_OF_COLUMNS to decide
			// which column to drop it in
			int columnToDropIn = generator.nextInt(NUMBER_OF_COLUMNS);

			//first check to see if there is a brick at the top of the column
			float highestBrick = getHighestBrick(mBlocks[columnToDropIn]);
			if ( highestBrick > mScreenHeight - mBlockHeight) {
				return; //end the game or something
			} else {
				float x,y;
				x = mBlockWidth * columnToDropIn;
				y = mScreenHeight /*- mBlockHeight*/ - mHeaderBuffer; //TODO: Why do i have mBlockHeight in there? it seems like i could get an extra row if i remove that
				
				int color = generator.nextInt(5);
				mBlocks[columnToDropIn].add( new Brick(x, y, mBlockHeight, mBlockHeight / 12, color));
			}

			mTimeOfLastBrickDrop = System.currentTimeMillis();
			mb_BlockIsFalling = true;
		}
	}

	private float convertNormalXToGLX(float x) {
		// return x;
		return -(mScreenWidth / 2) + x;
	}

	private float convertNormalYToGLY(float y) {
		return -(mScreenHeight / 2) + y - (mScreenHeight / 6);
		// return y;
	}

	@Override
	public void resize(int width, int height) {
		float aspectRatio = (float) width / (float) height;
		camera = new OrthographicCamera(2f * aspectRatio, 2f);
		// camera = new OrthographicCamera(width, height);

		mScreenWidth = 2f * aspectRatio;
		mScreenHeight = 2f;

		mBlockWidth = mScreenWidth / NUMBER_OF_COLUMNS;
		mBlockHeight = mBlockWidth;// mScreenHeight / NUMBER_OF_ROWS;
		
		mHeaderBuffer = mScreenHeight / 18;
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}
	
	/* Methods for the arraylist block algorithms */
	private float getHighestBrick(ArrayList<Brick> bricks) {
		float highest = -(mScreenHeight/2.0f); //set the highest to the lowest point
		
		if ( bricks == null || bricks.size() == 0)
			return highest;
		
		for ( Brick b : bricks ) {
			if ( b.getMf_y() > highest )
				highest = b.getMf_y();
		}
		
		return highest;
	}
	
	private Brick getBrickAt(int row, int col) {
		ArrayList<Brick> tempList = mBlocks[col];
		return tempList.get(row);
	}

}
