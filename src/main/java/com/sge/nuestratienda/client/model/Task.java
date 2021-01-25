package com.sge.nuestratienda.client.model;

public class Task {

    /**
     * Field set by Odoo
     */
    private int id;

    private boolean active = true;
    private String name;
    private boolean is_done = false;

    public Task()
    {

    }

    public Task( String name) {
        this.active = true;
        this.name = name;
        this.is_done = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIs_done() {
        return is_done;
    }

    public void setIs_done(boolean is_done) {
        this.is_done = is_done;
    }

}
