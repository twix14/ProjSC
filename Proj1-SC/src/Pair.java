import java.io.Serializable;

public class Pair<A, B> implements Serializable{
	
	private final A st;
	private final B nd;
	
	private static final long serialVersionUID = 8808383729701415259L;
	
	public Pair(A st, B nd){
		this.st = st;
		this.nd = nd;
	}
	
	public A getSt(){
		return st;
	}
	
	public B getNd(){
		return nd;
	}

}
