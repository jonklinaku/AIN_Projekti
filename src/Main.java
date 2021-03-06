import Entities.*;
import Entities.Event;
import Enums.RoomType;
import com.google.gson.Gson;
import com.sun.deploy.util.ArrayUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {

    static List<Course> Courses = Collections.synchronizedList(new ArrayList<>()) ;
    static List<Curriculum> Curricula =  Collections.synchronizedList(new ArrayList<>());
    static List<Room> Rooms =  Collections.synchronizedList(new ArrayList<>());
    static int PrimaryPrimaryDistance = 0;
    static int PrimarySecondaryDistance = 0;
    static int SecondarySecondaryDistance = 0;
    static int periodNum;

    public static void main(String[] args) {
        readInstances();
        List<Course[][]> solutions = new ArrayList<>();

        //while (solutions.size() != 1) {
        //    Course[][] solution = initSolution();
        //    if ( solution != null) {
        //        addUniqueSolution(solution, solutions);
        //    }
        //}
        //int gFit = getSolutionFitness(solutions.get(0));
        Course[][] g = initSolution();
        int gFit = getSolutionFitness(g);
       // Course[][] g = solutions.get(0);


        int resetEq = 0;
        int resetNe = 0;
        while (true){
            Course[][] c;
            double chance = Math.random();
            if (chance>=0.6)
             c = mutateSolutionReplaceBlank(g);
            else if (chance<=0.3)
             c = mutateSolutionSwap(g);
            else
             c = mutateSolutionChangeRoomSamePeriod(g);

            if (c==null)
                continue;
            int cFit = getSolutionFitness(c);
            if (cFit <= gFit)
            {
                if (cFit==gFit)
                {
                    if (++resetEq == 20)
                    {
                        g = initSolution();
                        gFit = getSolutionFitness(g);
                        resetEq= 0;
                    }
                }
                System.out.println("new global fit: "+cFit);
                gFit = cFit;
                g=c;
            } else {
                if (++resetNe == 100)
                {
                    g = initSolution();
                    gFit = getSolutionFitness(g);
                    resetNe= 0;
                }
            }
            //writeSolutionToFile(solutions.get(0));
        }


    }
    private static Course[][] initSolution(){
        Course[][] solution = new Course[Rooms.size()][periodNum];
        Collections.shuffle(Courses);
        for (int i = 0; i < Courses.size(); i++) {
            Course course = Courses.get(i);
            List<Room> candidateRooms = Rooms.parallelStream().filter(room -> room.Roomtype == course.getRoomsRequested().getRoomtype()).collect(Collectors.toList());
            try {
                insertSolutionEvent(candidateRooms, course, solution);
            } catch (Exception e) {
                solution = null;
                break;
            }
        }
        return solution;
    }
    private static void writeSolutionToFile(Course[][] solution){
        FileSolution fileSolution = new FileSolution();
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[0].length; j++) {
                if (solution[i][j] != null){
                    Course c = solution[i][j];
                    Optional<Assignement> a = fileSolution.getAssignments().stream().filter(assignement -> assignement.getCourse().equals(c.getId()+"")).findFirst();
                    int index = i;
                    if (a.isPresent()){

                        Event e = new Event();
                        e.setPeriod(j);
                        e.setRoom(Rooms.parallelStream().filter(room -> room.getSolutionId()==index).findFirst().get().getId()+"");
                        a.get().getEvents().add(e);
                    }
                    else{
                        Assignement assignement = new Assignement();
                        assignement.setCourse(c.getId()+"");
                        Event e = new Event();
                        e.setPeriod(j);
                        e.setRoom(Rooms.parallelStream().filter(room -> room.getSolutionId()==index).findFirst().get().getId()+"");
                        assignement.getEvents().add(e);
                        fileSolution.getAssignments().add(assignement);
                    }
                }
            }
        }
        try{
            FileWriter fileWriter = new FileWriter("C:\\Users\\Jon\\Downloads\\Test instances\\testOutput.json");
            Gson gson = new Gson();

            fileWriter.write(gson.toJson(fileSolution));
            fileWriter.close();
        }catch (Exception e ){

        }

    }
    private static Course[][] mutateSolutionChangeRoomSamePeriod(Course[][] OriginalSolution){
        Course[][] solution = OriginalSolution.clone();

        Course randomCourse = Courses.parallelStream().filter(course -> course.getRoomsRequested().getNumber()==1).findAny().get();
        int randomCoursePeriod = -1;
        int randomCourseSolutionRowIndex = -1;
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[0].length; j++) {
                if (solution[i][j] == randomCourse){
                    randomCoursePeriod = j;
                    randomCourseSolutionRowIndex = i;
                }
            }
        }
        List<Room> roomsList = Rooms.parallelStream().filter(room -> room.getRoomtype() == randomCourse.getRoomsRequested().getRoomtype()).collect(Collectors.toList());
        boolean mutated = false;
        for (int i = 0; i < roomsList.size(); i++) {
            if (solution[roomsList.get(i).getSolutionId()][randomCoursePeriod] == null){
                mutated = true;
                solution[roomsList.get(i).getSolutionId()][randomCoursePeriod] = randomCourse;
                solution[randomCourseSolutionRowIndex][randomCoursePeriod] = null;
            }

        }
        if (mutated)
            return solution;
        else
            return null;
    }
    private static Course[][] mutateSolutionSwap(Course[][] OriginalSolution){
        Course[][] solution = OriginalSolution.clone();
        Course randomCourse = Courses.parallelStream().filter(course -> course.getRoomsRequested().getNumber()==1).findAny().get();
        Course similarToRandomCourse = Courses.parallelStream().filter(course -> course.getRoomsRequested().getNumber() == 1 && course.getRoomsRequested().getRoomtype() == randomCourse.getRoomsRequested().getRoomtype() && course.getId() != randomCourse.getId()).findAny().get();
        int randomCoursePeriod = -1;
        int randomCourseSolutionRowIndex = -1;
        int similarToRandomCoursePeriod = -1;
        int similarToRandomCourseSolutionRowIndex = -1;
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[0].length; j++) {
                if (solution[i][j] == randomCourse){
                    randomCoursePeriod = j;
                    randomCourseSolutionRowIndex = i;
                }
                else if (solution[i][j] == similarToRandomCourse){
                    similarToRandomCoursePeriod = j;
                    similarToRandomCourseSolutionRowIndex = i;
                }
            }
        }


        if (randomCoursePeriod <0 || randomCourseSolutionRowIndex<0 || similarToRandomCoursePeriod<0 || similarToRandomCourseSolutionRowIndex<0)
            return null;
        boolean conflict = false;
        for (int i = 0; i < solution.length; i++) {

            if (solution[i][randomCoursePeriod] != null &&solution[i][randomCoursePeriod].getPrimaryCurriculaList() != null && similarToRandomCourse.getPrimaryCurriculaList() != null) {
                if (!(i==randomCourseSolutionRowIndex)) {
                    if ((solution[i][randomCoursePeriod].getPrimaryCurriculaList().parallelStream().anyMatch(curriculum ->
                            similarToRandomCourse.getPrimaryCurriculaList().contains(curriculum)) || solution[i][randomCoursePeriod].getTeacher() == similarToRandomCourse.getTeacher())) {
                        conflict = true;
                        break;
                    }
                }
            }
            if (solution[i][similarToRandomCoursePeriod] != null && solution[i][similarToRandomCoursePeriod].getPrimaryCurriculaList() != null && randomCourse.getPrimaryCurriculaList() != null) {
                if (!(i==similarToRandomCourseSolutionRowIndex)) {
                    if ((solution[i][similarToRandomCoursePeriod].getPrimaryCurriculaList().parallelStream().anyMatch(curriculum ->
                            randomCourse.getPrimaryCurriculaList().contains(curriculum)) || solution[i][similarToRandomCoursePeriod].getTeacher() == randomCourse.getTeacher())) {
                        conflict = true;
                        break;
                    }
                }
            }


        }
        if (!conflict) {
            Course temp = solution[randomCourseSolutionRowIndex][randomCoursePeriod];
            solution[randomCourseSolutionRowIndex][randomCoursePeriod] = solution[similarToRandomCourseSolutionRowIndex][similarToRandomCoursePeriod];
            solution[similarToRandomCourseSolutionRowIndex][similarToRandomCoursePeriod] = temp;
            return solution;
        }
        else
            return null;
    }
    private static Course[][] mutateSolutionReplaceBlank(Course[][] OriginalSolution){
        Course[][] solution = OriginalSolution.clone();
        Course randomCourse = Courses.parallelStream().filter(course -> course.getRoomsRequested().getNumber()==1).findAny().get();
        int randomCoursePeriod = -1;
        int randomCourseSolutionRowIndex = -1;
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[0].length; j++) {
                if (solution[i][j] == randomCourse){
                    randomCoursePeriod = j;
                    randomCourseSolutionRowIndex = i;
                }
            }
        }
        if (randomCoursePeriod <0 || randomCourseSolutionRowIndex<0)
            return null;
        int startIndex = (int)Math.random()*solution[0].length;
        boolean exitLoop = false;
        for (int i = startIndex; i < solution[0].length; i++) {
            if (i == randomCoursePeriod)
                continue;
            if (exitLoop)
                break;
            int[] insertPos = new int[randomCourse.getRoomsRequested().getNumber()];
            int counter = 0;
            for (int j = 0; j < solution.length; j++) {
                if (solution[j][i] == null) {
                    if (counter != randomCourse.getRoomsRequested().getNumber() && Rooms.get(j).getRoomtype() == randomCourse.getRoomsRequested().getRoomtype()) {
                        insertPos[counter] = j;
                        counter++;
                    }
                    if (counter == randomCourse.getRoomsRequested().getNumber()) {
                        exitLoop = true;
                    }
                } else {
                    if (solution[j][i].getPrimaryCurriculaList() != null && randomCourse.getPrimaryCurriculaList() != null) {
                        if ((solution[j][i].getPrimaryCurriculaList().parallelStream().anyMatch(curriculum ->
                                randomCourse.getPrimaryCurriculaList().contains(curriculum)) || solution[j][i].getTeacher() == randomCourse.getTeacher())) {
                            exitLoop = false;
                            break;
                        }
                    }
                }
            }
            if (!exitLoop)
                continue;
            for (int j = 0; j < counter; j++) {
                solution[insertPos[j]][i] = randomCourse;
                solution[randomCourseSolutionRowIndex][randomCoursePeriod] = null;
            }
        }
        if (exitLoop)
            return solution;
        else
            return null;
    }
    private static int getSolutionFitness(Course[][] solution){
        int[] coursePeriods = new int[Courses.size()];
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[0].length; j++) {
                if (solution[i][j] != null){
                    coursePeriods[Courses.indexOf(solution[i][j])] = j;
                }
            }
        }

        int conflicts = 0;
        for (int i = 0; i < Courses.size(); i++) {
            for (int j = i+1; j < Courses.size(); j++) {
                if (!Collections.disjoint(Courses.get(i).getPrimaryCurriculaList(),Courses.get(j).getSecondaryCurriculaList())){
                    if (Math.abs(coursePeriods[i]-coursePeriods[j])<=PrimarySecondaryDistance){
                        conflicts++;
                    }
                }
                else if (!Collections.disjoint(Courses.get(i).getSecondaryCurriculaList(),Courses.get(j).getSecondaryCurriculaList())){
                    if (coursePeriods[i]-coursePeriods[j]<=SecondarySecondaryDistance){
                        conflicts++;
                    }
                }
                else if (!Collections.disjoint(Courses.get(i).getPrimaryCurriculaList(),Courses.get(j).getPrimaryCurriculaList())){
                    if (Math.abs(coursePeriods[i]-coursePeriods[j])<=PrimaryPrimaryDistance){
                        conflicts++;
                    }
                }
            }
        }

        return conflicts;
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
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject input = (JSONObject) jsonParser.parse(new FileReader("C:\\Users\\Jon\\Downloads\\Test instances\\test.json"));
            JSONArray jsonCourses =(JSONArray) input.get("Courses");
            jsonCourses.parallelStream().forEach(course ->{
                Course c = new Course();
                c.setId(Integer.parseInt(((JSONObject)course).get("Course").toString()));
                c.setTeacher(Integer.parseInt(((JSONObject)course).get("Teacher").toString()));
                RoomsRequested r = new RoomsRequested();
                String roomTypeString =  ((JSONObject)(((JSONObject) course).get("RoomsRequested"))).get("Type").toString();
                RoomType roomType = getRoomType(roomTypeString);
                r.setRoomtype(roomType);
                r.setNumber(Integer.parseInt(((JSONObject)(((JSONObject) course).get("RoomsRequested"))).get("Number").toString()));
                c.setRoomsRequested(r);
                Courses.add(c);
            });
            JSONArray jsonCurricula = (JSONArray) input.get("Curricula");
            jsonCurricula.forEach(curriculum ->{
                Curriculum c = new Curriculum();
                c.setId(Integer.parseInt(((JSONObject)curriculum).get("Curriculum").toString()));
                JSONArray primaryCourses = (JSONArray) ((JSONObject)curriculum).get("PrimaryCourses");
                JSONArray secondaryCourses = (JSONArray) ((JSONObject)curriculum).get("SecondaryCourses");
                primaryCourses.parallelStream().forEach(courseId->{
                    Optional<Course> foundCourse = Courses.parallelStream().filter(course -> course.getId() == Integer.parseInt(courseId.toString())).findFirst();
                    if (foundCourse.isPresent()) {
                        c.getPrimaryCourses().add(foundCourse.get());
                        (foundCourse.get()).getPrimaryCurriculaList().add(c);
                    }
                });
                secondaryCourses.parallelStream().forEach(courseId->{
                    Optional<Course>  foundCourse = Courses.parallelStream().filter(course -> course.getId() == Integer.parseInt(courseId.toString())).findFirst();
                    if (foundCourse.isPresent()) {
                        c.getSecondaryCourses().add(foundCourse.get());
                        foundCourse.get().getSecondaryCurriculaList().add(c);
                    }
                });
                    Curricula.add(c);
            });

            JSONArray jsonRooms = (JSONArray) input.get("Rooms");
            int roomSolutionId = 0;
            Iterator jsonRoomIterator = jsonRooms.iterator();
            while(jsonRoomIterator.hasNext()){
                JSONObject room = (JSONObject)jsonRoomIterator.next();
                Room r = new Room();
                r.setRoomtype(getRoomType((room).get("Type").toString()));
                r.setId(Integer.parseInt((room).get("Room").toString()));

                r.setSolutionId(roomSolutionId++);

                Rooms.add(r);
            }

            periodNum = Integer.parseInt(input.get("Periods").toString());
            PrimaryPrimaryDistance = Integer.parseInt(input.get("PrimaryPrimaryDistance").toString());
            PrimarySecondaryDistance = Integer.parseInt(input.get("PrimarySecondaryDistance").toString());
        }
        catch (Exception e ){
            System.out.println(e);
        }

        /*Curriculum curriculum = new Curriculum();
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
        }*/
    }
    static  public RoomType getRoomType(String roomType){
        switch (roomType) {
            case "Small":
                return RoomType.SMALL;
            case "Medium":
                return RoomType.MEDIUM;

            case "Large":
                return RoomType.LARGE;
            default:
                return null;
        }
    }
}
