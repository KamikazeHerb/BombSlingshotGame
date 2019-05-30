package GameClasses;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.pooling.normal.DefaultWorldPool;

import java.awt.*;

import static GameClasses.Game.sticks;

/*
Stick class

Originally a weld joint was going to be used for sticky bombs, but this didn't work (problem with world.createJoint())
so this class is used instead.

 */
public class Stick {

    private final Body bomb;
    private final Body target;
    private Vec2 bombAnchor;
    private Vec2 targetAnchor;
    private Vec2 transform;

    /*
    Stick class

    Used instead of a weld joint
    Used to keep two bodies at a fixed transform from each other, allowing a bomb
    to be stuck to another object and stay fixed to it
     */
    public Stick(Body b1, Body b2, Vec2 anchor){
        this.bomb = b1;
        this.target = b2;
        bombAnchor = (b1.getLocalPoint(anchor));
        targetAnchor = (b2.getLocalPoint(anchor));
        bomb.setLinearVelocity(new Vec2(0,0));
        bomb.m_mass = 0;
        bomb.setLinearDamping(0.9f);
        bomb.m_fixtureList.m_density = 0;
        transform = bomb.getPosition().sub(target.getPosition());
    }

    //Sets the position of the bomb using the transform and position of the target body
    public void pull(){
        bomb.setTransform(target.getPosition().add(transform),target.getAngle());
    }
}
