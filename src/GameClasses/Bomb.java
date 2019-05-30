package GameClasses;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.awt.*;

public class Bomb {

    public int SCREEN_RADIUS;
    private final float rollingFriction,mass;
    public Color col;
    public Body body;
    private float radius;
    private Game.bombType bombType;
    public boolean stuck;

    public Bomb(float sx, float sy, float vx, float vy, float radius, float mass, float rollingFriction, int bombType) {
        BodyDef bodyDef = new BodyDef();  // a Box2D object
        bodyDef.type = BodyType.DYNAMIC; // this says the physics engine is to move it automatically
        bodyDef.position.set(sx, sy);
        bodyDef.linearVelocity.set(vx, vy);
        this.body = Game.world.createBody(bodyDef);
        CircleShape circleShape = new CircleShape();// This class is from Box2D
        circleShape.m_radius = radius;
        FixtureDef fixtureDef = new FixtureDef();// This class is from Box2D
        fixtureDef.shape = circleShape;
        fixtureDef.density = (float) (mass/(Math.PI*radius*radius));
        fixtureDef.friction = 0.0f;// this is surface friction;
        fixtureDef.restitution = 0.4f;
        body.createFixture(fixtureDef);
        this.rollingFriction=rollingFriction;
        this.mass=mass;
        this.SCREEN_RADIUS=(int)Math.max(Game.convertWorldLengthXToScreenLengthX(radius),1);
        this.radius = radius;
        this.stuck = false;
        switch (bombType){
            case 0 :
                this.bombType = Game.bombType.Explode;
                this.col = Color.RED;
                break;
            case 1 :
                this.bombType = Game.bombType.Implode;
                this.col = Color.BLUE;
                break;
            case 2 :
                this.bombType = Game.bombType.Sticky;
                this.col = Color.GREEN;
                break;
            case 3 :
                this.bombType = Game.bombType.Black_Hole;
                this.col = Color.MAGENTA;
                break;
        }
        body.setUserData(this);
    }

    public void draw(Graphics2D g) {
        this.SCREEN_RADIUS=(int)Math.max(Game.convertWorldLengthXToScreenLengthX(radius),1);
        int x = Game.convertWorldXtoScreenX(body.getPosition().x);
        int y = Game.convertWorldYtoScreenY(body.getPosition().y);
        g.setColor(col);
        g.fillOval(x - SCREEN_RADIUS, y - SCREEN_RADIUS, 2 * SCREEN_RADIUS, 2 * SCREEN_RADIUS);
        g.setColor(Color.white);
    }

    public void notificationOfNewTimestep(){
        if (rollingFriction>0) {
            Vec2 rollingFrictionForce=new Vec2(body.getLinearVelocity());
            rollingFrictionForce=rollingFrictionForce.mul(-rollingFriction*mass);
            body.applyForceToCenter(rollingFrictionForce);
        }
    }
    public void setPosition(Vec2 coordinates){
        this.body.setTransform(coordinates,this.body.getAngle());
    }

    public float getRadius(){
        return this.radius;
    }
    public Game.bombType getBombType() { return bombType; }

}
