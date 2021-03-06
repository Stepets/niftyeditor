/* Copyright 2012 Aguzzi Cristiano

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package jada.ngeditor.guiviews;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.java2d.input.InputSystemAwtImpl;
import de.lessvoid.nifty.java2d.renderer.FontProviderJava2dImpl;
import de.lessvoid.nifty.java2d.renderer.GraphicsWrapper;
import de.lessvoid.nifty.java2d.renderer.RenderDeviceJava2dImpl;
import de.lessvoid.nifty.tools.TimeProvider;
import jada.ngeditor.listeners.GuiSelectionListener;
import jada.ngeditor.listeners.events.RemoveElementEvent;
import jada.ngeditor.listeners.events.SelectionChanged;
import jada.ngeditor.model.GUI;
import jada.ngeditor.model.elements.GLayer;
import jada.ngeditor.model.utils.NiftyDDManager;
import jada.ngeditor.renderUtil.SoudDevicenull;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author cris
 */
public class J2DNiftyView extends javax.swing.JPanel implements GraphicsWrapper,Observer,ActionListener,ChangeListener{
    private static final BasicStroke BASIC_STROKE = new BasicStroke();
    protected Nifty nifty;
    private boolean selecting;
    private Graphics2D graphics2D;
    private GuiSelectionListener previous;
    private Rectangle selected;
    AffineTransform transformer = new AffineTransform();
    private long time =0;
    private long diff = 0;
    private int frames;
    private String fps = "";
    private final static Font fpsFont = new Font("arial",Font.BOLD, 14);
    private final static BasicStroke stroke = new BasicStroke(1.5f,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_ROUND,30,new float[] { 10.0f, 4.0f },0);;
    private Timer timer;
    private final GraphicsWrappImpl graphWrap;
    private NiftyDDManager dragDropManager;
    /**
     * Used if this panel is within a JScrollPanel
     */
    private Rectangle clipView = new Rectangle();
    /**
     * Creates new form J2DNiftyView
     */
      
