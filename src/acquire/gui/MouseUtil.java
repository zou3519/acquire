package acquire.gui;

public class MouseUtil {
    static double mouseX;
    static double mouseY;
    static double clickX;
    static double clickY;
    static boolean isMouseInScene;
    static boolean isMouseClicked;
    static boolean isMousePressed;

    static void setPosition(double x, double y) {
        mouseX = x;
        mouseY = y;
    }

    static void setClickPosition(double x, double y) {
        clickX = x;
        clickY = y;
    }

    static boolean mouseOver(Actor actor) {
        return actor.boundary().contains(mouseX, mouseY);
    }
    static boolean mouseClicked(Actor actor) {
        if (isMouseClicked && actor.boundary().contains(clickX, clickY)) {
            isMouseClicked = false; // un-register the click
            return true;
        } else {
            return false;
        }
    }
}

