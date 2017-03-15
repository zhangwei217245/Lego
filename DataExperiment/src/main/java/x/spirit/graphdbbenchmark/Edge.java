/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.spirit.graphdbbenchmark;

/**
 *
 * @author zhangwei
 */
public class Edge {
    
    private String sourceV;
    private String destinationV;
    private String value;

    public Edge(String sourceV, String destinationV, String value) {
        this.sourceV = sourceV;
        this.destinationV = destinationV;
        this.value = value;
    }
    
    

    /**
     * @return the sourceV
     */
    public String getSourceV() {
        return sourceV;
    }

    /**
     * @param sourceV the sourceV to set
     */
    public void setSourceV(String sourceV) {
        this.sourceV = sourceV;
    }

    /**
     * @return the destinationV
     */
    public String getDestinationV() {
        return destinationV;
    }

    /**
     * @param destinationV the destinationV to set
     */
    public void setDestinationV(String destinationV) {
        this.destinationV = destinationV;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    
}
