package GameClasses;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

public class Triangle extends Shape2D{

    public float ratioOfScreenScaleToWorldScaleX;
    public float ratioOfScreenScaleToWorldScaleY;
    private final float rollingFriction,mass;
    public final Color col;
    private final Path2D.Float polygonPath;

    public Triangle(float sx, float sy, float vx, float vy, float radius, Color col, float mass, float rollingFriction, int numSides) {
        this(sx, sy, vx, vy, radius, col, mass, rollingFriction,mkRegularPolygon(numSides, radius),numSides);
    }

    public Triangle(float sx, float sy, float vx, float vy, float radius, Color col, float mass, float rollingFriction, Path2D.Float polygonPath, int numSides) {
        World w=Game.world; // a Box2D object
        BodyDef bodyDef = new BodyDef();  // a Box2D object
        bodyDef.type = BodyType.DYNAMIC; // this says the physics engine is to move it automatically
        bodyDef.position.set(sx, sy);
        bodyDef.linearVelocity.set(vx, vy);
        bodyDef.angularDamping = 0.1f;
        bodyDef.linearDamping = 0.5f;
        this.body = w.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        Vec2[] vertices = verticesOfPath2D(polygonPath, numSides);
        shape.set(vertices, numSides);
        FixtureDef fixtureDef = new FixtureDef();// This class is from Box2D
        fixtureDef.shape = shape;
        fixtureDef.density = (float) (mass/((float) numSides)/2f*(radius*radius)*Math.sin(2*Math.PI/numSides));
        fixtureDef.friction = 0.1f;// this is surface friction;
        fixtureDef.restitution = 0.5f;
        body.createFixture(fixtureDef);
        this.rollingFriction=rollingFriction;
        this.mass=mass;
        this.ratioOfScreenScaleToWorldScaleX=Game.convertWorldLengthXToScreenLengthX(1);
        this.ratioOfScreenScaleToWorldScaleY=Game.convertWorldLengthYToScreenLengthY(1);
        this.col=col;
        this.polygonPath=polygonPath;
        body.setUserData(this);
    }

    // Vec2 vertices of Path2D
    public static Vec2[] verticesOfPath2D(Path2D.Float p, int n) {
        Vec2[] result = new Vec2[n];
        float[] values = new float[6];
        PathIterator pi = p.getPathIterator(null);
        int i = 0;
        while (!pi.isDone() && i < n) {
            int type = pi.currentSegment(values);
            if (type == PathIterator.SEG_LINETO) {
                result[i++] = new Vec2(values[0], values[1]);
            }
            pi.next();
        }
        return result;
    }
    public static Path2D.Float mkRegularPolygon(int n, float radius) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(radius, 0);
        for (int i = 0; i < n; i++) {
            float x = (float) (Math.cos((Math.PI * 2 * i) / n) * radius);
            float y = (float) (Math.sin((Math.PI * 2 * i) / n) * radius);
            p.lineTo(x, y);
        }
        p.closePath();
        return p;
    }

    @Override
    public void notificationOfNewTimestep(){
        if (rollingFriction>0) {
            Vec2 rollingFrictionForce=new Vec2(body.getLinearVelocity());
            rollingFrictionForce=rollingFrictionForce.mul(-rollingFriction*mass);
            body.applyForceToCenter(rollingFrictionForce);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        this.ratioOfScreenScaleToWorldScaleX=Game.convertWorldLengthXToScreenLengthX(1);
        this.ratioOfScreenScaleToWorldScaleY=Game.convertWorldLengthYToScreenLengthY(1);
        g.setColor(col);
        Vec2 position = body.getPosition();
        float angle = body.getAngle();
        AffineTransform af = new AffineTransform();
        af.translate(Game.convertWorldXtoScreenX(position.x), Game.convertWorldYtoScreenY(position.y));
        af.scale(ratioOfScreenScaleToWorldScaleX, -ratioOfScreenScaleToWorldScaleY);// there is a minus in here because screenworld is flipped upsidedown compared to physics world
        af.rotate(angle);
        Path2D.Float p = new Path2D.Float (polygonPath,af);
        g.fill(p);
    }
}
