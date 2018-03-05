package server;

import java.io.*;
import java.net.*;
import java.util.Date;


public class Partie extends Thread{
	private int numero;
	private long debut;
	private Joueur j;
	private Socket s; 
	public Partie() {}
	public Partie(Joueur j, long debut, Socket s) {
		this.j = j;
		this.debut = debut;
		this.s = s;
	}
	public void run() {
		try {
			InputStream is = s.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			OutputStream os = s.getOutputStream();
			PrintWriter pw =new PrintWriter(os,true);
			System.out.println("Départ de la partie avec joueur n° "+j.getNom());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
}
