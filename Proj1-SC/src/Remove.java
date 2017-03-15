import java.io.Serializable;

public class Remove implements Serializable{
	
	private final String user;
	private final String path;

	private static final long serialVersionUID = 6721648574636359162L;

	public Remove(String rep, String user, String masterUser) {
		this.user = user;
		this.path = masterUser + "/" + rep;
		
	}
	
	public String getUser(){
		return user;
	}
	
	public String getPath(){
		return path;
	}
}

