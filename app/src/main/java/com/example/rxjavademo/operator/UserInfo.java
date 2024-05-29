package com.example.rxjavademo.operator;

public class UserInfo {

    private int sex;
    private String name;

    public UserInfo(int sex, String name) {
        this.sex = sex;
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
