package example.Controller;

public class PatientSession {
    private static int selectedPatientId = -1;

    public static void setSelectedPatientId(int id) {
        selectedPatientId = id;
    }

    public static int getSelectedPatientId() {
        return selectedPatientId;
    }
}
