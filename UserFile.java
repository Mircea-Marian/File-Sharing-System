/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author John Prime
 */
public class UserFile {

    // atributes
    Integer id;
    String name;
    String ip;
    Integer port;
    String utilizator;
    String group_owned;
    boolean isOnline;

    public UserFile(Integer id, String name, String ip, Integer port, String utilizator, String group_owned, Boolean isOnline) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.utilizator = utilizator;
        this.group_owned = group_owned;
        this.isOnline = isOnline;
    }

}
