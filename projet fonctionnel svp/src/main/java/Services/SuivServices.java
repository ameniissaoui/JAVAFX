package Services;

import Main.DatabaseConnection;
import Model.historique_traitement;
import Model.suivie_medical;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuivServices implements Iservices<suivie_medical> {
    Connection cnx;

    public SuivServices() {
        // S'assurer que l'instance est créée avant d'accéder à cnx
        cnx = DatabaseConnection.getInstance().getCnx();
    }

    @Override
    public void add(suivie_medical suivieMedical) {
        String req = "INSERT INTO santee.suivie_medical (date, commentaire, id_historique_id) VALUES (?,?,?)";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setString(1, suivieMedical.getDate());
            stm.setString(2, suivieMedical.getCommentaire());
            stm.setInt(3, suivieMedical.getHistoriqueId());
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(suivie_medical suivieMedical) {
        String req = "UPDATE santee.suivie_medical SET date=?, commentaire=?, id_historique_id=? WHERE id=?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setString(1, suivieMedical.getDate());
            stm.setString(2, suivieMedical.getCommentaire());
            stm.setInt(3, suivieMedical.getHistoriqueId());
            stm.setInt(4, suivieMedical.getId());
            stm.executeUpdate();
            System.out.println("Suivi médical mis à jour avec succès.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(suivie_medical suivieMedical) {
        String req = "DELETE FROM santee.suivie_medical WHERE id=?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, suivieMedical.getId());
            stm.executeUpdate();
            System.out.println("Suivi médical supprimé avec succès.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public suivie_medical find(int id) {
        String req = "SELECT * FROM santee.suivie_medical WHERE id = ?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                suivie_medical sm = new suivie_medical();
                sm.setId(rs.getInt("id"));
                sm.setDate(rs.getString("date"));
                sm.setCommentaire(rs.getString("commentaire"));
                sm.setHistoriqueId(rs.getInt("id_historique_id"));
                return sm;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<suivie_medical> afficher() {
        List<suivie_medical> suivieMedicaux = new ArrayList<>();
        String req = "SELECT * FROM santee.suivie_medical";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            while (rs.next()) {
                suivie_medical suivieMedical = new suivie_medical();
                suivieMedical.setId(rs.getInt("id"));
                suivieMedical.setDate(rs.getString("date"));
                suivieMedical.setCommentaire(rs.getString("commentaire"));
                suivieMedical.setHistoriqueId(rs.getInt("id_historique_id"));
                suivieMedicaux.add(suivieMedical);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return suivieMedicaux;
    }

    @Override
    public suivie_medical getone() {
        String req = "SELECT * FROM santee.suivie_medical ORDER BY id DESC LIMIT 1";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);
            if (rs.next()) {
                suivie_medical sm = new suivie_medical();
                sm.setId(rs.getInt("id"));
                sm.setDate(rs.getString("date"));
                sm.setCommentaire(rs.getString("commentaire"));
                sm.setHistoriqueId(rs.getInt("id_historique_id"));
                return sm;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // Méthode pour trouver les suivis médicaux par ID d'historique
    public List<suivie_medical> findByHistoriqueId(int historiqueId) {
        List<suivie_medical> suivieMedicaux = new ArrayList<>();
        String req = "SELECT * FROM santee.suivie_medical WHERE id_historique_id = ?";
        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, historiqueId);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                suivie_medical suivieMedical = new suivie_medical();
                suivieMedical.setId(rs.getInt("id"));
                suivieMedical.setDate(rs.getString("date"));
                suivieMedical.setCommentaire(rs.getString("commentaire"));
                suivieMedical.setHistoriqueId(rs.getInt("id_historique_id"));
                suivieMedicaux.add(suivieMedical);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return suivieMedicaux;
    }

    public List<suivie_medical> getSuiviByHistoriqueId(int historiqueId) {
        List<suivie_medical> suivis = new ArrayList<>();

        try {
            String req = "SELECT * FROM santee.suivie_medical WHERE id_historique_id = ?";
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, historiqueId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                suivie_medical suivi = new suivie_medical();
                suivi.setId(rs.getInt("id"));
                suivi.setDate(rs.getString("date"));
                suivi.setCommentaire(rs.getString("commentaire"));
                suivi.setHistoriqueId(rs.getInt("id_historique_id"));
                suivis.add(suivi);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return suivis;
    }
}