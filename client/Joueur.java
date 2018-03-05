/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.Serializable;

/**
 *
 * @author guillaume
 */
public class Joueur implements Serializable {

    // Numéro de version de la class commun au client et serveur
    private static final long serialVersionUID = 1L;

    String nom, prenom, license;
    int score = 0;

    /**
     * Contructeur de la classe Joueur
     *
     * @param nom nom du joueur
     * @param prenom prénom du joueur
     * @param license numéro de license du joueur
     */
    public Joueur(String nom, String prenom, String license) {
        this.nom = nom;
        this.prenom = prenom;
        this.license = license;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Joueur{" + "nom=" + nom + ", prenom=" + prenom + ", license=" + license + ", score=" + score + '}';
    }

}
