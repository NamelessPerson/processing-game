class Camera{
	float height, width;
	SceneNode focus;

	public float getX(){
		float x = 0;
		if(focus != null) x = focus.x;
		if(x - (width/2) < 0) x = width/2;
		if(x + (width/2) > SceneGraph.width) x = SceneGraph.width-(width/2);
		return x;
	}

	public float getY(){
		float y = 0;
		if(focus != null) y = focus.y;
		if(y - (height/2) < 0) y = height/2;
		if(y + (height/2) > SceneGraph.height) y = SceneGraph.height-(height/2);
		//println("y: "+y);
		return y;
	}
}