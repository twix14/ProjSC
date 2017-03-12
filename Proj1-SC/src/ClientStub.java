import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;

public class ClientStub {
	
	private FileUtilities f = new FileUtilities(); 
	
	//Envia os dados de um push para um repositorio para o servidor
	public Result sendReceivePush(Push psh, ObjectOutputStream out, ObjectInputStream in, List<File> files, String user) throws IOException, ClassNotFoundException {
		out.writeObject(psh);
		
		int i = 0;
		List<Pair<String, Long>> fls = psh.getFiles();
		
		for(File fl : files){
			String version;
			Pair<String, Long> file = fls.get(i);
			if(!(version = f.sendReceiveCheckFile(in, out, file, user + "/" + psh.getPath())).equals("up-to-date"))
				f.uploadFile(in, out, fl, version);
			i++;
		}
		
		return (Result) in.readObject();
	}
	
	//Envia os dados de um pull de um repositorio para o servidor
	public Result sendReceivePull(Pull pll, ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
		out.writeObject(pll);
		
			
		return (Result) in.readObject();
	}

	// Envia os dados de um remove de permissoes de um repositorio partilhado para o servidor
	public Result sendReceiveRemove(Remove rm, ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException{
		out.writeObject(rm);
		
		return (Result) in.readObject();
	}

	//Envia os dados de um share de um repositorio para o servidor
	public Result sendReceiveShare(Share shr, ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
		out.writeObject(shr);
		
		/* ver com o daniel
		if(!in.readBoolean())
			System.out.println("Erro: o utilizador " + shr.getUserToShare() + " não existe");
		else{
			System.out.println("-- O repositório myrep foi partilhado com o utilizador " + shr.getUserToShare());
		}*/
		
		return (Result) in.readObject();
	}
	

}
