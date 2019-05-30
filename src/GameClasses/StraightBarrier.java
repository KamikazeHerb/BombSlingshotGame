package GameClasses;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import java.awt.*;

public class StraightBarrier {
    private Vec2 startPos,endPos;
    private final Color col;
    public final Body body;


    public StraightBarrier(float startx, float starty, float endx, float endy, Color col) {


        startPos=new Vec2(startx,starty);
        endPos=new Vec2(endx,endy);

        World w=Game.world;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC;
        bodyDef.position = new Vec2(startx,starty);
        Body body = w.createBody(bodyDef);
        this.body=body;
        body.setUserData(this);
        Vec2[] vertices = new Vec2[] { new Vec2(), new Vec2(endx-startx, endy-starty) };
        ChainShape chainShape = new ChainShape();
        chainShape.createChain(vertices, vertices.length);
        body.createFixture(chainShape, 0);


        this.col=col;
    }

    public void draw(Graphics2D g) {
        int x1 = Game.convertWorldXtoScreenX(startPos.x);
        int y1 = Game.convertWorldYtoScreenY(startPos.y);
        int x2 = Game.convertWorldXtoScreenX(endPos.x);
        int y2 = Game.convertWorldYtoScreenY(endPos.y);
        g.setColor(col);
        g.drawLine(x1, y1, x2, y2);
    }
}
