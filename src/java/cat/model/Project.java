/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.model;

/**
 *
 * @author vanyaramirez
 */

public class Project {

    private int id;
    private String name;
    private String sourceLang;
    private String targetLangs;
    private String status;
    private String description;

    public Project(int id, String name, String sourceLang,
                   String targetLangs, String status, String description) {
        this.id = id;
        this.name = name;
        this.sourceLang = sourceLang;
        this.targetLangs = targetLangs;
        this.status = status;
        this.description = description;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSourceLang() { return sourceLang; }
    public String getTargetLangs() { return targetLangs; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
}
