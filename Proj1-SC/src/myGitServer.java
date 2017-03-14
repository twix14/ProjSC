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
		}

		public void run(){

			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String user = (String)inStream.readObject();
				String passwd = (String)inStream.readObject();

				outStream.writeObject(verificaUtilizador(user, passwd));
				
				

				if(inStream.readBoolean()){ //true se tiver operacao pull,push...
					String owner = (String)inStream.readObject();
					boolean test = !FileUtilities.INSTANCE.userPath(owner);
					outStream.writeObject(test); 
					//verificar se o utilizador eh ou nao dono do repositorio que esta a tentar utilizar
					
					Object obj = inStream.readObject();
					Result res = null;

					switch (obj.getClass().getName()) {
						case "Pull":
							Pull pull = (Pull) obj;
							res = ServerStub.INSTANCE.doPull(pull, user, outStream, inStream);
							break;
						case "Push":
							Push push = (Push) obj;
							res = ServerStub.INSTANCE.doPush(push, user, outStream, inStream);
							break;
						case "Share":
							Share share = (Share) obj;
							if(FileUtilities.INSTANCE.userPath(share.getUserToShare())){
								res = ServerStub.INSTANCE.doShare(share);
							}else{
								res = new Result("Erro: O utilizador " + share.getUserToShare() + " não existe", false);
							}
							break;
						case "Remove":
							Remove remove = (Remove) obj;
							res = ServerStub.INSTANCE.doRemove(remove.getPath(), remove.getUser());
							break;
						default:
							res = new Result("", false);
					}
					

					outStream.writeObject(res); //teste

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

