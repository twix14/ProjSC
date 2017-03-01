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
	//Threads utilizadas para comunicacao com os clientes
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

				String user = null;
				String passwd = null;
				try {
					user = (String)inStream.readObject();
					passwd = (String)inStream.readObject();
					System.out.println("thread: depois de receber a password e o user");
				}catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
				
				outStream.writeObject(verificaUtilizador(user, passwd));
				
					
				outStream.close();
				inStream.close();

				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
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
				if(user.equals(curr[1])){
					if(passwd.equals(curr[2]))
						return 0;
					else{
						System.out.println("Palavra passe incorreta");
						return -1;
					}	
				}	
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter("utilizadores.txt")); 
			writer.write(user + " " + passwd);
			
			reader.close();
			writer.close();
			return 1;
		}


	}
}
