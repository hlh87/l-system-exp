/** Author: Helen Lily Hu */

package system;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class LSystemHandler {

	/** Graphics context associated with canvas to draw to */
	private GraphicsContext gc;

	/** Indicates which L-System to handle */
	private char sys;

	/** Color to draw with */
	private Color color;

	/** Size of line to draw with <br>
	 * Fractals drawn w/smaller sizes have more branches and faster animations (fewer frames/s) <br>
	 * Fractals drawn w/bigger sizes have less branches and slower animations (more frames/s) <br>
	 * This was done to try to even out the number of drawing operations per fractal <br>
	 * while maintaining aesthetic */
	private int size;

	/** Contains drawn fractals */
	private LinkedList<Node> toErase;

	/** Times drawing animations */
	private final ScheduledExecutorService executor;

	/** Constructor: initializes handler that draws Lindenmayer's original <br>
	 * L-system with stroke line width 5 and color black */
	public LSystemHandler(GraphicsContext gc) {
		this.gc= gc;
		color= Color.BLACK;
		size= 5;
		toErase= new LinkedList<>();
		executor= Executors.newScheduledThreadPool(0);
		gc.setLineWidth(5);
		gc.setLineCap(StrokeLineCap.BUTT);
		gc.setLineJoin(StrokeLineJoin.ROUND);

		sys= 'A';
	}

	/** Set L-system to handle */
	public void setSystem(char c) {
		sys= c;
	}

	/** Set color to draw with */
	public void setColor(Color color) {
		this.color= color;
	}

	/** Set size to draw with */
	public void setSize(int size) {
		this.size= size;
	}

	/** Starts generating L-system currently set to handle */
	public void startSystem(MouseEvent e) {
		Node elle= null;
		// queue where the nodes/chars generated are stored
		// so they can be drawn breadth order-ish
		LinkedList<Node> stringRecord= new LinkedList<>();
		double x0= e.getX();
		double y0= e.getY();

		switch (sys) {
		case 'A':
			// Original Lindenmayer
			elle= new Node('A', x0, y0, size);
			startHelper(elle, stringRecord);
			generatorOriginal(stringRecord, color, size);
			break;
		case 'B':
			// Barnsley fern-ish
			elle= new Node('1', x0, y0, size);
			startHelper(elle, stringRecord);
			elle.parentStorage= 2 * Math.random() * Math.PI;
			generatorBarnsley(stringRecord, color, size);
			break;
		case 'F':
			// fractal plant
			elle= new Node('X', x0, y0, size);
			startHelper(elle, stringRecord);
			// generate random seed between 0.3 and 1
			double rf= Math.max(Math.min(2 * Math.random(), 1), 0.3);
			// stack to save and restore location and angle info as needed
			LinkedList<Double> stack= new LinkedList<>();
			// array to remember location and angle info and to restore info to
			double[] memory= new double[] { x0, y0, 2 * Math.random() * Math.PI };
			generatorFracPlant(stringRecord, color, size, rf, stack, memory);
			break;
		case 'L':
			// Lichtenberg figure
			elle= new Node('L', x0, y0, size);
			startHelper(elle, stringRecord);
			elle.parentStorage= 2 * Math.random() * Math.PI;
			generatorLich(stringRecord, color, size);
			break;
		case 'C':
			// cracked earth
			elle= new Node('O', x0, y0, size);
			startHelper(elle, stringRecord);
			elle.parentStorage= 2 * Math.random() * Math.PI;
			generatorCrack(stringRecord, color, size);
			break;
		case 'P':
			// Porpita porpita
			elle= new Node('C', x0, y0, size);
			startHelper(elle, stringRecord);
			elle.parentStorage= 2 * Math.random() * Math.PI;
			generatorPorpita(stringRecord, color, size, x0, y0);
			break;
		}
	}

	/** Adds Node n to queue l and the toErase queue <br>
	 * Helper for startSystem to reduce code rep */
	private void startHelper(Node n, LinkedList<Node> l) {
		toErase.addLast(n);
		l.add(n);
	}

	/** Generates and draws Lindenmayer's original L-system <br>
	 * Terminates when line width/size degenerates to <=0 to prevent stack overflow */
	private void generatorOriginal(LinkedList<Node> q, Color color, int ogSize) {
		Node n= q.pop();
		// base case
		double nodeSize= n.size;
		if (nodeSize <= 0) { return; }

		// draw a branch and store end points in n
		drawOriginal(n, color);

		// calculate next wave of branches size
		double newSize= nodeSize - ogSize / Math.exp(-0.1 * ogSize + 2.7);

		// generate nodes with production rules and add to queue
		double nextX= n.endX;
		double nextY= n.endY;
		Node constant= new Node('A', nextX, nextY, newSize);
		n.children.addLast(constant);
		q.addLast(constant);
		if (n.value == 'A') {
			Node variable= new Node('B', nextX, nextY, newSize);
			n.children.addLast(variable);
			q.addLast(variable);
		}

		// recursive case
		generatorOriginal(q, color, ogSize);

	}

	/** Draws value in Node n to canvas associated with graphics context gc<br>
	 * For use with Lindenmayer's original L-system */
	private void drawOriginal(Node n, Color color) {
		double nodeSize= n.size;
		// value/symbol interpretation
		double rf= Math.random();
		double length= Math.exp(-0.1 * nodeSize + 2.7);
		double angle= rf * Math.PI * 0.74163;
		if (n.value == 'A') {
			length+= Math.pow(nodeSize, 1.618);
		} else {
			// n.value == 'B'
			length+= Math.pow(nodeSize, 3.236);
			angle= 2.39996 * angle;
		}

		drawHelper(n, length, angle, color);
	}

	/** Generates and draws a Barnsley fern-inspired L-system <br>
	 * Terminates when line width/size degenerates to <=0 to prevent stack overflow */
	private void generatorBarnsley(LinkedList<Node> q, Color color, int ogSize) {
		Node n= q.pop();
		// base case
		double nodeSize= n.size;
		if (nodeSize <= 0) { return; }

		// draw a branch, store end points in n, return angle n is drawn at
		double parAngle= drawBarnsley(n, color);

		// calculate next wave of branches size
		double newSize= nodeSize - ogSize / (6 - 0.05 * ogSize);

		// generate nodes with production rules and add to queue
		double nextX= n.endX;
		double nextY= n.endY;
		char c= n.value;
		if (c == '1') {
			// stem
			n.addChildren("123", nextX, nextY, newSize, parAngle, q);
		} else if (c == '2' || c == '5') {
			// right leaves
			n.addChildren("451", nextX, nextY, newSize, parAngle, q);
		} else {
			// c == '3' || c == '4'; left leaves
			n.addChildren("231", nextX, nextY, newSize, parAngle, q);
		}

		// recursive case
		generatorBarnsley(q, color, ogSize);

	}

	/** Draws value in Node n to canvas associated with graphics context gc<br>
	 * For use with Barnsley fern-inspired L-system <br>
	 * Returns angle n is drawn at */
	private double drawBarnsley(Node n, Color color) {
		double nodeSize= n.size;
		// value/symbol interpretation
		double rf= 2 * Math.random();
		double length= 2 * Math.max(Math.exp(1 - 0.03 * nodeSize), Math.pow(nodeSize, 2.2));
		double angle= n.parentStorage;
		double segX= length * Math.cos(angle);
		double segY= length * Math.sin(angle);
		char c= n.value;
		if (c == '1') {
			angle+= 0.03272 + 0.03272 * angle;
		} else if (c == '2') {
			n.startX= n.startX - segX;
			n.startY= n.startY - segY;
			angle+= 0.78540;
		} else if (c == '3') {
			n.startX= n.startX - 0.75 * segX;
			n.startY= n.startY - 0.75 * segY;
			angle-= 0.78540 * rf;
		} else if (c == '4') {
			n.startX= n.startX - segX;
			n.startY= n.startY - segY;
			angle-= 0.78540 * rf;
		} else {
			// c == '5'
			n.startX= n.startX - 0.75 * segX;
			n.startY= n.startY - 0.75 * segY;
			angle+= 0.78540;
		}

		drawHelper(n, length, angle, color);

		return angle;
	}

	/** Generates and draws a fractal plant L-system <br>
	 * Terminates when line width/size degenerates to <=0 to prevent stack overflow */
	private void generatorFracPlant(LinkedList<Node> q, Color color, int ogSize, double rf,
		LinkedList<Double> stack, double[] memory) {
		Node n= q.pop();
		// base case
		double nodeSize= n.size;
		if (nodeSize <= 0) { return; }

		// draw a branch and store end points in n
		drawFracPlant(n, memory[0], memory[1], memory[2], color);

		// calculate next wave of branches size
		double newSize= nodeSize - (ogSize / (-0.15 * ogSize + 5) + 0.1);

		// generate nodes with production rules and add to queue
		switch (n.value) {
		case 'X':
			double x= memory[0];
			double y= memory[1];
			double angle= memory[2];
			// separation of drawing chars from non-drawing chars in rule order to prevent streaking
			n.addChildren("F", x, y, newSize, angle, q);
			n.addChildren("+[[X]-X]-", 0, 0, newSize, 0, q);
			n.addChildren("F", x, y, newSize, angle, q);
			n.addChildren("[-", 0, 0, newSize, 0, q);
			n.addChildren("F", x, y, newSize, angle, q);
			n.addChildren("X]+X", 0, 0, newSize, 0, q);
			break;
		case 'F':
			// remember location info
			memory[0]= n.endX;
			memory[1]= n.endY;
			n.addChildren("FF", memory[0], memory[1], newSize, n.parentStorage, q);
			break;
		case '+':
			// adjust drawing angle
			memory[2]+= 0.43633 * rf;
			Node constant= new Node('+', 0, 0, newSize);
			n.children.addLast(constant);
			q.addLast(constant);
			break;
		case '-':
			// adjust drawing angle
			memory[2]-= 0.43633 * rf;
			Node constant1= new Node('-', 0, 0, newSize);
			n.children.addLast(constant1);
			q.addLast(constant1);
			break;
		case '[':
			// save position and angle info
			stack.addFirst(memory[0]); // x coordinate of n end point
			stack.addFirst(memory[1]); // y coordinate of n end point
			stack.addFirst(memory[2]); // drawing angle
			Node constant2= new Node('[', 0, 0, newSize);
			n.children.addLast(constant2);
			q.addLast(constant2);
			break;
		case ']':
			// retrieve saved position and angle info
			memory[2]= stack.pop(); // angle
			memory[1]= stack.pop(); // y coord
			memory[0]= stack.pop(); // x coord
			Node constant3= new Node(']', 0, 0, newSize);
			n.children.addLast(constant3);
			q.addLast(constant3);
			break;
		}

		// recursive case
		generatorFracPlant(q, color, ogSize, rf, stack, memory);

	}

	/** Draws value in Node n to canvas associated with graphics context gc<br>
	 * For use with fractal plant L-system <br>
	 * Returns angle n is drawn at */
	private void drawFracPlant(Node n, double x0, double y0, double angle, Color color) {
		// value/symbol interpretation
		if (n.value != 'F') {
			// no drawing action
			return;
		}
		// n.value == 'F'
		n.startX= x0;
		n.startY= y0;
		double nodeSize= n.size;
		double length= 2 * Math.max(Math.exp(1 - 0.03 * nodeSize),
			Math.pow(nodeSize, 2.2));

		drawHelper(n, length, angle, color);
	}

	/** Generates and draws a Lichtenberg figure-inspired L-system <br>
	 * Terminates when line width/size degenerates to <=0 to prevent stack overflow */
	private void generatorLich(LinkedList<Node> q, Color color, int ogSize) {
		Node n= q.pop();
		// base case
		double nodeSize= n.size;
		if (nodeSize <= 0) { return; }

		// draw a branch and store end points in n
		double parAngle= drawLich(n, color);

		// calculate next wave of branches size
		double newSize= nodeSize - ogSize / (8 - 0.05 * ogSize);

		// generate nodes with production rules and add to queue
		double nextX= n.endX;
		double nextY= n.endY;
		// probability for use with nondeterministic rules
		double p= Math.random();
		char c= n.value;
		if (c == 'L') {
			n.addChildren("R", nextX, nextY, newSize, parAngle, q);
			if (p > 0.5) {
				n.addChildren("L", nextX, nextY, newSize, parAngle, q);
			}
			if (p < 0.8) {
				n.addChildren("K", nextX, nextY, newSize, parAngle, q);
			}
		} else if (c == 'R') {
			n.addChildren("L", nextX, nextY, newSize, parAngle, q);
			if (p > 0.5) {
				n.addChildren("R", nextX, nextY, newSize, parAngle, q);
			}
			if (p < 0.8) {
				n.addChildren("J", nextX, nextY, newSize, parAngle, q);
			}
		} else if (c == 'J') {
			n.addChildren("K", nextX, nextY, newSize, parAngle, q);
			if (p > 0.5) {
				n.addChildren("J", nextX, nextY, newSize, parAngle, q);
			}
		} else {
			// c == 'K'
			n.addChildren("J", nextX, nextY, newSize, parAngle, q);
			if (p > 0.5) {
				n.addChildren("K", nextX, nextY, newSize, parAngle, q);
			}
		}

		// recursive case
		try {
			generatorLich(q, color, ogSize);
		} catch (StackOverflowError e) {
			System.out.println("Error!");
		}

	}

	/** Draws value in Node n to canvas associated with graphics context gc<br>
	 * For use with a Lichtenberg figure-inspired L-system <br>
	 * Returns angle n is drawn at */
	private double drawLich(Node n, Color color) {
		double nodeSize= n.size;
		// value/symbol interpretation
		double rf= Math.random();
		double length= Math.exp(1 + 0.08 * nodeSize);
		double angle= n.parentStorage;
		char c= n.value;
		if (c == 'L') {
			angle+= Math.max(rf, 0.4);
			length+= Math.exp(2 + 0.4 * nodeSize);
		} else if (c == 'R') {
			angle-= Math.max(rf * 1.57080, 0.4);
			length+= 2 + 0.4 * Math.pow(nodeSize, 2);
		} else if (c == 'J') {
			angle+= Math.max(rf * 0.78540, 0.2);
			length+= 1 + 0.1 * Math.pow(nodeSize, 2);
		} else {
			// c == 'K'
			angle-= Math.max(rf * 0.78540, 0.2);
			length+= 1.5 + 0.2 * Math.pow(nodeSize, 2);
		}

		drawHelper(n, length, angle, color);

		return angle;
	}

	/** Generates and draws cracked earth-based L-system <br>
	 * Terminates when line width/size degenerates to <=0 to prevent stack overflow */
	private void generatorCrack(LinkedList<Node> q, Color color, int ogSize) {
		Node n= q.pop();
		// base case
		double nodeSize= n.size;
		if (nodeSize <= 0) { return; }

		// draw a branch and store end points in n
		drawCrack(n, color, q);

		// calculate next wave of branches size
		double newSize= nodeSize - ogSize / Math.exp(-0.07 * ogSize + 2.7);

		// generate nodes with production rules and add to queue
		double nextX= n.endX;
		double nextY= n.endY;
		double angle= n.parentStorage;
		double rf= Math.random();
		// adjusting angle here instead of in drawCrack to simplify value/symbol interpretation
		char c= n.value;
		if (c == 'T') {
			if (rf > 0.5) {
				rf= -0.3;
			} else {
				rf= 0.3;
			}
			angle+= rf;
			n.addChildren("R", nextX, nextY, newSize, angle - 1.04720, q);
			n.addChildren("R", nextX, nextY, newSize, angle + 1.04720, q);
		} else if (c == 'R') {
			// non deterministic rule for 'R'
			double p= Math.random();
			if (p > 0.5 && p < 0.8) {
				n.addChildren("R", nextX, nextY, newSize, angle - 0.52360, q);
				n.addChildren("S", nextX, nextY, newSize, angle + 0.52360, q);
			} else if (p >= 0.8) {
				double adjAngle= rf * 1.57080;
				if (rf > 0.5) {
					adjAngle= -adjAngle;
				}
				n.addChildren("R", nextX, nextY, newSize, angle + adjAngle, q);
			} else {
				n.addChildren("T", nextX, nextY, newSize, angle, q);
			}
		} else if (c == 'S') {
			n.addChildren("S", nextX, nextY, newSize, angle, q);
		} else {
			// c == 'O', which should only be the axiom b/c geometry concerns
			n.addChildren("R", nextX, nextY, newSize, angle, q);
			n.addChildren("R", nextX, nextY, newSize, angle + 2.09440, q);
			n.addChildren("R", nextX, nextY, newSize, angle + 4.18879, q);
		}

		// recursive case
		generatorCrack(q, color, ogSize);

	}

	/** Draws value in Node n to canvas associated with graphics context gc<br>
	 * For use with cracked earth-based L-system L-system <br>
	 * Check q to draw branches based on context */
	private void drawCrack(Node n, Color color, LinkedList<Node> q) {
		double nodeSize= n.size;
		// value/symbol interpretation
		double length= Math.exp(2 + 0.4 * nodeSize);
		double angle= n.parentStorage;
		char c= n.value;
		if (c == 'R') {
			length*= 2 * Math.max(0.5, Math.random());
		} else if (c == 'S') {
			double f= Math.random();
			if (f > 0.5) {
				f= -1;
			} else {
				f= 1;
			}
			angle+= f * 0.3;
		} else {
			// c == 'O' or c == 'T' -> no drawing action
			return;
		}

		drawHelper(n, length, angle, color);
	}

	/** Generates and draws a Porpita porpita-inspired L-system <br>
	 * Terminates when line width/size degenerates to <=0 to prevent stack overflow */
	private void generatorPorpita(LinkedList<Node> q, Color color, int ogSize,
		double oX, double oY) {
		Node n= q.pop();
		// base case
		double nodeSize= n.size;
		if (nodeSize <= 0) { return; }

		// draw a branch and store end points in n
		double parAngle= drawPorpita(n, color, ogSize, oX, oY);

		// calculate next wave of branches size
		double newSize= nodeSize - ogSize / Math.exp(-0.1 * ogSize + 2.7);

		// generate nodes with production rules and add to queue
		double nextX= n.endX;
		double nextY= n.endY;
		char c= n.value;
		if (c == 'H') {
			// hydroid colony
			n.addChildren("HJK", nextX, nextY, newSize, n.parentStorage, q);
			n.addChildren("E", nextX, nextY, newSize, parAngle, q);
		} else if (c == 'E') {
			n.addChildren("JK", nextX, nextY, newSize, n.parentStorage, q);
			n.addChildren("E", nextX, nextY, newSize, parAngle, q);
		} else if (c == 'L') {
			// generate the hydroid colony
			n.addChildren("H", nextX, nextY, newSize, n.parentStorage, q);
		} else if (c == 'C') {
			// c == 'C' which should only be the axiom b/c geometry concerns
			// generate the float
			n.addChildren("L", nextX, nextY, newSize, 0, q);
			n.addChildren("L", nextX, nextY, newSize, 1, q);
			n.addChildren("L", nextX, nextY, newSize, 2, q);
			n.addChildren("L", nextX, nextY, newSize, 3, q);
			n.addChildren("L", nextX, nextY, newSize, 4, q);
			n.addChildren("L", nextX, nextY, newSize, 5, q);
			n.addChildren("L", nextX, nextY, newSize, 6, q);
			n.addChildren("L", nextX, nextY, newSize, 7, q);
			n.addChildren("L", nextX, nextY, newSize, 8, q);
			n.addChildren("L", nextX, nextY, newSize, 9, q);
		}
		// else c == 'J' or c == 'K' -> no new symbols generated

		// recursive case with origin of click info passed on
		generatorPorpita(q, color, ogSize, oX, oY);

	}

	/** Draws value in Node n to canvas associated with graphics context gc<br>
	 * For use with a Porpita porpita-inspired L-system <br>
	 * Returns angle node is drawn at */
	private double drawPorpita(Node n, Color color, int ogSize, double oX, double oY) {
		double nodeSize= n.size;
		double nodeParSign= n.parentStorage;
		// value/symbol interpretation
		double rf= Math.random();
		double length= 0;
		double arc10= 0.2 * Math.PI;
		double angle= (nodeParSign + 1) * arc10 + oY / oX;
		switch (n.value) {
		case 'H':
			// calc new start point
			angle+= rf * arc10;
			double rad= Math.exp(0.1 * ogSize + 3);
			double newX= oX + rad * Math.cos(angle);
			double newY= oY + rad * Math.sin(angle);
			n.startX= newX;
			n.endX= newX;
			n.startY= newY;
			n.endY= newY;
			length= Math.exp(1 + 0.2 * nodeSize);
			break;
		case 'E':
			if (nodeParSign < Math.PI) {
				rf= -rf;
			}
			angle= nodeParSign + rf * 0.4;
			length= Math.exp(3 - 0.05 * nodeSize);
			break;
		case 'J':
			angle+= arc10;
			length= Math.exp(1.5 + 0.05 * nodeSize);
			break;
		case 'K':
			angle-= arc10;
			length= Math.exp(1.5 + 0.05 * nodeSize);
			break;
		case 'L':
			length= Math.exp(0.1 * ogSize + 3);
			break;
		case 'C':
			// no drawing action
			return nodeParSign;
		}

		drawHelper(n, length, angle, color);

		return angle;
	}

	/** General helper for individual drawLSystem functions <br>
	 * Calculates stroke start and end points and calls actual drawing method */
	private void drawHelper(Node n, double length, double angle, Color color) {
		// get start and end points of line
		double x0= n.startX;
		double y0= n.startY;
		double x1= x0 + length * Math.cos(angle);
		double y1= y0 + length * Math.sin(angle);
		// storing end pt for erasing
		n.endX= x1;
		n.endY= y1;

		gradualDraw(x0, y0, x1, y1, color, n.size);
	}

	/** Erases the fractal at the front of the toErase queue <br>
	 * Leaves outline of fractal b/c size similarity of erase trace */
	public void erase() {
		Node fractal= toErase.pop();
		eraseHelper(fractal);
	}

	/** Erases a fractal w/root Node n recursively<br>
	 * Helper for erase */
	private void eraseHelper(Node n) {
		gradualDraw(n.startX, n.startY, n.endX, n.endY, Color.WHITE, n.size);

		// recursive/base case
		for (Node c : n.children) {
			eraseHelper(c);
		}
	}

	/** Gradually draws line from (x0,y0) to (x1, y1) with color and line width size<br>
	 * General drawing helper for all draw methods */
	private void gradualDraw(double x0, double y0, double x1, double y1, Color color, double size) {
		// calculate slope to get new points along line at appropriate intervals
		int factor= Math.max((int) (size * 3), 1);
		double runInc= (x1 - x0) / factor;
		double riseInc= (y1 - y0) / factor;

		// draw incrementally using a timer
		final double xNext= x0;
		final double yNext= y0;

		final ScheduledFuture<?> flipBook= executor.scheduleAtFixedRate(new Runnable() {

			/** x coordinate of point to draw from */
			public double xI= xNext;
			/** y coordinate of point to draw from */
			public double yI= yNext;
			/** x coordinate of point to draw to */
			public double xN= xNext;
			/** y coordinate of point to draw to */
			public double yN= yNext;

			@Override
			public void run() {
				// try to prevent GUI freezing at expense of smoothness
				Platform.runLater(() -> {
					xN= xI + runInc;
					yN= yI + riseInc;
					// actually draw
					synchronized (gc) {
						gc.setStroke(color);
						gc.setLineWidth(size);
						gc.beginPath();
						gc.moveTo(xI, yI);
						gc.strokeLine(xI, yI, xN, yN);
						gc.closePath();
					}
					xI= xN;
					yI= yN;
				});
			}

		}, 0, factor, TimeUnit.MILLISECONDS);

		// stop drawing when done with branch
		executor.schedule(new Runnable() {

			@Override
			public void run() {
				flipBook.cancel(true);
			}

		}, factor * factor, TimeUnit.MILLISECONDS);

	}

	/** An instance represents a node in a nonbinary tree representing <br>
	 * a L-system string */
	private class Node {

		/** char held by this node */
		private char value;

		/** size of the stroke to draw this node */
		private double size;

		/** x coordinate where drawing should start for this node */
		private double startX;

		/** y coordinate where drawing should start for this node */
		private double startY;

		/** x coordinate where drawing ends for this node <br>
		 * stored for replication purposes */
		private double endX;

		/** y coordinate where drawing ends for this node <br>
		 * stored for replication purposes */
		private double endY;

		/** The children of this node */
		private LinkedList<Node> children;

		/** catch-all for values node needs to remember from its parent <br>
		 * For Barnsley ferns: the drawing angle of the parent branch <br>
		 * For Lichtenberg figures: the drawing angle of the parent branch <br>
		 * For cracked earth: the angle that the node should be drawn at <br>
		 * For Porpita porpita: the arc position key for nodes besides 'E' and the parent angle for 'E' */
		private double parentStorage;

		/** Constructor: initializes node with value v, start pt and end pt (x,y), stroke size <br>
		 * size and no children */
		private Node(char v, double x, double y, double size) {
			value= v;
			startX= x;
			startY= y;
			// make end pt == start pt here b/c for small size nodes, end pt
			// calc in drawHelper may happen after eraseHelper is called (?),
			// causing streaking from (0,0) i.e. top left corner
			// initializing end pt here is a stopgap measure
			endX= x;
			endY= y;
			this.size= size;
			children= new LinkedList<>();
		}

		/** Add children that have values that are chars in string s <br>
		 * with same start point, same size, and same parent value to save <br>
		 * Also adds these children to queue q<br>
		 * Precondition: s should not be empty */
		private void addChildren(String s, double x, double y, double size, double parentV, LinkedList<Node> q) {
			while (s.length() > 1) {
				char sym= s.charAt(0);
				s= s.substring(1);
				Node n= new Node(sym, x, y, size);
				n.parentStorage= parentV;
				children.addLast(n);
				q.addLast(n);
			}
			Node l= new Node(s.charAt(0), x, y, size);
			l.parentStorage= parentV;
			children.addLast(l);
			q.addLast(l);
		}
	}
}
