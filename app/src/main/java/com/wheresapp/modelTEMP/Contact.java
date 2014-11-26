package com.wheresapp.modelTEMP;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.api.client.util.DateTime;

/**
 * Created by Victorma on 25/11/2014.
 */
@Table(name = "Contacts")
public class Contact extends Model{

    @Column(name = "Telephone")
    private String telephone;

    @Column(name = "Nickname")
    private String nickname;

    @Column(name = "Name")
    private String name;

    @Column(name = "State")
    private Integer state;

    @Column(name = "LastSeen")
    private DateTime lastSeen;

    @Column(name = "Favourite")
    private Boolean favourite;

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public DateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(DateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Boolean getFavourite() {
        return favourite;
    }

    public void setFavourite(Boolean favourite) {
        this.favourite = favourite;
    }
}