package GameClasses;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import java.awt.*;

/*
Class to create a vortex when a purple bomb is detonated
 */
public class Vortex {

    public Vec2 coordinates;
    public int vortexRange = 6;
    public int vortexCounter;
    private int SCREEN_RADIUS;

    public Vortex(Vec2 coordinates){

        vortexCounter = 0;
        this.coordinates = coordinates;
        this.SCREEN_RADIUS=(int)Math.max(Game.convertWorldLengthXToScreenLengthX(0.5f),1);

    }

    //Exerts a pull on all bodies within it's range
    public void pull( Body s){
            float blastX = coordinates.x - s.getPosition().x;
            float blastY = coordinates.y - s.getPosition().y;
            Vec2 blast = new Vec2(blastX,blastY);
            if(blast.length()<vortexRange){
                Vec2 pullUnitVector = new Vec2(blast.x/blast.normalize(), blast.y/blast.normalize());
                float pullMultiplier = 4000/blast.normalize() * (float)(1 - (vortexCounter/5000));
                float orbitMultiplier = 1000/blast.normalize() * (float)(1 - (vortexCounter/5000));
                Vec2 pullDirection = pullUnitVector.mul(pullMultiplier);
                Vec2 tangentUnitVector = new Vec2(-pullUnitVector.y, pullUnitVector.x);
                Vec2 tangentDirection = tangentUnitVector.mul(orbitMultiplier);
                s.applyForceToCenter(pullDirection);
                s.applyForceToCenter(tangentDirection);
            }
            vortexCounter++;
    }

    public void draw(Graphics2D g) {
        this.SCREEN_RADIUS=(int)Math.max(Game.convertWorldLengthXToScreenLengthX(0.5f),1);
        int x = Game.convertWorldXtoScreenX(this.coordinates.x);
        int y = Game.convertWorldYtoScreenY(this.coordinates.y);
        g.setColor(Color.MAGENTA);
        g.fillOval(x - SCREEN_RADIUS, y - SCREEN_RADIUS, 2 * SCREEN_RADIUS, 2 * SCREEN_RADIUS);
        g.setColor(Color.white);
    }

}
