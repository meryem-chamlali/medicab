package example.DAO;

import example.Util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    // Classe interne — ajout du champ maladie
    public static class RdvJour {
        public final String heure;
        public final String nomPatient;
        public final String maladie;   // ← nouveau
        public final String motif;

        public RdvJour(String heure, String nomPatient, String maladie, String motif) {
            this.heure      = heure;
            this.nomPatient = nomPatient;
            this.maladie    = maladie == null ? "" : maladie;
            this.motif      = motif;
        }

        public String getHeure()      { return heure; }
        public String getNomPatient() { return nomPatient; }
        public String getMaladie()    { return maladie; }
        public String getMotif()      { return motif; }
    }

    public int compterPatients() {
        try { return executeCount("SELECT COUNT(*) FROM patients"); }
        catch (SQLException e) { System.err.println("Erreur patients : " + e.getMessage()); return 0; }
    }

    public int compterPatientsCeMois() {
        try {
            return executeCount(
                "SELECT COUNT(*) FROM patients " +
                "WHERE MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())");
        }
        catch (SQLException e) { System.err.println("Erreur patients ce mois : " + e.getMessage()); return 0; }
    }

    public int compterMaladies() {
        try { return executeCount("SELECT COUNT(*) FROM maladies"); }
        catch (SQLException e) { System.err.println("Erreur maladies : " + e.getMessage()); return 0; }
    }

    public int compterRendezVousAujourdhui() {
        try { return executeCount("SELECT COUNT(*) FROM rendez_vous WHERE date_rdv = CURDATE()"); }
        catch (SQLException e) { System.err.println("Erreur rdv : " + e.getMessage()); return 0; }
    }

    public int compterRendezVousAujourdhuiAVenir() {
        try {
            return executeCount(
                "SELECT COUNT(*) FROM rendez_vous " +
                "WHERE date_rdv = CURDATE() AND heure_rdv > CURTIME() " +
                "AND statut IN ('Planifie', 'Confirme')");
        }
        catch (SQLException e) { System.err.println("Erreur rdv à venir : " + e.getMessage()); return 0; }
    }

    // Requête mise à jour avec LEFT JOIN maladies pour récupérer le nom de la maladie
    public List<RdvJour> getRdvAujourdhui() {
        List<RdvJour> rdvs = new ArrayList<>();
        String sql =
            "SELECT rv.heure_rdv, " +
            "       CONCAT(p.prenom, ' ', p.nom) AS nomPatient, " +
            "       COALESCE(m.nom, '')           AS maladie, " +
            "       rv.motif " +
            "FROM rendez_vous rv " +
            "JOIN patients p   ON rv.patient_id = p.id " +
            "LEFT JOIN maladies m ON rv.maladie_id = m.id " +
            "WHERE rv.date_rdv = CURDATE() " +
            "ORDER BY rv.heure_rdv ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rdvs.add(new RdvJour(
                    rs.getString("heure_rdv"),
                    rs.getString("nomPatient"),
                    rs.getString("maladie"),
                    rs.getString("motif")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur liste rdv jour : " + e.getMessage());
        }
        return rdvs;
    }

    private int executeCount(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}