    public J2DNiftyView(int width , int height) {
        initComponents();
        //Init cut&paste
        ActionMap map = this.getActionMap();
       map.put(TransferHandler.getCopyAction().getValue(javax.swing.Action.NAME),
                TransferHandler.getCopyAction());
        
        
        map.put(TransferHandler.getPasteAction().getValue(javax.swing.Action.NAME),
                TransferHandler.getPasteAction());
        map.put(TransferHandler.getCutAction().getValue(javax.swing.Action.NAME),
                TransferHandler.getCutAction());

        this.setPreferredSize(new Dimension(width,height));
        this.graphWrap = new GraphicsWrappImpl(width, height);
        this.setOpaque(true);
        previous=null;
        selecting=false;
        this.selected= new Rectangle();
        this.clipView.setSize(width, height);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public Graphics2D getGraphics2d() {
        return graphics2D;
    }

    private void registerFonts(FontProviderJava2dImpl fontProvider) {
        fontProvider.addFont("aurulent-sans-16.fnt", new Font("aurulent-sans-16",
	                                Font.ROMAN_BASELINE, 16));
        
    }

    public void init() {
        InputSystemAwtImpl inputSystem = new InputSystemAwtImpl();
        FontProviderJava2dImpl fontProvider = new FontProviderJava2dImpl();
	registerFonts(fontProvider);
        RenderDeviceJava2dImpl renderDevice = new RenderDeviceJava2dImpl(graphWrap);
	renderDevice.setFontProvider(fontProvider);
	nifty = new Nifty(renderDevice,  new SoudDevicenull(), inputSystem,new TimeProvider());
       
        java.net.URL empty = getClass().getResource("/jada/ngeditor/resources/empty.xml");
        try {
            nifty.fromXml(empty.getFile(),empty.openStream(), "screen1");
        } catch (IOException ex) {
            Logger.getLogger(J2DNiftyView.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.dragDropManager = new NiftyDDManager(nifty);
        timer = new Timer(30,this); 
        timer.start();
        this.setIgnoreRepaint(true);
        nifty.resolutionChanged();
    }
   private final static java.awt.Color line = new java.awt.Color(17,229,229);
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        boolean done = false;
        diff += System.currentTimeMillis() - time;
        time = System.currentTimeMillis();
        graphics2D = (Graphics2D) g;
        graphics2D.setBackground(this.getBackground());
        done = nifty.update();
        nifty.setAbsoluteClip(clipView.x,clipView.y,clipView.x+clipView.width,clipView.height+clipView.y);
        nifty.render(true);
        graphics2D.setPaintMode();
        graphics2D.setClip(clipView.x,clipView.y,clipView.x+clipView.width,clipView.height+clipView.y);
        if (nifty.isDebugOptionPanelColors()) {
            graphics2D.setColor(java.awt.Color.red);
            graphics2D.setFont(fpsFont);
            graphics2D.drawString(fps, 0, fpsFont.getSize());
        }
        if (selecting) {
            graphics2D.setColor(line);
            graphics2D.drawLine((int) selected.getCenterX() - 10, (int) selected.getCenterY(), (int) selected.getCenterX() + 10, (int) selected.getCenterY());
            graphics2D.drawLine((int) selected.getCenterX(), (int) selected.getCenterY() - 10, (int) selected.getCenterX(), (int) selected.getCenterY() + 10);
            graphics2D.setStroke(stroke);
            graphics2D.draw(selected);
            graphics2D.setColor(java.awt.Color.black);
            graphics2D.setStroke(BASIC_STROKE);
            graphics2D.drawRect((int) selected.getMaxX() - 6, (int) selected.getMaxY() - 6, 11, 11);
            nifty.getRenderEngine().renderQuad((int) selected.getMaxX() - 5, (int) selected.getMaxY() - 5, 10, 10);
            graphics2D.setColor(Color.WHITE);
            graphics2D.fillOval(selected.x - 4, selected.y - 4, 8, 8);
            graphics2D.setColor(Color.BLACK);
            graphics2D.drawOval(selected.x - 4, selected.y - 4, 8, 8);
        }
        graphics2D.setColor(Color.BLACK);
        graphics2D.setStroke(BASIC_STROKE);
        graphics2D.drawRect(0, 0, this.graphWrap.w-1, this.graphWrap.h-1);
        Toolkit.getDefaultToolkit().sync();
        frames++;
        if (diff >= 1000) {
            diff = 0;
            fps = "Fps: " + frames;
            frames = 0;
        }
        if (done) {
            timer.stop();
        }
    }
    public Nifty getNifty(){
         return nifty;
    }
     
    public void close(){
        nifty.exit();
        timer.stop();
    }
    protected void setClickListener(GuiSelectionListener list){
        this.removeMouseListener(previous);
        this.removeMouseMotionListener(previous);
        this.addMouseListener(list);
        this.addMouseMotionListener(list);
        this.addKeyListener(list);
        previous=list;
    }

    public NiftyDDManager getDDManager(){
        return this.dragDropManager;
    }
    
    public void newGui(GUI toChange) {
       
       toChange.addObserver(this);
       toChange.getSelection().addObserver(this);
       this.setClickListener( new GuiSelectionListener(toChange,this));
       this.selecting=false;
    }

    @Override
    public void update(Observable o, Object arg) {
       if(arg instanceof SelectionChanged){
            SelectionChanged event = (SelectionChanged) arg;
            if(!event.getNewSelection().isEmpty() && !(event.getElement() instanceof GLayer)){
                this.selecting= true;
            }else{
                this.selecting = false;
            }
       }
            if( arg instanceof RemoveElementEvent){
                this.selecting = false;
             }
         
    }
    
    public void moveRect(int x,int y){
        if(selected!=null){
            int dx=(int)(x-this.selected.getCenterX());
            int dy=(int)(y-this.selected.getCenterY());
            this.selected.translate(dx, dy);
        }
    }
    
    public void displayRect(int x,int y,int h,int w){
        this.selected.setBounds(x, y, w, h);
        this.selecting=true;
    }
    
    public void cancelRect(){
        this.selecting=false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.repaint();
    }
    
    public void setResoltion(int width,int height){
        this.setPreferredSize(new Dimension(width, height));
        this.graphWrap.w = width;
        this.graphWrap.h = height;
        this.revalidate();
        this.nifty.resolutionChanged();
        
    }
    @Override
    public void stateChanged(ChangeEvent e) {
       JViewport temp = (JViewport) e.getSource();
       int h = temp.getExtentSize().height > this.graphWrap.h ? this.graphWrap.h : temp.getExtentSize().height; 
       int w = temp.getExtentSize().width > this.graphWrap.w ? this.graphWrap.w : temp.getExtentSize().width;
       this.clipView.setBounds(temp.getViewPosition().x, temp.getViewPosition().y, w, h);
    }

    private class GraphicsWrappImpl implements GraphicsWrapper{
        private int w;
        private  int h;
        
        
        public GraphicsWrappImpl(int width, int height){
            this.h = height;
            this.w = width;
        }
        @Override
        public Graphics2D getGraphics2d() {
            return graphics2D;
        }

        @Override
        public int getHeight() {
            return h;
        }

        @Override
        public int getWidth() {
           return w;
        }
        
        
    } 
    
    private class TransferActionListener implements ActionListener,
                                              PropertyChangeListener {
    private JComponent focusOwner = null;

    public TransferActionListener() {
        KeyboardFocusManager manager = KeyboardFocusManager.
           getCurrentKeyboardFocusManager();
        manager.addPropertyChangeListener("permanentFocusOwner", this);
    }

    public void propertyChange(PropertyChangeEvent e) {
        Object o = e.getNewValue();
        if (o instanceof JComponent) {
            focusOwner = (JComponent)o;
        } else {
            focusOwner = null;
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (focusOwner == null)
            return;
        String action = (String)e.getActionCommand();
        javax.swing.Action a = focusOwner.getActionMap().get(action);
        if (a != null) {
            a.actionPerformed(new ActionEvent(focusOwner,
                                              ActionEvent.ACTION_PERFORMED,
                                              null));
        }
    }
}
}
