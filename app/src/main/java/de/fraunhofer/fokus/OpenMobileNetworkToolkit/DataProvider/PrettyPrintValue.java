package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

public class PrettyPrintValue{
    boolean to_show;
    String name;

    public PrettyPrintValue(boolean to_show, String name){
        this.to_show = to_show;
        this.name = name;
    }

    public boolean getToShow(){
        return this.to_show;
    }
    public String getName(){
        return this.name;
    }

}
