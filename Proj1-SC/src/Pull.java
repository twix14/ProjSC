import java.io.Serializable;
import java.util.List;

public class Pull implements Serializable{

	private String repRem;
	private String locRep;
	List<Pair<String,Long>> files;
	private boolean isFile;
	private static final long serialVersionUID = -857533466048543308L;

	public Pull(String repRem, String locRep, boolean isFile) {
		this.locRep = locRep;
		this.repRem = repRem;
		this.isFile = isFile;
	}

	public List<Pair<String, Long>> getFiles() {
		return files;
	}
	
	public boolean isFile(){
		return isFile;
	}
	public void addFiles(List<Pair<String,Long>> files){
		this.files = files;
	}
	
	public String getRep(){
		return repRem;
	}

	public String getLocRep(){
		return locRep;
	}
}
