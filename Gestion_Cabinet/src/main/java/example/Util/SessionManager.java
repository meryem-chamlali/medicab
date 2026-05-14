package example.Util;

import example.Model.Utilisateur;

public class SessionManager {

    private static Utilisateur utilisateurConnecte = null;

    public static void setUtilisateur(Utilisateur u) {
        utilisateurConnecte = u;
    }

    public static Utilisateur getUtilisateur() {
        return utilisateurConnecte;
    }

    public static void deconnexion() {
        utilisateurConnecte = null;
    }

    public static boolean estConnecte() {
        return utilisateurConnecte != null;
    }
}
