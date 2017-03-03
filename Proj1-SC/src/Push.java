import java.io.Serializable;

public class Push implements Serializable{

	private static String path;
	private static final long serialVersionUID = 2355826059562172987L;

	public Push(String args) {
		this.path = args;
	}

	public String getPath(){
		return path;
	}
	
}
