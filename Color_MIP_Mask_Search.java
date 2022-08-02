import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
<<<<<<< HEAD
<<<<<<< HEAD
import java.util.HashSet;
=======
>>>>>>> parent of f1d5276 (horizontal image)
=======
>>>>>>> parent of f1d5276 (horizontal image)
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ListIterator; 
import java.util.Iterator; 

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.awt.*;

import javax.imageio.ImageIO; 
import javax.imageio.ImageReader; 
import javax.imageio.stream.ImageInputStream; 
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.ImageTypeSpecifier;

import ij.*;
import ij.gui.*;
import ij.io.*;

import ij.plugin.filter.*;
import ij.process.*;
import ij.process.ImageProcessor;
import ij.measure.ResultsTable;

public class Color_MIP_Mask_Search implements PlugInFilter
{
	ImagePlus imp, imp2;
	ImageProcessor ip1, nip1, ip2, ip3, ip4, ip5, ip6, ip33;
	int pix1=0, CheckPost,UniqueLineName=0,IsPosi;
	int pix3=0,Check=0,arrayPosition=0,dupdel=1,FinalAdded=1,enddup=0;
	ImagePlus newimp, newimpOri;
	String linename,LineNo, LineNo2,preLineNo="A",FullName,LineName,arrayName,PostName;
	String args [] = new String[10],PreFullLineName,ScorePre,TopShortLinename;
	
	ExecutorService m_executor;
	
	boolean DUPlogon;
	
	public class SearchResult{
		String m_name;
		int m_sid;
		long m_offset;
		int m_strip;
		byte[] m_pixels;
		byte[] m_colocs;
		ImageProcessor m_iporg;
		ImageProcessor m_ipcol;
		SearchResult(String name, int sid, long offset, int strip, byte[] pxs, byte[] coloc, ImageProcessor iporg, ImageProcessor ipcol){
			m_name = name;
			m_sid = sid;
			m_offset = offset;
			m_strip = strip;
			m_pixels = pxs;
			m_colocs = coloc;
			m_iporg = iporg;
			m_ipcol = ipcol;
		}
	}
	
	class ByteVector {
		public byte[] data;
		private int size;
		
		public ByteVector() {
			data = new byte[10];
			size = 0;
		}
		
		public ByteVector(int initialSize) {
			data = new byte[initialSize];
			size = 0;
		}
		
		public ByteVector(byte[] byteBuffer) {
			data = byteBuffer;
			size = 0;
		}
		
		public void add(byte x) {
			if (size>=data.length) {
				doubleCapacity();
				add(x);
			} else
			data[size++] = x;
		}
		
		public int size() {
			return size;
		}
		
		public void add(byte[] array) {
			int length = array.length;
			while (data.length-size<length)
			doubleCapacity();
			System.arraycopy(array, 0, data, size, length);
			size += length;
		}
		
		void doubleCapacity() {
			byte[] tmp = new byte[data.length*2 + 1];
			System.arraycopy(data, 0, tmp, 0, data.length);
			data = tmp;
		}
		
		public void clear() {
			size = 0;
		}
		
		public byte[] toByteArray() {
			byte[] bytes = new byte[size];
			System.arraycopy(data, 0, bytes, 0, size);
			return bytes;
		}
	}
	
	public int packBitsUncompress(byte[] input, byte[] output, int offset, int expected) {
		if (expected==0) expected = Integer.MAX_VALUE;
		int index = 0;
		int pos = offset;
		while (pos < expected && pos < output.length && index < input.length) {
			byte n = input[index++];
			if (n>=0) { // 0 <= n <= 127
				byte[] b = new byte[n+1];
				for (int i=0; i<n+1; i++)
				b[i] = input[index++];
				System.arraycopy(b, 0, output, pos, b.length);
				pos += (int)b.length;
				b = null;
			} else if (n != -128) { // -127 <= n <= -1
				int len = -n + 1;
				byte inp = input[index++];
				for (int i=0; i<len; i++) output[pos++] = inp;
			}
		}
		return pos;
	}
	
	public int setup(String arg, ImagePlus imp)
	{
		IJ.register (Color_MIP_Mask_Search.class);
		if (IJ.versionLessThan("1.32c")){
			IJ.showMessage("Error", "Please Update ImageJ.");
			return 0;
		}
		
		//	IJ.log(" wList;"+String.valueOf(wList));
		
		this.imp = imp;
		if(imp.getType()!=ImagePlus.COLOR_RGB){
			IJ.showMessage("Error", "Plugin requires RGB image");
			return 0;
		}
		return DOES_RGB;
		
		//	IJ.log(" noisemethod;"+String.valueOf(ff));
	}
	
	public String getZeroFilledNumString(int num, int digit) {
		String stri = Integer.toString(num);
		if (stri.length() < digit) {
			String zeros = "";
			for (int i = digit - stri.length(); i > 0; i--)
			zeros += "0";
			stri = zeros + stri;
		}
		return stri;
	}
	
	public String getZeroFilledNumString(double num, int decimal_len, int fraction_len) {
		String strd = String.format("%."+fraction_len+"f", num);
		int decdigit = strd.length()-(fraction_len+1);
		if (decdigit < decimal_len) {
			String zeros = "";
			for (int i = decimal_len - decdigit; i > 0; i--)
			zeros += "0";
			strd = zeros + strd;
		}
		return strd;
	}
	
	public void run(ImageProcessor ip){
		
		int wList [] = WindowManager.getIDList();
		if (wList==null || wList.length<2) {
			IJ.showMessage("There should be at least two windows open");
			return;
		}
		int imageno = 0; int SingleSliceMIPnum=0; int MultiSliceStack=0;
		String titles [] = new String[wList.length];
		int slices [] = new int[wList.length];
		
		for (int i=0; i<wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp!=null){
				titles[i] = imp.getTitle();//Mask.tif and Data.tif
				slices[i] = imp.getStackSize();
				
				if(slices[i]>1){
					titles[i] = titles[i]+"  ("+slices[i]+") slices";
					MultiSliceStack = i;
				}else{
					titles[i] = titles[i]+"  ("+slices[i]+") slice";
					SingleSliceMIPnum = i;
				}
				imageno = imageno +1;
			}else
			titles[i] = "";
		}
		
		String[] negtitles = new String[titles.length+1];
		negtitles[0] = "none";
		System.arraycopy(titles, 0, negtitles, 1, titles.length);
		
		/////Dialog//////////////////////////////////////////////		
		int Mask=(int)Prefs.get("Mask.int",0);
		boolean mirror_mask=(boolean)Prefs.get("mirror_mask.boolean",false);
		int NegMask=(int)Prefs.get("NegMask.int",0);
		boolean mirror_negmask=(boolean)Prefs.get("mirror_negmask.boolean",false);
		int datafile=(int)Prefs.get("datafile.int",1);
		int Thres=(int)Prefs.get("Thres.int",100);
		double pixThres=(double)Prefs.get("pixThres.double",10);
		int dupline=(int)Prefs.get("dupline.int",2);
		int colormethod=(int)Prefs.get("colormethod.int",1);
		double pixflu=(double)Prefs.get("pixflu.double",2);
		int xyshift=(int)Prefs.get("xyshift.int",0);
		boolean logon=(boolean)Prefs.get("logon.boolean",false);
		int Thresm=(int)Prefs.get("Thresm.int",50);
		int NegThresm=(int)Prefs.get("NegThresm.int",50);
		boolean logNan=(boolean)Prefs.get("logNan.boolean",false);
		int labelmethod=(int)Prefs.get("labelmethod.int",0);
		boolean DUPlogon=(boolean)Prefs.get("DUPlogon.boolean",false);
		boolean GCON=(boolean)Prefs.get("GCON.boolean",false);
		boolean ShowCo=(boolean)Prefs.get("ShowCo.boolean",false);
		int NumberSTint=(int)Prefs.get("NumberSTint.int",0);
		int threadNum=(int)Prefs.get("threadNum.int",8);
		int maxnumberN=(int)Prefs.get("maxnumberN.int",200);
		
		//	boolean EMsearch=(boolean)Prefs.get("EMsearch.boolean",false);
		
		if(datafile >= imageno){
			int singleslice=0; int Maxsingleslice=0; int MaxStack=0;
			
			for(int isliceSearch=0; isliceSearch<wList.length; isliceSearch++){
				singleslice=slices[isliceSearch];
				
				if(singleslice>Maxsingleslice){
					Maxsingleslice=singleslice;
					MaxStack=isliceSearch;
				}
			}
			datafile=MaxStack;
		}
		
		if(Mask >= imageno){
			int singleslice=0; int isliceSearch=0;
			
			while(singleslice!=1){
				singleslice=slices[isliceSearch];
				isliceSearch=isliceSearch+1;
				
				if(isliceSearch>imageno){
					IJ.showMessage("Need to be a single slice open");
					return;
				}
			}
			Mask=isliceSearch-1;
		}
		
		if(NegMask >= imageno+1)
		NegMask = 0;
		
		
		ImagePlus impMask = WindowManager.getImage(wList[Mask]);
		int MaskSliceNum = impMask.getStackSize();
		
		ImagePlus impData = WindowManager.getImage(wList[datafile]);
		int DataSliceNum = impData.getStackSize();
		
		if(MaskSliceNum!=1){
			Mask=SingleSliceMIPnum;
		}
		
		
		if(DataSliceNum==1){
			datafile=MultiSliceStack;
		}
		
		
		//	IJ.log("mask; "+String.valueOf(Mask)+"datafile; "+String.valueOf(datafile)+"imageno; "+String.valueOf(imageno)+"wList.length; "+String.valueOf(wList.length));
		if(labelmethod>1)
		labelmethod=1;
		
		GenericDialog gd = new GenericDialog("ColorMIP_3D_Mask search");
<<<<<<< HEAD
<<<<<<< HEAD
		gd.setInsets(0, 340, 0);
		gd.addCheckbox("Horizontal ", horizontal); //Horizontal
		gd.setInsets(5, 0, 0);
		
=======
>>>>>>> parent of f1d5276 (horizontal image)
=======
>>>>>>> parent of f1d5276 (horizontal image)
		gd.addChoice("Mask", titles, titles[Mask]); //Mask
		gd.addSlider("1.Threshold for mask", 0, 255, Thresm);
		gd.setInsets(0, 340, 0);
		gd.addCheckbox("1.Add mirror search", mirror_mask);
		
		gd.setInsets(20, 0, 0);
		gd.addChoice("Negative Mask", negtitles, negtitles[NegMask]); //Negative Mask
		gd.addSlider("2.Threshold for negative mask", 0, 255, NegThresm);
		gd.setInsets(0, 340, 0);
		gd.addCheckbox("2.Add mirror search", mirror_negmask);
		
		gd.setInsets(20, 0, 0);
		gd.addChoice("Data for the ColorMIP", titles, titles[datafile]); //Data
		
		//gd.addNumericField("Threshold", slicenumber,0);
		gd.addSlider("3.Threshold for data", 0, 255, Thres);
		gd.addMessage("");
		//	gd.addSlider("100x % of Positive PX Threshold", (double) 0, (double) 10000, pixThres);
		gd.addNumericField("Positive PX Threshold 0-100 %", pixThres, 2);
		gd.addSlider("Pix Color Fluctuation, +- Z slice", 0, 20, pixflu);
		
		gd.setInsets(20, 0, 0);
		gd.addNumericField("Max number of the hits", maxnumberN, 0);
		
		String[] dupnumstr = new String[11];
		for (int i = 0; i < dupnumstr.length; i++)
		dupnumstr[i] = Integer.toString(i);
		gd.addChoice("Duplicated line numbers; (only for R & VT), 0 = no check", dupnumstr, dupnumstr[dupline]); //Mask
		
		gd.setInsets(20, 0, 0);
		gd.addNumericField("Thread", threadNum, 0);
		
		//gd.setInsets(0, 372, 0);
		//gd.addCheckbox("Show Duplication log",DUPlogon);
		
		//gd.setInsets(0, 362, 5);
		//String []	ColorDis = {"Combine", "Two windows"};
		//gd.addRadioButtonGroup("Result windows", ColorDis, 1, 2, ColorDis[colormethod]);
		
		//gd.setInsets(0, 362, 5);
		//String []	labelmethodST = {"overlap value", "overlap value + line name"};
		//gd.addRadioButtonGroup("Slice sorting method; ", labelmethodST, 1, 2, labelmethodST[labelmethod]);
		
		gd.setInsets(0, 362, 5);
		String []	shitstr = {"0px    ", "2px    ", "4px    "};
		gd.addRadioButtonGroup("XY Shift: ", shitstr, 1, 3, shitstr[xyshift/2]);
		
		gd.setInsets(0, 362, 5);
		String []	NumberST = {"%", "absolute value"};
		gd.addRadioButtonGroup("Scoring method; ", NumberST, 1, 2, NumberST[NumberSTint]);
		
		gd.setInsets(20, 372, 0);
		gd.addCheckbox("Show log",logon);
		
		gd.setInsets(20, 372, 0);
		gd.addCheckbox("Clear memory before search. Slow at beginning but fast search",GCON);
		
		gd.setInsets(20, 372, 0);
		gd.addCheckbox("Co-localized stack shown (more memory needs)",ShowCo);
		
		//	gd.setInsets(20, 372, 0);
		//	gd.addCheckbox("EM search &sorting",EMsearch);
		
		gd.showDialog();
		if(gd.wasCanceled()){
			return;
		}
		
		Mask = gd.getNextChoiceIndex(); //Mask
		Thresm=(int)gd.getNextNumber();
		mirror_mask = gd.getNextBoolean();
		NegMask = gd.getNextChoiceIndex(); //Negative Mask
		NegThresm=(int)gd.getNextNumber();
		mirror_negmask = gd.getNextBoolean();
		datafile = gd.getNextChoiceIndex(); //Color MIP
		Thres=(int)gd.getNextNumber();
		pixThres=(double)gd.getNextNumber();
		pixflu=(double)gd.getNextNumber();
		maxnumberN=(int)gd.getNextNumber();
		dupline=Integer.parseInt(dupnumstr[gd.getNextChoiceIndex()]);
		DUPlogon = false;
		threadNum = (int)gd.getNextNumber();
		xyshift=Integer.parseInt( ((String)gd.getNextRadioButton()).substring(0,1) );
		
