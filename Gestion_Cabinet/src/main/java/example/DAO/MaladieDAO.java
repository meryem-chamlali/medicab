package example.DAO;

import example.Model.Maladie;
import example.Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MaladieDAO {

    public boolean ajouterMaladie(Maladie maladie) throws SQLException {
        String sql = "INSERT INTO maladies (code, nom, categorie, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maladie.getCode().trim());
            stmt.setString(2, maladie.getNom().trim());
            stmt.setString(3, maladie.getCategorie().trim());
            stmt.setString(4, maladie.getDescription() == null ? "" : maladie.getDescription().trim());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean modifierMaladie(Maladie maladie) throws SQLException {
        String sql = "UPDATE maladies SET code=?, nom=?, categorie=?, description=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maladie.getCode().trim());
            stmt.setString(2, maladie.getNom().trim());
            stmt.setString(3, maladie.getCategorie().trim());
            stmt.setString(4, maladie.getDescription() == null ? "" : maladie.getDescription().trim());
            stmt.setInt(5, maladie.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean supprimerMaladie(int id) throws SQLException {
        String sql = "DELETE FROM maladies WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Maladie> getToutesLesMaladies() throws SQLException {
        List<Maladie> liste = new ArrayList<>();
        String sql = "SELECT id, code, nom, categorie, description FROM maladies ORDER BY nom ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(extraireMaladie(rs));
            }
        }
        return liste;
    }

    public List<Maladie> rechercherMaladies(String motCle) throws SQLException {
        List<Maladie> liste = new ArrayList<>();
        String sql = "SELECT id, code, nom, categorie, description FROM maladies " +
                "WHERE LOWER(code) LIKE ? OR LOWER(nom) LIKE ? OR LOWER(categorie) LIKE ? ORDER BY nom ASC";
        String pattern = "%" + motCle.toLowerCase().trim() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(extraireMaladie(rs));
                }
            }
        }
        return liste;
    }

    public int compterMaladies() throws SQLException {
        String sql = "SELECT COUNT(*) FROM maladies";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public static boolean validerCode(String code) {
        return code != null && !code.trim().isEmpty() && code.trim().matches("[A-Za-z0-9\\-\\.]{2,20}");
    }

    public static boolean validerNom(String nom) {
        return nom != null && !nom.trim().isEmpty() && nom.trim().length() >= 2;
    }

    public static boolean validerCategorie(String categorie) {
        return categorie != null && !categorie.trim().isEmpty() && categorie.trim().length() >= 2;
    }

    private Maladie extraireMaladie(ResultSet rs) throws SQLException {
        return new Maladie(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("nom"),
                rs.getString("categorie"),
                rs.getString("description")
        );
    }
}
