package GameClasses;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;

public class BasicMouseListener extends MouseInputAdapter {
    /* Author: Michael Fairbank
     * Creation Date: 2016-01-28
     * Adapted by Patrick O'Dell
     * Significant changes applied: 2016-02-10 added mouseJoint code to allow dragging of bodies
     */
    private static int mouseX, mouseY;
    private static boolean mouseButtonPressed;

    private static MouseJoint mouseJoint;

    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        mouseButtonPressed = false;
        if (mouseJoint != null) {
            linkMouseDragEventToANewMouseJoint(null);
        }
    }

    public void mouseClicked(MouseEvent e) {
        Game.fire = true;
    }

    public void mouseReleased(MouseEvent e) {
        Game.fire = false;
    }

    public static boolean isMouseButtonPressed() {
        if (mouseButtonPressed) {
            Game.fire = true;
        } else {
            Game.fire = false;
        }
        return mouseButtonPressed;
    }

    public static Vec2 getWorldCoordinatesOfMousePointer() {
        return new Vec2(Game.convertScreenXtoWorldX(mouseX), Game.convertScreenYtoWorldY(mouseY));
    }

    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        float aX = Game.WORLD_WIDTH/4;
        float aY = Game.WORLD_HEIGHT/3;
        Vec2 catapult = new Vec2(aX,aY);
        float slingshot = (catapult.sub(getWorldCoordinatesOfMousePointer())).length();
        Vec2 normal = (catapult.sub(getWorldCoordinatesOfMousePointer()));
        normal.normalize();
        Vec2 worldCoordinatesOfMousePointer;
        if(slingshot > 12){
            worldCoordinatesOfMousePointer = new Vec2((aX),(aY)).sub(normal.mul(12));
        }
        else{
            mouseX = e.getX();
            mouseY = e.getY();
            worldCoordinatesOfMousePointer = getWorldCoordinatesOfMousePointer();
        }
        Game.mouseCoordinates = worldCoordinatesOfMousePointer;
        mouseButtonPressed = true;

        if (mouseJoint != null) {
            mouseJoint.setTarget(worldCoordinatesOfMousePointer);
        } else if (Game.ALLOW_MOUSE_POINTER_TO_DRAG_BODIES_ON_SCREEN) {
            MouseJointDef mj = new MouseJointDef();
            Body bodyUnderMousePointer = findBodyAtWorldCoords(worldCoordinatesOfMousePointer);
            if (bodyUnderMousePointer != null) {
                mj.bodyA = bodyUnderMousePointer;
                mj.bodyB = bodyUnderMousePointer;
                mj.target.set(new Vec2(worldCoordinatesOfMousePointer));
                mj.collideConnected = false;
                mj.maxForce = 10000000 * mj.bodyB.getMass();
                mj.dampingRatio = 0;
                MouseJoint mouseJoint = (MouseJoint) Game.world.createJoint(mj);
                BasicMouseListener.linkMouseDragEventToANewMouseJoint(mouseJoint);
            }


        }
    }

    public static void linkMouseDragEventToANewMouseJoint(MouseJoint mj) {
        if (mouseJoint != null) {
            // tidy up and destroy old one
            Game.world.destroyJoint(mouseJoint);
            mouseJoint = null;
        }
        mouseJoint = mj;
    }


    private static final AABB queryAABB = new AABB();
    private static final TestQueryCallback callback = new TestQueryCallback();


    public static Body findBodyAtWorldCoords(Vec2 worldCoords) {
        queryAABB.lowerBound.set(worldCoords.x - .001f, worldCoords.y - .001f);
        queryAABB.upperBound.set(worldCoords.x + .001f, worldCoords.y + .001f);
        callback.point.set(worldCoords);
        callback.fixture = null;
        Game.world.queryAABB(callback, queryAABB);

        if (callback.fixture != null) {
            Body body = callback.fixture.getBody();
            return body;
        } else
            return null;
    }

    private static class TestQueryCallback implements QueryCallback {
        public final Vec2 point;
        public Fixture fixture;

        public TestQueryCallback() {
            point = new Vec2();
            fixture = null;
        }

        @Override
        public boolean reportFixture(Fixture argFixture) {
            Body body = argFixture.getBody();
            if (body.getType() == BodyType.DYNAMIC) {
                boolean inside = argFixture.testPoint(point);
                if (inside) {
                    fixture = argFixture;

                    return false;
                }
            }
            return true;
        }
    }
}