		String thremethodSTR="Two windows";
		String labelmethodSTR="overlap value + line name";
		String ScoringM=(String)gd.getNextRadioButton();
		logon = gd.getNextBoolean();
		GCON = gd.getNextBoolean();
		ShowCo = gd.getNextBoolean();
		//	EMsearch = gd.getNextBoolean();
		
		if(GCON==true)
		System.gc();
		
		int tz=0;
		
		if(logon==true && tz==1){
			GenericDialog gd2 = new GenericDialog("log option");
			gd2.addCheckbox("ShowNaN",logNan);
			
			gd2.showDialog();
			if(gd2.wasCanceled()){
				return;
			}
			String logmethodSTR=(String)gd2.getNextRadioButton();
			logNan = gd2.getNextBoolean();
			Prefs.set("logNan.boolean",logNan);
		}//if(logon==true){
		
		colormethod=1;
		if(thremethodSTR=="Combine")
		colormethod=0;
		
		
		if(labelmethodSTR=="overlap value")
		labelmethod=0;//on top
		if(labelmethodSTR=="overlap value + line name")
		labelmethod=1;
		
		if(ScoringM=="%")
		NumberSTint=0;
		else
		NumberSTint=1;
		
		Prefs.set("Mask.int", Mask);
		Prefs.set("mirror_mask.boolean", mirror_mask);
		Prefs.set("mirror_negmask.boolean", mirror_negmask);
		Prefs.set("Thresm.int", Thresm);
		Prefs.set("NegMask.int", NegMask);
		Prefs.set("NegThresm.int", NegThresm);
		Prefs.set("pixThres.double", pixThres);
		Prefs.set("Thres.int", Thres);
		Prefs.set("datafile.int",datafile);
		Prefs.set("colormethod.int",colormethod);
		Prefs.set("pixflu.double", pixflu);
		Prefs.set("xyshift.int",xyshift);
		Prefs.set("logon.boolean",logon);
		Prefs.set("labelmethod.int",labelmethod);
		Prefs.set("DUPlogon.boolean",DUPlogon);
		Prefs.set("dupline.int",dupline);
		Prefs.set("GCON.boolean",GCON);
		Prefs.set("ShowCo.boolean",ShowCo);
		Prefs.set("NumberSTint.int",NumberSTint);
		Prefs.set("threadNum.int",threadNum);
		//	Prefs.set("EMsearch.boolean",EMsearch);
		Prefs.set("maxnumberN.int",maxnumberN);
		
		IJ.log("dupline; "+dupline+"  pixThres; "+pixThres+"  pixflu; "+pixflu);
		double pixfludub=pixflu/100;
		//	IJ.log(" pixfludub;"+String.valueOf(pixfludub));
		
		final double pixThresdub = pixThres/100;///10000
		//	IJ.log(" pixThresdub;"+String.valueOf(pixThresdub));
		///////		
		ImagePlus imask = WindowManager.getImage(wList[Mask]); //Mask
		ImagePlus inegmask = NegMask > 0 ? WindowManager.getImage(wList[NegMask-1]) : null; //Negative Mask
		titles[Mask] = imask.getTitle();
		if (inegmask != null) negtitles[NegMask] = inegmask.getTitle();
		ImagePlus idata = WindowManager.getImage(wList[datafile]); //Data
		
		ip1 = imask.getProcessor(); //Mask
		nip1 = NegMask > 0 ? inegmask.getProcessor() : null; //Negative Mask
		int slicenumber = idata.getStackSize();
		
		int width = imask.getWidth();
		int height = imask.getHeight();
		
		int widthD = idata.getWidth();
		int heightD = idata.getHeight();
		
		if(width!=widthD){
			IJ.showMessage ("Image size is different between the mask and data!  mask width; "+width+" px   data width; "+widthD+" px");
			IJ.log("Image size is different between the mask and data!");
			return;
		}
		
