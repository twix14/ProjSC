import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public enum ServerStub {
	INSTANCE;

	public Result doPush(Push push, String user, ObjectOutputStream out, ObjectInputStream in, boolean owner) throws IOException, ClassNotFoundException{
		/*boolean b;
		if(owner){
			b = true;
		}
		else{
			b = FileUtilities.INSTANCE.checkUserPermission(user, push.getPath());
			out.writeBoolean(b);
		}

		if(!b)
			return new Result("O utilizador não tem permissão para fazer push", false);*/
		File rep = null;
		String[] path = push.getPath().split("\\\\");
		boolean ok = true;
		Result res = new Result("", false);

		if(!FileUtilities.INSTANCE.userPath(path[0])){
			rep = new File(user + "/" + push.getPath());

			if(push.isDir()){
				File f = new File(user);
				File share = new File(user + "/" + push.getPath() + "/" + "share.txt");

				if(!f.exists() && !share.exists()){
					f.mkdir();
				}

				if(!rep.exists()){
					rep.mkdir();
					share.createNewFile();
					res.setCreated();
					res.setS("-- O repositório "+ rep.getName() +" foi criado no servidor \n");
					//diretorio criado
				}
			}
		} else {
			ok = FileUtilities.INSTANCE.checkUserPermission(user, push.getPath());
			rep = new File(push.getPath());
		}

		if(ok) {
			for(Pair<String, Long> file : push.getFiles()){
				int pos = file.getSt().lastIndexOf(".");
				String fileName = file.getSt().substring(0, pos);
				String extension = file.getSt().substring(pos + 1);

				if(!FileUtilities.INSTANCE.checkFile(in, out)) //se o ficheiro nao estiver atualizado
					FileUtilities.INSTANCE.downloadFile(in, out, rep + " " + fileName + " " +  extension, true);
			}
			StringBuilder sb = new StringBuilder();
			ArrayList<String> removed = new ArrayList<String>();
			for(File fl : rep.listFiles()){
				if(!fl.getName().equals("share.txt") && !fl.getName().contains("_deleted")){
					String[] nameFile = fl.getName().split("_v[\\d]+\\.");
					File[] files = FileUtilities
							.INSTANCE.getNewestVersion(rep, nameFile[0], nameFile[1]);
					if(fl.equals(files[files.length-1])){
						boolean encontrou = false;

						for(Pair<String, Long> fileClient : push.getFiles()){
							int pos = fileClient.getSt().lastIndexOf(".");
							if(fileClient.getSt().substring(0, pos).equals(nameFile[0])){
								encontrou = true;
								sb.append("-- O ficheiro " + fileClient.getSt() + " foi enviado para o servidor \n");
							}
						}
						if(!encontrou){
							System.out.println(rep.getPath());
							removed.add(fl.getName());
							fl.renameTo(new File(rep.getPath() + "\\" + nameFile[0] + "_deleted." + nameFile[1]));
						}
					}	
				}
			}
			if(removed.size() == 1){
				sb.append("-- O ficheiro "+ removed.get(0) +" vai ser eliminado no servidor");
			}
			else if(removed.size() >1){
				sb.append("Os ficheiros ");
				for(int i = 0; i < removed.size()-1; i++)
					sb.append(removed.get(i) + ", ");
				sb.append(removed.get(removed.size()-1)+ " vão ser eliminados no servidor");
			}
			res.setS(sb.toString());
		}

		return res;
	}

	public Result doPull(Pull pull, String user, ObjectOutputStream out, ObjectInputStream in, boolean owner) throws IOException, ClassNotFoundException{
		if(pull.isFile()){

			String[] s = pull.getRep().split("/");
			String f = s[s.length-1];
			String[] file = f.split("\\."); 
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < s.length-1; i++)
				sb.append(s[i]+ "/");
			sb.deleteCharAt(sb.length()-1);
			String r = sb.toString();
			File rep = new File(r);
			File[] versions = FileUtilities.INSTANCE.getNewestVersion(rep, file[0], file[1]);
			List<Pair<String, Long>> single =  new ArrayList<Pair<String, Long>>();
			single.add(new Pair<String,Long>(versions[versions.length-1].getName(), versions[versions.length-1].lastModified()));
			pull.addFiles(single);
			out.writeObject(pull);

			if(!(FileUtilities.INSTANCE.sendReceiveCheckFile(in, out, single.get(0), pull.getLocRep())).equals("up-to-date"))
				FileUtilities.INSTANCE.uploadFile(in, out, versions[versions.length-1], "", true);
		}
		else{
			List <File> files = getFilesDir(new File(pull.getRep()));
			pull.addFiles(getFiles(files));
			out.writeObject(pull);

			int i = 0;
			List<Pair<String, Long>> fls = pull.getFiles();

			for(File fl : files){
				Pair<String, Long> file = fls.get(i);
				if(!file.getSt().contains("_deleted"))
					if(!(FileUtilities.INSTANCE.sendReceiveCheckFile(in, out, file, pull.getLocRep())).equals("up-to-date"))
						FileUtilities.INSTANCE.uploadFile(in, out, fl, "", true);
				i++;
			}
		}
		return new Result("", true);
	}

	private List<Pair<String, Long>> getFiles(List<File> files){
		List<Pair<String, Long>> result = new ArrayList<Pair<String, Long>>();
		for(File fl : files)
			result.add(new Pair<String, Long>(fl.getName(), fl.lastModified()));

		return result;
	}

	private List<File> getFilesDir(File rep){
		List<File> result = new ArrayList<File>();
		for(File fl : rep.listFiles()){
			if(!fl.getName().equals("share.txt")){
				String[] s;
				if(fl.getName().contains("_deleted"))
					s = fl.getName().split("_deleted+\\.");
				else
					s = fl.getName().split("_v[\\d]+\\.");
				File[] versions = FileUtilities.INSTANCE.getNewestVersion(rep, s[0], s[1]);
				if(fl.getName().equals(versions[versions.length-1].getName())){
					result.add(fl);
				}
			}
		}
		return result;
	}
	public Result doShare(Share share) throws IOException{
		File shr = new File(share.pathDestiny() + "/" + "share.txt");

		if(!shr.exists()){
			File dir = new File(share.pathDestiny());
			dir.mkdirs();
			shr.createNewFile();
		}

		BufferedReader reader = new BufferedReader(new FileReader(share.pathDestiny() + "/" + "share.txt"));

		String line;
		boolean userE = false;

		while((line = reader.readLine()) != null){
			userE = line.equals(share.getUserToShare());
		}

		reader.close();

		if(!userE){
			BufferedWriter writer = new BufferedWriter(new FileWriter(share.pathDestiny() + "/" + "share.txt", true)); 
			writer.write(share.getUserToShare());
			writer.newLine();
			writer.close();
			String[] s = share.pathDestiny().split("/");
			return new Result("-- O repositorio " + s[1] + " foi partilhado com o utilizador " + share.getUserToShare() , true); //a mudar para construtor!
		}

		return new Result("Erro: O utilizador" + share.getUserToShare() + " não existe", false);
	}

	public Result doRemove(String path, String user) throws IOException{

		File inputFile = new File(path + "/" + "share.txt");
		File tempFile = new File("myTempFile.txt");

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

		String currentLine;

		while((currentLine = reader.readLine()) != null) {
			if(currentLine.equals(user)) continue;
			writer.write(currentLine);
			writer.newLine();
		}
		writer.close(); 
		reader.close();
		inputFile.delete();
		tempFile.renameTo(inputFile);

		String[] s = path.split("/");
		return new Result("-- O utilizador " + user + " foi removido do repositório " + s[1], true);
	}

}
