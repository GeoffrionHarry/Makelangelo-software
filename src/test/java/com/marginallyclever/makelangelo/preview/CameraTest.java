package com.marginallyclever.makelangelo.preview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.marginallyclever.convenience.Point2D;


public class CameraTest {

    private Camera camera;

    /*
    On initialise une nouvelle caméra avant chaque test (ARRANGE).
    */
    @BeforeEach
    public void setUp() {
        // ARRANGE - create camera object
        camera = new Camera();
    }

    /*
    Les deux prochains tests valident le bon fonctionnement du zoom de la caméra pour être sure 
    qu'une erreur dans un test d'une fonction qui utilise le zoom ne découle pas
    d'un problème dans la fonction zoom elle-même. Le premier regarde notamment que le zoom
    augmente en fonction du step_size et que la valeur du zoom est bien bornée vers le bas.
    */
    @Test
    public void testZoomOut() {
                                                                        // ARRANGE - fait dans le @BeforeEach
        camera.zoom(-1);                                                // ACT - zoom out by 1
        assertEquals(1.0 + Camera.ZOOM_STEP_SIZE, camera.getZoom());    // ASSERT - expect 1.15

                                                                        // ARRANGE - fait dans le @BeforeEach
        camera.zoom(Integer.MIN_VALUE);                                 // ACT - zoom out by max possible
        assertEquals(Camera.CAMERA_ZOOM_MAX, camera.getZoom());         // ASSERT - expect 1000.0
    }


    /*
    Ce test s'assure que le zoom diminue en fonction du step_size et que la valeur du zoom est 
    bien bornée vers le haut.
    */
    @Test
    public void testZoomIn() {
                                                                        // ARRANGE - fait dans le @BeforeEach
        camera.zoom(1);                                                 // ACT - zoom in by 1
        assertEquals(1.0 - Camera.ZOOM_STEP_SIZE, camera.getZoom());    // ASSERT - expect 0.85

                                                                        // ARRANGE - fait dans le @BeforeEach
        camera.zoom(Integer.MAX_VALUE);                                 // ACT - zoom in by max possible
        assertEquals(Camera.CAMERA_ZOOM_MIN, camera.getZoom());         // ASSERT - expect 0.25
    }


    /*
    Ce test cherche à valider que la caméra renvoit les bonnes valeurs de position
    lorsqu'on la déplace dans la fenêtre de preview. Il valide d'abord que le déplacement
    à partir de la position de départ fonctionne, et ensuite un autre déplacement.
    */
    @Test
    public void testMoveRelative() {
        // ARRANGE - set offset
        double initialOffsetX = 20.0;
        double initialOffsetY = 10.0;
        // ACT - move camera
        camera.moveRelative(initialOffsetX, initialOffsetY);
        // ASSERT - expect position to be (20.0, 10.0)
        assertEquals(initialOffsetX, camera.getX());
        assertEquals(initialOffsetY, camera.getY());


        // ARRANGE - set offset
        double secondOffsetX = -10.0;
        double secondOffsetY = -5.0;
        // ACT - move camera
        camera.moveRelative(secondOffsetX, secondOffsetY);
        // ASSERT - expect position to be (10, 5)
        assertEquals(initialOffsetX + secondOffsetX, camera.getX());
        assertEquals(initialOffsetY + secondOffsetY, camera.getY());
    }


    /*
    Ce test valide le bon fonctionnement de la fonction screenToWorldSpace() qui . 
    Pour ce faire, nous testons les quatres cas suivants:
    - Cas de base : la caméra est à position initiale et zoom initial.
    - Cas de déplacement : la caméra est déplacée de (100.0, 100.0) et zoom initial.
    - Cas de zoom : la caméra est à position initiale et zoomée jusqu'à 10x.
    - Cas de déplacement avec zoom : la caméra est déplacée de (100.0, 100.0) et zoomée jusqu'à 10x.
    */
    @Test
    public void testScreenToWorldSpace() {

        // Equation: output.xy = input.xy/zoom + offsetXY;

        // ARRANGE - create input data
        Point2D input = new Point2D(100, 100);
        // ACT - convert to world space
        Point2D output = camera.screenToWorldSpace(input);
        // ASSERT - position is (100, 100) so equation should be:
        // output.xy = 100.0/1.0 + 0.0;
        assertEquals(input.x, output.x);
        assertEquals(input.y, output.y);


        // ARRANGE - move camera to test offset
        camera.moveRelative(100.0, 100.0);
        // ACT - convert to world space
        output = camera.screenToWorldSpace(input);
        // ASSERT - offset is (100, 100) so equation should be:
        // output.xy = 100.0/1.0 + 100.0;
        assertEquals(input.x + 100.0, output.x);
        assertEquals(input.y + 100.0, output.y);
        

        // ARRANGE - zoom camera to test zoom
        camera.moveRelative(-100.0, -100.0); // reset position
        camera.zoom((int) (-9.0/Camera.ZOOM_STEP_SIZE));
        // ACT - convert to world space
        output = camera.screenToWorldSpace(input);
        // ASSERT - zoom is (1 + 9) = 10 so equation should be:
        // output.xy = 100.0/10.0 + 0.0;
        assertEquals(input.x / 10.0, output.x);
        assertEquals(input.y / 10.0, output.y);


        // ARRANGE - move camera to test offset and zoom simultaneously
        camera.zoom((int) (9.0/Camera.ZOOM_STEP_SIZE)); // undo zoom
        camera.moveRelative(100.0, 100.0);
        camera.zoom((int) (-9.0/Camera.ZOOM_STEP_SIZE));
        // ACT - convert to world space
        output = camera.screenToWorldSpace(input);
        // ASSERT - offset is (100, 100) and zoom is 10 so equation should be:
        // output.xy = 100.0/10.0 + 100.0;
        assertEquals(input.x / 10.0 + 100.0, output.x);
        assertEquals(input.y / 10.0 + 100.0, output.y);
    }

    /*
    Ce test vérifie que la fonction zoomToFit() ajuste le zoom correctement.
    */
    @Test
    public void testZoomToFit() {
        // ARRANGE - set initial width and height
        double width = 2000.0;
        double height = 1000.0;

        // ACT - call zoomToFit with new dimensions
        camera.zoomToFit(width, height);

        // ASSERT - check if zoom is what we expect
        double expectedZoom = Math.max(width / height, height / width);
        assertEquals(expectedZoom, camera.getZoom());
    }
}