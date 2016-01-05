public class Actor implements iDrawable{

	public SceneNode node;

	public Actor(float x, float y){
		node = new SceneNode(x,y,1,1,this);
	}

	public Actor(){
		node = new SceneNode(0,0,1,1,this);
	}

	public void draw(){

	}

}

public class Controller implements iTickable{
	public Actor actor;

	public Controller(float x, float y){
		actor = new Actor(x, y);
	}

	public Controller(){
		actor = new Actor();
	}

	public void preTick(int dt){

	}

	public void tick(int dt){

	}

	public void postTick(int dt){

	}
	public Actor getActor(){
		return actor;
	}

}