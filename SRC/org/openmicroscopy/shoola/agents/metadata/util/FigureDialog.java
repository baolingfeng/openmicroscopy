/*
 * org.openmicroscopy.shoola.agents.metadata.util.FigureDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;


//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTaskPane;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingView;

//Application-internal dependencies
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.ColorListRenderer;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.DrawingComponent;
import org.openmicroscopy.shoola.util.ui.slider.GridSlider;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;
import pojos.ChannelData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.TagAnnotationData;

/** 
 * Modal dialog displaying option to create a figure of a collection of 
 * images. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FigureDialog 
	extends JDialog
	implements ActionListener, ChangeListener, DocumentListener, 
	PropertyChangeListener
{

	/** Indicates that the dialog is for a split view. */
	public static final int			SPLIT = 0;
	
	/** Indicates that the dialog is for a split view and ROI. */
	public static final int			SPLIT_ROI = 1;
	
	/** Indicates that the dialog is for a movie figure. */
	public static final int			MOVIE = 2;
	
	/** Indicates that the dialog is for a thumbnails figure. */
	public static final int			THUMBNAILS = 3;
	
	/** Bound property indicating to create a split view figure. */
	public static final String		CREATE_FIGURE_PROPERTY = "createFigure";
	
	/** Bound property indicating to close the dialog. */
	public static final String		CLOSE_FIGURE_PROPERTY = "closeFigure";
	
	/** Action id indicating to close the dialog. */
	public static final int 		CLOSE = 0;
	
	/** Action id indicating to create a movie. */
	public static final int 		SAVE = 1;
	
	/** The text displayed next to the merged image. */
	private static final String		MERGED_TEXT = "Merged";
	
	/** Action id indicating to allow the modification of the scale bar. */
	private static final int 		SCALE_BAR = 2;
	
	/** Action id indicating to arrange the thumbnails by tags. */
	private static final int 		ARRANGE_BY_TAGS = 3;
	
	/** Action id indicating to turn on or off the projection's controls. */
	private static final int 		PROJECTION = 4;
	
	/** Default text describing the compression check box.  */
    private static final String		PROJECTION_DESCRIPTION = 
    				"Select the type of projection.";
    
    /** The default number of thumbnails per row. */
    private static final int		ITEMS_PER_ROW = 10;
    
    /** The height of the component displaying the available tags. */
    private static final int		MAX_HEIGHT = 150;
    
    /** The possible options for row names. */
    private static final String[]	ROW_NAMES;

    /** The possible options for row names. */
    private static final String[]	MAGNIFICATION;
    
    /** Index to <code>Auto</code>. */
    private static final int		ZOOM_AUTO = 0;
    
    /** Index to <code>100%</code> magnification. */
    private static final int		ZOOM_100 = 1;
    
    /** Index to <code>200%</code> magnification. */
    private static final int		ZOOM_200 = 2;
    
    /** Index to <code>300%</code> magnification. */
    private static final int		ZOOM_300 = 3;
    
    /** Index to <code>400%</code> magnification. */
    private static final int		ZOOM_400 = 4;
    
    /** Index to <code>500%</code> magnification. */
    private static final int		ZOOM_500 = 5;
    
    /** Index corresponding to a <code>24x24</code> thumbnail. */
    private static final int		SIZE_24 = 0;
    
    /** Index corresponding to a <code>32x32</code> thumbnail. */
    private static final int		SIZE_32 = 1;
    
    /** Index corresponding to a <code>48x48</code> thumbnail. */
    private static final int		SIZE_48 = 2;
    
    /** Index corresponding to a <code>64x64</code> thumbnail. */
    private static final int		SIZE_64 = 3;
    
    /** Index corresponding to a <code>96x96</code> thumbnail. */
    private static final int		SIZE_96 = 4;
    
    /** Index corresponding to a <code>128x128</code> thumbnail. */
    private static final int		SIZE_128 = 5;
    
    /** Index corresponding to a <code>160x160</code> thumbnail. */
    private static final int		SIZE_160 = 6;
    
    /** The size available for thumbnails creation. */
    private static final String[]	SIZE_OPTIONS;

	static {
		ROW_NAMES = new String[3];
		ROW_NAMES[FigureParam.IMAGE_NAME] = "Image's name";
		ROW_NAMES[FigureParam.DATASET_NAME] = "Datasets";
		ROW_NAMES[FigureParam.TAG_NAME] = "Tags";
		MAGNIFICATION = new String[6];
		MAGNIFICATION[ZOOM_AUTO] = "Auto - 1st row";
		MAGNIFICATION[ZOOM_100] = "100%";
		MAGNIFICATION[ZOOM_200] = "200%";
		MAGNIFICATION[ZOOM_300] = "300%";
		MAGNIFICATION[ZOOM_400] = "400%";
		MAGNIFICATION[ZOOM_500] = "500%";
		SIZE_OPTIONS = new String[7];
		SIZE_OPTIONS[SIZE_24] = "24x24";
		SIZE_OPTIONS[SIZE_32] = "32x32";
		SIZE_OPTIONS[SIZE_48] = "48x48";
		SIZE_OPTIONS[SIZE_64] = "64x64";
		SIZE_OPTIONS[SIZE_96] = "96x96";
		SIZE_OPTIONS[SIZE_128] = "128x128";
		SIZE_OPTIONS[SIZE_160] = "160x160";
	}
	
	/** The name to give to the figure. */
	private JTextField 						nameField;
	
	/** Component to select the z-section interval. */
	private TextualTwoKnobsSlider			zRange;
	
	/** Button to close the dialog. */
	private JButton							closeButton;
	
	/** Button to save the result. */
	private JButton							saveButton;

	/** The supported movie formats. */
	private JComboBox						formats;
	
	/** The type of supported projections. */
    private JComboBox						projectionTypesBox;
    
    /** The type of supported projections. */
    private JRadioButton					splitPanelColor;
    
    /** The type of supported projections. */
    private JRadioButton					splitPanelGrey;
    
	/** The type of projection. */
	private Map<Integer, Integer> 			projectionTypes;

    /** Sets the stepping for the mapping. */
    private JSpinner			   			projectionFrequency;
    
    /** The possible options for naming the rows. */
    private JComboBox						rowName;
      
    /** Components displaying the image. */
    private Map<Integer, FigureComponent> components;
    
    /** List of channel buttons. */
    private List<ChannelComponent>			channelList;
    
	/** Option chosen by the user. */
	private int								option;
	
	/** Reference to the renderer.  */
	private Renderer 						renderer;
	
	/** The default plate object. */
	private PlaneDef 						pDef;

	/** The component displaying the merger image. */
	private FigureCanvas					mergeCanvas;
	
	/** The image with all the active channels. */
	private BufferedImage					mergeImage;
 
	/** The width of a thumbnail. */
	private int 							thumbnailWidth;

	/** The height of a thumbnail. */
	private int 							thumbnailHeight;

	/** The width of the image. */
	private NumericalTextField				widthField;
	
	/** The height of the image. */
	private NumericalTextField				heightField;
	
	/** The supported value of the scale bar. */
	private NumericalTextField				scaleBar;
	
	/** Add a scale bar if selected. */
	private JCheckBox						showScaleBar;
	
	/** The selected color for scale bar. */
	private JComboBox						colorBox;
	
	/** The index of the dialog. One of the constants. */
	private int								dialogType;
	
	/** The pixels set of reference. */
	private PixelsData 						pixels;
	
	/** The components hosting the channel components. */
	private JXTaskPane						channelsPane;

	/** The component hosting the canvas. */
	private JLayeredPane					pane;
	
	/** Component hosting the ROI. */
	private ROIComponent					roiComponent;

	/** 
	 * The drawing component to create drawing, view and editor and link them.
	 */
	private DrawingComponent 				drawingComponent;
	
	/** The size of the thumbnail. */
	private Dimension						size;
	
	/** The magnification factor. */
	private JComboBox						zoomBox;
	
	/** The size of thumbnails. */
	private JComboBox						sizeBox;
	
	/** The number of items. */
	private NumericalTextField				numberPerRow;
	
	/** Indicates to create a figure with the displayed objects. */
	private JRadioButton					displayedObjects;
	
	/** Indicates to create a figure with the selected objects. */
	private JRadioButton					selectedObjects;
	
	/** The type of objects to handle. */
	private Class							type;
	
	/** Indicates to arrange thumbnails by tags. */
	private JCheckBox						arrangeByTags;
	
	/**
	 * The component displaying the controls to create the thumbnails figure.
	 */
	private JPanel							thumbnailsPane;
	
	/** The map containing the selected tags. */
	private Map<JCheckBox, TagAnnotationData> tagsSelection;
	
	/** Use to sort data objects.*/
	private ViewerSorter 					sorter;
	
	/** The component displaying the collection of selected tags. */
	private JPanel							selectedTags;
	
	/** The selection of tags. */
	private List<JCheckBox>					selection;
	
	/** Determines the time-points frequency for the movie figure. */
    private JSpinner			   			movieFrequency;
    
    /** The slider displaying the number of time-points. */
    private GridSlider						movieSlider;
    
    /** Determines the selected plane. */
    private JSpinner						planeSelection;
    
    /** Indicates to turn on or off the projection. */
    private JCheckBox						projectionBox;
    
    /** The time of options. */
	private JComboBox						timesBox;
	
	/** 
	 * Lays out the selected tags. 
	 * 
	 * @param selectedTag Control to select or not the tags.
	 */
	private void layoutSelectedTags(JCheckBox selectedTag)
	{
		selectedTags.removeAll();
		if (selection == null) selection = new ArrayList<JCheckBox>();
		if (selection.contains(selectedTag))
			selection.remove(selectedTag);
		else selection.add(selectedTag);
		
		Iterator<JCheckBox>	i = selection.iterator();
		JCheckBox box;
		int index = 1;
		JLabel label;
		TagAnnotationData tag;
		while (i.hasNext()) {
			box = i.next();
			label = new JLabel();
			tag = tagsSelection.get(box);
			label.setText(index+". "+tag.getTagValue());
			selectedTags.add(label);
			index++;
		}
		selectedTags.revalidate();
		selectedTags.repaint();
	}
	
	/**
	 * Sets the channel selection.
	 * 
	 * @param channel   The selected channel.
	 * @param active	Pass <code>true</code> to set the channel active,
	 * 					<code>false</code> otherwise.
	 */
	private void setChannelSelection(int channel, boolean active)
	{
		renderer.setActive(channel, active);
		mergeImage = getMergedImage();
		mergeCanvas.setImage(mergeImage);
		Iterator<ChannelComponent> i = channelList.iterator();
		ChannelComponent btn;
		List<Integer> actives = renderer.getActiveChannels();
        int v;
        while (i.hasNext()) {
			btn = i.next();
			v = btn.getChannelIndex();
			btn.setSelected(actives.contains(v));
		}
        FigureComponent comp = components.get(channel);
		switch (dialogType) {
			case SPLIT:
		        boolean grey = splitPanelGrey.isSelected();
		        if (active) {
		        	if (grey) comp.resetImage(grey);
		        	else comp.resetImage(!active);
		        } else comp.resetImage(!active);
				break;

			case SPLIT_ROI:
				comp.setSelected(active);
				comp.setEnabled(active);
		}
	}
	
	/**
	 * Returns the merged image.
	 * 
	 * @return See above.
	 */
	private BufferedImage getMergedImage()
	{
		return scaleImage(renderer.renderPlane(pDef));
	}
	
	/**
	 * Scales the passed image.
	 * 
	 * @param image The image to scale down.
	 * @return See above.
	 */
	private BufferedImage scaleImage(BufferedImage image)
	{
		return Factory.scaleBufferedImage(image, size.width, 
				size.height);
	}
	
	/**
	 * Returns the image corresponding to the passed index.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	private BufferedImage getChannelImage(int index)
	{
		//merge image is RGB
		if (renderer.isChannelActive(index)) {
			if (renderer.isMappedImageRGB(renderer.getActiveChannels())) {
				//if red
				DataBuffer buf = mergeImage.getRaster().getDataBuffer();
				if (renderer.isColorComponent(Renderer.RED_BAND, index)) {
					return Factory.createBandImage(buf,
							size.width, size.height, 
							Factory.RED_MASK, Factory.BLANK_MASK,
							Factory.BLANK_MASK);
				} else if (renderer.isColorComponent(Renderer.GREEN_BAND, 
						index)) {
					return Factory.createBandImage(buf,
							size.width, size.height,  
							Factory.BLANK_MASK, Factory.GREEN_MASK, 
							Factory.BLANK_MASK);
				} else if (renderer.isColorComponent(Renderer.BLUE_BAND, 
						index)) {
					return Factory.createBandImage(buf,
							size.width,  size.height, 
							Factory.BLANK_MASK, Factory.BLANK_MASK,
							Factory.BLUE_MASK);
				}
			} else { //not rgb 
				return scaleImage(renderer.createSingleChannelImage(true, index, 
						pDef));
			}
		}
		//turn off all other channels, create an image and reset channels
		return scaleImage(renderer.createSingleChannelImage(true, index, 
				pDef));
	}
	
	/** Initializes the components. */
	private void initialize()
	{
		size = Factory.computeThumbnailSize(thumbnailWidth, thumbnailHeight, 
        		renderer.getPixelsDimensionsX(), 
        		renderer.getPixelsDimensionsY());
		pDef = new PlaneDef();
		pDef.t = renderer.getDefaultT();
		pDef.z = renderer.getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		
		mergeCanvas = new FigureCanvas();
		mergeImage = getMergedImage();
		mergeCanvas.setPreferredSize(new Dimension(thumbnailWidth, 
				thumbnailHeight));
		mergeCanvas.setImage(mergeImage);
	}
	
	/**
	 * Returns the magnification factor.
	 * 
	 * @return See above.
	 */
	private double getMagnificationFactor()
	{
		int maxY = renderer.getPixelsDimensionsY();
		int maxX = renderer.getPixelsDimensionsX();
		if (maxX > thumbnailWidth || maxY >thumbnailHeight) {
			double ratioX = (double) thumbnailWidth/maxX;
			double ratioY = (double) thumbnailHeight/maxY;
			if (ratioX < ratioY) return ratioX;
			return ratioY;
		}
		return -1;
	}
	
	/** Initializes the ROI channels components. */
	private void initChannelROIComponents()
	{
		initialize();
		//draw the roi.
		//Determine the scaling factor.
		
		components = new LinkedHashMap<Integer, FigureComponent>();
		//Initializes the channels
		List<ChannelData> data = renderer.getChannelData();
        ChannelData d;
        //ChannelToggleButton item;
        ChannelButton item;
        Iterator<ChannelData> k = data.iterator();
        List<Integer> active = renderer.getActiveChannels();
        FigureComponent split;
        int j;
        while (k.hasNext()) {
			d = k.next();
			j = d.getIndex();
			split = new FigureComponent(this, renderer.getChannelColor(j), 
					d.getChannelLabeling(), j);
			if (!active.contains(j)) {
				split.setSelected(false);
				split.setEnabled(false);
			}
			split.setCanvasSize(thumbnailWidth, thumbnailHeight);
			components.put(j, split);
		}
		
		
		k = data.iterator();
        channelList = new ArrayList<ChannelComponent>();
        ChannelComponent comp;
        while (k.hasNext()) {
        	d = k.next();
			j = d.getIndex();
			comp = new ChannelComponent(j, renderer.getChannelColor(j), 
					active.contains(j));
			channelList.add(comp);
			comp.addPropertyChangeListener(this);
		}
	}
	
	/** Initializes the channels components. */
	private void initChannelComponents()
	{
		initialize();
		if (dialogType == SPLIT_ROI) {
			zoomBox = new JComboBox(MAGNIFICATION);
			DrawingView canvasView = drawingComponent.getDrawingView();
			double factor = getMagnificationFactor();
			if (factor != -1)
				canvasView.setScaleFactor(factor);
			Coord3D c = new Coord3D(renderer.getDefaultZ(), 
					renderer.getDefaultT());
			try {
				ShapeList list = roiComponent.getShapeList(c);
				ROIFigure figure;
				Drawing drawing = drawingComponent.getDrawing();
				if (list != null) {
					TreeMap map = list.getList();
					Iterator i = map.values().iterator();
					ROIShape shape;
					while (i.hasNext()) {
						shape = (ROIShape) i.next();
						if (shape != null) {
							figure = shape.getFigure();
							//canvasView.addToSelection(figure);
							drawing.add(figure);
						}
					}
					drawingComponent.getDrawingView().setDrawing(drawing);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		

		components = new LinkedHashMap<Integer, FigureComponent>();
		//Initializes the channels
		List<ChannelData> data = renderer.getChannelData();
        ChannelData d;
        //ChannelToggleButton item;
        ChannelButton item;
        Iterator<ChannelData> k = data.iterator();
        List<Integer> active = renderer.getActiveChannels();
        FigureComponent split;
        int j;
        while (k.hasNext()) {
			d = k.next();
			j = d.getIndex();
			split = new FigureComponent(this, renderer.getChannelColor(j), 
					d.getChannelLabeling(), j);
			split.setOriginalImage(getChannelImage(j));
			split.setCanvasSize(thumbnailWidth, thumbnailHeight);
			if (!active.contains(j))
				split.resetImage(true);
			components.put(j, split);
		}

        k = data.iterator();
        channelList = new ArrayList<ChannelComponent>();
        ChannelComponent comp;
        while (k.hasNext()) {
        	d = k.next();
			j = d.getIndex();
			comp = new ChannelComponent(j, renderer.getChannelColor(j), 
					active.contains(j));
			channelList.add(comp);
			comp.addPropertyChangeListener(this);
		}
	}
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param name The default name of the file.
	 */
	private void initComponents(String name)
	{	
		projectionBox = new JCheckBox();
		projectionBox.setActionCommand(""+PROJECTION);
		projectionBox.addActionListener(this);
		planeSelection = new JSpinner(new SpinnerNumberModel(1, 1, 
				pixels.getSizeZ()+1, 1));
		sorter = new ViewerSorter();
		pane = new JLayeredPane();
		thumbnailHeight = Factory.THUMB_DEFAULT_HEIGHT;
		thumbnailWidth = Factory.THUMB_DEFAULT_WIDTH;	
		closeButton = new JButton("Cancel");
		closeButton.setToolTipText(UIUtilities.formatToolTipText(
				"Close the window."));
		closeButton.setActionCommand(""+CLOSE);
		closeButton.addActionListener(this);
		saveButton = new JButton("Create");
		saveButton.setEnabled(false);
		saveButton.setToolTipText(UIUtilities.formatToolTipText(
				"Create a figure."));
		saveButton.setActionCommand(""+SAVE);
		saveButton.addActionListener(this);
		nameField = new JTextField();
		String s = UIUtilities.removeFileExtension(name);
		if (s != null) {
			nameField.setText(s);
			saveButton.setEnabled(true);
		}
		nameField.getDocument().addDocumentListener(this);
		Map<Integer, String> map = FigureParam.FORMATS;
		String[] f = new String[map.size()];
		Entry entry;
		Iterator i = map.entrySet().iterator();
		int index = 0;
		int v;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			v = (Integer) entry.getKey();
			f[v] = (String) entry.getValue();
			if (v == FigureParam.PNG);
				index = v;
		}
		formats = new JComboBox(f);
		formats.setSelectedIndex(index);
		int maxZ = pixels.getSizeZ();
		zRange = new TextualTwoKnobsSlider(1, maxZ, 1, maxZ);
		zRange.layoutComponents();
		zRange.setEnabled(maxZ > 1);
		String[] names = new String[ProjectionParam.PROJECTIONS.size()];
        int k = 0;
        i = ProjectionParam.PROJECTIONS.entrySet().iterator();
        projectionTypes = new HashMap<Integer, Integer>();
        int j;
        while (i.hasNext()) {
        	entry = (Entry) i.next();
			j = (Integer) entry.getKey();
			projectionTypes.put(k, j);
			names[k] = (String) entry.getValue();
			k++;
		}
        rowName = new JComboBox(ROW_NAMES); 
        projectionTypesBox = new JComboBox(names);
        projectionTypesBox.setToolTipText(PROJECTION_DESCRIPTION);
        
		projectionFrequency = new JSpinner(new SpinnerNumberModel(1, 1, maxZ+1, 
				1));
		
		ButtonGroup group = new ButtonGroup();
		splitPanelGrey = new JRadioButton("Grey");
		splitPanelColor = new JRadioButton("Color");
		splitPanelColor.addChangeListener(this);
		splitPanelGrey.addChangeListener(this);
		group.add(splitPanelGrey);
		group.add(splitPanelColor);
		splitPanelColor.setSelected(true);
		
        showScaleBar = new JCheckBox("Scale Bar");
		showScaleBar.setFont(showScaleBar.getFont().deriveFont(Font.BOLD));
		showScaleBar.setActionCommand(""+SCALE_BAR);
		showScaleBar.addActionListener(this);
		scaleBar = new NumericalTextField();
		scaleBar.setText(""+EditorUtil.DEFAULT_SCALE);
		
        colorBox = new JComboBox();
		Map<Color, String> colors = EditorUtil.COLORS_BAR;
		Object[][] cols = new Object[colors.size()][2];
		k = 0;
		i = colors.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			cols[k] = new Object[]{entry.getKey(), entry.getValue()};
			k++;
		}
		
		colorBox.setModel(new DefaultComboBoxModel(cols));	
		colorBox.setSelectedIndex(cols.length-1);
		colorBox.setRenderer(new ColorListRenderer());
        
		showScaleBar.setSelected(false);
		scaleBar.setEnabled(false);
        
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { close(); }
		});
		sizeBox = new JComboBox(SIZE_OPTIONS);
		sizeBox.setSelectedIndex(SIZE_96);
		numberPerRow = new NumericalTextField(1, 100);
		numberPerRow.setColumns(3);
		numberPerRow.setText(""+ITEMS_PER_ROW);
		ButtonGroup optionsGroups = new ButtonGroup();
		displayedObjects = new JRadioButton("Displayed Images");
		selectedObjects = new JRadioButton("Selected Images");
		optionsGroups.add(displayedObjects);
		optionsGroups.add(selectedObjects);
		selectedObjects.setSelected(true);
		
		arrangeByTags = new JCheckBox();
		arrangeByTags.addActionListener(this);
		arrangeByTags.setActionCommand(""+ARRANGE_BY_TAGS);
		int maxT = pixels.getSizeT();
		movieFrequency = new JSpinner(new SpinnerNumberModel(1, 1, maxT+1, 1));
		movieFrequency.addChangeListener(this);
		widthField = new NumericalTextField(0, pixels.getSizeX());
		widthField.setColumns(5);
		widthField.setText(""+pixels.getSizeX());
		heightField = new NumericalTextField(0, pixels.getSizeY());
		heightField.setColumns(5);
		heightField.setText(""+pixels.getSizeY());
		
		widthField.getDocument().addDocumentListener(this);
		heightField.getDocument().addDocumentListener(this);
		movieSlider = new GridSlider(maxT);
		
		setProjectionSelected(false);
		map = FigureParam.TIMES;
		f = new String[map.size()];
		i = map.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			v = (Integer) entry.getKey();
			f[v] = (String) entry.getValue();
		}
		timesBox = new JComboBox(f);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		IconManager icons = IconManager.getInstance();
		
		TitlePanel tp;
		String text = null;
		switch (dialogType) {
			case THUMBNAILS:
				text = "Create a thumbnail Figure.";
				break;
			case SPLIT:
			case SPLIT_ROI:
				text = "Create a Split View Figure.";
				break;
			case MOVIE:
				text = "Create a Movie Figure.";
		}
		tp = new TitlePanel("Create Figure", text, 
				"The figure will be saved to the server.", 
				icons.getIcon(IconManager.SPLIT_VIEW_48));
		Container c = getContentPane();
		c.setLayout(new BorderLayout(5, 5));
		c.add(tp, BorderLayout.NORTH);
		c.add(buildBody(), BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/** 
	 * Builds and lays out the control.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.add(closeButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(saveButton);
		bar.add(Box.createHorizontalStrut(20));
		JPanel p = UIUtilities.buildComponentPanelRight(bar);
		p.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return p;
	}
	
	/**
	 * Builds and lays out the components displaying the dimensions.
	 * 
	 * @return See above.
	 */
	private JPanel buildDimensionComponent()
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridy = 0;
		c.gridx = 0;
        c.weightx = 0.0;  
        p.add(UIUtilities.setTextFont("Thumbnail Width: "), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        p.add(widthField, c);  
        c.gridx++;
        p.add(new JLabel("pixels"), c); 
        c.gridx = 0;
        c.gridy++;
        p.add(UIUtilities.setTextFont("Thumbnail Height: "), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        p.add(heightField, c); 
        c.gridx++;
        p.add(new JLabel("pixels"), c); 
		return UIUtilities.buildComponentPanel(p);
	}

	/**
	 * Enables or not the projection controls.
	 * 
	 * @param selected  Pass <code>true</code> to enable the controls,
	 * 					<code>false</code> otherwise.
	 */
	private void setProjectionSelected(boolean selected)
	{
		projectionTypesBox.setEnabled(selected);
		projectionFrequency.setEnabled(selected);
		zRange.setEnabled(selected);
		planeSelection.setEnabled(!selected);
	}
	
	/**
	 * Builds the projection component.
	 * 
	 * @return See above.
	 */
	private JPanel buildProjectionComponent()
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
		p.add(UIUtilities.setTextFont("Project stack"), c);
		c.gridx++;
		p.add(projectionBox, c);
		c.gridy++;
		c.gridx = 0;
		p.add(UIUtilities.setTextFont("Intensity"), c);
		c.gridx++;
		p.add(projectionTypesBox, c);
		c.gridx = 0;
		c.gridy++;
		p.add(UIUtilities.setTextFont("Every n-th slice"), c);
		c.gridx++;
	    p.add(UIUtilities.buildComponentPanel(projectionFrequency), c);
	    c.gridy++;
		c.gridx = 0;
	    p.add(UIUtilities.setTextFont("Z-sections Range"), c);
	    c.gridx++;
        p.add(UIUtilities.buildComponentPanel(zRange), c);
		return UIUtilities.buildComponentPanel(p);
	}
	
	/**
	 * Builds the component offering name and formats options.
	 * 
	 * @return See above.
	 */
	private JPanel buildTypeComponent()
	{
		JPanel p = new JPanel();
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.PREFERRED, 
			TableLayout.PREFERRED, TableLayout.PREFERRED,
			TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 
				5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED,
				5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED,
				5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}; //rows
		p.setLayout(new TableLayout(tl));
		int i = 0;
        p.add(UIUtilities.setTextFont("Name"), "0, "+i+"");
        p.add(nameField, "1, "+i+", 4, "+i);
        i = i+2;
        p.add(UIUtilities.setTextFont("Format"), "0, "+i+"");
        p.add(formats, "1, "+i);
        if (dialogType == THUMBNAILS) {
        	i = i+2;
        	p.add(UIUtilities.setTextFont("Thumbnails Size"), "0, "+i+"");
        	p.add(UIUtilities.buildComponentPanel(sizeBox), "1, "+i);
        	i = i+2;
        	p.add(UIUtilities.setTextFont("Thumbnails per row"), "0, "+i+"");
        	p.add(UIUtilities.buildComponentPanel(numberPerRow), "1, "+i);
        } else {
        	i = i+2;
        	p.add(UIUtilities.setTextFont("Image Label"), "0, "+i+"");
        	p.add(rowName, "1, "+i);
        	i = i+2;
        	p.add(showScaleBar, "0, "+i);
        	p.add(scaleBar, "1, "+i);
        	p.add(new JLabel("microns"), "2, "+i);
        	i = i+2;
        	p.add(UIUtilities.setTextFont("Overlay"), "0, "+i);
        	p.add(UIUtilities.buildComponentPanel(colorBox), "1, "+i);
        }
        if (ImageData.class.equals(type)) {
        	i = i+2;
        	p.add(UIUtilities.setTextFont("Made of"), "0, "+i+"," +
        	" LEFT, TOP");
        	JPanel controls = new JPanel();
        	controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        	controls.add(displayedObjects);
        	controls.add(selectedObjects);
        	p.add(UIUtilities.buildComponentPanel(controls), "1, "+i);
        }
        switch (dialogType) {
			case SPLIT:
				i = i+2;
	    		p.add(UIUtilities.setTextFont("Select Z-section"), "0, "+i);
	        	p.add(UIUtilities.buildComponentPanel(planeSelection), "1, "+i);
				break;
	
			case MOVIE:
				if (pixels.getSizeT() > 1) {
					i = i+2;
		        	p.add(buildMovieComponent(), "0, "+i+", 4, "+i);
				}
				i = i+2;
	    		p.add(UIUtilities.setTextFont("Select Z-section"), "0, "+i);
	        	p.add(UIUtilities.buildComponentPanel(planeSelection), "1, "+i);
				break;
		}
		return p;
	}
	
	/** 
	 * Builds and lays out the component displaying the channels.
	 * 
	 * @return See above
	 */
	private JPanel buildChannelsComponent()
	{
		JPanel p = new JPanel();
		Iterator<Integer> i = components.keySet().iterator();
		while (i.hasNext()) {
			p.add(components.get(i.next()));
		}
		JPanel merge = new JPanel();
		double s[][] = {{TableLayout.PREFERRED, 5, TableLayout.PREFERRED}, 
				{TableLayout.PREFERRED}};
		merge.setLayout(new TableLayout(s));
		merge.add(p, "0, 0, LEFT, BOTTOM");
		merge.add(buildMergeComponent(), "2, 0, LEFT, TOP");

		JPanel splitPanel = new JPanel();
		splitPanel.add(UIUtilities.setTextFont("Split Panel"));
		splitPanel.add(splitPanelColor);
		splitPanel.add(splitPanelGrey);
		
		JPanel controls = new JPanel();
		double size[][] = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
				TableLayout.PREFERRED}};
		controls.setLayout(new TableLayout(size));
		controls.add(splitPanel, "0, 0, LEFT, CENTER");
		controls.add(merge, "0, 2");
		if (dialogType == SPLIT_ROI) {
			JPanel zoomPanel = new JPanel();
			zoomPanel.add(UIUtilities.setTextFont("Zoom"));
			zoomPanel.add(zoomBox);
			JPanel splitControls = new JPanel();
			splitControls.setLayout(new BoxLayout(splitControls, 
					BoxLayout.X_AXIS));
			splitControls.add(zoomPanel);
			splitControls.add(buildDimensionComponent());
			controls.add(UIUtilities.buildComponentPanel(splitControls), "0, 4");
		} else {
			controls.add(buildDimensionComponent(), "0, 4");
		}
		
		return controls;
	}
	
	/**
	 * Builds the component displaying the merge image.
	 * 
	 * @return See above.
	 */
	private JPanel buildMergeComponent()
	{
		JComponent comp = mergeCanvas;
		if (dialogType == SPLIT_ROI) {
			JComponent c = drawingComponent.getDrawingView();
			Dimension d = mergeCanvas.getPreferredSize();
			c.setSize(d);
			c.setPreferredSize(d);
			mergeCanvas.setSize(d);
			pane.setPreferredSize(d);
			pane.setSize(d);
			pane.add(mergeCanvas, new Integer(0));
			pane.add(c, new Integer(1));
			comp = pane;
		}
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		Iterator<ChannelComponent> i = channelList.iterator();
		while (i.hasNext()) {
			buttonPanel.add(i.next());
			buttonPanel.add(Box.createHorizontalStrut(5));
		}
		JPanel mergePanel = new JPanel();
		mergePanel.setLayout(new BoxLayout(mergePanel, BoxLayout.Y_AXIS));
		mergePanel.add(UIUtilities.buildComponentPanel(new JLabel(MERGED_TEXT),
				0, 0));
		mergePanel.add(buttonPanel);
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(0, 0));
		p.add(UIUtilities.buildComponentPanelCenter(mergePanel, 0, 2), 
				BorderLayout.NORTH);
		p.add(UIUtilities.buildComponentPanelCenter(comp), 
				BorderLayout.CENTER);
		return p;
	}
	
	/** 
	 * Builds and lays out the controls for the movie figure.
	 * 
	 * @return See above.
	 */
	private JPanel buildMovieComponent()
	{
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridy = 0;
		c.gridx = 0;
        c.weightx = 0.0;  
        p.add(UIUtilities.setTextFont("Time-point frequency"), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        p.add(UIUtilities.buildComponentPanel(movieFrequency), c);  
        c.gridy++;
        c.gridx = 0;
        p.add(UIUtilities.setTextFont("Selected Time-points"), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c); 
        c.gridx++;
        p.add(movieSlider, c);
        c.gridy++;
        c.gridx = 0;
        p.add(UIUtilities.setTextFont("Time units"), c);
        c.gridx++;
        p.add(Box.createHorizontalStrut(5), c);
        c.gridx++;
        p.add(timesBox, c);
        
		controls.add(UIUtilities.buildComponentPanel(p));
		controls.add(buildDimensionComponent());
		//controls.add(p, "0, 2");
		return controls;
	}
	
	/**
	 * Builds the main component.
	 * 
	 * @return See above.
	 */
	private JPanel buildBody()
	{
		if (dialogType == THUMBNAILS) return buildThumbnailsPane();
			
		double[][] tl = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED,
			TableLayout.PREFERRED}}; //rows
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		p.setLayout(new TableLayout(tl));
		JXTaskPane pane = EditorUtil.createTaskPane("General");
		pane.setCollapsed(false);
		pane.add(buildTypeComponent());
		int i = 0;
		p.add(pane, "0, "+i);
		if (pixels.getSizeZ() > 1 && dialogType != SPLIT_ROI) {
			pane = EditorUtil.createTaskPane("Projection");
			pane.add(buildProjectionComponent());
			i++;
			p.add(pane, "0, "+i);
		}
		if (dialogType != MOVIE) {
			i++;
			channelsPane = EditorUtil.createTaskPane("Image");
			channelsPane.setCollapsed(false);
			channelsPane.add(buildDefaultPane());
			p.add(channelsPane, "0, "+i);
		}
		return p;
	}
	
	/**
	 * Returns the components for the thumbnails script.
	 * 
	 * @return See above.
	 */
	private JPanel buildThumbnailsPane()
	{
		thumbnailsPane = new JPanel();
		thumbnailsPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		double[][] tl = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED}}; //rows
		thumbnailsPane.setLayout(new TableLayout(tl));
		thumbnailsPane.add(buildTypeComponent(), "0, 0");
		return thumbnailsPane;
	}
	
	/**
	 * Builds the default component.
	 * 
	 * @return See above.
	 */
	private JPanel buildDefaultPane()
	{
		JPanel p = new JPanel();
		JXBusyLabel label = new JXBusyLabel();
		label.setBusy(true);
		p.add(label);
		return p;
	}
	
	/** Closes the dialog. */
	private void close()
	{
		option = CLOSE;
		firePropertyChange(CLOSE_FIGURE_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
		setVisible(false);
		dispose();
	}
	
	/**
	 * Collects the parameters.
	 * 
	 * @param p The value to fill.
	 */
	private void collectParam(FigureParam p)
	{
		p.setSelectedObjects(selectedObjects.isSelected());
		p.setWidth((Integer) widthField.getValueAsNumber());
		p.setHeight((Integer) heightField.getValueAsNumber());
		p.setSplitGrey(splitPanelGrey.isSelected());
		
		//scale bar
		int scale = -1;
		if (showScaleBar.isSelected()) {
			Number n = scaleBar.getValueAsNumber();
			if (n != null) scale = n.intValue();
		}
		p.setScaleBar(scale);
		int index = colorBox.getSelectedIndex();
		
		Map<Color, String> m = EditorUtil.COLORS_BAR;
		Iterator i = m.entrySet().iterator();
		int j = 0;
		Entry entry;
		Color c = null;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			if (j == index) c = (Color) entry.getKey();
			j++;
		}
		p.setColor(c);
		//projection
		if (projectionBox.isSelected()) {
			p.setZStart(zRange.getStartValue()-1);
			p.setZEnd(zRange.getEndValue()-1);
			p.setStepping((Integer) projectionFrequency.getValue());
			p.setProjectionType(
					projectionTypes.get(projectionTypesBox.getSelectedIndex()));
		} else {
			Integer n = (Integer) planeSelection.getValue()-1;
			p.setZStart(n);
			p.setZEnd(n);
			p.setStepping(1);
			p.setProjectionType(ProjectionParam.MAXIMUM_INTENSITY);
		}
	}
	
	/** Collects the parameters to create a figure. */
	private void saveSplitFigure()
	{
		Map<Integer, String> split = new LinkedHashMap<Integer, String>();
		FigureComponent comp;
		Entry entry;
		Iterator i = components.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			comp = (FigureComponent) entry.getValue();
			if (comp.isSelected()) {
				split.put((Integer) entry.getKey(), comp.getChannelLabel());
			}
		}
		Map<Integer, Color> merge = new LinkedHashMap<Integer, Color>();
		List<Integer> active = renderer.getActiveChannels();
		i = active.iterator();
		int index;
		while (i.hasNext()) {
			index = (Integer) i.next();
			merge.put(index, renderer.getChannelColor(index));
		}
		
		String name = nameField.getText().trim();
		int format = formats.getSelectedIndex();
		int label = rowName.getSelectedIndex();
		FigureParam p = new FigureParam(format, name, split, merge, label);
		
		collectParam(p);
		close();
		firePropertyChange(CREATE_FIGURE_PROPERTY, null, p);
	}
	
	/** Collects the parameters to create a ROI figure. */
	private void saveROIFigure()
	{
		Map<Integer, String> split = new LinkedHashMap<Integer, String>();	
		Map<Integer, Color> merge = new LinkedHashMap<Integer, Color>();
		List<Integer> active = renderer.getActiveChannels();
		Iterator i = active.iterator();
		int index;
		while (i.hasNext()) {
			index = (Integer) i.next();
			merge.put(index, renderer.getChannelColor(index));
			split.put(index, ""+index);
		}
		
		String name = nameField.getText().trim();
		int format = formats.getSelectedIndex();
		int label = rowName.getSelectedIndex();
		FigureParam p = new FigureParam(format, name, split, merge, label);
		p.setIndex(FigureParam.SPLIT_VIEW_ROI);
		collectParam(p);
		double zoom = 0;
		switch (zoomBox.getSelectedIndex()) {
			case ZOOM_100:
				zoom = 1;
				break;
			case ZOOM_200:
				zoom = 2;
				break;
			case ZOOM_300:
				zoom = 3;
				break;
			case ZOOM_400:
				zoom = 4;
				break;
			case ZOOM_500:
				zoom = 5;
		}
		p.setMagnificationFactor(zoom);
		
		close();
		firePropertyChange(CREATE_FIGURE_PROPERTY, null, p);
	}
	
	/** Saves the movie figure. */
	private void saveMovieFigure()
	{
		String name = nameField.getText().trim();
		int format = formats.getSelectedIndex();
		int label = rowName.getSelectedIndex();
		FigureParam p = new FigureParam(format, name, label);
		p.setIndex(FigureParam.MOVIE);
		p.setTime(timesBox.getSelectedIndex());
		p.setTimepoints(sorter.sort(movieSlider.getSelectedCells()));
		collectParam(p);
		close();
		firePropertyChange(CREATE_FIGURE_PROPERTY, null, p);
	}
	
	/** Saves the thumbnails figure. */
	private void saveThumbnailsFigure()
	{
		String name = nameField.getText().trim();
		int format = formats.getSelectedIndex();
		FigureParam p = new FigureParam(format, name);
		p.setIndex(FigureParam.THUMBNAILS);
		int width = 96;
		switch (sizeBox.getSelectedIndex()) {
			case SIZE_24:
				width = 24;
				break;
			case SIZE_32:
				width = 32;
				break;
			case SIZE_48:
				width = 48;
				break;
			case SIZE_64:
				width = 64;
				break;
			case SIZE_96:
				width = 96;
				break;
			case SIZE_128:
				width = 128;
				break;
			case SIZE_160:
				width = 160;
				break;
		}
		Number n = numberPerRow.getValueAsNumber();
		if (n != null && n instanceof Integer)
			p.setHeight((Integer) n);
		p.setWidth(width);
		
		p.setSelectedObjects(selectedObjects.isSelected());
		//retrieve the id of the selected tags
		if (arrangeByTags.isSelected() && selection != null 
				&& selection.size() > 0) { 
			Iterator<JCheckBox> i = selection.iterator();
			JCheckBox box;
			TagAnnotationData tag;
			List<Long> ids = new ArrayList<Long>();
			while (i.hasNext()) {
				box = i.next();
				tag = tagsSelection.get(box);
				ids.add(tag.getId());
			}
			p.setTags(ids);
		}
		close();
		firePropertyChange(CREATE_FIGURE_PROPERTY, null, p);
	}
	
	/** Collects the parameters to create a figure. */
	private void save()
	{
		switch (dialogType) {
			case SPLIT:
				saveSplitFigure();
				break;
			case SPLIT_ROI:
				saveROIFigure();
				break;
			case MOVIE:
				saveMovieFigure();
				break;
			case THUMBNAILS:
				saveThumbnailsFigure();
				break;
		}
	}
	
	/** 
	 * Sets the enabled flag of the {@link #saveButton} depending on
	 * the value to the name field.
	 */
	private void handleText()
	{
		String text = nameField.getText();
		saveButton.setEnabled(!(text == null || text.trim().length() == 0));
	}
	
	/** 
	 * Handles the changes in the image dimension. 
	 * 
	 * @param field The modified numerical field.
	 */
	private void handleDimensionChange(NumericalTextField field)
	{
		Integer n = (Integer) field.getValueAsNumber();
		if (n == null) return;
		Document doc;
		int v;
		if (field == widthField) {
			v = (int) ((n*renderer.getPixelsDimensionsY())/
					renderer.getPixelsDimensionsX());
			doc = heightField.getDocument();
			doc.removeDocumentListener(this);
			heightField.setText(""+v);
			doc.addDocumentListener(this);
		} else {
			v = (int) ((n*renderer.getPixelsDimensionsX())/
					renderer.getPixelsDimensionsY());
			doc = widthField.getDocument();
			doc.removeDocumentListener(this);
			widthField.setText(""+v);
			doc.addDocumentListener(this);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param name  The default name for the file.
	 * @param pixels The pixels object of reference.
	 * @param index One of the constants defined by this class.
	 * @param type  The type of objects to handle.
	 */
	public FigureDialog(JFrame owner, String name, PixelsData pixels,
			int index, Class type)
	{
		super(owner, true);
		this.type = type;
		this.pixels = pixels;
		this.dialogType = index;
		initComponents(name);
		buildGUI();
		setSize(500, 700);
	}
	
	/**
	 * Creates and returns a greyScale image with only the selected channel
	 * turned on.
	 * 
	 * @param channel The index of the channel.
	 * @return See above.
	 */
	BufferedImage createSingleGreyScaleImage(int channel)
	{
		return scaleImage(renderer.createSingleChannelImage(false, channel, 
				pDef));
	}
	
	/**
	 * Sets the renderer.
	 * 
	 * @param renderer 	Reference to the renderer.
	 */
	public void setRenderer(Renderer renderer)
	{
		this.renderer = renderer;
		channelsPane.removeAll();
		switch (dialogType) {
			case SPLIT:
			case SPLIT_ROI:
				initChannelComponents();
				channelsPane.add(buildChannelsComponent());
				break;
		}
		saveButton.setEnabled(true);
		pack();
	}
	
	/**
	 * Sets the collections of tags.
	 * 
	 * @param tags The values to set.
	 */
	public void setTags(Collection tags)
	{
		if (tags == null || tags.size() == 0) return;
		if (thumbnailsPane == null) return;
		JPanel p = new JPanel();
		double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
				{40, TableLayout.PREFERRED}}; //rows
		p.setLayout(new TableLayout(tl));
		tagsSelection = new LinkedHashMap<JCheckBox, TagAnnotationData>();
		List l = sorter.sort(tags);
		Iterator i = l.iterator();
		TagAnnotationData tag;
		JCheckBox box;
		JPanel tagPane = new JPanel();
		tagPane.setLayout(new BoxLayout(tagPane, BoxLayout.Y_AXIS));
		ActionListener listener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				layoutSelectedTags((JCheckBox) e.getSource());
			}
		};
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			box = new JCheckBox(tag.getTagValue());
			box.setEnabled(false);
			box.addActionListener(listener);
			tagsSelection.put(box, tag);
			tagPane.add(box);
		}
		selectedTags = new JPanel();
		selectedTags.setLayout(new BoxLayout(selectedTags, BoxLayout.Y_AXIS));
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(UIUtilities.setTextFont("Arrange by Tags"));
		controls.add(UIUtilities.buildComponentPanel(arrangeByTags));
		p.add(controls, "0, 0, LEFT, TOP");
		p.add(selectedTags, "0, 1, LEFT, TOP");
		JScrollPane pane = new JScrollPane(tagPane);
		Dimension d = pane.getPreferredSize();
		pane.setPreferredSize(new Dimension(d.width, MAX_HEIGHT));
		p.add(pane, "1, 0, 1, 1");
		thumbnailsPane.add(p, "0, 1");
		thumbnailsPane.revalidate();
		thumbnailsPane.repaint();
	}
	
	/**
	 * Sets the collection of ROIs related to the primary select.
	 * 
	 * @param rois The value to set.
	 */
	public void setROIs(Collection rois)
	{
		if (rois == null) return;
		drawingComponent = new DrawingComponent();
		drawingComponent.getDrawingView().setScaleFactor(1.0);
		roiComponent = new ROIComponent();
		Iterator r = rois.iterator();
		ROIResult result;
		try {
			while (r.hasNext()) {
				result = (ROIResult) r.next();
				roiComponent.loadROI(result.getROIs(), true);
			}
		} catch (Exception e) {}
	}
	
    /**
     * Centers and shows the dialog. Returns the option selected by the user. 
     * 
     * @return The option selected by the user. 
     */
    public int centerDialog()
    {
    	UIUtilities.centerAndShow(this);
    	return option;	
    }
    
	/**
	 * Closes or creates a figure.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CLOSE:
				close();
				break;
			case SAVE:
				save();
				break;
			case SCALE_BAR:
				scaleBar.setEnabled(showScaleBar.isSelected());
				break;
			case ARRANGE_BY_TAGS:
				boolean b = arrangeByTags.isSelected();
				Iterator<JCheckBox> i = tagsSelection.keySet().iterator();
				while (i.hasNext())
					i.next().setEnabled(b);
				break;
			case PROJECTION:
				setProjectionSelected(projectionBox.isSelected());
		}
	}

	/**
	 * Sets the <code>enabled</code> flag of the controls.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{ 
		Document doc = e.getDocument();
		if (doc == nameField.getDocument())
			handleText(); 
		else if (doc == widthField.getDocument()) 
			handleDimensionChange(widthField);
		else handleDimensionChange(heightField);
	}

	/**
	 * Sets the <code>enabled</code> flag of the controls.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{ 
		Document doc = e.getDocument();
		if (doc == nameField.getDocument())
			handleText(); 
		else if (doc == widthField.getDocument()) 
			handleDimensionChange(widthField);
		else handleDimensionChange(heightField);
	}
	
	/**
	 * Listens to channel button selection
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ChannelButton.CHANNEL_SELECTED_PROPERTY.equals(name)) {
			//mergeCanvas.setImage(getMergedImage());
			Map map = (Map) evt.getNewValue();
			if (map == null) return;
			if (map.size() != 1) return;
			Set set = map.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Integer index;
			ChannelButton obj = (ChannelButton) evt.getSource();
			while (i.hasNext()) {
				entry = (Entry) i.next();
				index = (Integer) entry.getKey();
				setChannelSelection(index.intValue(), 
						((Boolean) entry.getValue()));
			}
		} else if (ChannelComponent.CHANNEL_SELECTION_PROPERTY.equals(name)) {
			ChannelComponent c = (ChannelComponent) evt.getNewValue();
			setChannelSelection(c.getChannelIndex(), c.isActive());
		}
	}
	
	/**
	 * Reacts to change in the type of split i.e. either grey or color.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		Object src = e.getSource();
		switch (dialogType) {
			case SPLIT:
			case SPLIT_ROI:
				if (src == splitPanelGrey) {
					boolean grey = splitPanelGrey.isSelected();
					if (components == null) return;
					Iterator<Integer> i = components.keySet().iterator();
					FigureComponent comp;
					List active = renderer.getActiveChannels();
					Integer index;
					while (i.hasNext()) {
						index = i.next();
						comp = components.get(index);
						if (grey) comp.resetImage(grey);
						else {
							if (active.contains(index)) comp.resetImage(grey);
							else comp.resetImage(!grey);
						}
					}
				}
				
			break;
			case MOVIE:
				if (src == movieFrequency) {
					Integer value = (Integer) movieFrequency.getValue();
					movieSlider.selectCells(value);
				}
				
				
				break;
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-operation
	 * implementation in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}


}
