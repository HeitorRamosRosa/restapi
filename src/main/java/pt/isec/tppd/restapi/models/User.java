package pt.isec.tppd.restapi.models;

public class User
{
    private String username;
    private String password;
    private String token;
    private boolean userExists;
    private boolean rightPassword;
    private boolean userWasOnline;
    private boolean loggedIn;
    private String message;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isUserExists() {
        return userExists;
    }

    public void setUserExists(boolean userExists) {
        this.userExists = userExists;
    }

    public boolean isPasswordRight() {
        return rightPassword;
    }

    public void setRightPassword(boolean rightPassword) {
        this.rightPassword = rightPassword;
    }

    public boolean isRightPassword() {
        return rightPassword;
    }

    public boolean isUserWasOnline() {
        return userWasOnline;
    }

    public void setUserWasOnline(boolean userWasOnline) {
        this.userWasOnline = userWasOnline;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
