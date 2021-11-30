package Entities;

import Enums.RoomType;

public class Room {
public int Id;
public RoomType Roomtype;
int solutionId;

    public int getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(int solutionId) {
        this.solutionId = solutionId;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public RoomType getRoomtype() {
        return Roomtype;
    }

    public void setRoomtype(RoomType roomtype) {
        Roomtype = roomtype;
    }

}
