/*
 * org.openmicroscopy.shoola.env.rnd.Renderer
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.rnd;


//Java imports
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainChain;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.defs.ChannelBindings;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.metadata.MetadataSource;
import org.openmicroscopy.shoola.env.rnd.metadata.MetadataSourceException;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsGlobalStatsEntry;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStats;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class Renderer
{
	private int 				imageID;
	private int					pixelsID;
	private long 				omeisPixelsID;
	private PixelsDimensions	pixelsDims;
	private PixelsStats			pixelsStats;
	private RenderingDef		renderingDef;
	
	private PlaneDef			planeDef;
	private QuantumManager		quantumManager;
	private DataSink			dataSink;
	private RenderingStrategy	renderingStrategy;
	private CodomainChain		codomainChain;

	private RenderingEngine		engine;	
	
	
	/** 
	 * Helper method to create the default settings if none is available.
	 * In this case we use a grayscale model to map the first wavelength in
	 * the pixels file.  The mapping is linear and the intervals are selected
	 * according to a "best-guess" statistical approach.
	 * 
	 * @param stats	For each wavelength, it contains the global minimum and
	 * 				maximum of the wavelength stack across time.
	 * @return	The default rendering settings.
	 */
	private RenderingDef createDefaultRenderingDef(PixelsDimensions dims,
											PixelsStats stats, int pixelType)
	{
		QuantumDef qDef = new QuantumDef(QuantumFactory.LINEAR, pixelType, 1, 
											0, QuantumFactory.DEPTH_8BIT,
											QuantumFactory.DEPTH_8BIT);
		ChannelBindings[] waves = new ChannelBindings[dims.sizeW];
		PixelsGlobalStatsEntry wGlobal;
		for (int w = 0; w < dims.sizeW; ++w) {
			wGlobal = stats.getGlobalEntry(w);
			//TODO: calcultate default interval using sigma, etc.
			waves[w] = new ChannelBindings(w, wGlobal.getGlobalMin(),
											wGlobal.getGlobalMax(),
											0, 0, 0, 255, false);
		}
		waves[0].setActive(true);  //NOTE: ImageDimensions enforces 1 < sizeW.
		return new RenderingDef(dims.sizeZ/2+dims.sizeZ%2-1, 0, 
								RenderingDef.GS, qDef, waves);
		//NOTE: middle of stack is z=1 if szZ==3, z=1 if szZ==4, etc.
	}
	
	/**
	 * Creates a new instance to render the specified pixels set.
	 * The {@link #initialize() initialize} method has to be called straight
	 * after in order to get this new instance ready for rendering.
	 * 
	 * @param imageID	The id of the image the pixels set belongs to.
	 * @param pixelsID	The id of the pixels set.
	 * @param engine	Reference to the rendering engine.
	 */
	Renderer(int imageID, int pixelsID, RenderingEngine engine)
	{
		this.imageID = imageID;
		this.pixelsID = pixelsID;
		this.engine = engine;
	}
	
	/**
	 * Initializes the rendering environment, loads the pixels metadata and
	 * the display options.
	 * 
	 * @throws MetadataSourceException If an error occurs while retrieving the
	 * 									data from the source repository.
	 */
	void initialize()
		throws MetadataSourceException
	{
		MetadataSource source = engine.getMetadataSource(imageID, pixelsID);
		source.load();
		omeisPixelsID = source.getOmeisPixelsID();
		pixelsDims = source.getPixelsDims();
		pixelsStats = source.getPixelsStats();
		renderingDef = source.getDisplayOptions();
		if (renderingDef == null)
			renderingDef = createDefaultRenderingDef(pixelsDims, pixelsStats,
														source.getPixelType());
		planeDef = null;  //RE will pass this to render().
		QuantumDef qd = renderingDef.getQuantumDef();
		quantumManager = new QuantumManager(pixelsDims.sizeW);
		quantumManager.initStrategies(qd, pixelsStats);
		codomainChain = new CodomainChain(qd.cdStart, qd.cdEnd);
		dataSink = engine.getDataSink(imageID, pixelsID);
		renderingStrategy = RenderingStrategy.makeNew(renderingDef.getModel());
	}
	
	/** Render a specified plane. */
	BufferedImage render(PlaneDef pd)
	{
		if (pd == null)
			throw new NullPointerException("No plane definition.");
		planeDef = pd;
		return renderingStrategy.render(this);
	}

	PixelsDimensions getPixelsDims()
	{
		return pixelsDims;
	}

	PixelsStats getPixelsStats()
	{
		return pixelsStats;
	}

	PlaneDef getPlaneDef()
	{
		return planeDef;
	}

	RenderingDef getRenderingDef()
	{
		return renderingDef;
	}

	QuantumManager getQuantumManager()
	{
		return quantumManager;
	}

	DataSink getDataSink()
	{
		return dataSink;
	}

	CodomainChain getCodomainChain()
	{
		return codomainChain;
	}

}
