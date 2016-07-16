/*
 * Created on 29 juin 2003
 * Copyright (C) 2003, 2004, 2005, 2006 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * AELITIS, SAS au capital de 46,603.30 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 *
 */
package lbms.azsmrc.remote.client.plugins.ui.swt;

import org.eclipse.swt.widgets.Composite;



// XXX This class is used by plugins.  Don't remove any functions from it!
public abstract class AbstractIView implements IView {


  public void initialize(Composite composite){    
  }

  public Composite getComposite(){ return null; }
  public void refresh(){}

  /**
   * A basic implementation that disposes the composite
   * Should be called with super.delete() from any extending class.
   * Images, Colors and such SWT handles must be disposed by the class itself.
   */
  public void delete(){
	Composite comp = getComposite();
	if (comp != null && !comp.isDisposed())
	  comp.dispose();
  }

  public String getData(){ return null; }

  /**
   * Called in order to set / update the title of this View.  When the view
   * is being displayed in a tab, the full title is used for the tooltip.
   * 
   * By default, this function will return text from the message bundles which
   * correspond to the key returned in #getData()
   * 
   * @return the full title for the view
   */
  public String getFullTitle(){
	  String	key = getData();

/*	 if ( MessageText.keyExists( key )){

		 return MessageText.getString(getData());
	 }*/

	 return( key.replace( '.', ' ' ));	// support old plugins
  }
}
