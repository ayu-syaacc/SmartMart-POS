package com.smartmart.model;

public class FreshSave {

    private int idAturan;
    private int batasHari;
    private int persenDiskon;

    public FreshSave() {
    }

    public FreshSave(int idAturan, int batasHari, int persenDiskon) {
        this.idAturan = idAturan;
        this.batasHari = batasHari;
        this.persenDiskon = persenDiskon;
    }

    public FreshSave(int batasHari, int persenDiskon) {
        this.batasHari = batasHari;
        this.persenDiskon = persenDiskon;
    }

    public int getIdAturan() {
        return idAturan;
    }

    public void setIdAturan(int idAturan) {
        this.idAturan = idAturan;
    }

    public int getBatasHari() {
        return batasHari;
    }

    public void setBatasHari(int batasHari) {
        this.batasHari = batasHari;
    }

    public int getPersenDiskon() {
        return persenDiskon;
    }

    public void setPersenDiskon(int persenDiskon) {
        this.persenDiskon = persenDiskon;
    }

    @Override
    public String toString() {
        return batasHari + " hari - " + persenDiskon + "%";
    }
}