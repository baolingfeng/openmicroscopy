/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.ContainerFinder
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.browser;




//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import pojos.CategoryData;
import pojos.DatasetData;
import pojos.TagAnnotationData;

/** 
 * Finds the {@link TreeImageSet} representing either a {@link CategoryData} or 
 * a {@link DatasetData}.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class ContainerFinder
	implements TreeImageDisplayVisitor
{

    /** Set of <code>TreeImageSet</code>s */
    private Set containerNodes;
    
    /** Set of corresponding <code>DataObject</code>s */
    private Set containers;
    
    /** Creates a new instance. */
    public ContainerFinder()
    {
        containerNodes = new HashSet();
        containers = new HashSet();
    }
    
    /**
     * Returns the collection of found nodes.
     * 
     * @return See above.
     */
    public Set getContainerNodes() { return containerNodes; }
    
    /**
     * Returns the collection of found <code>DataObject</code>s.
     * 
     * @return See above.
     */
    public Set getContainers() { return containers; }

    /** 
     * Required by the {@link TreeImageDisplayVisitor} I/F but no-op 
     * implementation in our case.
     * @see TreeImageDisplayVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node) {}

    /** 
     * Implemented as specified by the {@link TreeImageDisplayVisitor} I/F.
     * @see TreeImageDisplayVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node)
    {
        Object userObject = node.getUserObject();
        if ((userObject instanceof DatasetData) || 
            (userObject instanceof CategoryData) || 
            userObject instanceof TagAnnotationData) {
            containerNodes.add(node); 
            containers.add(userObject);
        }
    }

}
