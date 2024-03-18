package com.puneeth.ctabustracker;

import java.io.Serializable;
import java.util.ArrayList;

public class Route implements Serializable {

    String routeId;
    String routeName;
    String routeColor;
    ArrayList<String> directions=new ArrayList<>();

    public Route(String routeId, String routeName, String routeColor) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.routeColor = routeColor;
    }

    public void setDirections(ArrayList<String> directions) {
        this.directions = directions;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getRouteColor() {
        return routeColor;
    }

    public ArrayList<String> getDirections() {
        return directions;
    }
}
