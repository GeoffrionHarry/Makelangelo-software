package com.marginallyclever.makelangelo.turtle;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.marginallyclever.convenience.LineCollection;
import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.Point2D;

class TurtleTest {

    @Test
    public void empty() {
        // given
        Turtle turtle = new Turtle();

        // then
        assertEquals("[TOOL R0 G0 B0 A255 D1.000]", turtle.history.toString());
    }

    @Test
    public void travel() {
        // given
        Turtle turtle = new Turtle();

        // when
        turtle.penUp();
        turtle.moveTo(10, 12);
        turtle.moveTo(2, 3);

        // then
        assertEquals("[TOOL R0 G0 B0 A255 D1.000, TRAVEL X10.000 Y12.000, TRAVEL X2.000 Y3.000]", turtle.history.toString());
        assertFalse(turtle.getHasAnyDrawingMoves());
    }

    @Test
    public void penDownLinePenUp() {
        // given
        Turtle turtle = new Turtle();

        // when
        turtle.penDown();
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 12);
        turtle.penUp();
        turtle.moveTo(-15, -7);
        turtle.penUp();

        // then
        assertEquals("[TOOL R0 G0 B0 A255 D1.000, DRAW_LINE X20.000 Y30.000, DRAW_LINE X10.000 Y12.000, TRAVEL X-15.000 Y-7.000]", turtle.history.toString());
    }

    @Test
    public void moveAndDraw() {
        // given
        Turtle turtle = new Turtle();

        // when
        turtle.penDown();
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 15);

        // then
        assertEquals("[TOOL R0 G0 B0 A255 D1.000, DRAW_LINE X20.000 Y30.000, DRAW_LINE X10.000 Y15.000]", turtle.history.toString());
        assertTrue(turtle.getHasAnyDrawingMoves());
    }

    @Test
    public void angle() {
        // given
        Turtle turtle = new Turtle();

        // when
        turtle.setAngle(0);
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 15);
        turtle.turn(-45);
        turtle.forward(2);
        turtle.jumpTo(-15, -7);

        // then
        assertEquals("[TOOL R0 G0 B0 A255 D1.000, TRAVEL X20.000 Y30.000, TRAVEL X10.000 Y15.000, TRAVEL X11.414 Y13.586, TRAVEL X-15.000 Y-7.000]", turtle.history.toString());
        assertFalse(turtle.getHasAnyDrawingMoves());
    }

    @Test
    public void colorChange() {
        // given
        Turtle turtle = new Turtle(new Color(1,2,3));

        // when
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 15);
        turtle.setColor(new Color(4, 5, 6));
        turtle.jumpTo(-15, -7);

        // then
        assertEquals("[TOOL R1 G2 B3 A255 D1.000, TRAVEL X20.000 Y30.000, TRAVEL X10.000 Y15.000, TOOL R4 G5 B6 A255 D1.000, TRAVEL X-15.000 Y-7.000]", turtle.history.toString());
        assertEquals(new Color(1,2,3), turtle.getFirstColor());
    }

    @Test
    public void firstColor() {
        // given
        Turtle turtle = new Turtle(new Color(1,2,3));

        // then
        assertEquals("[TOOL R1 G2 B3 A255 D1.000]", turtle.history.toString());
    }

    @Test
    public void toolChange() {
        // given
        Turtle turtle = new Turtle(new Color(1,2,3));

        // when
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 15);
        turtle.setDiameter(10);

        // then
        assertEquals("[TOOL R1 G2 B3 A255 D1.000, TRAVEL X20.000 Y30.000, TRAVEL X10.000 Y15.000, TOOL R1 G2 B3 A255 D10.000]", turtle.history.toString());
    }

    @Test
    public void bounds() {
        // given
        Turtle turtle = new Turtle();

        // when
        turtle.penDown();
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 15);
        turtle.jumpTo(-15, -7);
        turtle.moveTo(3, 4);
        turtle.jumpTo(12, 18);

        // then
        Rectangle2D.Double r = turtle.getBounds();

        assertEquals(35, r.width);
        assertEquals(37, r.height);
        assertEquals(-15, r.x);
        assertEquals(-7, r.y);
    }

    @Test
    public void scale() {
        // given
        Turtle turtle = new Turtle();

        // when
        turtle.penDown();
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 15);
        turtle.jumpTo(-15, -7);
        turtle.moveTo(3, 4);
        turtle.jumpTo(12, 18);

        turtle.scale(2, 3);

        // then
        assertEquals("[TOOL R0 G0 B0 A255 D1.000, DRAW_LINE X40.000 Y90.000, DRAW_LINE X20.000 Y45.000, TRAVEL X-30.000 Y-21.000, DRAW_LINE X6.000 Y12.000, TRAVEL X24.000 Y54.000]", turtle.history.toString());
    }

    @Test
    public void translate() {
        // given
        Turtle turtle = new Turtle();

        // when
        turtle.penDown();
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 15);
        turtle.jumpTo(-15, -7);
        turtle.moveTo(3, 4);
        turtle.jumpTo(12, 18);

        turtle.translate(-10, 3);

        // then
        assertEquals("[TOOL R0 G0 B0 A255 D1.000, DRAW_LINE X10.000 Y33.000, DRAW_LINE X0.000 Y18.000, TRAVEL X-25.000 Y-4.000, DRAW_LINE X-7.000 Y7.000, TRAVEL X2.000 Y21.000]", turtle.history.toString());
    }

    @Test
    public void splitByToolChange() {
        // given
        Turtle turtle = new Turtle();

        // when
        turtle.penDown();
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 15);
        turtle.jumpTo(-15, -7);
        turtle.moveTo(3, 4);
        turtle.jumpTo(12, 18);
        turtle.setDiameter(2);
        turtle.jumpTo(-8, 4);
        turtle.moveTo(1, 6);

        // then
        List<Turtle> turtles = turtle.splitByToolChange();
        assertNotNull(turtles);
        assertEquals(2, turtles.size());
        assertEquals("[TOOL R0 G0 B0 A255 D1.000, DRAW_LINE X20.000 Y30.000, DRAW_LINE X10.000 Y15.000, TRAVEL X-15.000 Y-7.000, DRAW_LINE X3.000 Y4.000, TRAVEL X12.000 Y18.000]", turtles.get(0).history.toString());
        assertEquals("[TOOL R0 G0 B0 A255 D2.000, TRAVEL X-8.000 Y4.000, DRAW_LINE X1.000 Y6.000]", turtles.get(1).history.toString());
    }

    @Test
    public void addTurtle() {
        // given
        Turtle turtle = new Turtle();

        // when
        turtle.penDown();
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 15);
        turtle.jumpTo(-15, -7);
        turtle.moveTo(3, 4);

        Turtle turtle2 = new Turtle(new Color(6,7,8));
        turtle2.jumpTo(-8, 4);
        turtle2.moveTo(1, 6);

        turtle.add(turtle2);

        // then
        assertEquals("[TOOL R0 G0 B0 A255 D1.000, DRAW_LINE X20.000 Y30.000, DRAW_LINE X10.000 Y15.000, TRAVEL X-15.000 Y-7.000, DRAW_LINE X3.000 Y4.000, TOOL R6 G7 B8 A255 D1.000, TRAVEL X-8.000 Y4.000, DRAW_LINE X1.000 Y6.000]", turtle.history.toString());
    }

    @Test
    public void equalsTwoTurtles() {
        // given
        Turtle turtle = new Turtle();

        turtle.penDown();
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 15);
        turtle.jumpTo(-15, -7);
        turtle.moveTo(3, 4);

        Turtle turtle2 = new Turtle();
        turtle2.penDown();
        turtle2.moveTo(20, 30);
        turtle2.moveTo(10, 15);
        turtle2.jumpTo(-15, -7);
        turtle2.moveTo(3, 4);

        // then
        assertEquals(turtle, turtle2);
    }

    @Test
    public void notEqualsTwoTurtles() {
        // given
        Turtle turtle = new Turtle();
        turtle.penDown();
        turtle.moveTo(20, 30);
        turtle.moveTo(10, 15);
        turtle.jumpTo(-15, -7);
        turtle.moveTo(3, 4);

        Turtle turtle2 = new Turtle();
        turtle2.penDown();
        turtle2.moveTo(3, 4);

        // then
        assertNotEquals(turtle, turtle2);
    }


    @Test
    public void testInterpolate() {
        final double EPSILON = 1e-6;

        Turtle turtle = new Turtle();
        turtle.penDown();
        turtle.forward(1000);
        double d = turtle.getDrawDistance();
        assertEquals(1000,d);
        for(int i=0;i<=10;++i) {
            assertTrue(new Point2D(i * 100, 0).distance(turtle.interpolate(d*(double)i/10.0)) < EPSILON);
        }
    }


    /*
    Cette fonction cherche à valider le bon fonctionnement de la fonction countLoops()
    qui compte le nombre de fois que la tortue a fait un trait non-relié aux autres ce
    qui constitue un dessin fermé.
    */
    @Test
    public void testCountLoops() {

        // ARRANGE - create turtle new object
        Turtle turtle = new Turtle();

        // ACT - draw an initial "loop"
        turtle.forward(1);
        turtle.penDown();
        turtle.forward(1);
        turtle.penUp();

        // ASSERT - expect 1 loop
        assertEquals(1, turtle.countLoops());


        // ACT - draw a second "loop" with two different movements
        turtle.forward(1);
        turtle.penDown();
        turtle.rotate(90.0);
        turtle.forward(1);
        turtle.penUp();
        // ASSERT - expect 2 loops
        assertEquals(2, turtle.countLoops());

        // ACT - draw a square
        turtle.forward(1);
        turtle.penDown();
        turtle.forward(1);
        turtle.rotate(90.0);
        turtle.forward(1);
        turtle.rotate(90.0);
        turtle.forward(1);
        turtle.rotate(90.0);
        turtle.forward(1);
        turtle.penUp();

        // ASSERT - expect 3 loops (square is one loop)
        assertEquals(3, turtle.countLoops());
    }

    /*
     * Cette fonction valide le bon fonctionnement de reset dans la class 
     * Turtle. Elle devrait remttre la tortue a sa position initiale.
     * Et mettre la couleur a noir.
     * x = 0 , y = 0, angle = 0
     * Cette fonction de test est importante, car un decalage de la position
     * initial de la tortue pourrait causer des erreurs dans les dessins.
     */
    @Test
    public void reset() {
        // ARRANGE + ACT - create turtle new object (reset implicitly called)
        Turtle t = new Turtle();

        // ASSERT - expected initial values for turtle
        assertEquals(t.getPosition().x, 0.0);
        assertEquals(t.getPosition().y, 0.0);
        assertEquals(t.getColor(), Color.BLACK);
        assertEquals(t.getAngle(), 0.0);
        assertEquals(t.isUp(), true);
    }

    /*
     * Cette fonction valide que lors de l'exécution de la fonction drawArc
     * la tortue se déplace correctement et se retrouve la bonne position
     * finale. 
     * La verification de cette fonction est importante au bon fonctionnement
     * de la tortue pour les dessins. La tortue doit se retrouver a la position
     * du dernier segment de l'arc.
     */
    @Test
    public void drawArc() {
        // ARRANGE - create turtle new object and init variables
        Turtle t = new Turtle();
        double cx = 0;
        double cy = 0;
        double a1 = 0;
        double a2 = Math.PI/2;
        double r = 3;
        int n = 10;

        // ACT - draw an arc
        t.drawArc(cx, cy, a1, a2, r, n); 
        
        // ASSERT - Validate Final position of the turtle is correct
        assertEquals(Math.round(cx + Math.cos(a2) * r), 0);
        assertEquals(Math.round(cy + Math.sin(a2) * r), 3);
    }

    


    /*
    Cette fonction valide que la fonction addLineSegments() ajoute les segments comme 
    attendu (en terme de déplacement de la tortue).
    */
    @Test
    public void testAddLineSegments() {
        
        // ARRANGE - create turtle and initialize line segments
        Turtle turtle = new Turtle();
        LineCollection segments = new LineCollection();
        segments.add(new LineSegment2D(new Point2D(0, 0), new Point2D(10, 10), Color.BLACK));
        segments.add(new LineSegment2D(new Point2D(10, 10), new Point2D(25, 25), Color.BLACK));

        // ACT - add line segments to turtle
        turtle.addLineSegments(segments);

        // ASSERT - expect history to be of 6 and final coords to be (25.0, 25.0)
        assertEquals("Turtle{history=6, px=25.0, py=25.0, nx=1.0, ny=0.0, angle=0.0, isUp=false, color=java.awt.Color[r=0,g=0,b=0], diameter=1.0}", turtle.toString());
    
    }

    /*
      Le but de ce test est de verifier que lorsque la tordue depasse les "bounds"
      la taille des bounds est reajustee pour contenir la nouvelle position de la tortue.
      Ceci est important afin de ne pas dessiner en dehors de la zone de dessin.
     */
    @Test
    public void getBoundsInternal() {
        // ARRANGE - create turtle new object
        Turtle t = new Turtle();

        // ACT - draw a line that goes beyond the bounds
        Rectangle2D.Double currentBounds = t.getBounds();
        t.forward(currentBounds.width + 100);
        t.forward(currentBounds.height + 100);

        // ASSERT - expected bounds to be updated
        assertEquals(t.getBounds().width, currentBounds.width);
        assertEquals(t.getBounds().height, currentBounds.height);
    }
}