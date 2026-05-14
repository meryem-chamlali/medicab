package example.Model;

public class Utilisateur {

    private int    id;
    private String username;
    private String nom;
    private String prenom;
    private String role;

    public Utilisateur() {}

    public Utilisateur(int id, String username, String nom, String prenom, String role) {
        this.id       = id;
        this.username = username;
        this.nom      = nom;
        this.prenom   = prenom;
        this.role     = role;
    }

    public int    getId()      { return id; }
    public String getUsername(){ return username; }
    public String getNom()     { return nom; }
    public String getPrenom()  { return prenom; }
    public String getRole()    { return role; }

    public void setId(int id)            { this.id = id; }
    public void setUsername(String u)    { this.username = u; }
    public void setNom(String nom)       { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setRole(String role)     { this.role = role; }

    public String getInitiales() {
        String p = (prenom != null && !prenom.isEmpty()) ? String.valueOf(prenom.charAt(0)) : "";
        String n = (nom    != null && !nom.isEmpty())    ? String.valueOf(nom.charAt(0))    : "";
        return (p + n).toUpperCase();
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

}
