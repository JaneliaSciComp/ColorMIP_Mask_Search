import ij.*;
import ij.io.*;
import ij.plugin.filter.*;
import ij.plugin.PlugIn;
import ij.process.*;
import ij.gui.*;
import java.math.*;
import java.io.*;
import java.util.Arrays;
import java.net.*;
import ij.Macro.*;
import java.awt.*;
//import ij.macro.*;
import ij.gui.GenericDialog.*;
import java.util.*;
import java.lang.*;



public class Slice_Label_Search implements PlugInFilter
{
	ImageProcessor ip1;
	ImagePlus imp,newimp;
	String Label,SearchWord;
	
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL;
	}
	
//	private boolean showDialog() {

		
//		return true;
//	}
	
	public void run(ImageProcessor ip1){
		imp = WindowManager.getCurrentImage();
		
	//	String dir=IJ.getDirectory("Choose folder for stacks");
		
		GenericDialog gd = new GenericDialog("Slice Label Search");
		gd.addStringField("Search word; ", "", 20);
		
		gd.showDialog();
		if(gd.wasCanceled()){
			return;
		}
		SearchWord =gd.getNextString();
		ImageStack st1 = imp.getStack();
		
		int width = imp.getWidth();
		int height = imp.getHeight();
		int slicenumber = imp.getStackSize();
		
		IJ.register (Slice_Label_Search.class);
		if (IJ.versionLessThan("1.32c")){
			IJ.showMessage("Error", "Please Update ImageJ.");
			return;
		}
		int[] wList = WindowManager.getIDList();
		if (wList==null) {
			IJ.error("No images are open.");
			return;
		}
		
		ImageStack dcStack = new ImageStack (width,height);
		
		if (slicenumber > 1) {
			for(int SliceScan=1; SliceScan<=slicenumber; SliceScan++){
				IJ.showProgress((double)SliceScan / (double)slicenumber);
				
				Label = st1.getSliceLabel(SliceScan);
				//IJ.log("Label; "+Label+"   SearchWord; "+SearchWord);
				int Check= Label != null ? (Label.indexOf(SearchWord)) : -1;
				
				if(Check!=-1){
					ip1 = st1.getProcessor(SliceScan); //Mask
					dcStack.addSlice(Label, ip1);
				}
			}
		} else {
			int Check= imp.getTitle().indexOf(SearchWord);
				
			if(Check!=-1){
				ip1 = imp.getProcessor();
				dcStack.addSlice(Label, ip1);
			}
		}

		int posislice2=dcStack.size();
		newimp = new ImagePlus(posislice2+" Search_results of "+SearchWord, dcStack);
		
		newimp.show();
		
	}
}