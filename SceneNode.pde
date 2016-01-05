public class SceneNode{
	public float x;
	public float y;
	public float width;
	public float height;

	protected iDrawable actor;

	public SceneNode(float x, float y, float width, float height, iDrawable actor){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.actor = actor;

	}

	public void draw(){
		actor.draw();
	}
}