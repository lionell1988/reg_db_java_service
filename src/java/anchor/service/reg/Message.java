/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package anchor.service.reg;

/**
 *
 * @author Leonardo
 */
public class Message {
    private int code;
    private String text;
    
    public Message(){}
    
    
    public int getCode(){
        return this.code; 
    }
    public String getText(){
        return this.text;
    }
    public void setCode(int code){
        this.code = code;
    }
    public void setText(String text){
        this.text = text;
    }
    
}
