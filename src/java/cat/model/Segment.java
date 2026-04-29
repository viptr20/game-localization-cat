/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.model;

/**
 *
 * @author vanyaramirez
 */

public class Segment {

    private int id;
    private int projectId;
    private String sourceText;
    private String targetText;
    private String status;

    public Segment(int id, int projectId, String sourceText,
                   String targetText, String status) {
        this.id = id;
        this.projectId = projectId;
        this.sourceText = sourceText;
        this.targetText = targetText;
        this.status = status;
    }

    public int getId() { return id; }
    public int getProjectId() { return projectId; }
    public String getSourceText() { return sourceText; }
    public String getTargetText() { return targetText; }
    public String getStatus() { return status; }

    public void setTargetText(String targetText) { this.targetText = targetText; }
    public void setStatus(String status) { this.status = status; }
}
