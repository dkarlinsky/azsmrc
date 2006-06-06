/*
 * Created on Nov, 16, 2005
 * Created by omschaub
 * 
 */
package lbms.azsmrc.plugin.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;



public class Utilities {
    
    /** Centers a Shell and opens it relative to the users Monitor
     * 
     * @param shell
     */
    
    public static void centerShellandOpen(Shell shell){
        //open shell
        shell.pack();
        
        //Center Shell
        Monitor primary = getDisplay().getPrimaryMonitor ();
        Rectangle bounds = primary.getBounds ();
        Rectangle rect = shell.getBounds ();
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y +(bounds.height - rect.height) / 2;
        shell.setLocation (x, y);
        
        //open shell
        shell.open();
    }
    
    /** Centers a Shell and opens it relative to given control
     * 
     * @param shell
     * @param control
     */
    
    public static void centerShellRelativeToandOpen(final Shell shell, final Control control){
        //open shell
        shell.pack();
        
        //Center Shell
       
        final Rectangle bounds = control.getBounds();
        final Point shellSize = shell.getSize();
        shell.setLocation(
                bounds.x + (bounds.width / 2) - shellSize.x / 2,
                bounds.y + (bounds.height / 2) - shellSize.y / 2
        );
        
        //open shell
        shell.open();
    } 
    
    /**
     * returns the display by using the Plugin.java and the UISWTInstance
     * @return Display
     */
    
    public static Display getDisplay(){
        return Plugin.getDisplay();
    }
    
    
    
    /**returns the current time formatted in 12 hr or 24 hr time
     * based on user preferences
     * @return String dateCurrentTime
     * @param boolean MilitarTime
     */
    
    public static String getCurrentTime(boolean MilitaryTime){
        String dateCurrentTime;
        SimpleDateFormat sdf;
        //Date when = new Date();
        //when.getTime();
        if(MilitaryTime)
        {
            sdf = new SimpleDateFormat("dd/MM/yyy HH:mm:ss" );
        }
        else
        {
            sdf = new SimpleDateFormat("MM/dd/yyy hh:mm:ss aa" );    
        }
        
        sdf.setTimeZone(TimeZone.getDefault());
        dateCurrentTime = sdf.format(Plugin.getPluginInterface().getUtilities().getCurrentSystemTime());
        return dateCurrentTime; 
    }
    
    public static void copy( File fSrc, File sFileDst, boolean bAppend ) throws IOException {		
		int    len  = 32768;
		byte[] buff = new byte[ (int)Math.min( len, fSrc.length() ) ];
		FileInputStream  fis = new FileInputStream(  fSrc );		
		FileOutputStream fos = new FileOutputStream( sFileDst.getAbsolutePath()+System.getProperty("file.separator")+fSrc.getName(), bAppend );
		try {
		while( 0 < (len = fis.read( buff )) )
			fos.write( buff, 0, len );
		} finally {
			fos.flush();
			fos.close();
			fis.close();
		}
	}
    
    public static Map<String, String> decodeHttpHeader(String header) {
    	Map<String,String> headers = new HashMap<String, String>();
    	String[] header_parts = header.split("\r\n");
    	headers.put("status", header_parts[0].trim());
    	for (int i = 1;i<header_parts.length;i++) {
    		String[] key_value = header_parts[i].split(":",2);
    		headers.put(key_value[0].trim().toLowerCase(), key_value[1].trim());
    	}
    	return headers;
    }
}
