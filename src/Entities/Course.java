package Entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Course {
    int Id;
    RoomsRequested RoomsRequested;
    int Teacher;
    List<Curriculum> PrimaryCurriculaList = Collections.synchronizedList(new ArrayList<>());
    List<Curriculum> SecondaryCurriculaList =  Collections.synchronizedList(new ArrayList<>());

    public List<Curriculum> getSecondaryCurriculaList() {
        return SecondaryCurriculaList;
    }

    public void setSecondaryCurriculaList(List<Curriculum> secondaryCurriculaList) {
        SecondaryCurriculaList = secondaryCurriculaList;
    }

    public List<Curriculum> getPrimaryCurriculaList() {
        return PrimaryCurriculaList;
    }

    public void setPrimaryCurriculaList(List<Curriculum> primaryCurriculaList) {
        PrimaryCurriculaList = primaryCurriculaList;
    }

    public void setId(int id) {
        Id = id;
    }

    public void setRoomsRequested(Entities.RoomsRequested roomsRequested) {
        RoomsRequested = roomsRequested;
    }

    public void setTeacher(int teacher) {
        Teacher = teacher;
    }

    public int getId() {
        return Id;
    }

    public Entities.RoomsRequested getRoomsRequested() {
        return RoomsRequested;
    }

    public int getTeacher() {
        return Teacher;
    }

    @Override
    public String toString() {
        return "Course "+getId();
    }
}
