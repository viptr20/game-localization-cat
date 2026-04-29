/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.model;

/**
 *
 * @author vanyaramirez
 */

public class DashboardStats {

    private int totalProjects;
    private int totalSegments;
    private int newSegments;
    private int inProgressSegments;
    private int doneSegments;

    private int newProjects;
    private int inProgressProjects;
    private int completedProjects;

    public int getTotalProjects() { return totalProjects; }
    public void setTotalProjects(int totalProjects) { this.totalProjects = totalProjects; }

    public int getTotalSegments() { return totalSegments; }
    public void setTotalSegments(int totalSegments) { this.totalSegments = totalSegments; }

    public int getNewSegments() { return newSegments; }
    public void setNewSegments(int newSegments) { this.newSegments = newSegments; }

    public int getInProgressSegments() { return inProgressSegments; }
    public void setInProgressSegments(int inProgressSegments) { this.inProgressSegments = inProgressSegments; }

    public int getDoneSegments() { return doneSegments; }
    public void setDoneSegments(int doneSegments) { this.doneSegments = doneSegments; }

    public int getNewProjects() { return newProjects; }
    public void setNewProjects(int newProjects) { this.newProjects = newProjects; }

    public int getInProgressProjects() { return inProgressProjects; }
    public void setInProgressProjects(int inProgressProjects) { this.inProgressProjects = inProgressProjects; }

    public int getCompletedProjects() { return completedProjects; }
    public void setCompletedProjects(int completedProjects) { this.completedProjects = completedProjects; }
}
