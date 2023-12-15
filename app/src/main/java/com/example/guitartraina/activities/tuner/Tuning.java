package com.example.guitartraina.activities.tuner;

import java.io.Serializable;

public class Tuning implements Serializable {
    private Integer tuningId;
    private String title;
    private String frequencies;
    public double[] getFrequencies(){
        String []strings=frequencies.split(",");
        double [] frequencies= new double[strings.length];
        for (int i = 0; i<frequencies.length;i++) {
            String a = strings[i].replaceAll("[a-zA-Z\\[\\]]*#?\\s*","");
            frequencies[i]=Double.parseDouble(a);
        }
        return frequencies;
    }
    public String[] getNoteNames(){
        String []strings=frequencies.split(",");
        String [] noteNames= new String[strings.length];
        for (int i = 0; i<noteNames.length;i++) {
            noteNames[i]=strings[i].replace(" ","").replaceAll("[\\[\\d]\\.?","");
        }
        return noteNames;
    }

    public Integer getId() {
        return tuningId;
    }

    public void setId(Integer id) {
        this.tuningId = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStrings() {
        return frequencies;
    }

    public void setStrings(String strings) {
        frequencies = strings;
    }
}
