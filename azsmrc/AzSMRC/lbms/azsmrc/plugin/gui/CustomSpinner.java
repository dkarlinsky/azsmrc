/*
 * Created on Nov 30, 2005
 * Created by omschaub
 * 
 */
package lbms.azsmrc.plugin.gui;

//import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

 

public class CustomSpinner extends Composite {

      static final int BUTTON_WIDTH = 16;

      //Text text;
      Label text;
      Button up, down;

      int minimum, maximum;

     

      public CustomSpinner(Composite parent, int style) {

            super(parent, style);

            //text = new Text(this, style | SWT.SINGLE | SWT.BORDER);
            
            text = new Label(this, style | SWT.NULL);
            text.setBackground(parent.getBackground());
            up = new Button(this, style | SWT.ARROW | SWT.UP);

            down = new Button(this, style | SWT.ARROW | SWT.DOWN);
            
            text.addListener(SWT.Verify, new Listener() {

                  public void handleEvent(Event e) {

                        verify(e);

                  }

            });

            text.addListener (SWT.Traverse, new Listener () {

                  public void handleEvent(Event e) {

                        traverse(e);                 

                  }

            });

            up.addListener(SWT.Selection, new Listener() {

                  public void handleEvent(Event e) {

                        up();

                  }

            });

            down.addListener(SWT.Selection, new Listener() {

                  public void handleEvent(Event e) {

                        down();

                  }

            });

            addListener(SWT.Resize, new Listener() {

                  public void handleEvent(Event e) {

                        resize();

                  }

            });

            addListener(SWT.FocusIn, new Listener() {

                  public void handleEvent(Event e) {

                        focusIn();             

                  }

            });

            text.setFont(getFont());

            minimum = 0;

            maximum = 9;

            setSelection(minimum);

      }

     

      void verify(Event e) {

            try {

                  Integer.parseInt(e.text);
                  
                  
            } catch (NumberFormatException ex) {

                  e.doit = false;

            }

      }

 

      void traverse(Event e) {

            switch (e.detail) {

                  case SWT.TRAVERSE_ARROW_PREVIOUS:

                        if (e.keyCode == SWT.ARROW_UP) {

                              e.doit = true;

                              e.detail = SWT.NULL;

                              up();

                        }

                        break;

                  case SWT.TRAVERSE_ARROW_NEXT:

                        if (e.keyCode == SWT.ARROW_DOWN) {

                              e.doit = true;

                              e.detail = SWT.NULL;

                              down();

                        }

                        break;

            }

      }

 

      void up() {

            setSelection(getSelection() + 1);

            notifyListeners(SWT.Selection, new Event());

      }

     

      void down() {

            setSelection(getSelection() - 1);

            notifyListeners(SWT.Selection, new Event());

      }

     

      void focusIn() {

            text.setFocus();

      }

 

      public void setFont(Font font) {

            super.setFont(font);

            text.setFont(font);

      }

     

      public void setSelection(int selection) {

            if (selection < minimum) {

                  selection = minimum;

            } else if (selection > maximum) {

                  selection = maximum;

            }

            text.setText(String.valueOf(selection));

            //text.selectAll();

            //text.setFocus();

      }

 

      public int getSelection() {

            return Integer.parseInt(text.getText());

      }

     

      public void setMaximum(int maximum) {

            checkWidget();

            this.maximum = maximum;

            resize();

      }

 

      public int getMaximum() {

            return maximum;

      }

 

      public void setMinimum(int minimum) {

            this.minimum = minimum;

      }

 

      public int getMinimum() {

            return minimum;

      }

 

      void resize() {

            Point pt = computeSize(SWT.DEFAULT, SWT.DEFAULT);
            
            int textWidth = pt.x - (BUTTON_WIDTH*2);
                                    
            int buttonHeight = pt.y ;
            
            text.setBounds(0, 0, textWidth, pt.y );

            up.setBounds(textWidth, 0, BUTTON_WIDTH , buttonHeight);
                       
            down.setBounds(textWidth + BUTTON_WIDTH, 0, BUTTON_WIDTH, buttonHeight);
            super.layout();
      }

 

      public Point computeSize(int wHint, int hHint, boolean changed) {

            GC gc = new GC(text);

            Point textExtent = gc.textExtent(String.valueOf(maximum));

            gc.dispose();

            Point pt = text.computeSize(textExtent.x, textExtent.y);

            int width = pt.x + BUTTON_WIDTH *2;

            int height = pt.y;

            if (wHint != SWT.DEFAULT) width = wHint;

            if (hHint != SWT.DEFAULT) height = hHint;

            return new Point(width, height);

      }
      /**
       * Enables / Disables the Up button of the spinner
       *
       */
      public void setUpEnabled(boolean enabled){
          up.setEnabled(enabled);
      }

      /**
       * Enables / Disables the Down button of the spinner
       *
       */
      public void setDownEnabled(boolean enabled){
          down.setEnabled(enabled);
      }
      
      /**
       * Returns a boolean of the Up Button status
       * @return boolean 
       */
      public boolean getUpEnabled(){
          return up.getEnabled();
      }
      
      /**
       * Returns a boolean of the Down Button status
       * @return boolean
       */
      public boolean getDownEnabled(){
          return down.getEnabled();
      }
      
      public void addSelectionListener(SelectionListener listener) {

            if (listener == null) throw new SWTError(SWT.ERROR_NULL_ARGUMENT);

            addListener(SWT.Selection, new TypedListener(listener));

      }

}

 
