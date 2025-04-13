package Main;
import java.sql.Date;
import models.Produit;
import services.ProduitServices;

public class Main {

    public static void main(String[] args) {
        DatabaseConnection.getInstance();
        ProduitServices ps= new ProduitServices();
        Date date = new Date(System.currentTimeMillis());
        Produit newProduct = new Produit("As", "Pain reliever", 5.99f, 100, date, "aspirin.jpg");
        ps.addProduit(newProduct);

        // Show all products
        System.out.println("All products:");
        ps.showProduit().forEach(System.out::println);

        // Get one product (assuming ID 1 exists)
        Produit product = ps.getoneProduit(4);
        if (product != null) {
            System.out.println("\nSingle product:");
            System.out.println(product);

            // Edit product
            product.setPrix(6.50f);
            product.setStock_quantite(90);
            ps.editProduit(product);

            // Show updated product
            System.out.println("\nUpdated product:");
            System.out.println(ps.getoneProduit(1));

            // Remove product
            ps.removeProduit(product);
        }

        // Verify deletion
        System.out.println("\nProducts after deletion:");
        ps.showProduit().forEach(System.out::println);
    }






    }

