/**
 * 
 */
package org.gamboni.remote.client;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

/**
 * @author tendays
 *
 */
public class TouchPad extends JComponent {

	private static final long serialVersionUID = 1L;
	
	/** Non-null only when the mouse is pressed. */
	Point previousPoint = null;
	/** Set if the mouse moves while a query is already running. */
	Point currentPoint = null;
	boolean requestPending = false;

	private Listener listener;
	
	public interface Listener {
		CompletionStage<?> move(int dx, int dy);
	}

	/**
	 * Invoke the listener only if no previous invocation is running. Otherwise wait
	 * for return and execute only the last invocation occuring in the meantime.
	 */
	private synchronized void invokeListener(Point newPoint) {
		if (requestPending) {
			//System.out.println("Not sending request because earlier request is pending.");
			currentPoint = newPoint;
		} else {
			var done = listener.move(newPoint.x - previousPoint.x,
					newPoint.y - previousPoint.y);
			previousPoint = newPoint;
			requestPending = true;
			done.thenAccept(__ -> sendPendingRequests());
		}
	}
	
	private synchronized void sendPendingRequests() {
		requestPending = false;
		if (currentPoint != null) {
			//System.out.println("Sending delayed request");
			var newPoint = currentPoint;
			currentPoint = null;
			invokeListener(newPoint); // will set requestPending back to true
		}
	}
	
	public TouchPad(Listener listener, Function<Character, ? extends Supplier<?>> keyListener) {
		this.listener = listener;
		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				//System.out.println("pressed "+ e);
				previousPoint = e.getPoint();
				requestFocusInWindow();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				//System.out.println("released "+ e);
				previousPoint = null;
			}
		});
		addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				//System.out.println("moved "+ e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				//System.out.println("dragged "+ e);
				if (previousPoint != null) {
					var newPoint = e.getPoint();
					if (newPoint.equals(previousPoint)) { return; }
					
					invokeListener(newPoint);
				}
			}
		});

		addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e) {
				keyListener.apply(e.getKeyChar()).get();
			}	
		});
		
		setFocusable(true);
		
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		setPreferredSize(new Dimension(200, 100));
		
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
}
