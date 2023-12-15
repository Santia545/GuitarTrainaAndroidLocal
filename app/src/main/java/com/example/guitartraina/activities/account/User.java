package com.example.guitartraina.activities.account;

public class User {
    private String userName;
    private String email;
    private String encryptedPassword;
    private String Rol;
    public String plainTextPassword;

    public User(String email,String userName, String encryptedPassword, String rol) {
        this.email = email;
        this.userName=userName;
        this.encryptedPassword = encryptedPassword;
        Rol = rol;
    }
    public User(String email) {
        this.email = email;
    }

    public User(String email, String encryptedPassword, String rol) {
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        Rol = rol;
    }

    public User(String email, String encryptedPassword) {
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.Rol="";
    }

    public User() {
        this.email = "";
        this.encryptedPassword= "";
        this.Rol="";
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }
    public String getRol() {
        return Rol;
    }

    public void setRol(String rol) {
        Rol = rol;
    }

    public String getString() {
        return email;
    }

    public void setString(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName){
        this.userName=userName;
    }
}
