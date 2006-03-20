/*
 * Created on Nov 16, 2005
 */
package lbms.azsmrc.pugin.gui;




import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEvent;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEventListener;


public class View implements UISWTViewEventListener {
	
    boolean isCreated = false;    
    public static final String VIEWID = "AZMultiUser_View";
    
	//The current Display
	private static Display display;
	
	//This view main GUI object
	public static Composite composite;
	
    
    //The Plugin interface
    PluginInterface pluginInterface;
    
   
    
    
  public View(PluginInterface pluginInterface) {
    this.pluginInterface = pluginInterface;
      
  }
	
	
	
	/**
	 * Here is the GUI initialization
	 */
	public void initialize(Composite parent) {
               
        
		// We store the Display variable as we'll need it for async GUI Changes
		display = parent.getDisplay();

                
        //Before starting the GUI we will initiate all of the pics
        ImageRepository.loadImages(display);
      
        
        //Main composite layout
		composite = new Composite(parent,SWT.NULL);
		
        //Make the composite have a grid layout
        GridLayout layout = new GridLayout();

        //set num of columns and margins
        layout.numColumns = 1;


        //set the composite to the layout
        composite.setLayout(layout);
		
        //GridData for the composite
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalSpan = 5;
        composite.setLayoutData(gridData);
        
        
        //Login Composite here and if successful, then show the main GUI
        GUILogin.openLogin(composite);
        
        
        
        //GUIMain.open(composite);
        
        
        composite.layout();
	
	}
	



/**
 * Delete function... runs at close of plugin from within Azureus
 */

public void delete() {
    //Unload any custom colors
    ColorUtilities.unloadColors();

    isCreated = false;


}

public boolean eventOccurred(UISWTViewEvent event) {
    switch (event.getType()) {
    
    
    case UISWTViewEvent.TYPE_CREATE:
        if (isCreated)
          return false;
        
        isCreated = true;
        break;
    
        
    case UISWTViewEvent.TYPE_INITIALIZE:
        initialize((Composite)event.getData());
        break;
        
        
        
    case UISWTViewEvent.TYPE_DESTROY:
        delete();
        break;
        
    }

    return true;
}





//EOF
}
