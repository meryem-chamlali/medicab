package example.DAO;

import example.Model.RendezVous;
import example.Model.Statut;
import example.Util.DBConnection;
import example.Util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {

    // ── Validations ─────────────────────────────────────────────────
    public static boolean validerPatient(int patientId) { return patientId > 0; }
    public static boolean validerDate(String date) {
        return date != null && date.matches("\\d{4}-\\d{2}-\\d{2}");
    }
    public static boolean validerHeure(String heure) {
        return heure != null && heure.matches("\\d{2}:\\d{2}");
    }
    public static boolean validerMotif(String motif) {
        return motif != null && motif.trim().length() >= 3;
    }
    public static boolean validerStatut(String statut) {
        return statut != null && (statut.equals("Planifie") || statut.equals("Confirme")
                || statut.equals("Annule") || statut.equals("Termine"));
    }

    public RendezVous recupererRDV(int idRDV) {
        String sql =
                "SELECT r.consultation, r.date_rdv, r.heure_rdv, r.statut, " +
                        "p.nom, p.prenom, m.nom AS maladie_nom " +
                        "FROM rendez_vous r " +
                        "JOIN patients p ON r.patient_id = p.id " +
                        "LEFT JOIN maladies m ON r.maladie_id = m.id " +
                        "WHERE r.id = ?";

        RendezVous rdv = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRDV);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    rdv = construireRDVPourConsultation(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rdv;
    }

    private RendezVous construireRDVPourConsultation(ResultSet rs) throws SQLException {
        RendezVous rdv = new RendezVous();

        rdv.setConsultation(rs.getString("consultation"));

        rdv.setNomPatient(rs.getString("nom") + " " + rs.getString("prenom"));

        rdv.setMaladieNom(rs.getString("maladie_nom"));
        return rdv;
    }

    // ── Helper pour construire un RendezVous depuis un ResultSet ────
    private RendezVous construireDepuisRS(ResultSet rs) throws SQLException {
        return new RendezVous(
            rs.getInt("id"),
            rs.getInt("patient_id"),
            rs.getInt("maladie_id"),
            rs.getDate("date_rdv"),
            rs.getTime("heure_rdv"),
            Statut.valueOf(rs.getString("statut")),
            rs.getString("nom") + " " + rs.getString("prenom"),
            rs.getString("maladie_nom")
        );
    }

    // ── AJOUTER ─────────────────────────────────────────────────────
    public boolean ajouterRendezVous(RendezVous rdv) throws SQLException {
        String sql = "INSERT INTO rendez_vous (patient_id, maladie_id, date_rdv, heure_rdv, statut) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rdv.getPatientId());
            stmt.setInt(2, rdv.getMaladieId());
            stmt.setDate(3, rdv.getDateRdv());
            stmt.setTime(4, rdv.getHeureRdv());
            stmt.setString(5, rdv.getStatut().name());
            stmt.executeUpdate();
            return true;
        }
    }

    // ── MODIFIER ────────────────────────────────────────────────────
    public boolean modifierRendezVous(RendezVous rdv, int idRDV) throws SQLException {
        String sql = "UPDATE rendez_vous SET patient_id=?, maladie_id=?, date_rdv=?, heure_rdv=?, statut=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rdv.getPatientId());
            stmt.setInt(2, rdv.getMaladieId());
            stmt.setDate(3, rdv.getDateRdv());
            stmt.setTime(4, rdv.getHeureRdv());
            stmt.setString(5, rdv.getStatut().name());
            stmt.setInt(6, idRDV);
            stmt.executeUpdate();
            return true;
        }

    }

    // ── SUPPRIMER ───────────────────────────────────────────────────
    public boolean supprimerRendezVous(int id) throws SQLException {
        String sql = "DELETE FROM rendez_vous WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // ── LIRE TOUS ───────────────────────────────────────────────────
    public List<RendezVous> getTousLesRendezVous() throws SQLException {
        String sql =
            "SELECT r.*, p.nom, p.prenom, " +
            "m.nom AS maladie_nom " +
            "FROM rendez_vous r " +
            "JOIN patients p ON r.patient_id = p.id " +
            "LEFT JOIN maladies m ON r.maladie_id = m.id " +
            "ORDER BY r.date_rdv DESC, r.heure_rdv ASC";
        List<RendezVous> rdvs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) rdvs.add(construireDepuisRS(rs));
        }
        return rdvs;
    }

    public List<RendezVous> getTousLesRendezVousParDate(String date) throws SQLException {
        String sql =
                "SELECT r.*, p.nom, p.prenom, " +
                        "m.nom AS maladie_nom " +
                        "FROM rendez_vous r " +
                        "JOIN patients p ON r.patient_id = p.id " +
                        "LEFT JOIN maladies m ON r.maladie_id = m.id " +
                        "WHERE r.date_rdv = ? " +
                        "ORDER BY r.date_rdv DESC, r.heure_rdv ASC";

        List<RendezVous> rdvs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, DateUtil.transformerStringEnDate(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rdvs.add(construireDepuisRS(rs));
                }
            }
        }
        return rdvs;
    }

    // ── RECHERCHER ──────────────────────────────────────────────────
    public List<RendezVous> rechercherRendezVous(String critere) throws SQLException {
        String sql =
            "SELECT r.*, p.nom, p.prenom, " +
            "m.nom AS maladie_nom " +
            "FROM rendez_vous r " +
            "JOIN patients p ON r.patient_id = p.id " +
            "LEFT JOIN maladies m ON r.maladie_id = m.id " +
            "WHERE p.nom LIKE ? OR p.prenom LIKE ? OR r.motif LIKE ? OR m.nom LIKE ? " +
            "ORDER BY r.date_rdv DESC, r.heure_rdv ASC";
        List<RendezVous> rdvs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String s = "%" + critere.trim() + "%";
            stmt.setString(1, s); stmt.setString(2, s);
            stmt.setString(3, s); stmt.setString(4, s);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) rdvs.add(construireDepuisRS(rs));
            }
        }
        return rdvs;
    }

    // ── RDV DU JOUR (pour Dashboard) ───────────────────────────────
    public List<RendezVous> getRdvDuJour() throws SQLException {
        String sql =
            "SELECT r.*, p.nom, p.prenom, " +
            "m.nom AS maladie_nom " +
            "FROM rendez_vous r " +
            "JOIN patients p ON r.patient_id = p.id " +
            "LEFT JOIN maladies m ON r.maladie_id = m.id " +
            "WHERE r.date_rdv = CURDATE() " +
            "ORDER BY r.heure_rdv ASC";
        List<RendezVous> rdvs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) rdvs.add(construireDepuisRS(rs));
        }
        return rdvs;
    }
}