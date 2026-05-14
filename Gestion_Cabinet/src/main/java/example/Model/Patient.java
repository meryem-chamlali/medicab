package example.Model;

public class Patient {

    private int    id;
    private String nom;
    private String prenom;
    private int    age;
    private String telephone;

    public Patient() {}

    public Patient(int id, String nom, String prenom, int age, String telephone) {
        this.id        = id;
        this.nom       = nom;
        this.prenom    = prenom;
        this.age       = age;
        this.telephone = telephone;
    }

    public int    getId()        { return id; }
    public String getNom()       { return nom; }
    public String getPrenom()    { return prenom; }
    public int    getAge()       { return age; }
    public String getTelephone() { return telephone; }

    public void setId(int id)                { this.id = id; }
    public void setNom(String nom)           { this.nom = nom; }
    public void setPrenom(String prenom)     { this.prenom = prenom; }
    public void setAge(int age)              { this.age = age; }
    public void setTelephone(String tel)     { this.telephone = tel; }

}
