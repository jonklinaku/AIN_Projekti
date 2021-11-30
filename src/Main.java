import Entities.*;
import Enums.RoomType;
import com.sun.deploy.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {

    static List<Course> Courses = new ArrayList<>();
    static List<Curriculum> Curricula = new ArrayList<>();
    static List<Room> Rooms = new ArrayList<>();
    static int periodNum;

    public static void main(String[] args) {
        readInstances();
        List<Course[][]> solutions = new ArrayList<>();

        while (solutions.size() != 10) {
            Course[][] solution = new Course[Rooms.size()][periodNum];
            Collections.shuffle(Courses);
            for (int i = 0; i < Courses.size(); i++) {
                Course course = Courses.get(i);
                List<Room> candidateRooms = Rooms.parallelStream().filter(room -> room.Roomtype == course.getRoomsRequested().getRoomtype()).collect(Collectors.toList());
                try {
                    insertSolutionEvent(candidateRooms, course, solution);
                } catch (Exception e) {
                    System.out.println("invalid solution");
                    solution = null;
                    break;
                }
            }
            if (solution != null) {
                addUniqueSolution(solution, solutions);
            }
        }

        /*Courses.forEach(course -> {
            List<Room> candidateRooms = Rooms.parallelStream().filter(room -> room.Roomtype == course.getRoomsRequested().getRoomtype()).collect(Collectors.toList());
            try {insertSolutionEvent(candidateRooms,course,solution);}
            catch (Exception e){
                System.out.println("invalid solution");
            }

        });*/
        solutions.forEach(s -> printSolution(s));

    }

    private static void addUniqueSolution(Course[][] solution, List<Course[][]> solutions) {
        int solHash = solution.hashCode();
        boolean unique = true;
        for (int i = 0; i < solutions.size(); i++) {
            if (solHash == solutions.get(i).hashCode()) {
                unique = false;
                break;
            }
        }
        if (unique)
            solutions.add(solution);
    }

    private static void insertSolutionEvent(List<Room> rooms, Course course, Course[][] solution) throws Exception {
        // int[] firstFreeIndex = new int[solution.length];
        AtomicInteger firstFreeIndex = new AtomicInteger(solution[0].length);

        rooms.parallelStream().forEach(room -> {
            for (int i = 0; i < solution[room.getSolutionId()].length; i++) {
                int valAt = i;
                if (solution[room.getSolutionId()][i] == null) {
                    firstFreeIndex.getAndUpdate(val -> {
                        if (valAt < val)
                            return valAt;
                        else
                            return val;
                    });
                    break;
                }

            }
        });
        boolean exitLoop = false;
        for (int i = firstFreeIndex.get(); i < solution[0].length; i++) {
            if (exitLoop)
                break;
            int[] insertPos = new int[course.getRoomsRequested().getNumber()];
            int counter = 0;
            for (int j = 0; j < solution.length; j++) {
                if (solution[j][i] == null) {
                    if (counter != course.getRoomsRequested().getNumber() && Rooms.get(j).getRoomtype() == course.getRoomsRequested().getRoomtype()) {
                        insertPos[counter] = j;
                        counter++;
                    }
                    if (counter == course.getRoomsRequested().getNumber()) {
                        exitLoop = true;
                    }
                } else {
                    if (solution[j][i].getPrimaryCurriculaList() != null && course.getPrimaryCurriculaList() != null) {
                        if ((solution[j][i].getPrimaryCurriculaList().parallelStream().anyMatch(curriculum ->
                                course.getPrimaryCurriculaList().contains(curriculum)) || solution[j][i].getTeacher() == course.getTeacher())) {
                            exitLoop = false;
                            break;
                        }
                    }
                }
            }
            if (!exitLoop)
                continue;
            for (int j = 0; j < counter; j++) {
                solution[insertPos[j]][i] = course;
            }
        }
        if (!exitLoop)
            throw new Exception("test");


    }

    private static void printSolution(Course[][] solution) {
        for (int i = 0; i < solution.length; i++) {
            System.out.println(Arrays.toString(solution[i]));

        }
        System.out.println();
    }

    private static void readInstances() {
        Curriculum curriculum = new Curriculum();
        Curriculum curriculum2 = new Curriculum();

        List<Curriculum> CList1 = new ArrayList<>();
        List<Curriculum> CList2 = new ArrayList<>();
        CList1.add(curriculum);
        CList2.add(curriculum2);


        Course c = new Course();
        c.setId(2414);
        RoomsRequested r = new RoomsRequested();
        r.setNumber(1);
        r.setRoomtype(RoomType.MEDIUM);
        c.setRoomsRequested(r);
        c.setTeacher(943);
        c.setPrimaryCurriculaList(CList1);

        Courses.add(c);

        c = new Course();
        c.setId(2416);
        r = new RoomsRequested();
        r.setNumber(2);
        r.setRoomtype(RoomType.LARGE);
        c.setRoomsRequested(r);
        c.setTeacher(942);
        c.setPrimaryCurriculaList(CList2);
        Courses.add(c);

        c = new Course();
        c.setId(2417);
        r = new RoomsRequested();
        r.setNumber(1);
        r.setRoomtype(RoomType.MEDIUM);
        c.setRoomsRequested(r);
        c.setTeacher(943);
        c.setPrimaryCurriculaList(CList2);
        Courses.add(c);

        c = new Course();
        c.setId(2418);
        r = new RoomsRequested();
        r.setNumber(1);
        r.setRoomtype(RoomType.LARGE);
        c.setRoomsRequested(r);
        c.setTeacher(962);
        Courses.add(c);

        c = new Course();
        c.setId(2419);
        r = new RoomsRequested();
        r.setNumber(1);
        r.setRoomtype(RoomType.MEDIUM);
        c.setRoomsRequested(r);
        c.setTeacher(960);
        Courses.add(c);

        Room room = new Room();
        room.setSolutionId(0);
        room.setId(100);
        room.setRoomtype(RoomType.MEDIUM);
        Rooms.add(room);

        room = new Room();
        room.setSolutionId(1);
        room.setId(101);
        room.setRoomtype(RoomType.LARGE);
        Rooms.add(room);

        room = new Room();
        room.setSolutionId(2);
        room.setId(103);
        room.setRoomtype(RoomType.LARGE);
        Rooms.add(room);

        periodNum = 4;


        for (int i = 0; i < Rooms.size(); i++) {
            Rooms.get(i).setSolutionId(i);
        }
    }
}
