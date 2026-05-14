package example.DAO;

import java.sql.*;
import example.Model.Utilisateur;
import example.Util.DBConnection;
import example.Util.SecurityUtils;

public class LoginDAO {

    public Utilisateur authentifier(String username, String password) throws SQLException {
        // 1. On hache le mot de passe reçu pour le comparer au hash de la base
        String passwordHache = SecurityUtils.hashSHA256(password);

        // 2. Requête SQL avec les noms exacts de tes colonnes pgAdmin
        String sql = "SELECT * FROM utilisateurs WHERE username = ? AND mot_de_passe = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            stmt.setString(2, passwordHache);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            throw e;
        }
        return null;
    }
}