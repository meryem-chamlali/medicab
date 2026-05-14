package example.Model;

public class Maladie {
    private int id;
    private String code;
    private String nom;
    private String categorie;
    private String description;

    public Maladie() {}

    public Maladie(int id, String code, String nom, String categorie, String description) {
        this.id = id;
        this.code = code;
        this.nom = nom;
        this.categorie = categorie;
        this.description = description;
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getNom() { return nom; }
    public String getCategorie() { return categorie; }
    public String getDescription() { return description; }

    public void setId(int id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setNom(String nom) { this.nom = nom; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public void setDescription(String description) { this.description = description; }
}
