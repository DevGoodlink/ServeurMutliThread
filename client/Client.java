/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    public static void main(String[] args) {

        Joueur j = joueurRegister();

        Socket socket;
        BufferedReader in;
        PrintWriter out;
        ObjectOutputStream outToServer;
        ObjectInputStream inFromServer;
        String message_sortant;
        String message_distant = "";

        try {
            //demande d'ouverture d'une connexion sur le serveur de jeu et le numero de port 60000
            socket = new Socket(ipAddr, 60000);//args[0]
            while (!message_distant.equalsIgnoreCase("disconnect")) {
                //attente du message serveur pour debut d'authentification
                inFromServer = new ObjectInputStream(socket.getInputStream());
                message_distant = inFromServer.readUTF();
                System.out.println("message :" + message_distant);

                //reponse au serveur avec l'objet Joueur
                outToServer = new ObjectOutputStream(socket.getOutputStream());
                if (message_distant.equalsIgnoreCase(new String("Auth-att"))) {

                    switch ()
                    outToServer.writeObject(j);
                    System.out.println("Authentification au prét du serveur (" + message_distant + ")");
                } else {
                    System.out.println("Le serveur à réfusé la connexion (" + message_distant + ")");
                }
                outToServer.flush();

                //attente du message de validation d'authentification du serveur
                inFromServer = new ObjectInputStream(socket.getInputStream());
                message_distant = inFromServer.readUTF();
                if (message_distant.equalsIgnoreCase(new String("Auth-ok"))) {
                    System.out.println("Authentification réussie (" + message_distant + ")");
                }

            }

            //fermeture de la connexion
            socket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Joueur joueurRegister(){

        String nom, prenom, license, ipAddr;
        boolean nomValid, prenomValid, licenseValid, ipAddrValid;
        Scanner sc = new Scanner(System.in);

        //Demande de la license
        do {
            System.out.println("Entree votre clé de license :");
            license = sc.nextLine();
            licenseValid = Client.validateLicense(license);
            if (!licenseValid) {
                System.err.println("Format de la license invalid");
            }
            System.out.flush();
        } while (!licenseValid);
        
        //Demande du nom
        do {
            System.out.println("Entree votre nom :");
            nom = sc.nextLine();
            nomValid = Client.validateNomPrenom(nom);
            if (!nomValid) {
                System.err.println("Format de votre nom invalid");
            }
            System.out.flush();
        } while (!nomValid);

        //Demande du prenom
        do {
            System.out.println("Entree votre prenom :");
            prenom = sc.nextLine();
            prenomValid = Client.validateNomPrenom(prenom);
            if (!prenomValid) {
                System.err.println("Format de votre prenom invalid");
            }
            System.out.flush();
        } while (!prenomValid);
        
        //Demande de l'adresse ip
        do {
            System.out.println("Entree l'adresse ip du serveur de jeu :");
            ipAddr = sc.nextLine();
            ipAddrValid = Client.validateIpAddress(ipAddr);
            if (!ipAddrValid) {
                System.err.println("Format de l'adresse ip invalid");
            }
            System.out.flush();
        } while (!ipAddrValid);

        // Joueur a partager avec le serveur.
        return new Joueur(nom, prenom, license); 
    }

    /**
     * Valide le bon format d'une adresse IPv4
     *
     * @param ipAddress adresse ip à valider
     * @return boolean
     */
    public static boolean validateIpAddress(String ipAddress) {
        String[] tokens = ipAddress.split("\\.");

        if (tokens.length != 4) {
            return false;
        }

        for (String str : tokens) {
            int i = Integer.parseInt(str);
            if ((i < 0) || (i > 255)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Valide le bon format d'un nom ou prénom
     *
     * @param chaine nom ou prénom à valider
     * @return boolean
     */
    public static boolean validateNomPrenom(String chaine) {
        if (chaine.length() <= 0) {
            return false;
        }
        if (chaine.matches(".*[0-9].*")) {
            return false;
        }

        return true;
    }

    /**
     * Valide la bon format d'une license du jeu
     *
     * @param license license à valider
     * @return boolean
     */
    public static boolean validateLicense(String license) {
        String[] tokens = license.split("-");

        if (tokens.length != 4) {
            return false;
        }

        for (String str : tokens) {
            int i = Integer.parseInt(str);
            if ((i < 0) || (i > 999)) {
                return false;
            }
        }

        return true;
    }
}
