public class LampActor extends Actor{
	PImage lamp_sprite;
	float angle;
	float maxAngle = 10;
	float step = .01;
	public  LampActor(float x, float y, float width, float height) {
		angle = random(-maxAngle, maxAngle);
		step = random(.01, .02);
		lamp_sprite = loadImage("res/spotlight.png");
		node = new SceneNode(x,y,width, height, this);
	}
	public void draw(){
		pushMatrix();
		imageMode(CORNER);
		translate(node.x-(node.width/2), node.y, 20);
		rotate(radians(angle), 0,0,1);
		image(lamp_sprite, 0, 0, node.width, node.height);
		popMatrix();
	}
	public void swing(){
		angle+= step;
		if(angle > 10 || angle < -10) step*=-1;
	}

}
public class LampController extends Controller {
	boolean swing;
	public LampController(float x, float y, float width, float height, boolean swing){
		actor = new LampActor(x,y, width, height);
		this.swing = swing;
		SceneGraph.addTickable(this);
	}
	public void tick(int dt){
		if(swing) ((LampActor)actor).swing();
	}

}