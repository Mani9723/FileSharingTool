import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Server class for Programming Assignment 1
 *
 * This class handles the server side of the
 * File Transfer System using Java sockets.
 *
 * @author Mani Shah, mshah22 G00974705, SWE622
 */

/**
 * TODO: UPLOAD(), DOWNLOAD()
 */
public class Server
{
	private static ServerSocket serverSocket;
	private static Socket socket;
	private static DataInputStream dataInputStream;
	private static DataOutputStream dataOutputStream;
	private static int port;

	public Server(int portNum)
	{
		port = portNum;
		if(startServer()){
			run();
		}else{
			System.out.println("Error starting the server at: " + port);
		}
	}
	/**
	 * Initializes the server and streams
	 * @return True is server started
	 */
	private boolean startServer()
	{
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Started server at: " + serverSocket.getLocalPort());
			socket = serverSocket.accept();
			System.out.println("Client socket has been accepted: " + socket.isConnected());
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			dataInputStream = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Processes the client commands
	 * @param command String commands
	 */
	private void processCommand(String command) throws IOException
	{
		String[] args = command.trim().split(" ");
		switch (args[0]) {
			case "shutdown":
				shutdown();
				break;
			case "upload":
				upload(args[1], args[2]);
				break;
			case "download":
				download();
				break;
			case "dir":
				dir(args[1]);
				break;
			case "mkdir":
				mkdir(args[1]);
				break;
			case "rmdir":
				rmdir(args[1]);
				break;
			case "rm":
				rm(args[1]);
				break;
		}
	}

	/**
	 * Uploads a file from client to the server
	 */
	// TODO Finish upload
	private void upload(String source, String dest) throws IOException
	{
		System.out.println("At upload");
		dataOutputStream.writeUTF(source +", "+dest);
		System.out.println("Sent to client");
	}

	/**
	 * Downloads a file from the server to the client
	 */
	// TODO Finish Download
	private void download()
	{

	}

	/**
	 * Creates a new directory in the server if it
	 * does not exist before
	 * @param path Directory path
	 * @throws IOException Network issues
	 */
	private void mkdir(String path) throws IOException
	{
		File directory = new File(path);
		System.out.println("Creating a directory at: " + directory.getPath());
		if(!directory.isDirectory() && !directory.isFile()) {
			System.out.println("Going to create it");
			boolean result = directory.mkdirs();
			System.out.println("Created: " + result);
			dataOutputStream.writeUTF(Boolean.toString(result));
		}
	}

	/**
	 * Prints the current directory in the server to the client
	 */
	private void dir(String path) throws IOException
	{
		File dirPath = new File(path);
		if(dirPath.isDirectory() && !dirPath.isFile()) {
			File[] dirList = dirPath.listFiles();
			dataOutputStream.writeUTF(Arrays.toString(dirList)
					.replace("[","")
					.replace("]","")
					.replace(",","\n"));
		}
	}

	/**
	 * Removes a file from the server as requested by the client
	 */
	private void rm(String path) throws IOException
	{
		File file = new File(path);
		if(!file.isDirectory() && file.exists()){
			boolean result = file.delete();
			dataOutputStream.writeUTF(Boolean.toString(result));
		}
	}

	/**
	 * Removes a directory from the server as requested by the client
	 */
	private void rmdir(String path) throws IOException
	{
		File directory = new File(path);
		if(directory.isDirectory() && !directory.isFile()){
			boolean result = directory.delete();
			dataOutputStream.writeUTF(Boolean.toString(result));
		}
	}

	/**
	 * Shuts down the server cleanly
	 */
	public static void shutdown()
	{
		try {
			if(dataInputStream != null && dataOutputStream != null) {
				dataOutputStream.flush();
				dataInputStream.close();
				dataOutputStream.close();
				serverSocket.close();
				socket.close();
				System.out.println("Closed everything");
			}
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Runs the main program
	 * @throws IOException is network error
	 */
	private void run()
	{
		try {
			while (true) {
				String command = dataInputStream.readUTF();
				System.out.println("User command: " + command);
				processCommand(command);
				break;
			}
		}catch (IOException e){
			System.out.println("Connection was terminated by Client");
		}finally {
			shutdown();
		}
	}

	public static void main(String[] args)
	{
		if(args.length != 2 || !args[0].equalsIgnoreCase("start")){
			System.out.println("java -cp pa1.jar server start <portnumber>");
			System.exit(-1);
		}else{
			try{
				int port = Integer.parseInt(args[1]);
				if(port < 1024 || port > 65535) throw new NumberFormatException();
				// start the server at port number
				new Server(port);
			}catch (NumberFormatException e){
				System.out.println("Port number must be int. 1024 <= port <= 65,535");
			}
		}
	}
}
