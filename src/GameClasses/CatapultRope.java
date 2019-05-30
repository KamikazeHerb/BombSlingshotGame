package GameClasses;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.RopeJoint;
import org.jbox2d.dynamics.joints.RopeJointDef;

public class CatapultRope {
	/* Author: Michael Fairbank
	 * Creation Date: 2016-01-28
	 * Significant changes applied:
	 */
	private final Body b1;
	private final Body b2;
	private float catapultHeight;
	private final float naturalLength;
	private final float springConstant;
	private final float motionDampingConstant;
	private final boolean canGoSlack;
	private final Color col;
	private final float hookesLawTruncation;
	private RopeJointDef ropeJoint;
	private Joint ropeJoint1;

	public CatapultRope(Body b1, Body b2, float naturalLength, float springConstant, float motionDampingConstant,
						boolean canGoSlack, Color col, float hookesLawTruncation, float catapultHeight) {
		this.b1 = b1;
		this.b2 = b2;
		this.catapultHeight = catapultHeight;
		this.naturalLength = naturalLength;
		this.springConstant = springConstant;
		this.motionDampingConstant=motionDampingConstant;
		this.canGoSlack = canGoSlack;
		this.hookesLawTruncation=hookesLawTruncation;
		this.col=col;
		this.ropeJoint = new RopeJointDef();
		ropeJoint.maxLength = 12;
		ropeJoint.localAnchorA.set(0,0);
		ropeJoint.localAnchorB.set(0,catapultHeight*2);
		ropeJoint.bodyA = b1;
		ropeJoint.bodyB = b2;
		this.ropeJoint1 = Game.world.createJoint(ropeJoint);
	}
	public void removerJoints(){
		Game.world.destroyJoint(ropeJoint1);
    }

	public float calculateTension() {
		// implementation of truncated hooke's law
		float dist=new Vec2(b1.getPosition().x-b2.getPosition().x,(b1.getPosition().y+catapultHeight)-b2.getPosition().y).normalize();
		if (dist<naturalLength && canGoSlack) return 0;
		float extensionRatio = (dist-naturalLength)/naturalLength;
		Float truncationLimit=this.hookesLawTruncation;// this stops Hooke's law giving too high a force which might cause instability in the numerical integrator
		if (truncationLimit!=null && extensionRatio>truncationLimit)
			extensionRatio=truncationLimit;
		if (truncationLimit!=null && extensionRatio<-truncationLimit)
			extensionRatio=-truncationLimit;
		float tensionDueToHookesLaw = extensionRatio*springConstant;
		float tensionDueToMotionDamping=motionDampingConstant*rateOfChangeOfExtension();
		return (tensionDueToHookesLaw+tensionDueToMotionDamping)*4;
	}

	public float rateOfChangeOfExtension() {
		Vec2 v12=new Vec2(b2.getPosition().x-b1.getPosition().x,b2.getPosition().y-(b1.getPosition().y+catapultHeight)); // goes from p1 to p2
		v12=new Vec2(v12.x/v12.normalize(),v12.y/v12.normalize()); // make it a unit vector.
		Vec2 relativeVeloicty=new Vec2(b2.m_linearVelocity.x-b1.m_linearVelocity.x,b2.m_linearVelocity.y-b1.m_linearVelocity.y); // goes from p1 to p2
		return (v12.x*relativeVeloicty.x)+(v12.y*relativeVeloicty.y);// if this is positive then it means the
		// connector is getting longer
	}

	public void applyTensionForceToBothParticles() {
		float tension=calculateTension();
		Vec2 p12=new Vec2(b2.getPosition().x-b1.getPosition().x,b2.getPosition().y-(b1.getPosition().y+catapultHeight)); // goes from p1 to p2
		p12=new Vec2(p12.x/p12.normalize(),p12.y/p12.normalize()); // make it a unit vector.
		Vec2 forceOnP1=p12.mul(tension);
		b1.applyForceToCenter(forceOnP1);
		Vec2 forceOnP2=p12.mul(-tension);// tension on second particle acts in opposite direction (an example of Newton's 3rd Law)
		b2.applyForceToCenter(forceOnP2);
		//System.out.println(forceOnP1 + " : " + forceOnP2);
	}

	public void draw(Graphics2D g) {
		int x1 = Game.convertWorldXtoScreenX(b1.getPosition().x);
		int y1 = Game.convertWorldYtoScreenY(b1.getPosition().y+catapultHeight);
		int x2 = Game.convertWorldXtoScreenX(b2.getPosition().x);
		int y2 = Game.convertWorldYtoScreenY(b2.getPosition().y);
		g.setColor(col);
		g.drawLine(x1, y1, x2, y2);
	}
	Body getB1(){
		return this.b1;
	}

	Body getB2(){
		return this.b2;
	}
}
