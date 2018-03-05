package server;

import java.io.Serializable;

public class Joueur implements Serializable{

	private static final long serialVersionUID = 1L;
	private String nom,prenom,licence;
	
	public Joueur() {}

	public Joueur(String nom, String prenom, String licence) {
		this.nom = nom;
		this.prenom = prenom;
		this.licence = licence;
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

	public String getLicence() {
		return licence;
	}

	public void setLicence(String licence) {
		this.licence = licence;
	}

}
