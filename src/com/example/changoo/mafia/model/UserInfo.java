package com.example.changoo.mafia.model;

import java.io.Serializable;


public class UserInfo implements Serializable {
    static final long serialVersionUID =6880704790547550457L;
    private String name;
    private Character character;
    private String state="";
    private String when="";
    private boolean wantnext=false;
    
    public UserInfo(String name) {
        this.name = name;
    }

    public boolean isWantnext() {
        return wantnext;
    }

    public void setWantnext(boolean wantnext) {
        this.wantnext = wantnext;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
