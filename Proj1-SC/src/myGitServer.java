import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class myGitServer {

	public static void main(String[] args) {
		System.out.println("servidor: main");
		myGitServer server = new myGitServer();
		server.startServer(Integer.parseInt(args[0]));
	}

	public void startServer (int socket){

		ServerSocket sSoc = null;

		try {
			sSoc = new ServerSocket(socket);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}
		//sSoc.close();
	}

	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
		}

		public void run(){

			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String user = (String)inStream.readObject();
				String passwd = (String)inStream.readObject();

				outStream.writeObject(verificaUtilizador(user, passwd));

				if(inStream.readBoolean()){ //true se tiver operacao pull,push...

					Object obj = inStream.readObject();
					String classe = "";

					switch (obj.getClass().getName()) {
					case "Pull":
						classe = "Pull";
						break;
					case "Push":
						Push push = (Push) obj;
						makePush(outStream, inStream, push, user);
						break;
					case "Share":
						Share share = (Share) obj;
						if(userPath(share.getUserToShare())){
							boolean escreveu = share(share.pathDestiny(), share.getUserToShare());
							if(escreveu){
								outStream.writeBoolean(true);
							}
							else{
								outStream.writeBoolean(false);
							}
						}
						else{
							outStream.writeBoolean(false);
						}
						break;
					case "Remove":
						Remove remove = (Remove) obj;
						remove.removeUser();
						break;
					}

					outStream.writeObject(classe); //teste

				}

				outStream.close();
				inStream.close();

				socket.close();

			} catch (SocketException e) {
				System.err.println("Um dos clientes desligou-se"); //para nao dar erro quando cliente fecha a socket
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		//mudar tipo para boolean para saber se escreveu do outro lado
		private boolean share(String pathDestiny, String userToShare) throws IOException {
			BufferedReader reader = null;
			reader = new BufferedReader(new FileReader(pathDestiny + "/" + "share.txt"));
			String line;
			boolean userE = false;
			while((line = reader.readLine()) != null){
				userE = line.equals(userToShare);
			}
			reader.close();
			if(!userE){
				BufferedWriter writer = new BufferedWriter(new FileWriter(pathDestiny + "/" + "share.txt", true)); 
				writer.write(userToShare);
				writer.newLine();
				writer.close();
				return true;
			}
			
			return false;
		}

		private void makePush(ObjectOutputStream outStream, ObjectInputStream inStream, Push push, String user) throws IOException {
			String[] path = push.getPath().split("\\\\");
			File rep = null;

			if(userPath(path[0])){
				//diretorio partilhado
			} else { //diretorio do dono
				rep = new File(user + "/" + path[0]);
				if(push.isDir()){
					File f = new File(user);
					File share = new File(user + "/" + push.getPath() + "/" + "share.txt");

					if(!f.exists() && !share.exists()){
						f.mkdir();
					}

					if(!rep.exists()){
						rep.mkdir();
						share.createNewFile();
						//diretorio criado
					}
				}
			}

			File[] files = push.getFiles();
			for(int i = 0; i < files.length; i++){
				FileInputStream fil =  new FileInputStream(files[i]);
				InputStream fis = new BufferedInputStream(fil);
				String[] extension = files[i].getName().split("\\.(?=[^\\.]+$)");
				String version = fileVersion(files[i], rep, extension[0], extension[1]);
				//controlar as versoes aqui

				if(!version.equals("up-to-date")){

					FileOutputStream fos = new FileOutputStream(rep + "/" + extension[0] + version + extension[1]);

					byte[] bytes = new byte[1024];

					int temp = (int) files[i].length();
					int count = 1024;

					while ((count = fis.read(bytes, 0, temp < 1024 ? temp : 1024)) > 0) {
						fos.write(bytes, 0, count);
						temp -= count;
						fos.flush();
					}
					fos.close();
				}

				fil.close();
				fis.close();
			}
		}

		/**
		 * Checks if file being received is more recent than the on the server
		 * if not returns "up-to-date"
		 * @param rep 
		 */
		private String fileVersion(File fl, File rep, String name, String extension) {
			File[] versions = getNewestVersion(rep, name, extension);
			if(versions.length == 0)
				return "_v0.";
			else if(fl.lastModified() > versions[versions.length-1].lastModified()){ //ficheiro mais recente do que aquele que esta no servidor
				return "_v" + versions.length + ".";
			} else return "up-to-date";
		}

		private File[] getNewestVersion(File dir, String name, String extension) {
			return dir.listFiles(new FilenameFilter(){
				public boolean accept(File dir, String name)
				{	
					return name.startsWith(name) && name.endsWith("." + extension);
				}
			});
		}

		private boolean userPath(String string) throws IOException {
			BufferedReader reader = null;

			try {
				reader = new BufferedReader(new FileReader("utilizadores.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			String line;
			while((line = reader.readLine()) != null){
				String[] curr = line.split(" ");
				if(string.equals(curr[0]))
					return true;
			}

			return false;
		}

		private int verificaUtilizador(String user, String passwd) throws IOException {
			BufferedReader reader = null;
			File utilizadores = new File ("utilizadores.txt");

			if(!utilizadores.exists())
				utilizadores.createNewFile();

			try {
				reader = new BufferedReader(new FileReader("utilizadores.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			String line;
			while((line = reader.readLine()) != null){
				String[] curr = line.split(" ");
				if(user.equals(curr[0]))
					return 0;
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter("utilizadores.txt", true)); 
			writer.write(user + " " + passwd);
			writer.newLine();

			writer.close();
			reader.close();
			return 1;
		}


	}
}

