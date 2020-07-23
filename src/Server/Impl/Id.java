package Server.Impl;

import Server.Inter.IId;

public class Id implements IId {
    private String id;
    public Id(String id){
        this.id = id;
    }

    String getId(){
        return id;
    }


}
