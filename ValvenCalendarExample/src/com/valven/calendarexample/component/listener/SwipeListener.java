package com.valven.calendarexample.component.listener;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SwipeListener implements View.OnTouchListener {

	private SwipeCallback callback;
	static final int MIN_DISTANCE = 150;
	private float downX, downY, upX, upY;

	public interface SwipeCallback {
		public void onRightToLeftSwipe(View view);

		public void onLeftToRightSwipe(View view);

		public void onTopToBottomSwipe(View view);

		public void onBottomToTopSwipe(View view);
	}

	public SwipeListener(SwipeCallback callback) {
		this.callback = callback;
	}

	public void onRightToLeftSwipe(View view) {
		callback.onRightToLeftSwipe(view);
	}

	public void onLeftToRightSwipe(View view) {
		callback.onLeftToRightSwipe(view);
	}

	public void onTopToBottomSwipe(View view) {
		callback.onTopToBottomSwipe(view);
	}

	public void onBottomToTopSwipe(View view) {
		callback.onBottomToTopSwipe(view);
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			downX = event.getX();
			downY = event.getY();
			return false;
		}
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			upX = event.getX();
			upY = event.getY();

			float deltaX = downX - upX;
			float deltaY = downY - upY;

			// swipe horizontal?
			if (Math.abs(deltaX) > MIN_DISTANCE) {
				// left or right
				if (deltaX < 0) {
					this.onLeftToRightSwipe(v);
					return true;
				}
				if (deltaX > 0) {
					this.onRightToLeftSwipe(v);
					return true;
				}
			} else {
				Log.i("SwipeListener","Swipe was only " + Math.abs(deltaX)
						+ " long, need at least " + MIN_DISTANCE);
			}

			// swipe vertical?
			if (Math.abs(deltaY) > MIN_DISTANCE) {
				// top or down
				if (deltaY < 0) {
					this.onTopToBottomSwipe(v);
					return true;
				}
				if (deltaY > 0) {
					this.onBottomToTopSwipe(v);
					return true;
				}
			} else {
				Log.i("SwipeListener","Swipe was only " + Math.abs(deltaY)
						+ " long, need at least " + MIN_DISTANCE);
			}
		}
		}
		return false;
	}
}