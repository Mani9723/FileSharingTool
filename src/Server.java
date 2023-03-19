import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

/**
 * Server class for Programming Assignment 1
 *
 * This class handles the server side of the
 * File Transfer System using Java sockets.
 *
 * @author Mani Shah, mshah22 G00974705, SWE622
 */

/**
 * TODO: DOWNLOAD(), handle Exceptions for File method calls
 */
public class Server
{
	private static ServerSocket serverSocket;
	private static Socket socket;
	private static DataInputStream dataInputStream;
	private static DataOutputStream dataOutputStream;
	private static int port;

	private final static int BUFFER_SIZE = 4096;

	public Server(int portNum)
	{
		port = portNum;
		if (startServer()) {
			run();
		} else {
			System.out.println("Error starting the server at: " + port);
		}
	}

	/**
	 * Initializes the server and streams
	 *
	 * @return True if server started
	 */
	private boolean startServer()
	{
		try {
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			dataInputStream = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Processes the client commands
	 *
	 * @param command String commands
	 */
	private String processCommand(String command) throws IOException
	{
		String[] args = command.trim().split(" ");
		switch (args[0]) {
			case "shutdown":
				shutdown();
				break;
			case "upload":
				upload(args[2]);
				break;
			case "download":
				download(args[1], args[2]);
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
			default:
				System.out.println("Invalid Command");
				return null;
		}
		return args[0];
	}

	/**
	 * Uploads a file from client to the server
	 * It can resume a previous upload if it was interrupted.
	 * If the file already exists in its entirety then it replaces the file
	 *
	 * @param dest Destination on the server
	 */
	private void upload(String dest) throws IOException
	{
		int bytes = 0;
		long size = dataInputStream.readLong();
		long resumeUploadFrom = 0, uploadedSoFar = 0;
		long fixedSize = size;
		byte[] buffer = new byte[BUFFER_SIZE];
		boolean resumingUpload = false, reachedUploadPoint = false, readNextBuffer = false;;

		FileOutputStream fileOutputStream;
		File file = new File(dest);
		boolean result = file.createNewFile();
		if(!result && file.length() < size) {
			resumingUpload = true;
			resumeUploadFrom = file.length();
		}
		fileOutputStream = new FileOutputStream(dest, resumingUpload);
		while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size)))
				!= -1) {
			while(resumingUpload && !reachedUploadPoint){
				uploadedSoFar += bytes;
				if(uploadedSoFar > resumeUploadFrom){
					reachedUploadPoint = true;
					readNextBuffer = false;
				}else {
					size -= bytes;
					readNextBuffer = true;
					break;
				}
			}
			if(readNextBuffer) continue;
			if(reachedUploadPoint){
				reachedUploadPoint = false;
				resumingUpload = false;
				int startIndexBuffer = getStartIndexBuffer(resumeUploadFrom, uploadedSoFar);
				buffer = Arrays.copyOfRange(buffer,startIndexBuffer,BUFFER_SIZE);
				bytes = size <= BUFFER_SIZE ? ((int)size-startIndexBuffer) :buffer.length;
			}
			fileOutputStream.write(buffer, 0, bytes);
			size -= 4096;
			dataOutputStream.writeInt(100 - (int)((double)(size)/fixedSize * 100));
			dataOutputStream.flush();

			if(buffer.length != 4096) buffer = new byte[4*1024];
		}
		dataOutputStream.writeUTF("Done");
		fileOutputStream.close();
	}

	/**
	 * Calculates the normalized buffer for the data
	 * @param resumeUploadFrom - Current file size of the interrupted upload
	 * @param uploadedSoFar - Skipping these bytes as they have been uploaded
	 * @return - Start index of the buffer
	 */
	private int getStartIndexBuffer(long resumeUploadFrom, long uploadedSoFar)
	{
		int startIndexBuffer = 0;
		if(resumeUploadFrom < 4096){
			startIndexBuffer = (int) resumeUploadFrom;
		}else{
			startIndexBuffer = (int)(resumeUploadFrom - 4096);
			if(startIndexBuffer > 4096){
				startIndexBuffer = 4096 - (int)(uploadedSoFar - resumeUploadFrom);
			}
		}
		return startIndexBuffer;
	}


	/**
	 * Send a file from the server to the client
	 */
	// TODO Finish Download, should be reverse of the the upload flow
	private void download(String source, String dest)
	{
		File sourceFile = new File(source);
		File destFile = new File(dest);

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
			boolean result = false;
			try{
				result = directory.mkdirs();
			}catch (SecurityException e){
				System.out.println(e.getMessage() + " at: " + path);
			}
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
			boolean result = false;
			try{
				result = file.delete();
			}catch (SecurityException e){
				System.out.println(e.getMessage() + " at: " + path);
			}
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
			boolean result = false;
			try {
				result = directory.delete();
			}catch (SecurityException e){
				System.out.println(e.getMessage() + " at: " + path);
			}
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
	 */
	private void run()
	{
		try {
			String cmd = "running";
			while (!Objects.requireNonNull(cmd).equalsIgnoreCase("shutdown")) {
				String command = dataInputStream.readUTF();
				System.out.println("User command: " + command);
				cmd = processCommand(command);
			}
			System.exit(0);
		}catch (IOException e){
			System.out.println("Connection was terminated by Client");
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
