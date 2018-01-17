package com.tester;


import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.*;
import javax.swing.*;
import processing.core.*;

import static processing.core.PApplet.*;


/**
 * User class containing basic login information.
 *
 * @author dhahaj
 */
public class User {

    private static boolean DEBUG = true;

    private String username = null;
    private String password = null;
    private boolean administrator;
    static Map<String, String> userMap = new HashMap<String, String>();
    public static List<User> userList = new ArrayList<>();
    private static final Logger LOGGER = GUIFrame.getLogger(); //LogControler.getLogger();
    private static Preferences prefs = null;

    public User(String name, String pass) {
        this(name, pass, false);
    }

    public User(String name, String password, boolean admin) {
        super();
        this.username = name;
        this.password = password;
        this.administrator = admin;
    }

    public static void setUsers(Map<String, String> map) {
        userMap = map;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Checks if this user is an administrator.
     *
     * @return True if the are an admin.
     */
    public boolean isAdmin() {
        return this.administrator;
        /*if (username.equalsIgnoreCase("DMH") || username.equalsIgnoreCase("SM") || username.equalsIgnoreCase("SDO") || username.equalsIgnoreCase("TCM"))
            return true;
        return administrator;*/
    }

    @SuppressWarnings("empty-statement")
    public static String[] getUserNames() {
        List<String> names = new ArrayList<String>();
        for (User u : userList)
            names.add(u.username);
        return names.toArray(new String[names.size()]);
        //        if (userMap != null)
        //            return (String[]) userMap.keySet().toArray();
        //        return null;
    }

    /**
     * Static method that can be used to verify a userMap password without
     * needing a
     * <i>User</i> instance object.
     *
     * @param name The name of the user
     * @param password The password to verify.
     * @return True if the user exists in the <i>userlist</i>, false otherwise.
     */
    public static boolean verifyPassword(String name, String password) {
        if (!userMap.containsKey(name))
            return false;
        String pass = userMap.get(name);
        return pass.equalsIgnoreCase(password);
    }

    /**
     * Static method that can be used to verify a userMap password without
     * needing a
     * <i>User</i> instance object.
     *
     * @param user A User object.
     * @param password A Password string to verify
     * @return True if the password argument equals the userMap real password.
     */
    public static boolean verifyPassword(User user, String password) {
        return user.password.equalsIgnoreCase(password);
    }

    /**
     * Simple method to check the password of an instance of this class.
     *
     * @param password A Password string to verify
     * @return True if the password argument equals the userMap real password.
     */
    public boolean verifyPassword(String password) {
        return this.password.equalsIgnoreCase(password);
    }

    public static String readUserFile(String path) {
        try {
            return Helpers.getContents(new File(path));//.readFile(path);
        } catch (Exception e) {
            LOGGER.throwing(User.class.getName(), "readUserFile(String)", e);
        }
        return "";
    }

    /**
     * Reads the userMap.txt file from the resource directory and returns the
     * contents as mapped values.
     *
     * @return
     */
    public static Map<String, String> readUserFile() {
        Map<String, String> map = new HashMap();

        // Read each line in the user file and add it as a key-pair in the map
        File userFile = new File(prefs.get("RESOURCE_DIR", null) + "users.txt");
        String[] users = PApplet.loadStrings(userFile);
        for (String user : users) {
            String[] name_pass = user.split(":");
            map.put(name_pass[0], name_pass[1]);
        }
        return map;
    }

    /**
     * Fetches a user from the list of userMap.
     *
     * @param name The name of the desired user to fetch.
     * @return A User object, or null if they are not in the list.
     */
    public static User getUser(String name) {
        for (User u : userList)
            if (u.username.equalsIgnoreCase(name))
                return u;
        String pass = userMap.get(name);
        return new User(name, pass);
    }

    /**
     * Initial user setup.
     */
    public static void setupUsers() {
        prefs = Preferences.userNodeForPackage(GUIFrame.class);
        String userFilePath = prefs.get("RESOURCE_DIR", null);
        Path userPath = Paths.get(userFilePath, "users.txt");
        if (Files.exists(userPath))
            try {
                Scanner s = new Scanner(userPath);
                DEBUG("Found user file.");
                userList.clear();
                while (s.hasNext()) {
                    String text = s.nextLine();
                    String[] info = text.split(":");
                    DEBUG(text);
                    if (info[0].equals("DMH") || info[0].equals("TCM") || info[0].equals("SDO") || info[0].equals("SM"))
                        userList.add(new User(info[0], info[1], true));
                    else
                        userList.add(new User(info[0], info[1], false));
                    userMap.put(info[0], info[1]);
                }
                DEBUG("userList size = " + userList.size());
                DEBUG("userMap size = " + userMap.size());
            } catch (Exception e) {
            }
        else {
            DEBUG("Did not find user file.");
            String[] usernames = {"DMH", "SM", "TCM", "SDO", "IGR", "AHR", "CKP", "RJM", "BTA", "JMD", "MAG"};
            String[] passwords = {"D853", "1234", "PINZ", "9897", "5858", "1964", "3235", "1991", "1419", "1114", "2524"};
            int i = 0;
            for (String username : usernames) {
                userList.add(new User(username, passwords[i]));
                userMap.put(username, passwords[i]);
                i++;
            }
        }
    }

    public void logoff() {
        String s = "User " + this.username + " logged off";
        LOGGER.info(s);
    }

    public static void addUser(String name, String pass, boolean admin) {
        User u = new User(name, pass, admin);
        userList.add(u);
        userMap.put(name, pass);
        saveUserFile();
    }

    static void removeUser(User user) {
        userList.remove(user);
        userMap.remove(user.username);
        DEBUG("Removed User: " + user.username);
        saveUserFile();
    }

    /**
     * Saves the current <code>List</code> of Users to the JSON file.
     */
    private static void saveUserFile() {
        String resourceDir = prefs.get("RESOURCE_DIR", null);
        String data = "";
        for (User u : userList)
            data += u + "\n";
        DEBUG("Saving new user list.");
        try {
            FilesUtil.writeToTextFile(resourceDir + "users.txt", data.trim());
        } catch (Exception e) {
            LOGGER.throwing(User.class.getName(), "method saveUserFile", e);
        }
        String userStr = "", passStr = "", adminStr = "";
        for (User u : userList) {
            userStr += u.username + ",";
            passStr += u.password + ",";
            if (u.administrator == true)
                //DEBUG(u.toString());
                adminStr += u.username + ",";
        }
        userStr = userStr.substring(0, userStr.lastIndexOf(','));
        passStr = passStr.substring(0, passStr.lastIndexOf(','));
        adminStr = adminStr.substring(0, adminStr.lastIndexOf(','));
        DEBUG("Users: " + userStr);
        DEBUG("Passwords: " + passStr);
        DEBUG("Admins: " + adminStr);
        prefs.put("USERS", userStr);
        prefs.put("PASSWORDS", passStr);
        prefs.put("ADMINS", adminStr);
    }

    /**
     * Read users from the preferences object.
     *
     * @throws com.tester.User.UsersNotFoundException
     */
    public static void readUsersFromPreferences() throws UsersNotFoundException {
        if (prefs == null)
            prefs = GUIFrame.getPreferences();

        String usersString = prefs.get("USERS", null);
        String passwordsString = prefs.get("PASSWORDS", null);
        String adminsString = prefs.get("ADMINS", null);

        if (usersString == null || passwordsString == null || adminsString == null) {
            JOptionPane.showMessageDialog(GUIFrame.frame, "failed to read"
                    + " users from preference node. Please fix the"
                    + " configuration data.", "Error", JOptionPane.ERROR_MESSAGE);
            throw new UsersNotFoundException("Exception reading users from preferences file.");
        }

        String[] userArray = PApplet.split(usersString, ',');
        String[] passwordArray = PApplet.split(passwordsString, ',');
        List<String> adminList = Arrays.asList(PApplet.split(adminsString, ','));

        userList.clear();
        userMap.clear();

        for (int i = 0; i < userArray.length; i++) {
            User u = new User(userArray[i], passwordArray[i]);
            if (adminList.contains(userArray[i]))
                u.administrator = true;
            else
                u.administrator = false;
            DEBUG("Adding user: " + u);
            userList.add(u);
            userMap.putIfAbsent(userArray[i], passwordArray[i]);
        }
        DEBUG("Added " + userList.size() + " users from preference node.");
    }

    private static void DEBUG(String string) {
        if (DEBUG)
            println(string);
    }

    @Override
    public String toString() {
        return this.username + ":" + this.password + ":" + this.administrator;
    }

    /**
     * Extends the Exception class for errors related to reading the user
     * configuration.
     */
    public static class UsersNotFoundException extends Exception {

        private static final long serialVersionUID = 8381627580301161495L;

        private UsersNotFoundException(String msg) {
            super(msg);
        }


    }


}
