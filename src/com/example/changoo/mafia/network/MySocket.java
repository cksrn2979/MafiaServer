package com.example.changoo.mafia.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;

public class MySocket{

	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	

	public MySocket(Socket socket) throws IOException {
		this.socket = socket;
		is = socket.getInputStream();
		os = socket.getOutputStream();
		ois = new ObjectInputStream(is);
		oos = new ObjectOutputStream(os);
	}

	public void writeObject(Object obj) throws ClassNotFoundException, IOException {
		oos.writeObject(obj);
		oos.reset();
	}


	public Object readObject() throws ClassNotFoundException, IOException {		
		return ois.readObject();
		
	}


	public void close() throws IOException {
		os.close();
		is.close();
		oos.close();
		ois.close();
		socket.close();
	}
	
    public Socket getSocket() {
        return socket;
    }
}
