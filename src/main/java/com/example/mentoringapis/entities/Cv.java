package com.example.mentoringapis.entities;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Cv implements Serializable {
    private List<School> schools;
    private List<Job> jobs;
    @Data
    private static class School implements Serializable{
        String name;
        String location;
    }
    @Data
    private static class Job implements Serializable{
        String name;
        String location;
    }
}