		if(height!=heightD){
			IJ.showMessage ("Image size is different between the mask and data!  mask height; "+height+" px   data height; "+heightD+" px");
			IJ.log("Image size is different between the mask and data!");
			return;
		}
		
<<<<<<< HEAD
<<<<<<< HEAD
		if (horizontal)
		{
			for(int ix=950; ix<width; ix++){// deleting color scale from mask
				for(int iy=335-85; iy<335; iy++){
					ip1.set(ix,iy,-16777216);
				}
				for(int iy=670-85; iy<670; iy++){
					ip1.set(ix,iy,-16777216);
				}
			}
		}
		else
		{
			for(int ix=950; ix<width; ix++){// deleting color scale from mask
				for(int iy=0; iy<85; iy++){
					ip1.set(ix,iy,-16777216);
				}
=======
		for(int ix=950; ix<width; ix++){// deleting color scale from mask
			for(int iy=0; iy<85; iy++){
				ip1.set(ix,iy,-16777216);
>>>>>>> parent of f1d5276 (horizontal image)
=======
		for(int ix=950; ix<width; ix++){// deleting color scale from mask
			for(int iy=0; iy<85; iy++){
				ip1.set(ix,iy,-16777216);
>>>>>>> parent of f1d5276 (horizontal image)
			}
		}
		
		if(IJ.escapePressed())
		return;
		
		
		IJ.showProgress(0.0);
		
		//	IJ.log("maxvalue; "+maxvalue2+"	 gap;	"+gap);
		
		
		final ImageStack st3 = idata.getStack();
		int posislice = 0;
		
		double posipersent2 = 0;
		double pixThresdub2 = 0;
		
		final ColorMIPMaskCompare cc = new ColorMIPMaskCompare (ip1, Thresm, mirror_mask, nip1, NegThresm, mirror_negmask, Thres, pixfludub, xyshift);
		m_executor = Executors.newFixedThreadPool(threadNum);
		
		final int maskpos_st = cc.getMaskStartPos()*3;
		final int maskpos_ed = cc.getMaskEndPos()*3;
		final int stripsize = maskpos_ed-maskpos_st+3;
		
		long start, end;
		start = System.currentTimeMillis();
		
		IJ.log("  st3.isVirtual(); "+st3.isVirtual());
		String fileformat="";
		if(st3.isVirtual()){
			VirtualStack vst = (VirtualStack)st3;
			String datapath = null;
			if (vst.getDirectory() == null) {
				FileInfo fi = idata.getOriginalFileInfo();
				if (fi.directory.length()>0 && !(fi.directory.endsWith(Prefs.separator)||fi.directory.endsWith("/")))
				fi.directory += Prefs.separator;
				datapath = fi.directory + fi.fileName;
			} else {
				String dirtmp = vst.getDirectory();
				if (dirtmp.length()>0 && !(dirtmp.endsWith(Prefs.separator)||dirtmp.endsWith("/")))
				dirtmp += Prefs.separator;
				String directory = dirtmp;
				datapath = directory + vst.getFileName(3);
			}
			IJ.log("426 datapath; "+datapath);
			
			FileInfo fi0 = idata.getOriginalFileInfo();
			
			if(fi0.compression==5)
			fileformat="tif PackBits";
			
			if(fi0.compression==1){
				int tifextindex=datapath.lastIndexOf(".tif");
				int pngextindex=datapath.lastIndexOf(".png");
				
				if(pngextindex!=-1 && tifextindex==-1){
					fileformat="png";
				}else{
					fileformat="tif none";
				}
			}
			
			IJ.log("compression; "+fileformat);
		}
		
		ArrayList<String> srlabels = new ArrayList<String>();
		ArrayList<String> finallbs = new ArrayList<String>();
		HashMap<String, SearchResult> srdict = new HashMap<String, SearchResult>(1000);
		
		final int fslicenum = slicenumber;
		final int fthreadnum = threadNum;
		final boolean fShowCo = ShowCo;
		final boolean flogon = logon;
		final boolean flogNan = logNan;
		final int fNumberSTint = NumberSTint;
		final int flabelmethod = labelmethod;
		final boolean isPackbits = fileformat.equals("tif PackBits");
		if(fileformat.equals("tif none") || fileformat.equals("tif PackBits")){
			if (st3.isVirtual()) {
				final VirtualStack vst = (VirtualStack)st3;
				if (vst.getDirectory() == null) {
					IJ.log("Virtual Stack (tif none)");
					
					if (fileformat.equals("tif none")) {
						final FileInfo fi = idata.getOriginalFileInfo();
						if (fi.directory.length()>0 && !(fi.directory.endsWith(Prefs.separator)||fi.directory.endsWith("/")))
						fi.directory += Prefs.separator;
						final String datapath = fi.directory + fi.fileName;
						final long size = fi.width*fi.height*fi.getBytesPerPixel();
						
						final List<Callable<ArrayList<SearchResult>>> tasks = new ArrayList<Callable<ArrayList<SearchResult>>>();
						for (int ithread = 0; ithread < threadNum; ithread++) {
							final int ftid = ithread;
							final int f_th_snum = fslicenum/fthreadnum;
							tasks.add(new Callable<ArrayList<SearchResult>>() {
									public ArrayList<SearchResult> call() {
										ArrayList<SearchResult> out = new ArrayList<SearchResult>();
										RandomAccessFile f = null;
										try {
											f = new RandomAccessFile(datapath, "r");
											for (int slice = fslicenum/fthreadnum*ftid+1, count = 0; slice <= fslicenum && count < f_th_snum; slice++, count++) {
												if( IJ.escapePressed() )
												break;	
												byte [] impxs = new byte[(int)size];
												byte [] colocs = null;
												if (fShowCo) colocs = new byte[(int)size];
												
												if (ftid == 0)
												IJ.showProgress((double)slice/(double)f_th_snum);
												
												long loffset = fi.getOffset() + (slice-1)*(size+fi.gapBetweenImages) + maskpos_st;
												f.seek(loffset);
												f.read(impxs, maskpos_st, stripsize);
												
												linename=st3.getSliceLabel(slice);
												
												ColorMIPMaskCompare.Output res = cc.runSearch(impxs, colocs);
												
												int posi = res.matchingPixNum;
												double posipersent = res.matchingPct;
												
												if(posipersent<=pixThresdub){
													if (flogon==true && flogNan==true)
													IJ.log("NaN");
												}else if(posipersent>pixThresdub){
													loffset = fi.getOffset() + (slice-1)*(size+fi.gapBetweenImages);
													
													double posipersent3=posipersent*100;
													double pixThresdub3=pixThresdub*100;
													
													posipersent3 = posipersent3*100;
													posipersent3 = Math.round(posipersent3);
													double posipersent2 = posipersent3 /100;
													
													pixThresdub3 = pixThresdub3*100;
													pixThresdub3 = Math.round(pixThresdub3);
													
													if(flogon==true && flogNan==true)// sort by name
													IJ.log("Positive linename; 	"+linename+" 	"+String.valueOf(posipersent2));
													
													String title="";
													if(fNumberSTint==0){
														String numstr = getZeroFilledNumString(posipersent2, 3, 2);
														title = (flabelmethod==0 || flabelmethod==1) ? numstr+"_"+linename : linename+"_"+numstr;
													}
													else if(fNumberSTint==1){
														String posiST=getZeroFilledNumString(posi, 4);
														title = (flabelmethod==0 || flabelmethod==1) ? posiST+"_"+linename : linename+"_"+posiST;
													}
													out.add(new SearchResult(title, slice, loffset, 0, impxs, colocs, null, null));
													if (ftid == 0)
													IJ.showStatus("Number of Hits (estimated): "+out.size()*fthreadnum);
												}
											}
											f.close();
										} catch (IOException e) {
											e.printStackTrace();
										} finally {
											try { if (f != null) f.close(); }
											catch  (IOException e) { e.printStackTrace(); }
										}
										return out;
									}
							});
						}
						try {
							List<Future<ArrayList<SearchResult>>> taskResults = m_executor.invokeAll(tasks);
							for (Future<ArrayList<SearchResult>> future : taskResults) {
								for (SearchResult r : future.get()) {
									srlabels.add(r.m_name);
									srdict.put(r.m_name, r);
									
									posislice=posislice+1;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (fileformat.equals("tif PackBits")) {
						final FileInfo fi = idata.getOriginalFileInfo();
						if (fi.directory.length()>0 && !(fi.directory.endsWith(Prefs.separator)||fi.directory.endsWith("/")))
						fi.directory += Prefs.separator;
						final String datapath = fi.directory + fi.fileName;
						final long size = fi.width*fi.height*fi.getBytesPerPixel();
						
						try {
							
							final TiffDecoder tfd = new TiffDecoder(fi.directory, fi.fileName);
							final FileInfo[] fi_list = tfd.getTiffInfo();
							IJ.log(fi_list[0].toString()+"virtual stack PackBits"+"  flogon; "+flogon);
							
							final List<Callable<ArrayList<SearchResult>>> tasks = new ArrayList<Callable<ArrayList<SearchResult>>>();
							for (int ithread = 0; ithread < threadNum; ithread++) {
								final int ftid = ithread;
								final int f_th_snum = fslicenum/fthreadnum;
								tasks.add(new Callable<ArrayList<SearchResult>>() {
										public ArrayList<SearchResult> call() {
											ArrayList<SearchResult> out = new ArrayList<SearchResult>();
											RandomAccessFile f = null;
											try {
												f = new RandomAccessFile(datapath, "r");
												for (int slice = fslicenum/fthreadnum*ftid+1, count = 0; slice <= fslicenum && count < f_th_snum; slice++, count++) {
													if( IJ.escapePressed() )
													break;	
													byte [] impxs = new byte[(int)size];
													byte [] colocs = null;
													if (fShowCo) colocs = new byte[(int)size];
													
													if (ftid == 0)
													IJ.showProgress((double)slice/(double)f_th_snum);
													
													long fioffset = fi_list[slice].getOffset();
													long loffset = 0;
													int ioffset = 0;
													int stripid = 0;
													for (int i=0; i<fi_list[slice].stripOffsets.length; i++) {
														f.seek(fioffset + (long)fi_list[slice].stripOffsets[i]);
														byte[] byteArray = new byte[fi_list[slice].stripLengths[i]];
														int read = 0, left = byteArray.length;
														while (left > 0) {
															int r = f.read(byteArray, read, left);
															if (r == -1) break;
															read += r;
															left -= r;
														}
														loffset = ioffset;
														stripid = i;
														ioffset = packBitsUncompress(byteArray, impxs, ioffset, maskpos_ed);
														if (ioffset >= maskpos_ed) {
															break;
														}
													}
													
													linename=st3.getSliceLabel(slice);
													
													ColorMIPMaskCompare.Output res = cc.runSearch(impxs, colocs);
													
													int posi = res.matchingPixNum;
													double posipersent = res.matchingPct;
													
													if(posipersent<=pixThresdub){
														if (flogon==true && flogNan==true)
														IJ.log("NaN");
													}else if(posipersent>pixThresdub){
														loffset = fi.getOffset() + (slice-1)*(size+fi.gapBetweenImages);
														
														double posipersent3=posipersent*100;
														double pixThresdub3=pixThresdub*100;
														
														posipersent3 = posipersent3*100;
														posipersent3 = Math.round(posipersent3);
														double posipersent2 = posipersent3 /100;
														
														pixThresdub3 = pixThresdub3*100;
														pixThresdub3 = Math.round(pixThresdub3);
														
														if(flogon==true && flogNan==true)// sort by name
														IJ.log("Positive linename; 	"+linename+" 	"+String.valueOf(posipersent2));
														
														String title="";
														if(fNumberSTint==0){
															String numstr = getZeroFilledNumString(posipersent2, 3, 2);
															title = (flabelmethod==0 || flabelmethod==1) ? numstr+"_"+linename : linename+"_"+numstr;
														}
														else if(fNumberSTint==1){
															String posiST=getZeroFilledNumString(posi, 4);
															title = (flabelmethod==0 || flabelmethod==1) ? posiST+"_"+linename : linename+"_"+posiST;
														}
														out.add(new SearchResult(title, slice, loffset, stripid, impxs, colocs, null, null));
														if (ftid == 0)
														IJ.showStatus("Number of Hits (estimated): "+out.size()*fthreadnum);
													}
												}
												f.close();
											} catch (IOException e) {
												e.printStackTrace();
											} finally {
												try { if (f != null) f.close(); }
												catch  (IOException e) { e.printStackTrace(); }
											}
											return out;
										}
								});
							}
							List<Future<ArrayList<SearchResult>>> taskResults = m_executor.invokeAll(tasks);
							for (Future<ArrayList<SearchResult>> future : taskResults) {
								for (SearchResult r : future.get()) {
									srlabels.add(r.m_name);
									srdict.put(r.m_name, r);
									
									posislice=posislice+1;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					IJ.log("Virtual Stack (directory)");
					
					String dirtmp = vst.getDirectory();
					if (dirtmp.length()>0 && !(dirtmp.endsWith(Prefs.separator)||dirtmp.endsWith("/")))
					dirtmp += Prefs.separator;
					final String directory = dirtmp;
					final long size = width*height*3;
					final List<Callable<ArrayList<SearchResult>>> tasks = new ArrayList<Callable<ArrayList<SearchResult>>>();
					
					try {
						String datapath = directory + vst.getFileName(1);
						RandomAccessFile f = new RandomAccessFile(datapath, "r");
						TiffDecoder tfd = new TiffDecoder(directory, vst.getFileName(1));
						FileInfo[] fi_list = tfd.getTiffInfo();
						IJ.log("NumberOfStripOffsets: "+fi_list[0].stripOffsets.length);
						f.close();
					} catch (IOException e) {
						return;
					}
					
					
					for (int ithread = 0; ithread < threadNum; ithread++) {
						final int ftid = ithread;
						final int f_th_snum = fslicenum/fthreadnum;
						tasks.add(new Callable<ArrayList<SearchResult>>() {
								public ArrayList<SearchResult> call() {
									ArrayList<SearchResult> out = new ArrayList<SearchResult>();
									for (int slice = fslicenum/fthreadnum*ftid+1, count = 0; slice <= fslicenum && count < f_th_snum; slice++, count++) {
										if( IJ.escapePressed() )
										break;
										byte [] impxs = new byte[(int)size];
										byte [] colocs = null;
										if (fShowCo) colocs = new byte[(int)size];
										String datapath = directory + vst.getFileName(slice);
										
										if (ftid == 0)
										IJ.showProgress((double)slice/(double)f_th_snum);
										
										try {
											TiffDecoder tfd = new TiffDecoder(directory, vst.getFileName(slice));
											if (tfd == null) continue;
											FileInfo[] fi_list = tfd.getTiffInfo();
											if (fi_list == null) continue;
											RandomAccessFile f = new RandomAccessFile(datapath, "r");
											
											long loffset = 0;
											int stripid = 0;
											if (!isPackbits) {
												loffset = fi_list[0].getOffset();
												f.seek(loffset+(long)maskpos_st);
												f.read(impxs, maskpos_st, stripsize);
											} else {
												int ioffset = 0;
												for (int i=0; i<fi_list[0].stripOffsets.length; i++) {
													f.seek(fi_list[0].stripOffsets[i]);
													byte[] byteArray = new byte[fi_list[0].stripLengths[i]];
													int read = 0, left = byteArray.length;
													while (left > 0) {
														int r = f.read(byteArray, read, left);
														if (r == -1) break;
														read += r;
														left -= r;
													}
													loffset = ioffset;
													stripid = i;
													ioffset = packBitsUncompress(byteArray, impxs, ioffset, maskpos_ed);
													if (ioffset >= maskpos_ed) {
														break;
													}
												}
											}
											
											String linename = st3.getSliceLabel(slice);
											
											ColorMIPMaskCompare.Output res = cc.runSearch(impxs, colocs);
											
											int posi = res.matchingPixNum;
											double posipersent = res.matchingPct;
											
											if(posipersent<=pixThresdub){
												if (flogon==true && flogNan==true)
												IJ.log("NaN");
											}else if(posipersent>pixThresdub){
												double posipersent3=posipersent*100;
												double pixThresdub3=pixThresdub*100;
												
												posipersent3 = posipersent3*100;
												posipersent3 = Math.round(posipersent3);
												double posipersent2 = posipersent3 /100;
												
												pixThresdub3 = pixThresdub3*100;
												pixThresdub3 = Math.round(pixThresdub3);
												
												if(flogon==true && flogNan==true)// sort by name
												IJ.log("Positive linename; 	"+linename+" 	"+String.valueOf(posipersent2));
												
												String title="";
												if(fNumberSTint==0){
													String numstr = getZeroFilledNumString(posipersent2, 3, 2);
													title = (flabelmethod==0 || flabelmethod==1) ? numstr+"_"+linename : linename+"_"+numstr;
												}
												else if(fNumberSTint==1) {
													String posiST=getZeroFilledNumString(posi, 4);
													title = (flabelmethod==0 || flabelmethod==1) ? posiST+"_"+linename : linename+"_"+posiST;
												}
												out.add(new SearchResult(title, slice, loffset, stripid, impxs, colocs, null, null));
												if (ftid == 0)
												IJ.showStatus("Number of Hits (estimated): "+out.size()*fthreadnum);
											}
											f.close();
										} catch (IOException e) {
											e.printStackTrace();
											continue;
										}
									}
									return out;
								}
						});
					}
					try {
						List<Future<ArrayList<SearchResult>>> taskResults = m_executor.invokeAll(tasks);
						for (Future<ArrayList<SearchResult>> future : taskResults) {
							for (SearchResult r : future.get()) {
								srlabels.add(r.m_name);
								srdict.put(r.m_name, r);
								
								posislice=posislice+1;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		}	else {
			IJ.log("getProcessor run");
			final List<Callable<ArrayList<SearchResult>>> tasks = new ArrayList<Callable<ArrayList<SearchResult>>>();
			for (int ithread = 0; ithread < threadNum; ithread++) {
				final int ftid = ithread;
				final int f_th_snum = fslicenum/fthreadnum;
				tasks.add(new Callable<ArrayList<SearchResult>>() {
						public ArrayList<SearchResult> call() {
							ArrayList<SearchResult> out = new ArrayList<SearchResult>();
							for (int slice = fslicenum/fthreadnum*ftid+1, count = 0; slice <= fslicenum && count < f_th_snum; slice++, count++) {
								if( IJ.escapePressed() )
								break;
								if (ftid == 0)
								IJ.showProgress((double)slice/(double)f_th_snum);
								
								ColorProcessor ipnew = null;
								if (fShowCo) ipnew = new ColorProcessor(width, height);
								
								ImageProcessor ip3 = st3.getProcessor(slice);
								String linename = st3.getSliceLabel(slice);
								
								ColorMIPMaskCompare.Output res = cc.runSearch(ip3, ipnew);
								
								int posi = res.matchingPixNum;
								double posipersent = res.matchingPct;
								
								if(posipersent<=pixThresdub){
									if (flogon==true && flogNan==true)
									IJ.log("NaN");
								}else if(posipersent>pixThresdub){
									double posipersent3=posipersent*100;
									double pixThresdub3=pixThresdub*100;
									
									posipersent3 = posipersent3*100;
									posipersent3 = Math.round(posipersent3);
									double posipersent2 = posipersent3 /100;
									
									pixThresdub3 = pixThresdub3*100;
									pixThresdub3 = Math.round(pixThresdub3);
									
									if(flogon==true && flogNan==true)// sort by name
									IJ.log("Positive linename; 	"+linename+" 	"+String.valueOf(posipersent2));
									
									String title="";
									if(fNumberSTint==0){
										String numstr = getZeroFilledNumString(posipersent2, 3, 2);
										title = (flabelmethod==0 || flabelmethod==1) ? numstr+"_"+linename : linename+"_"+numstr;
									}
									else if(fNumberSTint==1) {
										String posiST=getZeroFilledNumString(posi, 4);
										title = (flabelmethod==0 || flabelmethod==1) ? posiST+"_"+linename : linename+"_"+posiST;
									}
									out.add(new SearchResult(title, slice, 0L, 0, null, null, ip3, ipnew));
									if (ftid == 0)
									IJ.showStatus("Number of Hits (estimated): "+out.size()*fthreadnum);
								}
							}
							return out;
						}
				});
			}
			try {
				List<Future<ArrayList<SearchResult>>> taskResults = m_executor.invokeAll(tasks);
				for (Future<ArrayList<SearchResult>> future : taskResults) {
					for (SearchResult r : future.get()) {
						srlabels.add(r.m_name);
						srdict.put(r.m_name, r);
						
						posislice=posislice+1;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		long mid= System.currentTimeMillis();
		long gapmid=(mid-start)/1000;
		
		IJ.showStatus("Number of Hits: "+String.valueOf(posislice));
		IJ.log(" positive slice No.;"+String.valueOf(posislice)+"  sec; "+gapmid);
		
		int PositiveSlices=posislice;
		
		String OverlapValueLineArray [] = new String [posislice];
		for(int format=0; format<posislice; format++)
		OverlapValueLineArray [format]="0";
		
		String LineNameArray [] = new String[posislice];
		
		if(posislice>0){// if result is exist
			int posislice2=posislice;
			
			//		if(logon==true)
			ResultsTable rt = new ResultsTable();
			
			if(posislice==1)
			dupline=0;
			String linenameTmpo;
			
			for(int CreateLineArray=0; CreateLineArray<posislice; CreateLineArray++){
				linenameTmpo = srlabels.get(CreateLineArray);
				
				
				
				int LineBeginIndex=(linenameTmpo.indexOf("MB"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("GMR"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("VT"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("JRC_"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("_TR_"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("R_"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("GL_"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("TDC"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("JHS"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("BJD"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("SS"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("UAH"));
				if(LineBeginIndex==-1)
				LineBeginIndex=(linenameTmpo.indexOf("OL"));
				
				int DotPosi=(linenameTmpo.indexOf("."));
				
				if(LineBeginIndex==-1)
<<<<<<< HEAD
				LineBeginIndex=(linenameTmpo.indexOf("_"));
				//dupline=0;
=======
				dupline=0;
>>>>>>> parent of b45e1ea (Fiji plugin)
				
				
				
				if(dupline>0){
					//	IJ.log("JRCPosi; "+JRCPosi);
					
					int hyphen=(linenameTmpo.indexOf("-", 0 ));
					
					if(LineBeginIndex!=-1){
<<<<<<< HEAD
						
						if(hyphen==-1){
							int UnderS1=(linenameTmpo.indexOf("_", LineBeginIndex+1));
							int UnderS2=(linenameTmpo.indexOf("_", UnderS1+1 ));// end of line number
							if(UnderS2>UnderS1)
							LineNo=linenameTmpo.substring(LineBeginIndex, UnderS2);// GMR_01A02
							else 
							LineNo=linenameTmpo.substring(0, DotPosi);
						}else
=======
						int UnderS1=(linenameTmpo.indexOf("_", LineBeginIndex+1));
						int UnderS2=(linenameTmpo.indexOf("_", UnderS1+1 ));// end of line number
						
						if(hyphen==-1)
						LineNo=linenameTmpo.substring(LineBeginIndex, UnderS2);// GMR_01A02
						else
>>>>>>> parent of b45e1ea (Fiji plugin)
						LineNo=linenameTmpo.substring(LineBeginIndex, hyphen);// GMR_01A02
						
					//	IJ.log("LineNo; "+LineNo);
						
					}else{
						if(hyphen!=-1)
						LineNo=linenameTmpo.substring(0, hyphen);
						else
						LineNo=linenameTmpo.substring(0, DotPosi);
					}
				}else//	if(dupline>0){
				LineNo=linenameTmpo.substring(0, DotPosi);
				
<<<<<<< HEAD
				//	IJ.log("linenameTmpo; "+linenameTmpo+"dupline; "+dupline+"  LineBeginIndex; "+LineBeginIndex+"  LineNo; "+LineNo);
=======
			//	IJ.log("dupline; "+dupline+"  LineBeginIndex; "+LineBeginIndex+"  LineNo; "+LineNo);
>>>>>>> parent of b45e1ea (Fiji plugin)
				String posipersent2ST;
				if(labelmethod==0 || labelmethod==1){// on top score
					int UnderS0=(linenameTmpo.indexOf("_"));
					posipersent2ST = linenameTmpo.substring(0, UnderS0);// VT00002
				}else{
					int UnderS0=(linenameTmpo.lastIndexOf("_"));
					
					posipersent2ST = linenameTmpo.substring(UnderS0, linenameTmpo.length());// VT00002
					
				}
				//posipersent2= Double.parseDouble(posipersent2ST);
				
				LineNameArray[CreateLineArray]=LineNo+","+posipersent2ST+","+linenameTmpo;
				
				//		IJ.log("linenameTmpo;dcstack "+linenameTmpo);
			}
			Arrays.sort(LineNameArray, Collections.reverseOrder());
			
			//// duplication check and create top n score line list ////////////////////////////////////
			if(dupline!=0){
				
				//	LineNameArray[posislice]="Z,0,Z";
				
				
				//// scan complete line name list and copy the list to new positive list //////////////
				String[] FinalPosi = new String [posislice];
				int duplicatedLine=0;
				
				for(int LineInt=0; LineInt<posislice; LineInt++)
				
				if(DUPlogon==true)
				IJ.log(LineInt+"  "+LineNameArray[LineInt]);
				
				for(int Fposi=0; Fposi<=posislice; Fposi++){
					
					//////// pre line name ///////////////////////////
					if(Fposi>0){
						String arrayNamePre=LineNameArray[Fposi-1];
						
						arrayPosition=0;
						for (String retval: arrayNamePre.split(",")){
							args[arrayPosition]=retval;
							arrayPosition=arrayPosition+1;
						}
						
						preLineNo = args[0];// LineNo
					}
					
					///// current line name //////////////////////////
					if(Fposi<posislice){
						String arrayName=LineNameArray[Fposi];
						//		IJ.log("Original array; "+arrayName);
						
						arrayPosition=0;
						for (String retval: arrayName.split(",")){
							args[arrayPosition]=retval;
							arrayPosition=arrayPosition+1;
						}
						LineNo2 = args[0];// LineNo
						linename = args[2];//linename
					}
					
					if(Fposi==0)
					preLineNo=LineNo2;
					
					if(Fposi==posislice)
					LineNo2="End";
					
					//		IJ.log("LineNo 662; 	"+LineNo2+"   preLineNo; "+preLineNo+ "   duplicatedLine; "+duplicatedLine); // Sort OK!
					
					Check=-1;
					Check=(LineNo2.indexOf(preLineNo));
					
					//		IJ.log("Check; "+Check);
					
					if(Check!=-1 && Fposi!=0){
						duplicatedLine=duplicatedLine+1;
						//		IJ.log("Duplicated");
					}
					
					if(Check==-1 && duplicatedLine>dupline-1){// end of duplication, at next new file name
						if(DUPlogon==true){
							IJ.log("");
							IJ.log("Line Duplication; "+String.valueOf(duplicatedLine+1));
						}
						String [] Battle_Values = new String [duplicatedLine+1];
						
						for(int dupcheck=1; dupcheck<=duplicatedLine+1; dupcheck++){
							arrayName=LineNameArray[Fposi-dupcheck];
							
							arrayPosition=0;
							for (String retval: arrayName.split(",")){
								args[arrayPosition]=retval;
								arrayPosition=arrayPosition+1;
							}
							
							Battle_Values [dupcheck-1] = args[1]+","+args[2];//score + fullname
							
							if(DUPlogon==true)
							IJ.log("Overlap_Values; "+Battle_Values [dupcheck-1]);
							
						}//for(int dupcheck=1; dupcheck<=duplicatedLine+1; dupcheck++){
						
						//		Collections.reverse(Ints.asList(Battle_Values));
						Arrays.sort(Battle_Values, Collections.reverseOrder());
						
						for(int Endvalue=0; Endvalue<dupline; Endvalue++){// scan from top value to the border
							
							for(int dupcheck=1; dupcheck<=duplicatedLine+1; dupcheck++){
								
								arrayPosition=0;
								for (String retval: LineNameArray[Fposi-dupcheck].split(",")){
									args[arrayPosition]=retval;
									arrayPosition=arrayPosition+1;
								}
								
								String OverValue = args[1];//posipersent2
								LineName = args[0];//posipersent2
								FullName = args[2];//posipersent2
								
								
								arrayPosition=0;
								for (String retval: Battle_Values[Endvalue].split(",")){
									args[arrayPosition]=retval;
									arrayPosition=arrayPosition+1;
								}
								
								String TopValue = args[0];//posipersent2
								String FullNameBattle = args[1];//posipersent2
								
								int BattleCheck=(FullNameBattle.indexOf(FullName));
								
								//	IJ.log("OverValue; "+OverValue+"  TopValue; "+TopValue);
								//	IJ.log("FullNameBattle; "+FullNameBattle+"  FullName; "+FullName);
								
								double OverValueDB = Double.parseDouble(OverValue);
								double OverValueBV = Double.parseDouble(TopValue);
								
								if(OverValueBV==OverValueDB && BattleCheck!=-1){
									
									//				IJ.log("OverValueSlice1; "+LineNameArray[Fposi-dupcheck]);
									
									if(labelmethod==0)// Sort by value
									LineNameArray[Fposi-dupcheck]="10000000,10000000,100000000";//delete positive overlap array for negative overlap list
									
									if(labelmethod==1){// Sort by value and line
										if(Endvalue==0){// highest value
											if(DUPlogon==true)
											IJ.log(1+UniqueLineName+"  UniqueLineName; "+FullName);
											OverlapValueLineArray[UniqueLineName]=FullName+","+LineName;
											UniqueLineName=UniqueLineName+1;
										}
									}
									//				IJ.log("OverValueSlice2; "+LineNameArray[Fposi-dupcheck]);
									
									//		PositiveSlices=PositiveSlices-1;
								}
							}//for(int dupcheck=1; dupcheck<=duplicatedLine+1; dupcheck++){
						}//	for(int Endvalue=0; Endvalue<dupline; Endvalue++){// scan from top value to the border
						
						duplicatedLine=0; Check=2; 
					}//if(preLineNo!=LineNo && duplicatedLine>dupline-1){// end of duplication, at next new file name
					
					int initialNo=Fposi-duplicatedLine-1;
					if(initialNo>0 && Check==-1 && duplicatedLine<=dupline-1){//&& CheckPost==-1
						
						for(int dupwithinLimit=1; dupwithinLimit<=duplicatedLine+1; dupwithinLimit++){
							
							if(DUPlogon==true){
								IJ.log("");
								IJ.log("dupwithinLimit; "+LineNameArray[Fposi-dupwithinLimit]+"  dupwithinLimit; "+dupwithinLimit+"  duplicatedLine; "+duplicatedLine);
							}
							
							if(labelmethod==0)// Sort by value
							LineNameArray[Fposi-dupwithinLimit]="10000000,10000000,100000000";// delete positive files within duplication limit from negative list
							
							else if (labelmethod==1){// Sort by value and line
								
								arrayName=LineNameArray[Fposi-dupwithinLimit];
								
								arrayPosition=0;
								for (String retval: arrayName.split(",")){
									args[arrayPosition]=retval;
									arrayPosition=arrayPosition+1;
								}
								LineName = args[0];//posipersent2
								String ScoreCurrent= args[1];
								FullName = args[2];//posipersent2
								
								if(dupwithinLimit==2){/// 2nd file
									
									double CurrentScore = Double.parseDouble(ScoreCurrent);
									
									double PreScore = Double.parseDouble(ScorePre);
									
									if(DUPlogon==true)
									IJ.log("CurrentScore; "+CurrentScore+"  PreScore; "+PreScore);
									
									if(CurrentScore>PreScore){
										
										if(DUPlogon==true)
										IJ.log(1+UniqueLineName-1+"  UniqueLineName; "+FullName);
										OverlapValueLineArray[UniqueLineName-1]=FullName+","+LineName;
										
										LineNameArray[Fposi-dupwithinLimit+1]=LineName+","+ScoreCurrent+","+FullName;
										LineNameArray[Fposi-dupwithinLimit]=LineName+","+ScorePre+","+PreFullLineName;
										
									}else{
										if(DUPlogon==true)
										IJ.log(1+UniqueLineName-1+"  UniqueLineName; "+PreFullLineName);
										//			OverlapValueLineArray[UniqueLineName]=PreFullLineName+","+LineName;
									}
									
								}//if(dupwithinLimit==2){
								
								
								if(dupwithinLimit==1){
									
									arrayName=LineNameArray[Fposi-dupwithinLimit-1];
									
									arrayPosition=0;
									for (String retval: arrayName.split(",")){
										args[arrayPosition]=retval;
										arrayPosition=arrayPosition+1;
									}
									String PreSLineName = args[0];//posipersent2
									
									ScorePre= ScoreCurrent;
									PreFullLineName = FullName;//posipersent2
									
									int PreCheck=(LineName.indexOf(PreSLineName));
									
									if(PreCheck==-1 && DUPlogon==true)
									IJ.log(1+UniqueLineName+"  UniqueLineName; "+FullName);
									
									OverlapValueLineArray[UniqueLineName]=FullName+","+LineName;
									UniqueLineName=UniqueLineName+1;
									//			LineNameArray[Fposi-dupwithinLimit]="0";
								}//if(dupwithinLimit==1){
							}//else if (labelmethod==2){// Sort by value and line
							
							//		PositiveSlices=PositiveSlices-1;
						}
						duplicatedLine=0;
					}//if(initialNo!=0 && preLineNo!=LineNo && duplicatedLine<=dupline-1){
					
					if(initialNo<=0 && Check==-1 && duplicatedLine<=dupline-1 && Fposi>0){// && CheckPost==-1
						for(int dupwithinLimit=1; dupwithinLimit<=duplicatedLine+1; dupwithinLimit++){
							
							if(DUPlogon==true){
								IJ.log("");
								IJ.log("dupwithinLimit start; "+LineNameArray[Fposi-dupwithinLimit]);
							}
							if(labelmethod==0)// Sort by value
							LineNameArray[Fposi-dupwithinLimit]="10000000,10000000,100000000";// delete positive files within duplication limit from negative list
							
							else if (labelmethod==1){// Sort by value and line
								
								arrayName=LineNameArray[Fposi-dupwithinLimit];
								
								arrayPosition=0;
								for (String retval: arrayName.split(",")){
									args[arrayPosition]=retval;
									arrayPosition=arrayPosition+1;
								}
								LineName = args[0];//posipersent2
								FullName = args[2];//posipersent2
								
								if(dupwithinLimit==1){
									if(DUPlogon==true)
									IJ.log(1+UniqueLineName+"  UniqueLineName; "+FullName);
									OverlapValueLineArray[UniqueLineName]=FullName+","+LineName;
									UniqueLineName=UniqueLineName+1;
									//			LineNameArray[Fposi-dupwithinLimit]="0";
								}
							}
							
							//				PositiveSlices=PositiveSlices-1;
						}
						
						duplicatedLine=0;
					}//if(initialNo<=0 && Check==-1 && duplicatedLine<=dupline-1 && Fposi>0){
					
					//		preLineNo=LineNo2;
					
				}//for(int Fposi=0; Fposi<posislice; Fposi++){
				Arrays.sort(LineNameArray, Collections.reverseOrder());// negative list
				
			}//	if(dupline!=0){
			
			if (labelmethod==1)
			Arrays.sort(OverlapValueLineArray, Collections.reverseOrder());// top value list
			else
			UniqueLineName=posislice2-1;
			
			//		for(int overarray=0; overarray<UniqueLineName; overarray++){
			//			IJ.log("OverlapValueLineArray;  "+OverlapValueLineArray[overarray]);
			//		}
			
			/// sorting order /////////////////////////////////////////////////////////////////
			String[] weightposi = new String [posislice];
			for(int wposi=0; wposi<posislice; wposi++){
				weightposi[wposi] = srlabels.get(wposi);
			}
			Arrays.sort(weightposi, Collections.reverseOrder());
			
			if(DUPlogon==true)
			IJ.log("UniqueLineName number; "+UniqueLineName);
			
			if(dupline!=0){
				for(int wposi=0; wposi<UniqueLineName; wposi++){
					for(int slicelabel=1; slicelabel<=posislice2; slicelabel++){
						
						//		if(DUPlogon==true)
						//		IJ.log("wposi; "+wposi);
						
						if (labelmethod==1){
							arrayName=OverlapValueLineArray[wposi];
							//		IJ.log(" arrayName;"+arrayName);
							arrayPosition=0;
							for (String retval: arrayName.split(",")){
								args[arrayPosition]=retval;
								arrayPosition=arrayPosition+1;
							}
							String LineNameTop = args[0];// Full line name of OverlapValueLineArray
							TopShortLinename = args[1];//short linename for the file
							
							IsPosi=(LineNameTop.indexOf(srlabels.get(slicelabel-1)));
							
						}//if (labelmethod==2){
						
						if (labelmethod!=1)
						IsPosi=(weightposi[wposi].indexOf(srlabels.get(slicelabel-1)));
						
						
						//	IJ.log(IsPosi+" LineNameTop;"+LineNameTop+"  linename; "+linename+"   dcStack; "+dcStack.getSliceLabel(slicelabel));
						
						if(IsPosi!=-1){// if top value slice is existing in dsStack
							
							if (labelmethod==1){
								// get line name from top value array//////////////////////				
								// get 2nd 3rd line name from deleted array //////////////////////			
								
								for(int PosiSliceScan=0; PosiSliceScan<PositiveSlices; PosiSliceScan++){
									
									String PositiveArrayName=LineNameArray[PosiSliceScan];
									arrayPosition=0;
									for (String retval: PositiveArrayName.split(",")){
										args[arrayPosition]=retval;
										arrayPosition=arrayPosition+1;
									}
									
									String linenamePosi2 = args[0];//linename
									String FullLinenamePosi2 = args[2];//linename
									
									int PosiLine=(TopShortLinename.indexOf(linenamePosi2));//Top lineName and LineNameArray
									
									//		IJ.log(PosiLine+"  917 LineNameTop;"+LineNameTop+"  linename; "+linename+"   linenamePosi2; "+linenamePosi2+"   AlreadyAdded; "+AlreadyAdded+"  slicelabel; "+slicelabel);
									
									//		if(PosiLine==-1)
									//		AlreadyAdded=0;
									
									if(PosiLine!=-1 ){//&& AlreadyAdded==0// if TopShortLinename exist in LineNameArray
										
										for(int AddedSlice=0; AddedSlice<dupline; AddedSlice++){
											
											if(slicelabel+AddedSlice<=posislice2 && PosiSliceScan+AddedSlice<PositiveSlices){
												
												PositiveArrayName=LineNameArray[PosiSliceScan+AddedSlice];
												arrayPosition=0;
												for (String retval: PositiveArrayName.split(",")){
													args[arrayPosition]=retval;
													arrayPosition=arrayPosition+1;
												}
												String ScanlinenamePosi2 = args[0];//short linename
												FullLinenamePosi2 = args[2];//LineNameArray for adding slices
												
												PosiLine=(TopShortLinename.indexOf(ScanlinenamePosi2));//Top lineName and LineNameArray
												
												if(PosiLine!=-1){// same short-line name as topValue one, if different = single file.
													for(int dcStackScan=1; dcStackScan<=posislice2; dcStackScan++){
														
														int IsPosi0=(FullLinenamePosi2.indexOf(srlabels.get(dcStackScan-1)));
														
														if(IsPosi0!=-1){
															finallbs.add(FullLinenamePosi2);
															srlabels.set(dcStackScan-1, "Done");
															//		dcStackOrigi.deleteSlice(dcStackScan);
															
															//		AlreadyAdded=1;
															//	posislice2=posislice2-1;
															//	posislice2=dcStack.size();
															
															//			if(logon==true)
															//			IJ.log(String.valueOf(FinalAdded)+"  Added Slice; "+FullLinenamePosi2);
															
															FinalAdded=FinalAdded+1;
															LineNameArray[PosiSliceScan+AddedSlice]="Deleted";
															dcStackScan=posislice2+1;// finish scan for dcStack
															
														}//if(IsPosi0!=-1){
													}//	for(int dcStackScan=1; dcStackScan<posislice2; dcStackScan++){
													//		slicelabel=1;
												}//if(PosiLine!=-1){// same short-line name as topValue one, if different = single file.
											}
										}//for(int AddedSlice=1; AddedSlice<=dupline; AddedSlice++){
										
										int OnceDeleted=0;
										/// delete rest of duplicated slices from LineNameArray /////////////////////
										for(int deleteDuplicatedSlice=0; deleteDuplicatedSlice<posislice; deleteDuplicatedSlice++){
											
											PositiveArrayName=LineNameArray[deleteDuplicatedSlice];
											arrayPosition=0;
											for (String retval: PositiveArrayName.split(",")){
												args[arrayPosition]=retval;
												arrayPosition=arrayPosition+1;
											}
											
											linenamePosi2 = args[0];//linename
											String DelFulllinename = args[2];//linename
											
											int PosiLineDUP=(TopShortLinename.indexOf(linenamePosi2));
											//			IJ.log(" linename; 	"+linename+"  linenamePosi2; "+linenamePosi2);
											
											if(PosiLineDUP!=-1){//if there is more duplicated lines in PositiveArrayName
												
												if(DUPlogon==true)
												IJ.log(dupdel+" Duplicated & deleted; 	"+DelFulllinename);
												dupdel=dupdel+1;
												
												LineNameArray[deleteDuplicatedSlice]="Deleted";
												OnceDeleted=1;
											}else{
												//				if(OnceDeleted==1)
												//				deleteDuplicatedSlice=posislice;
											}
										}//for(int deleteDuplicatedSlice=0; deleteDuplicatedSlice<1000; deleteDuplicatedSlice++){
										
										PosiSliceScan=PositiveSlices;// end after 1 added
									}//	if(PosiLine!=-1){// if exist
								}//for(int PosiSliceScan=0; PosiSliceScan<PositiveSlices; PosiSliceScan++){
							}//if (labelmethod==2){
							
							
							if (labelmethod!=1){
								if(dupline!=0){
									int NegativeExist=0;
									/// negative slice check //////////////////////////
									for(int negativeSlice=0; negativeSlice<PositiveSlices; negativeSlice++){
										
										int arrayPosition=0;
										for (String retval: LineNameArray[negativeSlice].split(",")){
											args[arrayPosition]=retval;
											arrayPosition=arrayPosition+1;
										}
										
										String linenameNega = args[2];//linename
										
										int NegaCheck=(weightposi[wposi].indexOf( linenameNega ));
										
										if(NegaCheck!=-1){
											NegativeExist=1;
											negativeSlice=PositiveSlices;
										}
									}//for(int negativeSlice=0; negativeSlice<PositiveSlices; negativeSlice++){
									
									if(NegativeExist==0){// if no-existing negative
										finallbs.add(weightposi[wposi]);
										srlabels.remove(slicelabel-1);
									}else{//NegativeExist==1
										srlabels.remove(slicelabel-1);
										
										if(DUPlogon==true)
										IJ.log(dupdel+" Duplicated & deleted; 	"+weightposi[wposi]);
										dupdel=dupdel+1;
									}
								}else{//if(dupline!=0){
									finallbs.add(weightposi[wposi]);
									srlabels.remove(slicelabel-1);
								}
								slicelabel=posislice2+1;
							}//	if (labelmethod!=2){
							
							//		posislice2=posislice2-1;
						}//if(IsPosi!=-1){
					}//	for(int slicelabel=1; slicelabel<=posislice2; slicelabel++){
				}//	for(int wposi=0; wposi<posislice; wposi++){
			}else{//dupline==0
				for(int wposi=0; wposi<posislice; wposi++){
					for(int slicelabel=1; slicelabel<=posislice2; slicelabel++){
						if(weightposi[wposi]==srlabels.get(slicelabel-1)){
							
							finallbs.add(weightposi[wposi]);
							srlabels.remove(slicelabel-1);
							
							if(logon==true && logNan==false)
							IJ.log("Positive linename; 	"+weightposi[wposi]);
							
							slicelabel=posislice2;
							posislice2=posislice2-1;
						}
					}
				}
			}//}else{//dupline==0
			
			ImageStack dcStackfinal = new ImageStack (width,height);
			ImageStack OrigiStackfinal = new ImageStack (width,height);
			
			int[] m_mask2;
			m_mask2 = get_mskpos_array(ip1, Thresm);
			
			
			
			//	VirtualStack OrigiStackfinal = new VirtualStack (width,height);
			int slnum =0;
			try {
				long size = width*height*3;
				slnum = finallbs.size();
				
				IJ.log("slnum; "+String.valueOf (slnum)+"  maxnumberN; "+maxnumberN+"  dupline; "+dupline);
				
				if(slnum>maxnumberN)
				slnum=maxnumberN;
				
				if(fileformat.equals("tif none") || fileformat.equals("tif PackBits")){
					if (st3.isVirtual()) {
						VirtualStack vst = (VirtualStack)st3;
						if (vst.getDirectory() == null) {
							FileInfo fi = idata.getOriginalFileInfo();
							if (fi.directory.length()>0 && !(fi.directory.endsWith(Prefs.separator)||fi.directory.endsWith("/")))
							fi.directory += Prefs.separator;
							String datapath = fi.directory + fi.fileName;
							RandomAccessFile f = new RandomAccessFile(datapath, "r");
							for (int s = 0; s < slnum; s++) {
								String label = finallbs.get(s);
								SearchResult sr = srdict.get(label);
								if (sr != null) {
									ColorProcessor cp = new ColorProcessor(width, height);
									ColorProcessor cpcoloc = new ColorProcessor(width, height);
									byte[] impxs = sr.m_pixels;
									long loffset = sr.m_offset;
									
									f.seek(loffset);
									f.read(impxs, 0, maskpos_st);
									f.seek(loffset+(long)maskpos_ed+3L);
									f.read(impxs, maskpos_ed+3, impxs.length-maskpos_ed-3);
									
									for (int i = 0, id = 0; i < size; i+=3,id++) {
										int red2 = impxs[i] & 0xff;
										int green2 = impxs[i+1] & 0xff;
										int blue2 = impxs[i+2] & 0xff;
										int pix2 = 0xff000000 | (red2 << 16) | (green2 << 8) | blue2;
										cp.set(id, pix2);
									}
									
									int underindex = label.indexOf("_");
									String scoreST2 = label.substring(0,underindex);
									double scorepercentdouble = Double.parseDouble(scoreST2);
									double percentscore= scorepercentdouble*100;
									
									double matchingpxnumDub = (scorepercentdouble*m_mask2.length)/100;
									int matchingpxnum = (int) matchingpxnumDub;
									
									String lineName = label.substring(underindex+1,label.length());
									
									if(logon==true){
										IJ.log("Mask px size;	"+m_mask2.length+"	positive px num;	"+matchingpxnum+"	ratio;	"+scoreST2+"	"+lineName);
										
										rt.incrementCounter();
										rt.addValue("Mask px size", m_mask2.length);//median value
										rt.addValue("positive px num", matchingpxnum);//number of the pixels
										rt.addValue("ratio", scoreST2);//number of the pixels
										rt.addValue("Line", lineName);//number of the pixels
										
									}
									OrigiStackfinal.addSlice(label, cp);
									
									if (ShowCo) {
										impxs = sr.m_colocs;
										for (int i = maskpos_st, id = maskpos_st/3; i <= maskpos_ed; i+=3,id++) {
											int red2 = impxs[i] & 0xff;
											int green2 = impxs[i+1] & 0xff;
											int blue2 = impxs[i+2] & 0xff;
											int pix2 = 0xff000000 | (red2 << 16) | (green2 << 8) | blue2;
											cpcoloc.set(id, pix2);
										}
										dcStackfinal.addSlice(label, cpcoloc);
									}
								}
							}
							f.close();
						} else {
							String directory = vst.getDirectory();
							if (directory.length()>0 && !(directory.endsWith(Prefs.separator)||directory.endsWith("/")))
							directory += Prefs.separator;
							try {
								for (int s = 0; s < slnum; s++) {
									String label = finallbs.get(s);
									SearchResult sr = srdict.get(label);
									if (sr != null) {
										ColorProcessor cp = new ColorProcessor(width, height);
										ColorProcessor cpcoloc = new ColorProcessor(width, height);
										byte[] impxs = sr.m_pixels;
										int sliceid = sr.m_sid;
										long loffset = sr.m_offset;
										String datapath = directory + vst.getFileName(sliceid);
										RandomAccessFile f = new RandomAccessFile(datapath, "r");
										
										if (fileformat.equals("tif none")) {
											f.seek(loffset);
											f.read(impxs, 0, maskpos_st);
											f.seek(loffset+(long)maskpos_ed+3L);
											f.read(impxs, maskpos_ed+3, impxs.length-maskpos_ed-3);
										} else if (maskpos_ed < size) {
											TiffDecoder tfd = new TiffDecoder(directory, vst.getFileName(sliceid));
											if (tfd == null) continue;
											FileInfo[] fi_list = tfd.getTiffInfo();
											if (fi_list == null) continue;
											int stripid = sr.m_strip;
											int ioffset = (int)loffset;
											for (int i=stripid; i<fi_list[0].stripOffsets.length; i++) {
												f.seek(fi_list[0].stripOffsets[i]);
												byte[] byteArray = new byte[fi_list[0].stripLengths[i]];
												int read = 0, left = byteArray.length;
												while (left > 0) {
													int r = f.read(byteArray, read, left);
													if (r == -1) break;
													read += r;
													left -= r;
												}
												ioffset = packBitsUncompress(byteArray, impxs, ioffset, 0);
											}
										}
										
										for (int i = 0, id = 0; i < size; i+=3,id++) {
											int red2 = impxs[i] & 0xff;
											int green2 = impxs[i+1] & 0xff;
											int blue2 = impxs[i+2] & 0xff;
											int pix2 = 0xff000000 | (red2 << 16) | (green2 << 8) | blue2;
											cp.set(id, pix2);
										}
										
										int underindex = label.indexOf("_");
										String scoreST2 = label.substring(0,underindex);
										double scorepercentdouble = Double.parseDouble(scoreST2);
										double percentscore= scorepercentdouble*100;
										
										double matchingpxnumDub = (scorepercentdouble*m_mask2.length)/100;
										int matchingpxnum = (int) matchingpxnumDub;
										
										String lineName = label.substring(underindex+1,label.length());
										
										if(logon==true){
											IJ.log("Mask px size;	"+m_mask2.length+"	positive px num;	"+matchingpxnum+"	ratio;	"+scoreST2+"	"+lineName);
											rt.incrementCounter();
											rt.addValue("Mask px size", m_mask2.length);//median value
											rt.addValue("positive px num", matchingpxnum);//number of the pixels
											rt.addValue("ratio", scoreST2);//number of the pixels
											rt.addValue("Line", lineName);//number of the pixels
											
										}
										OrigiStackfinal.addSlice(label, cp);
										
										if (ShowCo) {
											impxs = sr.m_colocs;
											for (int i = maskpos_st, id = maskpos_st/3; i <= maskpos_ed; i+=3,id++) {
												int red2 = impxs[i] & 0xff;//data
												int green2 = impxs[i+1] & 0xff;//data
												int blue2 = impxs[i+2] & 0xff;//data
												int pix2 = 0xff000000 | (red2 << 16) | (green2 << 8) | blue2;
												cpcoloc.set(id, pix2);
											}
											dcStackfinal.addSlice(label, cpcoloc);
										}
										f.close();
									}
								}
							} catch(OutOfMemoryError e) {
								IJ.outOfMemory("FolderOpener");
								if (OrigiStackfinal!=null) OrigiStackfinal.trim();
							}
						}
					}//	if (st3.isVirtual()) {
				}	else {
					for (int s = 0; s < slnum; s++) {
						String label = finallbs.get(s);
						SearchResult sr = srdict.get(label);
						if (sr != null) {
							
							int underindex = label.indexOf("_");
							String scoreST2 = label.substring(0,underindex);
							double scorepercentdouble = Double.parseDouble(scoreST2);
							double percentscore= scorepercentdouble*100;
							
							double matchingpxnumDub = (scorepercentdouble*m_mask2.length)/100;
							int matchingpxnum = (int) matchingpxnumDub;
							
							String lineName = label.substring(underindex+1,label.length());
							
							if(logon==true){
								IJ.log("Mask px size;	"+m_mask2.length+"	positive px num;	"+matchingpxnum+"	ratio;	"+scoreST2+"	"+lineName);
								rt.incrementCounter();
								rt.addValue("Mask px size", m_mask2.length);//median value
								rt.addValue("positive px num", matchingpxnum);//number of the pixels
								rt.addValue("ratio", scoreST2);//number of the pixels
								rt.addValue("Line", lineName);//number of the pixels
							}
							OrigiStackfinal.addSlice(label, sr.m_iporg);
							if (ShowCo) dcStackfinal.addSlice(label, sr.m_ipcol);
						}
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			if(thremethodSTR.equals("Combine")){
				ImageStack combstack=new ImageStack(width*2, height, OrigiStackfinal.getColorModel());
				ImageProcessor ip6 = OrigiStackfinal.getProcessor(1);
				
				for (int i=1; i<=posislice; i++) {
					IJ.showProgress((double)i/posislice);
					ip5 = ip6.createProcessor(width*2, height);
					
					if(ShowCo==true){
						ip5.insert(dcStackfinal.getProcessor(1),0,0);
						dcStackfinal.deleteSlice(1);
					}
					ip5.insert(OrigiStackfinal.getProcessor(1),width,0);
					OrigiStackfinal.deleteSlice(1);
					combstack.addSlice(weightposi[i-1], ip5);
				}
				
				if(ShowCo==true){
					newimp = new ImagePlus("Co-localized_And_Original.tif_"+pixThresdub2+" %_"+titles[Mask]+"", combstack);
					newimp.show();
				}
			}else{
				if(slnum>0){
					
					if(ShowCo==true){
						newimp = new ImagePlus("Co-localized.tif_"+pixThresdub2+" %_"+titles[Mask]+"", dcStackfinal);
						newimp.show();
					}
					
					newimpOri = new ImagePlus("Original_RGB.tif_"+pixThresdub2+" %_"+titles[Mask]+"", OrigiStackfinal);
					newimpOri.show();
				}//if(slnum>0){
			}
			
			if(logon==true)
			rt.show("Results");
		}//if(posislice>0){
		
		
		
		end = System.currentTimeMillis();
		//		int gap=(int)(end-start)/1000;
		
		IJ.log("Search time: "+(double)((double)end-(double)start)/1000+"sec");
		
		if(posislice==0)
		IJ.log("No positive slice");
		
		imask.unlock();
		idata.unlock();
		
		//	IJ.log("Done; "+increment+" mean; "+mean3+" Totalmaxvalue; "+totalmax+" desiremean; "+desiremean);
		
		//	if(EMsearch==true){
		//		String plugindir = IJ.getDirectory("plugins");
		
		//		IJ.log("plugin dir; "+plugindir);
		
		//		IJ.runMacroFile(""+plugindir+"Macros/CDM_area_measure.ijm");
		//	}
		//	System.gc();
	} //public void run(ImageProcessor ip){
	
	
	
	public static int[] get_mskpos_array(ImageProcessor msk, int thresm){
		int sumpx = msk.getPixelCount();
		ArrayList<Integer> pos = new ArrayList<Integer>();
		int pix, red, green, blue;
		for(int n4=0; n4<sumpx; n4++){
			
			pix= msk.get(n4);//Mask
			
			red = (pix>>>16) & 0xff;//mask
			green = (pix>>>8) & 0xff;//mask
			blue = pix & 0xff;//mask
			
			if(red>thresm || green>thresm || blue>thresm)
			pos.add(n4);
		}
		return pos.stream().mapToInt(i -> i).toArray();
	}
	
	public static int[] shift_mskpos_array(int[] src, int xshift, int yshift, int w, int h){
		ArrayList<Integer> pos = new ArrayList<Integer>();
		int x, y;
		int ypitch = w;
		for(int i = 0; i < src.length; i++) {
			int val = src[i];
			x = (val % ypitch) + xshift;
			y = val / ypitch + yshift;
			if (x >= 0 && x < w && y >= 0 && y < h)
			pos.add(y*w+x);
			else
			pos.add(-1);
		}
		return pos.stream().mapToInt(i -> i).toArray();
	}
	
	public static int[][] generate_shifted_masks(int[] in, int xyshift, int w, int h) {
		int[][] out = new int[1+(xyshift/2)*8][];
		
		out[0] = in.clone();
		int maskid = 1;
		for (int i = 2; i <= xyshift; i += 2) {
			for (int xx = -i; xx <= i; xx += i) {
				for (int yy = -i; yy <= i; yy += i) {
					if (xx == 0 && yy == 0) continue;
					out[maskid] = shift_mskpos_array(in, xx, yy, w, h);
					maskid++;
				}
			}
		}
		return out;
	}
	
	public static int[] mirror_mask(int[] in, int ypitch) {
		int[] out = in.clone();
		int masksize = in.length;
		int x;
		for(int j = 0; j < masksize; j++) {
			int val = in[j];
			x = val % ypitch;
			out[j] = val + (ypitch-1) - 2*x;
		}
		return out;
	}
	
	public static int calc_score(ImageProcessor src, int[] srcmaskposi, ImageProcessor tar, int[] tarmaskposi, int th, double pixfludub, ImageProcessor coloc_out) {
		
		int masksize = srcmaskposi.length <= tarmaskposi.length ? srcmaskposi.length : tarmaskposi.length;
		int posi = 0;
		for(int masksig=0; masksig<masksize; masksig++){
			
			if (srcmaskposi[masksig] == -1 || tarmaskposi[masksig] == -1) continue;
			
			int pix1= src.get(srcmaskposi[masksig]);
			int red1 = (pix1>>>16) & 0xff;
			int green1 = (pix1>>>8) & 0xff;
			int blue1 = pix1 & 0xff;
			
			int pix2= tar.get(tarmaskposi[masksig]);
			int red2 = (pix2>>>16) & 0xff;
			int green2 = (pix2>>>8) & 0xff;
			int blue2 = pix2 & 0xff;
			
			if(red2>th || green2>th || blue2>th){
				
				double pxGap = calc_score_px(red1, green1, blue1, red2, green2, blue2); 
				
				if(pxGap<=pixfludub){
					if(coloc_out!=null)
					coloc_out.set(tarmaskposi[masksig], pix2);
					posi++;
				}
				
			}
		}
		
		return posi;
		
	}
	
	public static double calc_score_px(int red1, int green1, int blue1, int red2, int green2, int blue2) {
		int RG1=0; int BG1=0; int GR1=0; int GB1=0; int RB1=0; int BR1=0;
		int RG2=0; int BG2=0; int GR2=0; int GB2=0; int RB2=0; int BR2=0;
		double rb1=0; double rg1=0; double gb1=0; double gr1=0; double br1=0; double bg1=0;
		double rb2=0; double rg2=0; double gb2=0; double gr2=0; double br2=0; double bg2=0;
		double pxGap=10000; 
		double BrBg=0.354862745; double BgGb=0.996078431; double GbGr=0.505882353; double GrRg=0.996078431; double RgRb=0.505882353;
		double BrGap=0; double BgGap=0; double GbGap=0; double GrGap=0; double RgGap=0; double RbGap=0;
		
		if(blue1>red1 && blue1>green1){//1,2
			if(red1>green1){
				BR1=blue1+red1;//1
				if(blue1!=0 && red1!=0)
				br1= (double) red1 / (double) blue1;
			}else{
				BG1=blue1+green1;//2
				if(blue1!=0 && green1!=0)
				bg1= (double) green1 / (double) blue1;
			}
		}else if(green1>blue1 && green1>red1){//3,4
			if(blue1>red1){
				GB1=green1+blue1;//3
				if(green1!=0 && blue1!=0)
				gb1= (double) blue1 / (double) green1;
			}else{
				GR1=green1+red1;//4
				if(green1!=0 && red1!=0)
				gr1= (double) red1 / (double) green1;
			}
		}else if(red1>blue1 && red1>green1){//5,6
			if(green1>blue1){
				RG1=red1+green1;//5
				if(red1!=0 && green1!=0)
				rg1= (double) green1 / (double) red1;
			}else{
				RB1=red1+blue1;//6
				if(red1!=0 && blue1!=0)
				rb1= (double) blue1 / (double) red1;
			}
		}
		
		if(blue2>red2 && blue2>green2){
			if(red2>green2){//1, data
				BR2=blue2+red2;
				if(blue2!=0 && red2!=0)
				br2= (double) red2 / (double) blue2;
			}else{//2, data
				BG2=blue2+green2;
				if(blue2!=0 && green2!=0)
				bg2= (double) green2 / (double) blue2;
			}
		}else if(green2>blue2 && green2>red2){
			if(blue2>red2){//3, data
				GB2=green2+blue2;
				if(green2!=0 && blue2!=0)
				gb2= (double) blue2 / (double) green2;
			}else{//4, data
				GR2=green2+red2;
				if(green2!=0 && red2!=0)
				gr2= (double) red2 / (double) green2;
			}
		}else if(red2>blue2 && red2>green2){
			if(green2>blue2){//5, data
				RG2=red2+green2;
				if(red2!=0 && green2!=0)
				rg2= (double) green2 / (double) red2;
			}else{//6, data
				RB2=red2+blue2;
				if(red2!=0 && blue2!=0)
				rb2= (double) blue2 / (double) red2;
			}
		}
		
		///////////////////////////////////////////////////////					
		if(BR1>0){//1, mask// 2 color advance core
			if(BR2>0){//1, data
				if(br1>0 && br2>0){
					if(br1!=br2){
						pxGap=br2-br1;
						pxGap=Math.abs(pxGap);
					}else
					pxGap=0;
					
					if(br1==255 & br2==255)
					pxGap=1000;
				}
			}else if (BG2>0){//2, data
				if(br1<0.44 && bg2<0.54){
					BrGap=br1-BrBg;//BrBg=0.354862745;
					BgGap=bg2-BrBg;//BrBg=0.354862745;
					pxGap=BrGap+BgGap;
				}
			}
			//		IJ.log("pxGap; "+String.valueOf(pxGap)+"  BR1;"+String.valueOf(BR1)+", br1; "+String.valueOf(br1)+", BR2; "+String.valueOf(BR2)+", br2; "+String.valueOf(br2)+", BG2; "+String.valueOf(BG2)+", bg2; "+String.valueOf(bg2));
		}else if(BG1>0){//2, mask/////////////////////////////
			if(BG2>0){//2, data, 2,mask
				
				if(bg1>0 && bg2>0){
					if(bg1!=bg2){
						pxGap=bg2-bg1;
						pxGap=Math.abs(pxGap);
						
					}else if(bg1==bg2)
					pxGap=0;
					if(bg1==255 & bg2==255)
					pxGap=1000;
				}
				//	IJ.log(" pxGap BG2;"+String.valueOf(pxGap)+", bg1; "+String.valueOf(bg1)+", bg2; "+String.valueOf(bg2));
			}else if(GB2>0){//3, data, 2,mask
				if(bg1>0.8 && gb2>0.8){
					BgGap=BgGb-bg1;//BgGb=0.996078431;
					GbGap=BgGb-gb2;//BgGb=0.996078431;
					pxGap=BgGap+GbGap;
					//			IJ.log(" pxGap GB2;"+String.valueOf(pxGap));
				}
			}else if(BR2>0){//1, data, 2,mask
				if(bg1<0.54 && br2<0.44){
					BgGap=bg1-BrBg;//BrBg=0.354862745;
					BrGap=br2-BrBg;//BrBg=0.354862745;
					pxGap=BrGap+BgGap;
				}
			}
			//		IJ.log("pxGap; "+String.valueOf(pxGap)+"  BG1;"+String.valueOf(BG1)+"  BG2;"+String.valueOf(BG2)+", bg1; "+String.valueOf(bg1)+", bg2; "+String.valueOf(bg2)+", GB2; "+String.valueOf(GB2)+", gb2; "+String.valueOf(gb2)+", BR2; "+String.valueOf(BR2)+", br2; "+String.valueOf(br2));
		}else if(GB1>0){//3, mask/////////////////////////////
			if(GB2>0){//3, data, 3mask
				if(gb1>0 && gb2>0){
					if(gb1!=gb2){
						pxGap=gb2-gb1;
						pxGap=Math.abs(pxGap);
						
						//	IJ.log(" pxGap GB2;"+String.valueOf(pxGap));
					}else
					pxGap=0;
					if(gb1==255 & gb2==255)
					pxGap=1000;
				}
			}else if(BG2>0){//2, data, 3mask
				if(gb1>0.8 && bg2>0.8){
					BgGap=BgGb-gb1;//BgGb=0.996078431;
					GbGap=BgGb-bg2;//BgGb=0.996078431;
					pxGap=BgGap+GbGap;
				}
			}else if(GR2>0){//4, data, 3mask
				if(gb1<0.7 && gr2<0.7){
					GbGap=gb1-GbGr;//GbGr=0.505882353;
					GrGap=gr2-GbGr;//GbGr=0.505882353;
					pxGap=GbGap+GrGap;
				}
			}//2,3,4 data, 3mask
		}else if(GR1>0){//4mask/////////////////////////////
			if(GR2>0){//4, data, 4mask
				if(gr1>0 && gr2>0){
					if(gr1!=gr2){
						pxGap=gr2-gr1;
						pxGap=Math.abs(pxGap);
					}else
					pxGap=0;
					if(gr1==255 & gr2==255)
					pxGap=1000;
				}
			}else if(GB2>0){//3, data, 4mask
				if(gr1<0.7 && gb2<0.7){
					GrGap=gr1-GbGr;//GbGr=0.505882353;
					GbGap=gb2-GbGr;//GbGr=0.505882353;
					pxGap=GrGap+GbGap;
				}
			}else if(RG2>0){//5, data, 4mask
				if(gr1>0.8 && rg2>0.8){
					GrGap=GrRg-gr1;//GrRg=0.996078431;
					RgGap=GrRg-rg2;
					pxGap=GrGap+RgGap;
				}
			}//3,4,5 data
		}else if(RG1>0){//5, mask/////////////////////////////
			if(RG2>0){//5, data, 5mask
				if(rg1>0 && rg2>0){
					if(rg1!=rg2){
						pxGap=rg2-rg1;
						pxGap=Math.abs(pxGap);
					}else
					pxGap=0;
					if(rg1==255 & rg2==255)
					pxGap=1000;
				}
				
			}else if(GR2>0){//4 data, 5mask
				if(rg1>0.8 && gr2>0.8){
					GrGap=GrRg-gr2;//GrRg=0.996078431;
					RgGap=GrRg-rg1;//GrRg=0.996078431;
					pxGap=GrGap+RgGap;
					//	IJ.log(" pxGap GR2;"+String.valueOf(pxGap));
				}
			}else if(RB2>0){//6 data, 5mask
				if(rg1<0.7 && rb2<0.7){
					RgGap=rg1-RgRb;//RgRb=0.505882353;
					RbGap=rb2-RgRb;//RgRb=0.505882353;
					pxGap=RbGap+RgGap;
				}
			}//4,5,6 data
		}else if(RB1>0){//6, mask/////////////////////////////
			if(RB2>0){//6, data, 6mask
				if(rb1>0 && rb2>0){
					if(rb1!=rb2){
						pxGap=rb2-rb1;
						pxGap=Math.abs(pxGap);
					}else if(rb1==rb2)
					pxGap=0;
					if(rb1==255 & rb2==255)
					pxGap=1000;
				}
			}else if(RG2>0){//5, data, 6mask
				if(rg2<0.7 && rb1<0.7){
					RgGap=rg2-RgRb;//RgRb=0.505882353;
					RbGap=rb1-RgRb;//RgRb=0.505882353;
					pxGap=RgGap+RbGap;
					//	IJ.log(" pxGap RG;"+String.valueOf(pxGap));
				}
			}
		}//2 color advance core
		
		return pxGap;
	}
	
	public class ColorMIPMaskCompare {
		
		public class Output {
			int matchingPixNum;
			double matchingPct;
			public Output (int pixnum, double pct) {
				matchingPixNum = pixnum;
				matchingPct = pct;
			}
		}
		
		ImageProcessor m_query;
		ImageProcessor m_negquery;
		int[] m_mask;
		int[] m_negmask;
		int[][] m_tarmasklist;
		int[][] m_tarmasklist_mirror;
		int[][] m_tarnegmasklist;
		int[][] m_tarnegmasklist_mirror;
		int m_th;
		double m_pixfludub;
		
		boolean m_mirror;
		boolean m_mirrorneg;
		int m_xyshift;
		
		int m_width;
		int m_height;
		
		int m_maskpos_st;
		int m_maskpos_ed;
		
		
		
		//Basic Search
		ColorMIPMaskCompare (ImageProcessor query, int mask_th, int search_th, double toleranceZ) {
			m_query = query;
			m_width = m_query.getWidth();
			m_height = m_query.getHeight();
			
			m_mask = get_mskpos_array(m_query, mask_th);
			m_negmask = null;
			m_th = search_th;
			m_pixfludub = toleranceZ;
			m_mirror = false;
			m_mirrorneg = false;
			m_xyshift = 0;
			
			m_tarmasklist = new int[1][];
			m_tarmasklist_mirror = null;
			m_tarnegmasklist = null;
			m_tarnegmasklist_mirror = null;
			
			m_tarmasklist[0] = m_mask;
			
			m_maskpos_st = m_mask[0];
			m_maskpos_ed = m_mask[m_mask.length-1];
			
		}
		
		
		//Advanced Search
		ColorMIPMaskCompare (ImageProcessor query, int mask_th, boolean mirror_mask, ImageProcessor negquery, int negmask_th, boolean mirror_negmask, int search_th, double toleranceZ, int xyshift) {
			m_query = query;
			m_negquery = negquery;
			m_width = m_query.getWidth();
			m_height = m_query.getHeight();
			
			m_mask = get_mskpos_array(m_query, mask_th);
			if (m_negquery != null) m_negmask = get_mskpos_array(m_negquery, negmask_th);
			m_th = search_th;
			m_pixfludub = toleranceZ;
			m_mirror = mirror_mask;
			m_mirrorneg = mirror_negmask;
			m_xyshift = xyshift;
<<<<<<< HEAD
<<<<<<< HEAD
			
			m_horizontal = horizontal;
=======
>>>>>>> parent of f1d5276 (horizontal image)
=======
>>>>>>> parent of f1d5276 (horizontal image)
			
			//shifting
			m_tarmasklist = generate_shifted_masks(m_mask, m_xyshift, m_width, m_height);
			if (m_negquery != null) m_tarnegmasklist = generate_shifted_masks(m_negmask, m_xyshift, m_width, m_height);
			else m_tarnegmasklist = null;
			
			//mirroring
			if (m_mirror) {
				m_tarmasklist_mirror = new int[1+(xyshift/2)*8][];
				for (int i = 0; i < m_tarmasklist.length; i++)
				m_tarmasklist_mirror[i] = mirror_mask(m_tarmasklist[i], m_width);
			} else {
				m_tarmasklist_mirror = null;
			}
			if (m_mirrorneg && m_negquery != null) {
				m_tarnegmasklist_mirror = new int[1+(xyshift/2)*8][];
				for (int i = 0; i < m_tarnegmasklist.length; i++)
				m_tarnegmasklist_mirror[i] = mirror_mask(m_tarnegmasklist[i], m_width);
			} else {
				m_tarnegmasklist_mirror = null;
			}
			
			m_maskpos_st = m_width*m_height;
			m_maskpos_ed = 0;
			for (int i = 0; i < m_tarmasklist.length; i++) {
				if (m_tarmasklist[i][0] < m_maskpos_st) m_maskpos_st = m_tarmasklist[i][0];
				if (m_tarmasklist[i][m_tarmasklist[i].length-1] > m_maskpos_ed) m_maskpos_ed = m_tarmasklist[i][m_tarmasklist[i].length-1];
			}
			if (m_mirror) {
				for (int i = 0; i < m_tarmasklist_mirror.length; i++) {
					if (m_tarmasklist_mirror[i][0] < m_maskpos_st) m_maskpos_st = m_tarmasklist_mirror[i][0];
					if (m_tarmasklist_mirror[i][m_tarmasklist_mirror[i].length-1] > m_maskpos_ed) m_maskpos_ed = m_tarmasklist_mirror[i][m_tarmasklist_mirror[i].length-1];
				}
			}
			if (m_negquery != null) {
				for (int i = 0; i < m_tarnegmasklist.length; i++) {
					if (m_tarnegmasklist[i][0] < m_maskpos_st) m_maskpos_st = m_tarnegmasklist[i][0];
					if (m_tarnegmasklist[i][m_tarnegmasklist[i].length-1] > m_maskpos_ed) m_maskpos_ed = m_tarnegmasklist[i][m_tarnegmasklist[i].length-1];
				}
				if (m_mirrorneg) {
					for (int i = 0; i < m_tarnegmasklist_mirror.length; i++) {
						if (m_tarnegmasklist_mirror[i][0] < m_maskpos_st) m_maskpos_st = m_tarnegmasklist_mirror[i][0];
						if (m_tarnegmasklist_mirror[i][m_tarnegmasklist_mirror[i].length-1] > m_maskpos_ed) m_maskpos_ed = m_tarnegmasklist_mirror[i][m_tarnegmasklist_mirror[i].length-1];
					}
				}
			}
			
		}
		
		public int getMaskSize() {
			return m_mask.length;
		}
		
		public int getNegMaskSize() {
			return m_negmask != null ? m_negmask.length : 0;
		}
		
		public int getMaskStartPos() {
			return m_maskpos_st;
		}
		
		public int getMaskEndPos() {
			return m_maskpos_ed;
		}
		
		public void setThreshold(int th) {
			m_th = th;
		}
		
		public void setToleranceZ(double tolerance) {
			m_pixfludub = tolerance;
		}
		
		public Output runSearch(byte[] tarimg_in, byte[] coloc_out) {
			int posi = 0;
			double posipersent = 0.0;
			int masksize = m_mask.length;
			int negmasksize = m_negquery != null ? m_negmask.length : 0;
			
			for (int mid = 0; mid < m_tarmasklist.length; mid++) {
				int tmpposi = calc_scoreb(m_query, m_mask, tarimg_in, m_tarmasklist[mid], m_th, m_pixfludub, coloc_out);
				if (tmpposi > posi) {
					posi = tmpposi;
					posipersent= (double) posi/ (double) masksize;
				}
			}
			if (m_tarnegmasklist != null) {
				int nega = 0;
				double negapersent = 0.0;
				for (int mid = 0; mid < m_tarnegmasklist.length; mid++) {
					int tmpnega = calc_scoreb(m_negquery, m_negmask, tarimg_in, m_tarnegmasklist[mid], m_th, m_pixfludub, null);
					if (tmpnega > nega) {
						nega = tmpnega;
						negapersent = (double) nega/ (double) negmasksize;
					}
				}
				posipersent -= negapersent;
				posi = (int)Math.round((double)posi - (double)nega*((double)masksize/(double)negmasksize));
			}
			
			if (m_tarmasklist_mirror != null) {
				int mirror_posi = 0;
				double mirror_posipersent = 0.0;
				for (int mid = 0; mid < m_tarmasklist_mirror.length; mid++) {
					int tmpposi = calc_scoreb(m_query, m_mask, tarimg_in, m_tarmasklist_mirror[mid], m_th, m_pixfludub, coloc_out);
					if (tmpposi > mirror_posi) {
						mirror_posi = tmpposi;
						mirror_posipersent= (double) mirror_posi/ (double) masksize;
					}
				}
				if (m_tarnegmasklist_mirror != null) {
					int nega = 0;
					double negapersent = 0.0;
					for (int mid = 0; mid < m_tarnegmasklist_mirror.length; mid++) {
						int tmpnega = calc_scoreb(m_negquery, m_negmask, tarimg_in, m_tarnegmasklist_mirror[mid], m_th, m_pixfludub, null);
						if (tmpnega > nega) {
							nega = tmpnega;
							negapersent = (double) nega/ (double) negmasksize;
						}
					}
					mirror_posipersent -= negapersent;
					mirror_posi = (int)Math.round((double)mirror_posi - (double)nega*((double)masksize/(double)negmasksize));
				}
				if (posipersent < mirror_posipersent) {
					posi = mirror_posi;
					posipersent = mirror_posipersent;
				}
			}
			
			return new Output(posi, posipersent);
		}
		
		public Output runSearch(ImageProcessor tarimg_in, ImageProcessor coloc_out) {
			int posi = 0;
			double posipersent = 0.0;
			int masksize = m_mask.length;
			int negmasksize = m_negquery != null ? m_negmask.length : 0;
			
			for (int mid = 0; mid < m_tarmasklist.length; mid++) {
				int tmpposi = calc_score(m_query, m_mask, tarimg_in, m_tarmasklist[mid], m_th, m_pixfludub, coloc_out);
				if (tmpposi > posi) {
					posi = tmpposi;
					posipersent= (double) posi/ (double) masksize;
				}
			}
			if (m_tarnegmasklist != null) {
				int nega = 0;
				double negapersent = 0.0;
				for (int mid = 0; mid < m_tarnegmasklist.length; mid++) {
					int tmpnega = calc_score(m_negquery, m_negmask, tarimg_in, m_tarnegmasklist[mid], m_th, m_pixfludub, null);
					if (tmpnega > nega) {
						nega = tmpnega;
						negapersent = (double) nega/ (double) negmasksize;
					}
				}
				posipersent -= negapersent;
				posi = (int)Math.round((double)posi - (double)nega*((double)masksize/(double)negmasksize));
			}
			
			if (m_tarmasklist_mirror != null) {
				int mirror_posi = 0;
				double mirror_posipersent = 0.0;
				for (int mid = 0; mid < m_tarmasklist_mirror.length; mid++) {
					int tmpposi = calc_score(m_query, m_mask, tarimg_in, m_tarmasklist_mirror[mid], m_th, m_pixfludub, coloc_out);
					if (tmpposi > mirror_posi) {
						mirror_posi = tmpposi;
						mirror_posipersent= (double) mirror_posi/ (double) masksize;
					}
				}
				if (m_tarnegmasklist_mirror != null) {
					int nega = 0;
					double negapersent = 0.0;
					for (int mid = 0; mid < m_tarnegmasklist_mirror.length; mid++) {
						int tmpnega = calc_score(m_negquery, m_negmask, tarimg_in, m_tarnegmasklist_mirror[mid], m_th, m_pixfludub, null);
						if (tmpnega > nega) {
							nega = tmpnega;
							negapersent = (double) nega/ (double) negmasksize;
						}
					}
					mirror_posipersent -= negapersent;
					mirror_posi = (int)Math.round((double)mirror_posi - (double)nega*((double)masksize/(double)negmasksize));
				}
				if (posipersent < mirror_posipersent) {
					posi = mirror_posi;
					posipersent = mirror_posipersent;
				}
			}
			
			return new Output(posi, posipersent);
		}
		
		public int calc_scoreb(ImageProcessor src, int[] srcmaskposi, byte[] tar, int[] tarmaskposi, int th, double pixfludub, byte[] coloc_out) {
			
			int masksize = srcmaskposi.length <= tarmaskposi.length ? srcmaskposi.length : tarmaskposi.length;
			int posi = 0;
<<<<<<< HEAD
<<<<<<< HEAD
			
			int center = m_width * m_height / 2;
			
			HashSet<Integer> scset = new HashSet<Integer>();
=======
>>>>>>> parent of f1d5276 (horizontal image)
=======
>>>>>>> parent of f1d5276 (horizontal image)
			for(int masksig=0; masksig<masksize; masksig++){
				
				if (srcmaskposi[masksig] == -1 || tarmaskposi[masksig] == -1) continue;
				
				int pix1= src.get(srcmaskposi[masksig]);
				int red1 = (pix1>>>16) & 0xff;
				int green1 = (pix1>>>8) & 0xff;
				int blue1 = pix1 & 0xff;
				
				int p = tarmaskposi[masksig]*3;
				int red2 = tar[p] & 0xff;
				int green2 = tar[p+1] & 0xff;
				int blue2 = tar[p+2] & 0xff;
				
				if(red2>th || green2>th || blue2>th){
					
					double pxGap = calc_score_px(red1, green1, blue1, red2, green2, blue2); 
					
					if(pxGap<=pixfludub){
						if(coloc_out!=null) {
							coloc_out[p] = tar[p];
							coloc_out[p+1] = tar[p+1];
							coloc_out[p+2] = tar[p+2];
						}
<<<<<<< HEAD
<<<<<<< HEAD
						
						if (m_horizontal)
						{
							if (tarmaskposi[masksig] >= center)
							{
								if (!scset.contains(tarmaskposi[masksig]-center))
								posi++;
							}
							else
							scset.add(tarmaskposi[masksig]);
						}
						else
=======
>>>>>>> parent of f1d5276 (horizontal image)
=======
>>>>>>> parent of f1d5276 (horizontal image)
						posi++;
					}
					
				}
			}
<<<<<<< HEAD
<<<<<<< HEAD
			
			if (m_horizontal)
			posi += scset.size();
=======
>>>>>>> parent of f1d5276 (horizontal image)
=======
>>>>>>> parent of f1d5276 (horizontal image)
			
			return posi;
			
		}
<<<<<<< HEAD
<<<<<<< HEAD
		
		public int calc_score(ImageProcessor src, int[] srcmaskposi, ImageProcessor tar, int[] tarmaskposi, int th, double pixfludub, ImageProcessor coloc_out) {
			
			int masksize = srcmaskposi.length <= tarmaskposi.length ? srcmaskposi.length : tarmaskposi.length;
			int posi = 0;
			
			int center = m_width * m_height / 2;
			
			HashSet<Integer> scset = new HashSet<Integer>();
			for(int masksig=0; masksig<masksize; masksig++){
				
				if (srcmaskposi[masksig] == -1 || tarmaskposi[masksig] == -1) continue;
				
				int pix1= src.get(srcmaskposi[masksig]);
				int red1 = (pix1>>>16) & 0xff;
				int green1 = (pix1>>>8) & 0xff;
				int blue1 = pix1 & 0xff;
				
				int pix2= tar.get(tarmaskposi[masksig]);
				int red2 = (pix2>>>16) & 0xff;
				int green2 = (pix2>>>8) & 0xff;
				int blue2 = pix2 & 0xff;
				
				if(red2>th || green2>th || blue2>th){
					
					double pxGap = calc_score_px(red1, green1, blue1, red2, green2, blue2); 
					
					if(pxGap<=pixfludub){
						if(coloc_out!=null)
						coloc_out.set(tarmaskposi[masksig], pix2);
						
						if (m_horizontal)
						{
							if (tarmaskposi[masksig] >= center)
							{
								if (!scset.contains(tarmaskposi[masksig]-center))
								posi++;
							}
							else
							scset.add(tarmaskposi[masksig]);
						}
						else
						posi++;
					}	
				}
			}
			
			if (m_horizontal)
			posi += scset.size();
			
			return posi;
			
		}
		
		public double calc_score_px(int red1, int green1, int blue1, int red2, int green2, int blue2) {
			int RG1=0; int BG1=0; int GR1=0; int GB1=0; int RB1=0; int BR1=0;
			int RG2=0; int BG2=0; int GR2=0; int GB2=0; int RB2=0; int BR2=0;
			double rb1=0; double rg1=0; double gb1=0; double gr1=0; double br1=0; double bg1=0;
			double rb2=0; double rg2=0; double gb2=0; double gr2=0; double br2=0; double bg2=0;
			double pxGap=10000; 
			double BrBg=0.354862745; double BgGb=0.996078431; double GbGr=0.505882353; double GrRg=0.996078431; double RgRb=0.505882353;
			double BrGap=0; double BgGap=0; double GbGap=0; double GrGap=0; double RgGap=0; double RbGap=0;
			
			if(blue1>red1 && blue1>green1){//1,2
				if(red1>green1){
					BR1=blue1+red1;//1
					if(blue1!=0 && red1!=0)
					br1= (double) red1 / (double) blue1;
				}else{
					BG1=blue1+green1;//2
					if(blue1!=0 && green1!=0)
					bg1= (double) green1 / (double) blue1;
				}
			}else if(green1>blue1 && green1>red1){//3,4
				if(blue1>red1){
					GB1=green1+blue1;//3
					if(green1!=0 && blue1!=0)
					gb1= (double) blue1 / (double) green1;
				}else{
					GR1=green1+red1;//4
					if(green1!=0 && red1!=0)
					gr1= (double) red1 / (double) green1;
				}
			}else if(red1>blue1 && red1>green1){//5,6
				if(green1>blue1){
					RG1=red1+green1;//5
					if(red1!=0 && green1!=0)
					rg1= (double) green1 / (double) red1;
				}else{
					RB1=red1+blue1;//6
					if(red1!=0 && blue1!=0)
					rb1= (double) blue1 / (double) red1;
				}
			}
			
			if(blue2>red2 && blue2>green2){
				if(red2>green2){//1, data
					BR2=blue2+red2;
					if(blue2!=0 && red2!=0)
					br2= (double) red2 / (double) blue2;
				}else{//2, data
					BG2=blue2+green2;
					if(blue2!=0 && green2!=0)
					bg2= (double) green2 / (double) blue2;
				}
			}else if(green2>blue2 && green2>red2){
				if(blue2>red2){//3, data
					GB2=green2+blue2;
					if(green2!=0 && blue2!=0)
					gb2= (double) blue2 / (double) green2;
				}else{//4, data
					GR2=green2+red2;
					if(green2!=0 && red2!=0)
					gr2= (double) red2 / (double) green2;
				}
			}else if(red2>blue2 && red2>green2){
				if(green2>blue2){//5, data
					RG2=red2+green2;
					if(red2!=0 && green2!=0)
					rg2= (double) green2 / (double) red2;
				}else{//6, data
					RB2=red2+blue2;
					if(red2!=0 && blue2!=0)
					rb2= (double) blue2 / (double) red2;
				}
			}
			
			///////////////////////////////////////////////////////					
			if(BR1>0){//1, mask// 2 color advance core
				if(BR2>0){//1, data
					if(br1>0 && br2>0){
						if(br1!=br2){
							pxGap=br2-br1;
							pxGap=Math.abs(pxGap);
						}else
						pxGap=0;
						
						if(br1==255 & br2==255)
						pxGap=1000;
					}
				}else if (BG2>0){//2, data
					if(br1<0.44 && bg2<0.54){
						BrGap=br1-BrBg;//BrBg=0.354862745;
						BgGap=bg2-BrBg;//BrBg=0.354862745;
						pxGap=BrGap+BgGap;
					}
				}
				//		IJ.log("pxGap; "+String.valueOf(pxGap)+"  BR1;"+String.valueOf(BR1)+", br1; "+String.valueOf(br1)+", BR2; "+String.valueOf(BR2)+", br2; "+String.valueOf(br2)+", BG2; "+String.valueOf(BG2)+", bg2; "+String.valueOf(bg2));
			}else if(BG1>0){//2, mask/////////////////////////////
				if(BG2>0){//2, data, 2,mask
					
					if(bg1>0 && bg2>0){
						if(bg1!=bg2){
							pxGap=bg2-bg1;
							pxGap=Math.abs(pxGap);
							
						}else if(bg1==bg2)
						pxGap=0;
						if(bg1==255 & bg2==255)
						pxGap=1000;
					}
					//	IJ.log(" pxGap BG2;"+String.valueOf(pxGap)+", bg1; "+String.valueOf(bg1)+", bg2; "+String.valueOf(bg2));
				}else if(GB2>0){//3, data, 2,mask
					if(bg1>0.8 && gb2>0.8){
						BgGap=BgGb-bg1;//BgGb=0.996078431;
						GbGap=BgGb-gb2;//BgGb=0.996078431;
						pxGap=BgGap+GbGap;
						//			IJ.log(" pxGap GB2;"+String.valueOf(pxGap));
					}
				}else if(BR2>0){//1, data, 2,mask
					if(bg1<0.54 && br2<0.44){
						BgGap=bg1-BrBg;//BrBg=0.354862745;
						BrGap=br2-BrBg;//BrBg=0.354862745;
						pxGap=BrGap+BgGap;
					}
				}
				//		IJ.log("pxGap; "+String.valueOf(pxGap)+"  BG1;"+String.valueOf(BG1)+"  BG2;"+String.valueOf(BG2)+", bg1; "+String.valueOf(bg1)+", bg2; "+String.valueOf(bg2)+", GB2; "+String.valueOf(GB2)+", gb2; "+String.valueOf(gb2)+", BR2; "+String.valueOf(BR2)+", br2; "+String.valueOf(br2));
			}else if(GB1>0){//3, mask/////////////////////////////
				if(GB2>0){//3, data, 3mask
					if(gb1>0 && gb2>0){
						if(gb1!=gb2){
							pxGap=gb2-gb1;
							pxGap=Math.abs(pxGap);
							
							//	IJ.log(" pxGap GB2;"+String.valueOf(pxGap));
						}else
						pxGap=0;
						if(gb1==255 & gb2==255)
						pxGap=1000;
					}
				}else if(BG2>0){//2, data, 3mask
					if(gb1>0.8 && bg2>0.8){
						BgGap=BgGb-gb1;//BgGb=0.996078431;
						GbGap=BgGb-bg2;//BgGb=0.996078431;
						pxGap=BgGap+GbGap;
					}
				}else if(GR2>0){//4, data, 3mask
					if(gb1<0.7 && gr2<0.7){
						GbGap=gb1-GbGr;//GbGr=0.505882353;
						GrGap=gr2-GbGr;//GbGr=0.505882353;
						pxGap=GbGap+GrGap;
					}
				}//2,3,4 data, 3mask
			}else if(GR1>0){//4mask/////////////////////////////
				if(GR2>0){//4, data, 4mask
					if(gr1>0 && gr2>0){
						if(gr1!=gr2){
							pxGap=gr2-gr1;
							pxGap=Math.abs(pxGap);
						}else
						pxGap=0;
						if(gr1==255 & gr2==255)
						pxGap=1000;
					}
				}else if(GB2>0){//3, data, 4mask
					if(gr1<0.7 && gb2<0.7){
						GrGap=gr1-GbGr;//GbGr=0.505882353;
						GbGap=gb2-GbGr;//GbGr=0.505882353;
						pxGap=GrGap+GbGap;
					}
				}else if(RG2>0){//5, data, 4mask
					if(gr1>0.8 && rg2>0.8){
						GrGap=GrRg-gr1;//GrRg=0.996078431;
						RgGap=GrRg-rg2;
						pxGap=GrGap+RgGap;
					}
				}//3,4,5 data
			}else if(RG1>0){//5, mask/////////////////////////////
				if(RG2>0){//5, data, 5mask
					if(rg1>0 && rg2>0){
						if(rg1!=rg2){
							pxGap=rg2-rg1;
							pxGap=Math.abs(pxGap);
						}else
						pxGap=0;
						if(rg1==255 & rg2==255)
						pxGap=1000;
					}
					
				}else if(GR2>0){//4 data, 5mask
					if(rg1>0.8 && gr2>0.8){
						GrGap=GrRg-gr2;//GrRg=0.996078431;
						RgGap=GrRg-rg1;//GrRg=0.996078431;
						pxGap=GrGap+RgGap;
						//	IJ.log(" pxGap GR2;"+String.valueOf(pxGap));
					}
				}else if(RB2>0){//6 data, 5mask
					if(rg1<0.7 && rb2<0.7){
						RgGap=rg1-RgRb;//RgRb=0.505882353;
						RbGap=rb2-RgRb;//RgRb=0.505882353;
						pxGap=RbGap+RgGap;
					}
				}//4,5,6 data
			}else if(RB1>0){//6, mask/////////////////////////////
				if(RB2>0){//6, data, 6mask
					if(rb1>0 && rb2>0){
						if(rb1!=rb2){
							pxGap=rb2-rb1;
							pxGap=Math.abs(pxGap);
						}else if(rb1==rb2)
						pxGap=0;
						if(rb1==255 & rb2==255)
						pxGap=1000;
					}
				}else if(RG2>0){//5, data, 6mask
					if(rg2<0.7 && rb1<0.7){
						RgGap=rg2-RgRb;//RgRb=0.505882353;
						RbGap=rb1-RgRb;//RgRb=0.505882353;
						pxGap=RgGap+RbGap;
						//	IJ.log(" pxGap RG;"+String.valueOf(pxGap));
					}
				}
			}//2 color advance core
			
			return pxGap;
		}
=======
>>>>>>> parent of f1d5276 (horizontal image)
=======
>>>>>>> parent of f1d5276 (horizontal image)
		
	}//public class ColorMIPMaskCompare {
	
} //public class Two_windows_mask_search implements PlugInFilter{


