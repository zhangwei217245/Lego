/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.meshwork.edge.exception;

/**
 *
 * @author zhangwei
 */
public class GraphDBException extends Exception {
    private static final long serialVersionUID = -510641059496385493L;

    /**
     * Creates a new instance of <code>GraphDBException</code> without detail
     * message.
     */
    public GraphDBException() {
    }

    /**
     * Constructs an instance of <code>GraphDBException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public GraphDBException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>GraphDBException</code> with the
     * specified detail message and relevant root cause
     * @param msg
     * @param cause 
     */
    public GraphDBException(String msg, Throwable cause){
        super(msg, cause);
    }
}
