import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public enum ClientStub {
	Instance;
	//Envia os dados de um push para um repositorio para o servidor
	public Result sendReceivePush(Push psh, ObjectOutputStream out, ObjectInputStream in, List<File> files, String user) throws IOException, ClassNotFoundException {
		out.writeObject(psh);
		if(!in.readBoolean())
			return (Result) in.readObject();

		int i = 0;
		List<Pair<String, Long>> fls = psh.getFiles();

		for(File fl : files){
			String version;
			Pair<String, Long> file = fls.get(i);
			if(!(version = FileUtilities.INSTANCE.sendReceiveCheckFile(in, out, file, user + "/" + psh.getPath())).equals("up-to-date"))
				FileUtilities.INSTANCE.uploadFile(in, out, fl, version, false);
			i++;
		}

		return (Result) in.readObject();
	}

	//Envia os dados de um pull de um repositorio para o servidor
	public Result sendReceivePull(Pull pll, ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
		out.writeObject(pll);
		if(!in.readBoolean())
			return new Result("Erro, o utilizador nao tem permissao", false);

		Pull pll2 = (Pull) in.readObject();

		Result res = new Result("", true);
		StringBuilder sb = new StringBuilder();
		List<String> removed = new ArrayList<String>();
		if(!pll2.isFile()){
			for(Pair<String, Long> file : pll2.getFiles()){
				String[] extension;
				if(file.getSt().contains("_deleted")){
					extension = file.getSt().split("_deleted+\\.");
					removed.add(extension[0]+"."+ extension[1]);
				}
				else{
					extension = file.getSt().split("_v[\\d]+\\.");
					if(!FileUtilities.INSTANCE.checkFile(in, out)){ //se o ficheiro nao estiver atualizado
						FileUtilities.INSTANCE.downloadFile(in, out, pll2.getLocRep() + " " + extension[0] + " " +  extension[1], false);
						sb.append("-- O ficheiro "+ extension[0] + "."+ extension[1] +" foi copiado do servidor \n");
					}
				}
			}



			res = (Result) in.readObject();
			if(removed.size() == 1){
				res.setS("-- O ficheiro " + removed.get(0) +" existe localmente mas foi eliminado no servidor \n");
				//System.out.println("-- O ficheiro " + removed.get(0) +" existe localmente mas foi eliminado no servidor");
			}
			else if(removed.size() > 1){
				sb.append("-- Os ficheiros " + removed.get(0) + ", ");
				//System.out.print("-- Os ficheiros " + removed.get(0) + ", ");
				for(int i = 1; i < removed.size() -1; i++)
					sb.append(removed.get(i) + ", ");
				//System.out.print(removed.get(i) + ", ");
				sb.append(removed.get(removed.size()-1) + " existem localmente mas foram eliminados no servidor \n");
				//System.out.println(removed.get(removed.size()-1) + " existem localmente mas foram eliminados no servidor");
				res.setS(sb.toString());
			}
			else{
				res.setS(sb.toString());
			}
		}
		else{
			String[] extension = pll2.getFiles().get(0).getSt().split("_v[\\d]+\\.");
			String[] s = pll2.getLocRep().split("/");
			StringBuilder sb1 = new StringBuilder();
			for(int i = 0; i < s.length-1; i++)
				 sb1.append(s[i]+ "/");
			String r = sb1.toString();
			if(!FileUtilities.INSTANCE.checkFile(in, out)){ //se o ficheiro nao estiver atualizado
				FileUtilities.INSTANCE.downloadFile(in, out,r + " " + extension[0] + " " +  extension[1], false);
				sb.append("-- O ficheiro "+ extension[0] + "."+ extension[1] +" foi copiado do servidor \n");
			}
			res.setS(sb.toString());
		}
		return res;
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
