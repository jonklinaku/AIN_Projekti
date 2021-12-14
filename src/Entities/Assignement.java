package Entities;

import java.util.ArrayList;
import java.util.List;

public class Assignement {
    String Course;
    List<Event> Events = new ArrayList<>();

    public String getCourse() {
        return Course;
    }

    public void setCourse(String course) {
        Course = course;
    }

    public List<Event> getEvents() {
        return Events;
    }

    public void setEvents(List<Event> events) {
        Events = events;
    }
}
