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

        Socket socket;
        ObjectOutputStream outToServer;
        ObjectInputStream inFromServer;
        Requete ans, resp = null;
        Scanner sc = new Scanner(System.in);
        Joueur j;

        String ipAddr = ipAddress();

        try {
            //demande d'ouverture d'une connexion sur le serveur de jeu et le numero de port 60000
            socket = new Socket(ipAddr, 60000);//args[0]

            inFromServer = new ObjectInputStream(socket.getInputStream());
            outToServer = new ObjectOutputStream(socket.getOutputStream());

            try{
               resp = (Requete) inFromServer.readObject(); 
            } catch (Exception e){
                System.err.println(e.getMessage());
            }
            

            if(resp.intent.equalsIgnoreCase("connected")){
                System.out.println(resp.answer);

                boolean auth = false;
                do {
                    ans = authentification();
                    System.out.println(ans.intent);

                    outToServer.writeObject(ans);

                    try{
                       resp = (Requete) inFromServer.readObject(); 
                    } catch (Exception e){
                        System.err.println(e.getMessage());
                    }

                    if(ans.intent.equalsIgnoreCase("login")){
                        if(resp.answer.equalsIgnoreCase("login-success")){
                            System.out.println("Authentification reussi");
                            j = resp.getJoueur();
                            auth = true;
                        }else if(resp.answer.equalsIgnoreCase("login-fail")){
                            System.out.println("Authentification échoué");
                        } else {
                            System.err.println("Erreur de communication avec le serveur");
                        }
                    }else if(ans.intent.equalsIgnoreCase("signup")){
                        System.err.printf(resp.answer);
                        if(resp.answer.equalsIgnoreCase("signup-success")){
                            System.out.println("Inscription reussi");
                            j = resp.getJoueur();
                            auth = true;
                        } else {
                            System.err.println("Erreur de communication avec le serveur (ER001)");
                        }
                    }
                }while(!auth);

                pause();

                System.out.println("Préparation du jeux");

                resp = new Requete(null, "start", null, 0L);
                outToServer.writeObject(resp);

                try{
                   resp = (Requete) inFromServer.readObject(); 
                } catch (Exception e){
                    System.err.println(e.getMessage());
                }

                if(ans.answer.equalsIgnoreCase("start-ok")){

                    boolean win = false;
                    String answer;
                    do {
                        System.out.flush();
                        System.out.println("Proposer une suite de 5 lettre :");
                        answer = sc.nextLine();
                        if(answer.length() == 5){
                            resp = new Requete(null, "try", answer, 0L);
                            outToServer.writeObject(resp);

                            try{
                               resp = (Requete) inFromServer.readObject(); 
                            } catch (Exception e){
                                System.err.println(e.getMessage());
                            }

                            if(ans.intent.equalsIgnoreCase("answer")){
                                String[] tokens = ans.answer.split(":");
                                System.out.println(tokens[0] + " lettre(s) sont bonnes et " + tokens[1] + " lettres sont bien placée(s)." );
                            }else if(ans.answer.equalsIgnoreCase("game-success")){
                                System.out.println("Bravo tu à trouvée en " + ans.time);
                                win = true;
                            }else {
                                System.err.println("Erreur de communication avec le serveur (ER003)");
                            }
                        }else {
                            System.err.println("Format de la réponse incorect");
                        }
                        pause();
                    }while(win);
                } else {
                    System.err.println("Erreur de communication avec le serveur (ER002)");
                }
                

            } else {
               System.err.println("Connexion au serveur echouée"); 
            }
            
            //fermeture de la connexion
            socket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String ipAddress(){

        String ipAddr;
        boolean ipAddrValid;
        Scanner sc = new Scanner(System.in);

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

        return ipAddr;
    }

    public static Requete authentification(){
        int choix;
        Scanner sc = new Scanner(System.in);

        //Demande d'authetification
        do {
            System.out.println("Choisissez la maniére de vous identifier :");
            System.out.println("1. Connexion");
            System.out.println("2. Inscription");
            System.out.println("Entrez 1 ou 2 pour votre choix :");
            choix = sc.nextInt();
            if (choix != 1 && choix != 2) {
                System.err.println("Format invalid");
            }
            System.out.flush();
        } while (choix != 1 && choix != 2);

        if(choix == 1){
            int license = joueurLogin();
            Joueur j = new Joueur(license);
            return new Requete(j, "login", null, 0L);
        } else {
            Joueur j = joueurRegister();
            return new Requete(j, "signup", null, 0L);
        }
    }

    public static int joueurLogin(){
        int license;
        boolean licenseValid;
        Scanner sc = new Scanner(System.in);
        //Demande de la license
        do {
            System.out.println("Entree votre clé de license :");
            license = sc.nextInt();
            if (license >= 0001 && license <10000) {
                System.err.println("Format de la license invalid");
            }
            System.out.flush();
        } while (license >= 0001 && license <10000);

        return license;
    }

    public static Joueur joueurRegister(){

        String nom, prenom;
        boolean nomValid, prenomValid;
        Scanner sc = new Scanner(System.in);
        
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

        // Joueur a partager avec le serveur.
        return new Joueur(nom, prenom, 0000); 
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
     * Mettre le programme en attente
     *
     * @return boolean
     */
    public static void pause() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Pour continuer appuyer sur une touche :");
        sc.nextLine();
        System.out.flush();
    }
}