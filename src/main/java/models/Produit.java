package models;
import java.sql.Date;


public class  Produit {

    private int id;
    private String nom;
    private String description;
    private float prix;
    private int stock_quantite;
    private Date date;
    private String image;

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public float getPrix() {
        return prix;
    }

    public int getStock_quantite() {
        return stock_quantite;
    }

    public Date getDate() {
        return date;
    }

    public String getImage() {
        return image;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setStock_quantite(int stock_quantite) {
        this.stock_quantite = stock_quantite;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    public Produit(int id, String nom, String description, float prix, int stock_quantite, Date date, String image) {

        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock_quantite = stock_quantite;
        this.date = date;
        this.image = image;
    }

    public Produit(String nom, String description, float prix, int stock_quantite, Date date, String image) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock_quantite = stock_quantite;
        this.date = date;
        this.image = image;
    }

    public Produit() {

    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", stock_quantite=" + stock_quantite +
                ", date=" + date +
                ", image='" + image + '\'' +
                '}';
    }
}
