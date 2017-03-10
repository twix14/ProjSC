import java.io.Serializable;

public class Result implements Serializable{
	private boolean ok;
	private String s;
	
	private static final long serialVersionUID = 4722700293377523852L;
	
	public Result (String s, boolean ok){
		this.s = s;
		this.ok = ok;
	}

	public boolean allGood() {
		return ok;
	}
	
	@Override
	public String toString(){
		return s;
	}
	
}
