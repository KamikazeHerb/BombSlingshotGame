package GameClasses;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

public class Rectangle extends Shape2D{

    public float ratioOfScreenScaleToWorldScaleX;
    public float ratioOfScreenScaleToWorldScaleY;
    private final float rollingFriction,mass;
    public Color col;
    private final Path2D.Float polygonPath;
    private float width;
    private float height;
    private boolean isObjective = false;
    public Rectangle(float sx, float sy, float vx, float vy, float radius, Color col, float mass, float rollingFriction, int numSides) {
        this(sx, sy, vx, vy, radius, col, mass, rollingFriction,mkRegularPolygon(numSides, radius),numSides);
    }
    public Rectangle(float sx, float sy, float vx, float vy, Color col, float mass, float rollingFriction, float height, float width, boolean dynamic){
        World w=Game.world; // a Box2D object
        BodyDef bodyDef = new BodyDef();  // a Box2D object
        if(dynamic){
            bodyDef.type = BodyType.DYNAMIC;// this says the physics engine is to move it automatically
        }
        else{
            bodyDef.type = BodyType.STATIC;
        }
        bodyDef.position.set(sx, sy);
        bodyDef.linearVelocity.set(vx, vy);
        bodyDef.angularDamping = 0.4f;
        bodyDef.linearDamping = 0;
        this.body = w.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        Vec2[] vertices = {new Vec2(-width,height), new Vec2(-width,-height), new Vec2(width, -height), new Vec2(width, height)};
        shape.setAsBox(width,height);
        FixtureDef fixtureDef = new FixtureDef();// This class is from Box2D
        fixtureDef.shape = shape;
        fixtureDef.density = (mass/((width*height)));
        fixtureDef.friction = 0.1f;// this is surface friction;
        fixtureDef.restitution = 0.5f;
        body.createFixture(fixtureDef);
        this.rollingFriction=rollingFriction;
        this.mass=mass;
        this.ratioOfScreenScaleToWorldScaleX=Game.convertWorldLengthXToScreenLengthX(1);
        this.ratioOfScreenScaleToWorldScaleY = Game.convertWorldLengthYToScreenLengthY(1);
        this.col=col;
        //Polygon path making problem
        Path2D.Float p = new Path2D.Float();
        p.moveTo(vertices[0].x,vertices[0].y);
        p.lineTo(vertices[1].x,vertices[1].y);
        p.lineTo(vertices[2].x,vertices[2].y);
        p.lineTo(vertices[3].x,vertices[3].y);
        p.lineTo(vertices[0].x,vertices[0].y);
        p.closePath();
        this.polygonPath=p;
        this.height = height;
        this.width = width;
        body.setUserData(this);
    }
    public Rectangle(float sx, float sy, float vx, float vy, float radius, Color col, float mass, float rollingFriction, Path2D.Float polygonPath, int numSides) {
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
        this.width = (float) Math.sqrt((radius*radius)*2);
        this.height = this.width;
        body.setUserData(this);
        body.setTransform(body.getPosition(),45);
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
        if(this.body.getType().equals(BodyType.DYNAMIC) && !this.isObjective){
            switch (this.getHealth()){
                case 0:
                    this.col = Color.BLACK;
                    break;
                case 1:
                    this.col = Color.DARK_GRAY;
                    break;
                case 2:
                    this.col = Color.DARK_GRAY;
                    break;
                case 3:
                    this.col = Color.GRAY;
                    break;
                case 4:
                    this.col = Color.GRAY;
                    break;
                case 5:
                    this.col = Color.LIGHT_GRAY;
                    break;
                case 6:
                    this.col = Color.LIGHT_GRAY;
                    break;
                case 7:
                    this.col = Color.WHITE;
                    break;
                case 8:
                    this.col = Color.WHITE;
                    break;
                case 9:
                    this.col = Color.YELLOW;
                    break;
                case 10:
                    this.col = Color.YELLOW;
                    break;

            }
        }
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
    public boolean isObjective(){
        return this.isObjective;
    }

    public void setObjective(boolean objective){
        this.isObjective = objective;
    }

    public float getWidth(){
        return width;
    }

    public float getHeight() {
        return height;
    }
}
