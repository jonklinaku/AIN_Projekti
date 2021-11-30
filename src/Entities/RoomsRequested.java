package Entities;

import Enums.RoomType;

public class RoomsRequested {
    public int Number;
    public RoomType Roomtype;

    public int getNumber() {
        return Number;
    }

    public void setNumber(int number) {
        Number = number;
    }

    public RoomType getRoomtype() {
        return Roomtype;
    }

    public void setRoomtype(RoomType roomtype) {
        Roomtype = roomtype;
    }
}
