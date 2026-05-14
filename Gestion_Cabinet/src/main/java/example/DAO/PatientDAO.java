package example.DAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import example.Model.Patient;
import example.Util.DBConnection;

public class PatientDAO {

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^0[67]\\d{8}$|^\\d{2}(\\s\\d{2}){4}$");

    public boolean ajouterPatient(Patient p) throws SQLException {
        String sql = "INSERT INTO patients (nom, prenom, age, telephone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNom().trim());
            stmt.setString(2, p.getPrenom().trim());
            stmt.setInt(3, p.getAge());
            stmt.setString(4, p.getTelephone().trim());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean modifierPatient(Patient p) throws SQLException {
        String sql = "UPDATE patients SET nom=?, prenom=?, age=?, telephone=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNom().trim());
            stmt.setString(2, p.getPrenom().trim());
            stmt.setInt(3, p.getAge());
            stmt.setString(4, p.getTelephone().trim());
            stmt.setInt(5, p.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean supprimerPatient(int id) throws SQLException {
        String sql = "DELETE FROM patients WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Patient> getTousLesPatients() throws SQLException {
        List<Patient> liste = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY nom ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(extrairePatient(rs));
            }
        }
        return liste;
    }

    public List<Patient> rechercherPatients(String motCle) throws SQLException {
        List<Patient> liste = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ?";
        String pattern = "%" + motCle.toLowerCase().trim() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(extrairePatient(rs));
                }
            }
        }
        return liste;
    }

    public int compterPatients() throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public static boolean validerNom(String nom) {
        return nom != null && !nom.trim().isEmpty()
               && nom.trim().matches("[a-zA-ZÀ-ÿ\\s\\-']+");
    }

    public static boolean validerAge(String ageStr) {
        try {
            int age = Integer.parseInt(ageStr.trim());
            return age >= 0 && age <= 150;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean validerTelephone(String tel) {
        if (tel == null || tel.trim().isEmpty()) return false;
        String nettoye = tel.replaceAll("\\s", "");
        return PHONE_PATTERN.matcher(tel.trim()).matches()
               || nettoye.matches("\\d{10}");
    }

    private Patient extrairePatient(ResultSet rs) throws SQLException {
        return new Patient(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getInt("age"),
            rs.getString("telephone")
        );
    }
}
