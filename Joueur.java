import java.io.Serializable;
class Joueur implements Serializable{
    final static long serialVersionUID =1L;
    String nom,prenom;
    int licence=0;
    int score;
    int nbrJeux=0;
    
    public Joueur() {
        super();
    }

    public Joueur(int license) {
        super();
        this.licence = license;
    }

    public Joueur(String nom,String prenom,int licence){
        this.nom=nom;
        this.prenom=prenom;
        this.licence=licence;
    }
   
    @Override
    public String toString() {
        return "nom : "+this.nom+" prenom "+this.prenom+" score = "+this.score +" nombre de jeux = "+nbrJeux;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof Joueur)) return false;
        Joueur j = (Joueur)obj;
        return this.licence==j.licence;
    }
}