package example.Model;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.Date;

public class RendezVous {
    private int id;
    private int patientId;
    private int maladieId;
    private Date dateRdv;
    private Time heureRdv;
    private Statut statut;
    private String consultation;

    private String nomPatient;
    private String maladieNom;

    public RendezVous() {
    }

    public RendezVous(int id, int patientId, int maladieId, Date dateRdv, Time heureRdv, Statut statut, String consultation) {
        this.id = id;
        this.patientId = patientId;
        this.maladieId = maladieId;
        this.dateRdv = dateRdv;
        this.heureRdv = heureRdv;
        this.statut = statut;
        this.consultation = consultation;
    }

    public RendezVous(int patientId, int maladieId, Date dateRdv, Time heureRdv, Statut statut, String consultation) {
        this.patientId = patientId;
        this.maladieId = maladieId;
        this.dateRdv = dateRdv;
        this.heureRdv = heureRdv;
        this.statut = statut;
        this.consultation = consultation;
    }

    public RendezVous(int id, int patientId, int maladieId, Date dateRdv, Time heureRdv, Statut statut, String nomPatient, String maladieNom) {
        this.id = id;
        this.patientId = patientId;
        this.maladieId = maladieId;
        this.dateRdv = dateRdv;
        this.heureRdv = heureRdv;
        this.statut = statut;
        this.nomPatient = nomPatient;
        this.maladieNom = maladieNom;
    }

    public int getId() {
        return id;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getMaladieId() {
        return maladieId;
    }

    public Date getDateRdv() {
        return dateRdv;
    }

    public Time getHeureRdv() {
        return heureRdv;
    }

    public Statut getStatut() {
        return statut;
    }

    public String getConsultation() {
        return consultation;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public void setMaladieId(int maladieId) {
        this.maladieId = maladieId;
    }

    public void setDateRdv(Date dateRdv) {
        this.dateRdv = dateRdv;
    }

    public void setHeureRdv(Time heureRdv) {
        this.heureRdv = heureRdv;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public void setConsultation(String consultation) {
        this.consultation = consultation;
    }

    public String getNomPatient() {
        return nomPatient;
    }

    public String getMaladieNom() {
        return maladieNom;
    }

    public void setNomPatient(String nomPatient) {
        this.nomPatient = nomPatient;
    }


    public void setMaladieNom(String maladieNom) {
        this.maladieNom = maladieNom;
    }
}