import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
			System.out.println("thread do server para cada cliente");
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
							  Push push =(Push) obj;
							  makePush(outStream, inStream, push, user);
							  break;
						  case "Share":
							  classe = "Share";
							  break;
						  case "Remove":
							  classe = "Remove";
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

		private void makePush(ObjectOutputStream outStream, ObjectInputStream inStream, Push push, String user) throws IOException {
			String[] path = push.getPath().split("/");
			
			if(userPath(path[0])){
				//diretorio partilhado
			} else { //diretorio do dono
				if(push.isDir()){
					File f = new File(user);
					File share = new File("share.txt");
					
					if(!f.exists() && !share.exists()){
						f.mkdir();
						share.createNewFile();
					}
					
					File rep = new File(user + "/" + path[0]);
					if(!rep.exists()){
						rep.mkdir();
						//diretorio criado
					}
				}
			}
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
			BufferedWriter writer = new BufferedWriter(new FileWriter("utilizadores.txt")); 
			writer.write(user + " " + passwd);
			
			reader.close();
			writer.close();
			return 1;
		}


	}
}
