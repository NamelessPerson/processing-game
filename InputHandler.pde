public class InputHandler {

	HashMap<KEY, Boolean> keys;
	HashMap<KEY, ArrayList<KeyListener>> listeners;
	boolean controller[];

	public InputHandler() {
		controller = new boolean[8];
		keys = new HashMap<KEY, Boolean>();
		listeners = new HashMap<KEY, ArrayList<KeyListener>>();
	}

	public void update(KEY k, boolean pressed){
		if(keys.get(k) == null) keys.put(k, false);
		if(keys.get(k) != pressed){
			keys.put(k, pressed);
			
		} 
	}

	public void tick(){
		 Iterator it = keys.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        if((Boolean)(pair.getValue()) == true){
	        	for(KeyListener kl : listeners.get((KEY)pair.getKey()))
					kl.keyPressed((KEY)pair.getKey());
	        }
	        else{
	        	for(KeyListener kl : listeners.get((KEY)pair.getKey()))
					kl.keyReleased((KEY)pair.getKey());
	        	it.remove(); // avoids a ConcurrentModificationException
	        }
	    }
	}

	public void addListener(KeyListener kl, KEY k){
		if(listeners.get(k) == null) listeners.put(k, new ArrayList<KeyListener>());
		listeners.get(k).add(kl);
	}
	public void removeListener(KeyListener kl, KEY k){
		listeners.get(k).remove(kl);
	}
	public void update(int contollerState){
		
	}
}