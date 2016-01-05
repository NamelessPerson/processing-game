public class PlayerActor extends Actor implements iCollidable{
	float direction = 1;
	
	PlayerState moveState = PlayerState.IDLE;
	PlayerState actionState = PlayerState.IDLE;
	
	int walk_index;
	PImage walk_sprite[];
	PImage jump_sprite;
	PImage push_sprite;

	float yVel = 0;
	float max_yVel = 6;
	final float moveVel = .7;
	boolean flag = false;
	int dt;

	public PlayerActor(float x, float y) {
		node = new SceneNode(x,y, 10, 50, this);
		walk_sprite = new PImage[4];
		walk_sprite[0] = loadImage("res/base_sprite.png");
		walk_sprite[1] = loadImage("res/extend_sprite.png");
		walk_sprite[2] = loadImage("res/base_sprite.png");
		walk_sprite[3] = loadImage("res/contracted_sprite.png");

		jump_sprite = loadImage("res/jump_sprite.png");
		push_sprite = loadImage("res/push_sprite.png");
		dt = millis();
	}

	public void draw(){
		fill(255);
		noStroke();
		imageMode(CENTER);
		pushMatrix();
		translate(node.x, node.y, 0);
		scale(direction, 1, 1);
		//rect(0,0, node.width, node.height);
		if(actionState == PlayerState.JUMPING || actionState == PlayerState.FALLING) image(jump_sprite, 0, 0, node.width, node.height);
		else if(actionState == PlayerState.PUSHING) image(push_sprite, 0, 0, node.width, node.height);
		else{
			if(moveState == PlayerState.IDLE) image(walk_sprite[0], 0, 0, node.width, node.height);
			else {
				image(walk_sprite[walk_index], 0, 0, node.width, node.height);
				if(millis() - dt > 300){
					walk_index =(walk_index+1)%4;
					dt = millis();
				}
			}
		}
		popMatrix();

	}

	public void update(){
		if(moveState == PlayerState.RIGHT) node.x+=moveVel;
		if(moveState == PlayerState.LEFT) node.x-=moveVel;
		

		if(actionState == PlayerState.JUMPING || actionState == PlayerState.FALLING){
			if(yVel + GRAVITY < max_yVel)yVel+=GRAVITY;
			if(yVel > 0) actionState = PlayerState.FALLING;
			node.y+=yVel;
		}
		if(actionState == PlayerState.IDLE && yVel != 0) yVel = 0;
	}
	public PVector nextMove(){
		PVector next = new PVector(node.x, node.y);
		if(moveState == PlayerState.RIGHT) next.x+=moveVel;
		if(moveState == PlayerState.LEFT) next.x-=moveVel;

		next.y+=yVel+GRAVITY;
		
		return next;
	}
	public boolean collides(float x, float y, float width, float height){
		return false;
	}
	public void jump(){
		actionState = PlayerState.JUMPING;
		yVel-=max_yVel;
	}
}

public class PlayerController extends Controller implements KeyListener {
	PlayerActor actor;
	int lastTime;
	boolean up = false;

	public PlayerController(float x, float y) {
		actor = new PlayerActor(x, y);
		SceneGraph.setCameraFocus(actor.node);
		Input.addListener(this, KEY.LEFT);
		Input.addListener(this, KEY.RIGHT);
		Input.addListener(this, KEY.JUMP);
		Input.addListener(this, KEY.UP);
	}

	public void tick(int dt){
		((PlayerActor)actor).flag = false;
		PVector nextMove = actor.nextMove();
		for(SceneNode s : SceneGraph.getColliders(actor.node, nextMove)){
			//println("Curr: "+actor.node.x+", "+actor.node.y+", Next: "+nextMove.x + ", "+nextMove.y);
			if(s.actor instanceof Platform){
				//println(actor.node.x+", "+actor.node.width/2.0+", "+s.x+", "+s.width/2.0);
				//Horizontal
				if(s.x - (s.width/2.0) >= actor.node.x+(actor.node.width/2.0) || s.x+(s.width/2.0) <= actor.node.x-(actor.node.width/2.0)){
					if(((PlayerActor)actor).actionState == PlayerState.IDLE){
						((PlayerActor)actor).actionState = PlayerState.PUSHING;
						((PlayerActor)actor).flag = true;
					}
					//collide right
					if(nextMove.x < s.x && nextMove.x+(actor.node.width/2.0) > s.x - (s.width/2.0)){
						actor.node.x = s.x - (s.width/2.0) - (actor.node.width/2.0);
						((PlayerActor)actor).moveState = PlayerState.IDLE;
					}
					//collide left
					else if(nextMove.x > s.x && nextMove.x-(actor.node.width/2.0) < s.x + (s.width/2.0)){
						actor.node.x = s.x + (s.width/2.0) + (actor.node.width/2.0);
						((PlayerActor)actor).moveState = PlayerState.IDLE;
					}
				}
				//Vertical
				else{
					//collide bottom
					if(nextMove.y < s.y && nextMove.y+(actor.node.height/2.0) > s.y - (s.height/2.0)){
						actor.node.y = s.y - (s.height/2.0) - (actor.node.height/2.0);
						if(!((PlayerActor)actor).flag) ((PlayerActor)actor).actionState = PlayerState.IDLE;
						((PlayerActor)actor).flag = true;
					}
					//collide top
					else if(nextMove.y > s.y && nextMove.y-(actor.node.height/2.0) < s.y + (s.height/2.0)){
						actor.node.y = s.y + (s.height/2.0) + (actor.node.height/2.0);
						((PlayerActor)actor).actionState = PlayerState.FALLING;
						((PlayerActor)actor).flag = true;
					}
				}
			}
			if(s.actor instanceof Door && up){
				if(Player.keys[((Door)s.actor).key])factory.generateLevel(((Door)s.actor).location);
			}
			if(s.actor instanceof Key){
				Player.keys[((Key)s.actor).num] = true;
				SceneGraph.removeSceneNode(s);
			}
		}
		if(!((PlayerActor)actor).flag && ((PlayerActor)actor).actionState != PlayerState.JUMPING && ((PlayerActor)actor).actionState != PlayerState.PUSHING) ((PlayerActor)actor).actionState = PlayerState.FALLING;
		actor.update();
	}

	public void keyPressed(KEY k){
		//println(k);
		if(k == KEY.RIGHT){
			((PlayerActor)actor).moveState = PlayerState.RIGHT;
			((PlayerActor)actor).direction = -1;
		}
		else if(k == KEY.LEFT){
			((PlayerActor)actor).moveState = PlayerState.LEFT;
			((PlayerActor)actor).direction = 1;
		}
		else if(k == KEY.JUMP && ((PlayerActor)actor).actionState == PlayerState.IDLE) ((PlayerActor)actor).jump();
		else if(k == KEY.UP ) up = true;
	}

	public void keyReleased(KEY k){
		//println("released: "+k);
		if(k == KEY.RIGHT || k == KEY.LEFT) ((PlayerActor)actor).moveState = PlayerState.IDLE;
		else if(k == KEY.UP ) up = false;
	}
	public PlayerActor getActor(){
		return actor;
	}
}
public class PlayerManager{
	boolean keys[];

	public PlayerManager () {
		keys = new boolean[10];
		keys[0] = true;
	}

	public void draw(){
		for(int i = 1, j = 1; i < keys.length; i++){
			if(keys[i]){
				pushMatrix();
				translate(displayWidth - (j*40), 10, 50);
				fill(235, 223, 34);
				stroke(82);
				rectMode(CENTER);
				rect(0, 0, 10, 30);
				popMatrix();
				j++;
			}
		}
	}
}