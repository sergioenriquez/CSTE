package org.fern.rest.fling;

/**
 * Implemented by activies that need to detect when the user
 * swipes a finger across the devices screen.
 * @author user
 *
 */
public interface FlingHandler {
	public void OnFlingLeft();
	public void OnFlingRight();
}
