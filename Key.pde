class Key extends Actor implements iCollidable{
	int num;

	public Key(float x, float y, int num){
		node = new SceneNode(x,y,10,30,this);
		this.num = num;
	}

	public boolean collides(float x, float y, float width, float height){
		if( abs(x - this.node.x) < (width/2.0)+(this.node.width/2.0) && abs(y - this.node.y) < (height/2.0)+(this.node.height/2.0))
			return true;
		return false;
	}

	public void draw(){
		pushMatrix();
		translate(node.x, node.y, 5);
		fill(235, 223, 34);
		stroke(82);
		rectMode(CENTER);
		rect(0, 0, node.width, node.height);
		popMatrix();
	}

}