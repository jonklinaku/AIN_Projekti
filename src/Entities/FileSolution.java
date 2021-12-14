package Entities;

import java.util.ArrayList;
import java.util.List;

public class FileSolution {
    public List<Assignement> getAssignments() {
        return Assignments;
    }

    public void setAssignments(List<Assignement> assignments) {
        Assignments = assignments;
    }

    List<Assignement> Assignments = new ArrayList<>();
}
