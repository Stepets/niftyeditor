/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jada.ngeditor.listeners.events;

import jada.ngeditor.model.elements.GElement;

/**
 *
 * @author cris
 */
public class AddElementEvent implements ElementEvent {
    private final GElement element;
    
    public AddElementEvent(GElement element){
        this.element = element;
        
    }

    /**
     * @return the element
     */
    public GElement getElement() {
        return element;
    }
    
}
