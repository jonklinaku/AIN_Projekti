package Entities;

import java.util.List;

public class Curriculum {
    public int Id;
    public List<Course> PrimaryCourses;
    public List<Course> SecondaryCourses;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public List<Course> getPrimaryCourses() {
        return PrimaryCourses;
    }

    public void setPrimaryCourses(List<Course> primaryCourses) {
        PrimaryCourses = primaryCourses;
    }

    public List<Course> getSecondaryCourses() {
        return SecondaryCourses;
    }

    public void setSecondaryCourses(List<Course> secondaryCourses) {
        SecondaryCourses = secondaryCourses;
    }
}
