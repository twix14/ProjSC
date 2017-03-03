import java.io.Serializable;

public class Share implements Serializable{
	
	private String userToShare;
	private String userSharing;
	private String path;
	private static final long serialVersionUID = 1509351590604451182L;

	public Share(String userToShare, String userSharing, String path) {
		this.userToShare = userToShare;
		this.userSharing = userSharing;
		this.path = path;
	}
	
	public String getPath(){
		return path;
	}
	
	public String getUserToShare(){
		return userToShare;
	}
	
	public String getUserSharing(){
		return userSharing;
	}
	
	public String pathDestiny(){
		return userSharing + "/" + path;
	}

}
