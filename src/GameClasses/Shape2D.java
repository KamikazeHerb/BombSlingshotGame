package GameClasses;

import org.jbox2d.dynamics.Body;

import java.awt.*;

public abstract class Shape2D {

    public Body body;
    private int health = 10;

    public abstract void notificationOfNewTimestep();
    public abstract void draw(Graphics2D g);

    public int getHealth(){
        return this.health;
    }

    public void setHealth(int health) {
        if(health < 0){
            this.health = 0;
        }
        else{
            this.health = health;
        }
    }
}
