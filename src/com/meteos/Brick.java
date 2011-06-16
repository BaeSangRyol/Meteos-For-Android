package com.meteos;

public class Brick {
	private float mf_x, mf_y;
	private boolean mb_MoveDown;
	
	private float mf_BeginMoveDownY;
	
	private float mf_BlockHeight;
	private float mf_Speed;
	
	//variables to handle the different colored blocks
	private int mi_Color;
	private float[][] mf_ColorList = {
			{ 1.0f, 0.0f, 0.0f, 1.0f}, //red
			{ 0.0f, 1.0f, 0.0f, 1.0f}, //green
			{ 0.0f, 0.0f, 1.0f, 1.0f}, //blue
			{ 1.0f, 0.584f, 0.1804f, 1.0f}, //orange
			{ 1.0f, 0.0f, 0.9647f, 1.0f} //purple
	};
	public Brick(float x, float y, float blockHeight, float speed, int color) {
		mf_x = x;
		mf_y = y;
		
		mb_MoveDown = false;
		
		mf_BlockHeight = blockHeight;
		mf_Speed = speed;
		
		mi_Color = color;
	}

	public float[] getColor() {
		return mf_ColorList[mi_Color];
	}

	public float getMf_x() {
		return mf_x;
	}

	public void setMf_x(float mf_x) {
		this.mf_x = mf_x;
	}

	public float getMf_y() {
		return mf_y;
	}

	public void setMf_y(float mf_y) {
		this.mf_y = mf_y;
	}
	
	/**
	 * This method moves the brick's coordinates down graphically
	 */
	public void moveDown() {
		
		if ( mb_MoveDown)
			mf_y -= mf_Speed;
		
	}
	
	/**
	 * Checks if it is done moving down and stops it from moving
	 * @return true if it is done, false if it is still moving
	 */
	public boolean checkIfDoneMoving() {
		if ( mb_MoveDown == false ) {
			return false;
		}
		
		if ( mf_BeginMoveDownY - mf_BlockHeight >= mf_y ) {
			//yes it is done moving down
			mb_MoveDown = false;
			//mf_BeginMoveDownY = mf_y;
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Marks this brick to be moved down
	 */
	public void setMoveDown(boolean move) {
		mb_MoveDown = move;
		
		if ( move )
			mf_BeginMoveDownY = mf_y;
	}
	
	/**
	 * returns whether or not the block is moving down because of gravity
	 * @return
	 */
	public boolean isMovingDown() {
		return mb_MoveDown;
	}

}
