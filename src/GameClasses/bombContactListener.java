package GameClasses;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.*;

import java.awt.*;

public class bombContactListener implements ContactListener {

    //Overridden beginContact method, used to allow sticky bombs to stick to other objects
    @Override
    public void beginContact(Contact contact) {
        if(contact.m_fixtureA.getBody().getUserData()!= null && contact.m_fixtureB.getBody().getUserData()!= null){
            if(contact.m_fixtureA.getBody().getUserData().getClass().equals(Bomb.class)){
                    Bomb b = (Bomb)contact.m_fixtureA.getBody().getUserData();
                    if(b.getBombType() == Game.bombType.Sticky && !Game.bombConnected){
                            Vec2 distance = (contact.m_fixtureB.getBody().getPosition().sub(contact.m_fixtureA.getBody().getPosition()));
                            WorldManifold worldManifold = new WorldManifold();
                            contact.getWorldManifold(worldManifold);
                            b.stuck = true;
                            Stick stick = new Stick(contact.m_fixtureA.getBody(),contact.m_fixtureB.getBody(),worldManifold.points[0]);
                            Game.sticks.add(stick);
                    }
            }
            else if(contact.m_fixtureB.getBody().getUserData().getClass().equals(Bomb.class)){
                Bomb b = (Bomb)contact.m_fixtureB.getBody().getUserData();
                if(b.getBombType() == Game.bombType.Sticky && !b.stuck && !Game.bombConnected){
                        Vec2 distance = (contact.m_fixtureA.getBody().getPosition().sub(contact.m_fixtureB.getBody().getPosition()));
                        WorldManifold worldManifold = new WorldManifold();
                        contact.getWorldManifold(worldManifold);
                        b.stuck = true;
                        Stick stick = new Stick(contact.m_fixtureB.getBody(),contact.m_fixtureA.getBody(),worldManifold.points[0]);
                        Game.sticks.add(stick);
                }
                else if(b.stuck){
                    contact.setEnabled(false);
                }
            }
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }
}
