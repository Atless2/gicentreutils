package org.gicentre.utils.move;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import processing.core.PApplet;
import processing.core.PGraphicsJava2D;
import processing.core.PVector;

// ********************************************************************************
/**
 * Class for limiting drawing to a fixed rectangular area.
 * 
 * Simple usage example:
 * 
 * import org.gicentre.utils.gui.Clipper;
 * Clipper clipper;
 * 
 * void setup() {
 *     //...
 *     size(600, 400);
 *     clipper = new Clipper(this, 100, 100, 300, 200); // x, y, width, height
 *     //...
 * }
 * 
 * void draw() {
 *     drawSomeStuffAsUsual();
 *     
 *     clipper.startClipping();
 *     drawStuffYouWantToClip();
 *     clipper.stopClipping();
 *     
 *     drawSomeOtherStuffAsUsual();
 * }
 * 
 * 
 * Limitations:
 * � only works with JAVA2D
 * � clipping is ignored when writing to PDF using startRecording()
 * � boundaries must be always given in absolute coordinate system (matrix transitions and scalings are ignored)
 * 
 * 
 * @author Alexander Kachkaev <alexander.kachkaev.1@city.ac.uk> with minor modifications by Jo Wood.
 * Inspired by Aidan Slingsby's <a.slingsby@city.ac.uk> GraphicBuffer
 * @version 1.1, 3rd June, 2012.
 *
 */
// ********************************************************************************

public class Clipper
{
	// ----------------------------- Object variables ------------------------------
	
	protected PApplet applet;
	protected PGraphicsJava2D pGraphics2D;
	protected Rectangle2D clippingRect;
	protected boolean enabled = true;
	
	// ------------------------------- Constructors --------------------------------

	/** Creates a new Clipper instance capable of limiting all drawing to within the given rectangular bounds.
	 *  Clipping is enabled by default, but not active.
	 *  @param applet Sketch in which to enable clipping.
	 *  @param x x coordinate of the top-left of the clipping rectangle.
	 *  @param y y coordinate of the top-left of the clipping rectangle.
	 *  @param width Width of the clipping rectangle.
	 *  @param height Height of the clipping rectangle.
	 *  @see #startClipping()
	 *  @see #stopClipping()
	 *  @see #setEnabled(boolean)
	 */
	public Clipper(PApplet applet, float x, float y, float width, float height) 
	{
		if (!(applet.g instanceof PGraphicsJava2D))
		{
			throw new IllegalArgumentException("Cannot clip with this renderer: "+applet.g.getClass().getName()+". Only processing.core.PGraphicsJava2D is supported.");
		}

		this.applet = applet;
		this.pGraphics2D = ((PGraphicsJava2D) applet.g);
		clippingRect = new java.awt.geom.Rectangle2D.Float();
		setClippingRect(x, y, width, height);
	}

	/** Creates a new Clipper instance capable of limiting all drawing to within the given rectangular bounds.
	 *  Clipping is enabled by default, but not active.
	 *  @param applet Sketch in which to enable clipping.
	 *  @param clippingRect Bounds of the rectangle within which clipping is to be applied.
	 *  @see #startClipping()
	 *  @see #stopClipping()
	 *  @see #setEnabled(boolean)
	 */
	public Clipper(PApplet applet, Rectangle2D clippingRect) 
	{
		this(applet, (float)clippingRect.getX(), (float)clippingRect.getY(), (float)clippingRect.getWidth(), (float)clippingRect.getHeight());
	}
	
	// ----------------------------------- Methods ------------------------------------

	/** Starts clipping all drawn content to the screen bounds of the current clip area.
	 *  If startClipping for the current clipper is called before stopClipping somewhere else,
	 *  an exception is thrown in order to force the developer keeping the code clear.
	 */
	public void startClipping() 
	{
		Shape currentClip = pGraphics2D.g2.getClip();
		if (currentClip != null && !clippingRect.equals(currentClip.getBounds2D()))
		{
			throw new IllegalStateException("Before you start clipping, you need to call stopClipping() for all other clippers in the sketch.");
		}

		if (enabled)
		{
			pGraphics2D.g2.setClip(clippingRect);
		}
	}

	/** Stops any active clipping.
	 */
	public void stopClipping() 
	{
		pGraphics2D.g2.setClip(null);
	}

	/** Sets the clipping rectangle and applies it if currently clipping
	 *  @param x x coordinate of the top-left of the clipping rectangle.
	 *  @param y y coordinate of the top-left of the clipping rectangle.
	 *  @param width Width of the clipping rectangle.
	 *  @param height Height of the clipping rectangle.
	 */
	public void setClippingRect(float x, float y, float width, float height) 
	{
		clippingRect.setFrame(x, y, width, height);
		if (isClipping())
		{
			startClipping();
		}
	}

	/** Sets the clipping rectangle and applies it if currently clipping
	 *  @param clippingRect The clipping rectangle to apply.
	 */
	public void setClippingRect(Rectangle2D clippingRect) 
	{
		if (clippingRect == null)
		{
			throw new IllegalArgumentException("clippingRect should not be null.");
		}
		setClippingRect((float)clippingRect.getX(), (float)clippingRect.getY(), (float)clippingRect.getWidth(), (float)clippingRect.getHeight());
	}
	
	/** Reports the clipping rectangle, whether or not is is currently active.
	 *  @return Bounds of the current clipping rectangle.
	 */
	public Rectangle2D getClippingRect() 
	{
		return clippingRect;
	}

	/** Reports whether or not the given point lies within the clipping rectangle. The result is independent of
	 *  whether or not clipping is currently active.
	 *  @param x x coordinate of the point to test.
	 *  @param y y coordinate of the point to test.
	 *  @return True if the given point is within the clipping rectangle, false otherwise.
	 */
	public boolean contains(float x, float y) 
	{
		return clippingRect.contains(x, y);
	}

	/** Reports whether or not the given point lies within the clipping rectangle. The result is independent of
	 *  whether or not clipping is currently active.
	 *  @param p Point to test.
	 *  @return True if the given point is within the clipping rectangle, false otherwise.
	 */
	public boolean contains(PVector p) 
	{
		return contains(p.x, p.y);
	}

	/** Reports whether or not clipping mode is currently active. Note that the result is only true between 
	 *  <code>startClipping()</code> and <code>stopClipping()</code> method calls.
	 *  @return True if called while clipping is currently active, otherwise false.
	 *  @see #isEnabled()
	 */
	public boolean isClipping() 
	{
		return pGraphics2D.g2.getClip() == null;
	}

	/** Reports whether or not clipping is enabled. Unlike <code>isClipping()</code>, this returns true even before 
	 *  <code>startClipping()</code> and after <code>stopClipping()</code>.
	 *  @return True if clipping is enabled.
	 *  @see #isClipping()
	 *  @see #setEnabled(boolean)
	 */
	public boolean isEnabled() 
	{
		return this.enabled;
	}

	/** Determines whether or not clipping is enabled.
	 *  If enabled is false, no clipping happens between startClipping() and stopClipping() (startClipping is ignored).
	 *  If called during clipping, clipping stops automatically.
	 */
	public void setEnabled(boolean enabled) 
	{
		this.enabled = enabled;
		if (!enabled)
		{
			stopClipping();
		}
	}
}
