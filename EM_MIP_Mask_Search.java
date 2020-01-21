import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;



import ij.*;
import ij.gui.*;
import ij.io.*;

import ij.plugin.filter.*;
import ij.process.*;
import ij.process.ImageProcessor;

public class EM_MIP_Mask_Search implements PlugInFilter
{
	ImagePlus imp, imp2;
	ImageProcessor ip1, nip1, ip2, ip3, ip4, ip5, ip6, ip33;
	int pix1=0, CheckPost,UniqueLineName=0,IsPosi;
	int pix3=0,Check=0,arrayPosition=0,dupdel=1,FinalAdded=1,enddup=0;;
	ImagePlus newimp, newimpOri;
	String linename,LineNo, LineNo2,preLineNo="A",FullName,LineName,arrayName,PostName;
	String args [] = new String[10],PreFullLineName,ScorePre,TopShortLinename;
	
	ExecutorService m_executor;
	
	boolean DUPlogonE;
	
	public class SearchResult{
		String m_name;
		int m_sid;
		long m_offset;
		byte[] m_pixels;
		byte[] m_colocs;
		ImageProcessor m_iporg;
		ImageProcessor m_ipcol;
		SearchResult(String name, int sid, long offset, byte[] pxs, byte[] coloc, ImageProcessor iporg, ImageProcessor ipcol){
			m_name = name;
			m_sid = sid;
			m_offset = offset;
			m_pixels = pxs;
			m_colocs = coloc;
			m_iporg = iporg;
			m_ipcol = ipcol;
		}
	}
	
	public int setup(String arg, ImagePlus imp)
	{
		IJ.register (EM_MIP_Mask_Search.class);
		if (IJ.versionLessThan("1.32c")){
			IJ.showMessage("Error", "Please Update ImageJ.");
			return 0;
		}
		
		int wList [] = WindowManager.getIDList();
		if (wList==null || wList.length<2) {
			IJ.showMessage("There should be at least two windows open; open the stack for search and a mask");
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
		int MaskE=(int)Prefs.get("MaskE.int",0);
		boolean mirror_maskE=(boolean)Prefs.get("mirror_maskE.boolean",false);
		int NegMaskE=(int)Prefs.get("NegMaskE.int",0);
		boolean mirror_negmaskE=(boolean)Prefs.get("mirror_negmaskE.boolean",false);
		int datafileE=(int)Prefs.get("datafileE.int",1);
		int ThresE=(int)Prefs.get("ThresE.int",100);
		double pixThresE=(double)Prefs.get("pixThresE.double",1);
		int duplineE=(int)Prefs.get("duplineE.int",1);
		int colormethodE=(int)Prefs.get("colormethodE.int",1);
		double pixfluE=(double)Prefs.get("pixfluE.double",1);
		int xyshiftE=(int)Prefs.get("xyshiftE.int",0);
		boolean logonE=(boolean)Prefs.get("logonE.boolean",false);
		int ThresmE=(int)Prefs.get("ThresmE.int",50);
		int NegThresmE=(int)Prefs.get("NegThresmE.int",50);
		boolean logNanE=(boolean)Prefs.get("logNanE.boolean",false);
		int labelmethodE=(int)Prefs.get("labelmethodE.int",0);
		boolean DUPlogonE=(boolean)Prefs.get("DUPlogonE.boolean",false);
		boolean GCONE=(boolean)Prefs.get("GCONE.boolean",false);
		boolean ShowCoE=(boolean)Prefs.get("ShowCoE.boolean",false);
		int NumberSTintE=(int)Prefs.get("NumberSTintE.int",0);
		int threadNumE=(int)Prefs.get("threadNumE.int",8);
		String gradientDIR_=(String)Prefs.get("gradientDIR_.String","");
		boolean GradientOnTheFly_ = (boolean)Prefs.get("GradientOnTheFly_.boolean", false);
		int maxnumber=(int)Prefs.get("maxnumber.int",100);
		boolean shownormal=(boolean)Prefs.get("shownormal.boolean",false);
		
		if(datafileE >= imageno){
			int singleslice=0; int Maxsingleslice=0; int MaxStack=0;
			
			for(int isliceSearch=0; isliceSearch<wList.length; isliceSearch++){
				singleslice=slices[isliceSearch];
				
				if(singleslice>Maxsingleslice){
					Maxsingleslice=singleslice;
					MaxStack=isliceSearch;
				}
			}
			datafileE=MaxStack;
		}
		
		if(MaskE >= imageno){
			int singleslice=0; int isliceSearch=0;
			
			while(singleslice!=1){
				singleslice=slices[isliceSearch];
				isliceSearch=isliceSearch+1;
				
				if(isliceSearch>imageno){
					IJ.showMessage("Need to be a single slice open");
					return;
				}
			}
			MaskE=isliceSearch-1;
		}
		
		if(NegMaskE >= imageno+1)
		NegMaskE = 0;
		
		
		ImagePlus impMask = WindowManager.getImage(wList[MaskE]);
		int MaskSliceNum = impMask.getStackSize();
		
		ImagePlus impData = WindowManager.getImage(wList[datafileE]);
		int DataSliceNum = impData.getStackSize();
		
		if(MaskSliceNum!=1){
			MaskE=SingleSliceMIPnum;
		}
		
		
		if(DataSliceNum==1){
			datafileE=MultiSliceStack;
		}
		
		
		//	IJ.log("mask; "+String.valueOf(MaskE)+"datafileE; "+String.valueOf(datafileE)+"imageno; "+String.valueOf(imageno)+"wList.length; "+String.valueOf(wList.length));
		if(labelmethodE>1)
		labelmethodE=1;
		
		GenericDialog gd = new GenericDialog("ColorDepthMIP_EM_Mask search");
		gd.addChoice("Mask", titles, titles[MaskE]); //MaskE
		gd.addSlider("1.Threshold for mask", 0, 255, ThresmE);
		gd.setInsets(0, 340, 0);
		gd.addCheckbox("1.Add mirror search", mirror_maskE);
		
		gd.setInsets(20, 0, 0);
		gd.addChoice("Negative Mask", negtitles, negtitles[NegMaskE]); //Negative MaskE
		gd.addSlider("2.Threshold for negative mask", 0, 255, NegThresmE);
		gd.setInsets(0, 340, 0);
		gd.addCheckbox("2.Add mirror search", mirror_negmaskE);
		
		gd.setInsets(20, 0, 0);
		gd.addChoice("EM_color_MIP Data for the search", titles, titles[datafileE]); //Data
		
		//gd.addNumericField("Threshold", slicenumber,0);
	//	gd.addSlider("3.Threshold for data", 0, 255, ThresE);
	//	gd.addMessage("");
		//	gd.addSlider("100x % of Positive PX Threshold", (double) 0, (double) 10000, pixThresE);
		
		gd.setInsets(20, 0, 0);
		gd.addNumericField("Positive PX % Threshold: EM matching is 0.5-1.5%", pixThresE, 4);
		gd.addSlider("Pix Color Fluctuation, +- Z slice", 0, 10, pixfluE);
		
		gd.setInsets(20, 0, 0);
		gd.addStringField("Gradient file path: ", gradientDIR_,50);
		
		gd.setInsets(20, 220, 0);// top, left, bottom
		gd.addCheckbox("Show Normal_MIP_search_result before the shape matching", shownormal);
		//		gd.addCheckbox("GradientOnTheFly; (slower with ON)",GradientOnTheFly_);
		gd.setInsets(20, 0, 0);
		gd.addNumericField("Max number of the hits", maxnumber, 0);
		
		
		//	String[] dupnumstr = new String[11];
		//	for (int i = 0; i < dupnumstr.length; i++)
		//	dupnumstr[i] = Integer.toString(i);
		//	gd.addChoice("Duplicated line numbers; (only for GMR & VT), 0 = no check", dupnumstr, dupnumstr[duplineE]); //MaskE
		
		gd.setInsets(20, 0, 0);
		gd.addNumericField("Thread", threadNumE, 0);
		
		
		gd.setInsets(0, 362, 5);
		String []	shitstr = {"0px    ", "2px    ", "4px    "};
		gd.addRadioButtonGroup("XY Shift: ", shitstr, 1, 3, shitstr[xyshiftE/2]);
		
	//	gd.setInsets(0, 362, 5);
	//	String []	NumberST = {"%", "absolute value"};
	//	gd.addRadioButtonGroup("Scoring method; ", NumberST, 1, 2, NumberST[NumberSTintE]);
		
		gd.setInsets(20, 372, 0);
		gd.addCheckbox("ShowLog",logonE);
		
		gd.setInsets(20, 372, 0);
		gd.addCheckbox("Clear memory before search. Slow at beginning but fast search",GCONE);
		
		gd.setInsets(20, 372, 0);
		gd.addCheckbox("Co-localized stack shown (more memory needs)",ShowCoE);
		
		gd.showDialog();
		if(gd.wasCanceled()){
			return;
		}
		
		MaskE = gd.getNextChoiceIndex(); //MaskE
		ThresmE=(int)gd.getNextNumber();
		mirror_maskE = gd.getNextBoolean();
		NegMaskE = gd.getNextChoiceIndex(); //Negative MaskE
		NegThresmE=(int)gd.getNextNumber();
		mirror_negmaskE = gd.getNextBoolean();
		datafileE = gd.getNextChoiceIndex(); //Color MIP
	//	ThresE=(int)gd.getNextNumber();
		pixThresE=(double)gd.getNextNumber();
		pixfluE=(double)gd.getNextNumber();
		gradientDIR_=gd.getNextString();
		GradientOnTheFly_ = false;//gd.getNextBoolean();
		shownormal = gd.getNextBoolean();
		maxnumber=(int)gd.getNextNumber();
		duplineE=0;
		DUPlogonE = false;
		threadNumE = (int)gd.getNextNumber();
		xyshiftE=Integer.parseInt( ((String)gd.getNextRadioButton()).substring(0,1) );
		
		String thremethodSTR="Two windows";
		String labelmethodSTR="overlap value + line name";
		String ScoringM="%";//(String)gd.getNextRadioButton();
		logonE = gd.getNextBoolean();
		GCONE = gd.getNextBoolean();
		ShowCoE = gd.getNextBoolean();
		boolean EMsearch = true;
		
		IJ.log("gradientDIR_; "+gradientDIR_);
		
		File file = new File(gradientDIR_);
		boolean graExt = file.exists();
		
		if(graExt==false){
			
			IJ.log("Choose gradient tiff directory");
			DirectoryChooser dirGradient = new DirectoryChooser("Gradient tiff directory");
			gradientDIR_ = dirGradient.getDirectory();
		}
		Prefs.set("gradientDIR_.String",gradientDIR_);
		Prefs.set("GradientOnTheFly_.boolean", GradientOnTheFly_);
		
		if(GCONE==true)
		System.gc();
		
		if(logonE==true){
			GenericDialog gd2 = new GenericDialog("log option");
			gd2.addCheckbox("ShowNaN",logNanE);
			
			gd2.showDialog();
			if(gd2.wasCanceled()){
				return;
			}
			String logmethodSTR=(String)gd2.getNextRadioButton();
			logNanE = gd2.getNextBoolean();
			Prefs.set("logNanE.boolean",logNanE);
		}//if(logonE==true){
		
		colormethodE=1;
		if(thremethodSTR=="Combine")
		colormethodE=0;
		
		
		if(labelmethodSTR=="overlap value")
		labelmethodE=0;//on top
		if(labelmethodSTR=="overlap value + line name")
		labelmethodE=1;
		
		if(ScoringM=="%")
		NumberSTintE=0;
		else
		NumberSTintE=1;
		
		Prefs.set("MaskE.int", MaskE);
		Prefs.set("mirror_maskE.boolean", mirror_maskE);
		Prefs.set("mirror_negmaskE.boolean", mirror_negmaskE);
		Prefs.set("ThresmE.int", ThresmE);
		Prefs.set("NegMaskE.int", NegMaskE);
		Prefs.set("NegThresmE.int", NegThresmE);
		Prefs.set("pixThresE.double", pixThresE);
		Prefs.set("ThresE.int", ThresE);
		Prefs.set("datafileE.int",datafileE);
		Prefs.set("colormethodE.int",colormethodE);
		Prefs.set("pixfluE.double", pixfluE);
		Prefs.set("xyshiftE.int",xyshiftE);
		Prefs.set("logonE.boolean",logonE);
		Prefs.set("labelmethodE.int",labelmethodE);
		Prefs.set("DUPlogonE.boolean",DUPlogonE);
		Prefs.set("duplineE.int",duplineE);
		Prefs.set("GCONE.boolean",GCONE);
		Prefs.set("ShowCoE.boolean",ShowCoE);
		Prefs.set("NumberSTintE.int",NumberSTintE);
		Prefs.set("threadNumE.int",threadNumE);
		Prefs.set("maxnumber.int",maxnumber);
		Prefs.set("shownormal.boolean",shownormal);
		
		double pixfludub=pixfluE/100;
		//	IJ.log(" pixfludub;"+String.valueOf(pixfludub));
		
		final double pixThresdub = pixThresE/100;///10000
		//	IJ.log(" pixThresdub;"+String.valueOf(pixThresdub));
		///////		
		ImagePlus imask = WindowManager.getImage(wList[MaskE]); //MaskE
		ImagePlus inegmask = NegMaskE > 0 ? WindowManager.getImage(wList[NegMaskE-1]) : null; //Negative MaskE
		titles[MaskE] = imask.getTitle();
		if (inegmask != null) negtitles[NegMaskE] = inegmask.getTitle();
		ImagePlus idata = WindowManager.getImage(wList[datafileE]); //Data
		
		ip1 = imask.getProcessor(); //MaskE
		nip1 = NegMaskE > 0 ? inegmask.getProcessor() : null; //Negative MaskE
		int slicenumber = idata.getStackSize();
		
		int width = imask.getWidth();
		int height = imask.getHeight();
		
		if(IJ.escapePressed())
		return;
		
		
		IJ.showProgress(0.0);
		
		//	IJ.log("maxvalue; "+maxvalue2+"	 gap;	"+gap);
		
		
		final ImageStack st3 = idata.getStack();
		int posislice = 0;
		
		double posipersent2 = 0;
		double pixThresdub2 = 0;
		
		final ColorMIPMaskCompare cc = new ColorMIPMaskCompare (ip1, ThresmE, mirror_maskE, nip1, NegThresmE, mirror_negmaskE, ThresE, pixfludub, xyshiftE);
		m_executor = Executors.newFixedThreadPool(threadNumE);
		
		final int maskpos_st = cc.getMaskStartPos()*3;
		final int maskpos_ed = cc.getMaskEndPos()*3;
		final int stripsize = maskpos_ed-maskpos_st+3;
		
		long start, end;
		start = System.currentTimeMillis();
		
		//IJ.log(" masksize;"+String.valueOf(masksize));
		
		ArrayList<String> srlabels = new ArrayList<String>();
		ArrayList<String> finallbs = new ArrayList<String>();
		HashMap<String, SearchResult> srdict = new HashMap<String, SearchResult>(1000);
		
		final int fslicenum = slicenumber;
		final int fthreadnum = threadNumE;
		final boolean fShowCo = ShowCoE;
		final boolean flogon = logonE;
		final boolean flogNan = logNanE;
		final int fNumberSTint = NumberSTintE;
		final int flabelmethod = labelmethodE;
		if (st3.isVirtual()) {
			final VirtualStack vst = (VirtualStack)st3;
			if (vst.getDirectory() == null) {
				IJ.log("Virtual Stack (stack)");
				
				final FileInfo fi = idata.getOriginalFileInfo();
				if (fi.directory.length()>0 && !(fi.directory.endsWith(Prefs.separator)||fi.directory.endsWith("/")))
				fi.directory += Prefs.separator;
				final String datapath = fi.directory + fi.fileName;
				final long size = fi.width*fi.height*fi.getBytesPerPixel();
				
				final List<Callable<ArrayList<SearchResult>>> tasks = new ArrayList<Callable<ArrayList<SearchResult>>>();
				for (int ithread = 0; ithread < threadNumE; ithread++) {
					final int ftid = ithread;
					final int f_th_snum = fslicenum/fthreadnum;
					tasks.add(new Callable<ArrayList<SearchResult>>() {
							public ArrayList<SearchResult> call() {
								ArrayList<SearchResult> out = new ArrayList<SearchResult>();
								RandomAccessFile f = null;
								try {
									f = new RandomAccessFile(datapath, "r");
									for (int slice = fslicenum/fthreadnum*ftid+1, count = 0; slice <= fslicenum && count < f_th_snum; slice++, count++) {
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
											}else if(fNumberSTint==1){
												String posiST=getZeroFilledNumString(posi, 4);
												title = (flabelmethod==0 || flabelmethod==1) ? posiST+"_"+linename : linename+"_"+posiST;
											}
											out.add(new SearchResult(title, slice, loffset, impxs, colocs, null, null));
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
				
			} else {
				
				String dirtmp = vst.getDirectory();
				IJ.log("Virtual Stack; "+dirtmp);
				if (dirtmp.length()>0 && !(dirtmp.endsWith(Prefs.separator)||dirtmp.endsWith("/")))
				dirtmp += Prefs.separator;
				final String directory = dirtmp;
				final long size = width*height*3;
				final List<Callable<ArrayList<SearchResult>>> tasks = new ArrayList<Callable<ArrayList<SearchResult>>>();
				for (int ithread = 0; ithread < threadNumE; ithread++) {
					final int ftid = ithread;
					final int f_th_snum = fslicenum/fthreadnum;
					tasks.add(new Callable<ArrayList<SearchResult>>() {
							public ArrayList<SearchResult> call() {
								ArrayList<SearchResult> out = new ArrayList<SearchResult>();
								for (int slice = fslicenum/fthreadnum*ftid+1, count = 0; slice <= fslicenum && count < f_th_snum; slice++, count++) {
									byte [] impxs = new byte[(int)size];
									byte [] colocs = null;
									if (fShowCo) colocs = new byte[(int)size];
									String datapath = directory + vst.getFileName(slice);
									
									if (ftid == 0)
									IJ.showProgress((double)slice/(double)f_th_snum);
									
									try {
										RandomAccessFile f = new RandomAccessFile(datapath, "r");
										TiffDecoder tfd = new TiffDecoder(directory, vst.getFileName(slice));
										if (tfd == null) continue;
										FileInfo[] fi_list = tfd.getTiffInfo();
										if (fi_list == null) continue;
										long loffset = fi_list[0].getOffset();
										
										f.seek(loffset+(long)maskpos_st);
										f.read(impxs, maskpos_st, stripsize);
										
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
											}else if(fNumberSTint==1) {
												String posiST=getZeroFilledNumString(posi, 4);
												title = (flabelmethod==0 || flabelmethod==1) ? posiST+"_"+linename : linename+"_"+posiST;
											}
											out.add(new SearchResult(title, slice, loffset, impxs, colocs, null, null));
											if (ftid == 0)
											IJ.showStatus("Number of Hits (estimated): "+out.size()*fthreadnum);
										}
										f.close();
									} catch (IOException e) {
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
			
		} else {
			final List<Callable<ArrayList<SearchResult>>> tasks = new ArrayList<Callable<ArrayList<SearchResult>>>();
			for (int ithread = 0; ithread < threadNumE; ithread++) {
				final int ftid = ithread;
				final int f_th_snum = fslicenum/fthreadnum;
				tasks.add(new Callable<ArrayList<SearchResult>>() {
						public ArrayList<SearchResult> call() {
							ArrayList<SearchResult> out = new ArrayList<SearchResult>();
							for (int slice = fslicenum/fthreadnum*ftid+1, count = 0; slice <= fslicenum && count < f_th_snum; slice++, count++) {
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
									out.add(new SearchResult(title, slice, 0L, null, null, ip3, ipnew));
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
		
		IJ.showStatus("Number of Hits: "+String.valueOf(posislice));
		IJ.log(" positive slice No.;"+String.valueOf(posislice));
		
		int PositiveSlices=posislice;
		
		String OverlapValueLineArray [] = new String [posislice];
		for(int format=0; format<posislice; format++)
		OverlapValueLineArray [format]="0";
		
		String LineNameArray [] = new String[posislice];
		
		if(posislice>0){// if result is exist
			int posislice2=posislice;
			
			if(posislice==1)
			duplineE=0;
			String linenameTmpo;
			
			for(int CreateLineArray=0; CreateLineArray<posislice; CreateLineArray++){
				linenameTmpo = srlabels.get(CreateLineArray);
				
				int GMRPosi=(linenameTmpo.indexOf("GMR"));
				int RPosi=(linenameTmpo.indexOf("R"));
				
				int JRCPosi=(linenameTmpo.indexOf("JRC_"));
				int BJDPosi=(linenameTmpo.indexOf("BJD"));
				int DotPosi=(linenameTmpo.indexOf("."));
				int VTPosi=(linenameTmpo.indexOf("VT"));
				
				if(GMRPosi==-1 && RPosi==-1 && JRCPosi==-1 && BJDPosi==-1 && VTPosi==-1)
				duplineE=0;
				
				if(JRCPosi!=-1)
				RPosi=-1;
				
				if(RPosi!=-1 && GMRPosi==-1){// it is R
					
					int UnderS2=(linenameTmpo.indexOf("_", RPosi+2 ));// end of line number
					
					LineNo=linenameTmpo.substring(RPosi+2, UnderS2);// R_01A02
				}else if(GMRPosi!=-1){// it is GMR 
					int UnderS1=(linenameTmpo.indexOf("_", GMRPosi+1));
					int UnderS2=(linenameTmpo.indexOf("_", UnderS1+1 ));// end of line number
					
					LineNo=linenameTmpo.substring(GMRPosi, UnderS2);// GMR_01A02
				}else if(VTPosi!=-1){//if VT
					int UnderS1=(linenameTmpo.indexOf("_", VTPosi+1));
					LineNo=linenameTmpo.substring(VTPosi, UnderS1);// VT00002
				}else if(JRCPosi!=-1){
					int UnderS1=(linenameTmpo.indexOf("_", JRCPosi+1));
					int UnderS2=(linenameTmpo.indexOf("_", UnderS1+1 ));
					
					LineNo=linenameTmpo.substring(JRCPosi, UnderS2);// GMR_01A02
				}else if(BJDPosi!=-1){
					int UnderS1=(linenameTmpo.indexOf("_", BJDPosi+1));
					int UnderS2=(linenameTmpo.indexOf("_", UnderS1+1 ));
					
					LineNo=linenameTmpo.substring(BJDPosi, UnderS2);// GMR_01A02
				}else{
					LineNo=linenameTmpo.substring(0, DotPosi);
				}
				
				String posipersent2ST;
				if(labelmethodE==0 || labelmethodE==1){// on top score
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
			if(duplineE!=0){
				
				//	LineNameArray[posislice]="Z,0,Z";
				
				
				//// scan complete line name list and copy the list to new positive list //////////////
				String[] FinalPosi = new String [posislice];
				int duplicatedLine=0;
				
				for(int LineInt=0; LineInt<posislice; LineInt++)
				
				if(DUPlogonE==true)
		//		IJ.log(LineInt+"  "+LineNameArray[LineInt]);
				
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
					
					if(Check==-1 && duplicatedLine>duplineE-1){// end of duplication, at next new file name
						if(DUPlogonE==true){
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
							
							if(DUPlogonE==true)
							IJ.log("Overlap_Values; "+Battle_Values [dupcheck-1]);
							
						}//for(int dupcheck=1; dupcheck<=duplicatedLine+1; dupcheck++){
						
						//		Collections.reverse(Ints.asList(Battle_Values));
						Arrays.sort(Battle_Values, Collections.reverseOrder());
						
						for(int Endvalue=0; Endvalue<duplineE; Endvalue++){// scan from top value to the border
							
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
									
									if(labelmethodE==0)// Sort by value
									LineNameArray[Fposi-dupcheck]="10000000,10000000,100000000";//delete positive overlap array for negative overlap list
									
									if(labelmethodE==1){// Sort by value and line
										if(Endvalue==0){// highest value
											if(DUPlogonE==true)
											IJ.log(1+UniqueLineName+"  UniqueLineName; "+FullName);
											OverlapValueLineArray[UniqueLineName]=FullName+","+LineName;
											UniqueLineName=UniqueLineName+1;
										}
									}
									//				IJ.log("OverValueSlice2; "+LineNameArray[Fposi-dupcheck]);
									
									//		PositiveSlices=PositiveSlices-1;
								}
							}//for(int dupcheck=1; dupcheck<=duplicatedLine+1; dupcheck++){
						}//	for(int Endvalue=0; Endvalue<duplineE; Endvalue++){// scan from top value to the border
						
						duplicatedLine=0; Check=2; 
					}//if(preLineNo!=LineNo && duplicatedLine>duplineE-1){// end of duplication, at next new file name
					
					int initialNo=Fposi-duplicatedLine-1;
					if(initialNo>0 && Check==-1 && duplicatedLine<=duplineE-1){//&& CheckPost==-1
						
						for(int dupwithinLimit=1; dupwithinLimit<=duplicatedLine+1; dupwithinLimit++){
							
							if(DUPlogonE==true){
								IJ.log("");
								IJ.log("dupwithinLimit; "+LineNameArray[Fposi-dupwithinLimit]+"  dupwithinLimit; "+dupwithinLimit+"  duplicatedLine; "+duplicatedLine);
							}
							
							if(labelmethodE==0)// Sort by value
							LineNameArray[Fposi-dupwithinLimit]="10000000,10000000,100000000";// delete positive files within duplication limit from negative list
							
							else if (labelmethodE==1){// Sort by value and line
								
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
									
									if(DUPlogonE==true)
									IJ.log("CurrentScore; "+CurrentScore+"  PreScore; "+PreScore);
									
									if(CurrentScore>PreScore){
										
										if(DUPlogonE==true)
										IJ.log(1+UniqueLineName-1+"  UniqueLineName; "+FullName);
										OverlapValueLineArray[UniqueLineName-1]=FullName+","+LineName;
										
										LineNameArray[Fposi-dupwithinLimit+1]=LineName+","+ScoreCurrent+","+FullName;
										LineNameArray[Fposi-dupwithinLimit]=LineName+","+ScorePre+","+PreFullLineName;
										
									}else{
										if(DUPlogonE==true)
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
									
									if(PreCheck==-1 && DUPlogonE==true)
									IJ.log(1+UniqueLineName+"  UniqueLineName; "+FullName);
									
									OverlapValueLineArray[UniqueLineName]=FullName+","+LineName;
									UniqueLineName=UniqueLineName+1;
									//			LineNameArray[Fposi-dupwithinLimit]="0";
								}//if(dupwithinLimit==1){
							}//else if (labelmethodE==2){// Sort by value and line
							
							//		PositiveSlices=PositiveSlices-1;
						}
						duplicatedLine=0;
					}//if(initialNo!=0 && preLineNo!=LineNo && duplicatedLine<=duplineE-1){
					
					if(initialNo<=0 && Check==-1 && duplicatedLine<=duplineE-1 && Fposi>0){// && CheckPost==-1
						for(int dupwithinLimit=1; dupwithinLimit<=duplicatedLine+1; dupwithinLimit++){
							
							if(DUPlogonE==true){
								IJ.log("");
								IJ.log("dupwithinLimit start; "+LineNameArray[Fposi-dupwithinLimit]);
							}
							if(labelmethodE==0)// Sort by value
							LineNameArray[Fposi-dupwithinLimit]="10000000,10000000,100000000";// delete positive files within duplication limit from negative list
							
							else if (labelmethodE==1){// Sort by value and line
								
								arrayName=LineNameArray[Fposi-dupwithinLimit];
								
								arrayPosition=0;
								for (String retval: arrayName.split(",")){
									args[arrayPosition]=retval;
									arrayPosition=arrayPosition+1;
								}
								LineName = args[0];//posipersent2
								FullName = args[2];//posipersent2
								
								if(dupwithinLimit==1){
									if(DUPlogonE==true)
									IJ.log(1+UniqueLineName+"  UniqueLineName; "+FullName);
									OverlapValueLineArray[UniqueLineName]=FullName+","+LineName;
									UniqueLineName=UniqueLineName+1;
									//			LineNameArray[Fposi-dupwithinLimit]="0";
								}
							}
							
							//				PositiveSlices=PositiveSlices-1;
						}
						
						duplicatedLine=0;
					}//if(initialNo<=0 && Check==-1 && duplicatedLine<=duplineE-1 && Fposi>0){
					
					//		preLineNo=LineNo2;
					
				}//for(int Fposi=0; Fposi<posislice; Fposi++){
				Arrays.sort(LineNameArray, Collections.reverseOrder());// negative list
				
			}//	if(duplineE!=0){
			
			if (labelmethodE==1)
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
			
			if(DUPlogonE==true)
			IJ.log("UniqueLineName number; "+UniqueLineName);
			
			if(duplineE!=0){
				for(int wposi=0; wposi<UniqueLineName; wposi++){
					for(int slicelabel=1; slicelabel<=posislice2; slicelabel++){
						
						//		if(DUPlogonE==true)
						//		IJ.log("wposi; "+wposi);
						
						if (labelmethodE==1){
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
							
						}//if (labelmethodE==2){
						
						if (labelmethodE!=1)
						IsPosi=(weightposi[wposi].indexOf(srlabels.get(slicelabel-1)));
						
						
						//	IJ.log(IsPosi+" LineNameTop;"+LineNameTop+"  linename; "+linename+"   dcStack; "+dcStack.getSliceLabel(slicelabel));
						
						if(IsPosi!=-1){// if top value slice is existing in dsStack
							
							if (labelmethodE==1){
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
										
										for(int AddedSlice=0; AddedSlice<duplineE; AddedSlice++){
											
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
															
															if(DUPlogonE==true)
															IJ.log("   "+FinalAdded+"  Added Slice; "+FullLinenamePosi2);
															
															FinalAdded=FinalAdded+1;
															LineNameArray[PosiSliceScan+AddedSlice]="Deleted";
															dcStackScan=posislice2+1;// finish scan for dcStack
															
														}//if(IsPosi0!=-1){
													}//	for(int dcStackScan=1; dcStackScan<posislice2; dcStackScan++){
													//		slicelabel=1;
												}//if(PosiLine!=-1){// same short-line name as topValue one, if different = single file.
											}
										}//for(int AddedSlice=1; AddedSlice<=duplineE; AddedSlice++){
										
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
												
												if(DUPlogonE==true)
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
							}//if (labelmethodE==2){
							
							
							if (labelmethodE!=1){
								if(duplineE!=0){
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
										
										if(DUPlogonE==true)
										IJ.log(dupdel+" Duplicated & deleted; 	"+weightposi[wposi]);
										dupdel=dupdel+1;
									}
								}else{//if(duplineE!=0){
									finallbs.add(weightposi[wposi]);
									srlabels.remove(slicelabel-1);
								}
								slicelabel=posislice2+1;
							}//	if (labelmethodE!=2){
							
							//		posislice2=posislice2-1;
						}//if(IsPosi!=-1){
					}//	for(int slicelabel=1; slicelabel<=posislice2; slicelabel++){
				}//	for(int wposi=0; wposi<posislice; wposi++){
			}else{//duplineE==0
				for(int wposi=0; wposi<posislice; wposi++){
					for(int slicelabel=1; slicelabel<=posislice2; slicelabel++){
						if(weightposi[wposi]==srlabels.get(slicelabel-1)){
							
							finallbs.add(weightposi[wposi]);
							srlabels.remove(slicelabel-1);
							
							if(logonE==true && logNanE==false)
							IJ.log("Positive linename; 	"+weightposi[wposi]);
							
							slicelabel=posislice2;
							posislice2=posislice2-1;
						}
					}
				}
			}//}else{//duplineE==0
			
			ImageStack dcStackfinal = new ImageStack (width,height);
			ImageStack OrigiStackfinal = new ImageStack (width,height);
			//	VirtualStack OrigiStackfinal = new VirtualStack (width,height);
			
			try {
				long size = width*height*3;
				int slnum = finallbs.size();
				if(slnum>400){
					slnum=400;
					if(maxnumber>400)
					slnum=maxnumber+200;
				}
				
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
								OrigiStackfinal.addSlice(label, cp);
								
								if (ShowCoE) {
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
									
									OrigiStackfinal.addSlice(label, cp);
									
									if (ShowCoE) {
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
				} else {
					for (int s = 0; s < slnum; s++) {
						String label = finallbs.get(s);
						SearchResult sr = srdict.get(label);
						if (sr != null) {
							OrigiStackfinal.addSlice(label, sr.m_iporg);
							if (ShowCoE) dcStackfinal.addSlice(label, sr.m_ipcol);
						}
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			if(thremethodSTR=="Combine"){
				ImageStack combstack=new ImageStack(width*2, height, OrigiStackfinal.getColorModel());
				ImageProcessor ip6 = OrigiStackfinal.getProcessor(1);
				
				for (int i=1; i<=posislice; i++) {
					IJ.showProgress((double)i/posislice);
					ip5 = ip6.createProcessor(width*2, height);
					
					if(ShowCoE==true){
						ip5.insert(dcStackfinal.getProcessor(1),0,0);
						dcStackfinal.deleteSlice(1);
					}
					ip5.insert(OrigiStackfinal.getProcessor(1),width,0);
					OrigiStackfinal.deleteSlice(1);
					combstack.addSlice(weightposi[i-1], ip5);
				}
				
				if(ShowCoE==true){
					newimp = new ImagePlus("Co-localized_And_Original.tif_"+pixThresdub2+" %_"+titles[MaskE]+"", combstack);
					newimp.show();
				}
			}else{
				
				if(ShowCoE==true){
					newimp = new ImagePlus("Co-localized.tif_"+pixThresdub2+" %_"+titles[MaskE]+"", dcStackfinal);
					newimp.show();
				}
				
				newimpOri = new ImagePlus("Original_RGB.tif_"+pixThresdub2+" %_"+titles[MaskE]+"", OrigiStackfinal);
			
			}
			
		}//if(posislice>0){
		
		end = System.currentTimeMillis();
		IJ.log("time: "+((float)(end-start)/1000)+"sec");
		
		if(posislice==0)
		IJ.log("No positive slice");
		
		imask.unlock();
		idata.unlock();
		
		//	IJ.log("Done; "+increment+" mean; "+mean3+" Totalmaxvalue; "+totalmax+" desiremean; "+desiremean);
		
		if(EMsearch==true && posislice>2){
			IJ.showStatus("EM MIP sorting");
			if(shownormal==true)
			newimpOri.show();
			
			ImagePlus newimp2 = CDM_area_measure (newimpOri, imask,gradientDIR_,GradientOnTheFly_,ThresmE,maxnumber);
			//	newimpOri.close();
			
			//IJ.runMacroFile(""+plugindir+"Macros/CDM_area_measure.ijm");
		}else{
			newimpOri.show();
		}
		//	System.gc();
	} //public void run(ImageProcessor ip){
	
	
	
	public static int[] get_mskpos_array(ImageProcessor msk, int thresm){
		int sumpx = msk.getPixelCount();
		ArrayList<Integer> pos = new ArrayList<Integer>();
		int pix, red, green, blue;
		for(int n4=0; n4<sumpx; n4++){
			
			pix= msk.get(n4);//MaskE
			
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
	
	public static int[][] generate_shifted_masks(int[] in, int xyshiftE, int w, int h) {
		int[][] out = new int[1+(xyshiftE/2)*8][];
		
		out[0] = in.clone();
		int maskid = 1;
		for (int i = 2; i <= xyshiftE; i += 2) {
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
	
	public static int[] mirror_maskE(int[] in, int ypitch) {
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
		ColorMIPMaskCompare (ImageProcessor query, int mask_th, boolean mirror_maskE, ImageProcessor negquery, int negmask_th, boolean mirror_negmaskE, int search_th, double toleranceZ, int xyshiftE) {
			m_query = query;
			m_negquery = negquery;
			m_width = m_query.getWidth();
			m_height = m_query.getHeight();
			
			m_mask = get_mskpos_array(m_query, mask_th);
			if (m_negquery != null) m_negmask = get_mskpos_array(m_negquery, negmask_th);
			m_th = search_th;
			m_pixfludub = toleranceZ;
			m_mirror = mirror_maskE;
			m_mirrorneg = mirror_negmaskE;
			m_xyshift = xyshiftE;
			
			//shifting
			m_tarmasklist = generate_shifted_masks(m_mask, m_xyshift, m_width, m_height);
			if (m_negquery != null) m_tarnegmasklist = generate_shifted_masks(m_negmask, m_xyshift, m_width, m_height);
			else m_tarnegmasklist = null;
			
			//mirroring
			if (m_mirror) {
				m_tarmasklist_mirror = new int[1+(xyshiftE/2)*8][];
				for (int i = 0; i < m_tarmasklist.length; i++)
				m_tarmasklist_mirror[i] = mirror_maskE(m_tarmasklist[i], m_width);
			} else {
				m_tarmasklist_mirror = null;
			}
			if (m_mirrorneg && m_negquery != null) {
				m_tarnegmasklist_mirror = new int[1+(xyshiftE/2)*8][];
				for (int i = 0; i < m_tarnegmasklist.length; i++)
				m_tarnegmasklist_mirror[i] = mirror_maskE(m_tarnegmasklist[i], m_width);
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
						posi++;
					}
					
				}
			}
			
			return posi;
			
		}
		
	}//public class ColorMIPMaskCompare {
	
	ImagePlus CDM_area_measure (ImagePlus impstack, ImagePlus impmask, String gradientDIR, boolean rungradientonthefly, int ThresmEf, int maxnumberF){
		
		int thread_num_=0; int Threval=0; int stackslicenum=0;
		ImagePlus imp,impgradient;
		
		int wList [] = WindowManager.getIDList();
		if (wList==null || wList.length<2) {
			IJ.showMessage("There should be at least two windows open");
		}
		
		int flip=1;
		
		int [] info= impstack.getDimensions();
		stackslicenum = info[3];//52
		
		
		ImageStack originalresultstack=impstack.getStack();
		long startT=System.currentTimeMillis();
		if(stackslicenum>1){
			//fill name
			for(int iz=1; iz<=stackslicenum; iz++){
				ImageProcessor fillip=originalresultstack.getProcessor(iz);
				
				int pix0=fillip.get(0,0);
				
				for(int ix=0; ix<250; ix++){
					for(int iy=0; iy<40; iy++){
						
						int pixf=fillip.getPixel(ix,iy);
						
						int red1 = (pixf>>>16) & 0xff;
						int green1 = (pixf>>>8) & 0xff;
						int blue1 = pixf & 0xff;
						
						if(red1>0 && green1>0 && blue1>0)
						fillip.set(ix,iy,pix0);
					}
				}
			}
			
			
			long [] areaarray= new long[stackslicenum];
			// original area measurement////////////////////
			String maskname = impmask.getTitle();
			IJ.log("maskname; "+maskname);
			int test=1;
			
			ImagePlus impgradientMask = impmask.duplicate();
			impgradientMask.hide();
			ImageConverter ic = new ImageConverter(impgradientMask);
			ic.convertToGray8();
			
			ImageProcessor EightIMG = impgradientMask.getProcessor();
			
			info= impgradientMask.getDimensions();
			int WW = info[0];
			int HH = info[1];
			int sumpx= WW*HH;
			for(int ipix=0; ipix<sumpx; ipix++){// 255 binary mask creation
				if(EightIMG.get(ipix)>ThresmEf){
					EightIMG.set(ipix, 255);
				}else
				EightIMG.set(ipix, 0);
			}
			
		//		if(test==1){
		//			impgradientMask.show();
		//			return impgradientMask;
		//		}
			
			IJ.run(impgradientMask,"Max Filter2D", "expansion=10 cpu=1 xyscaling=1");
			
			String lastcha=gradientDIR.substring(gradientDIR.length()-1,gradientDIR.length());
			String OSTYPE = System.getProperty("os.name").toLowerCase();
			//						IJ.log("OSTYPE; "+OSTYPE);
			
			if(OSTYPE.equals("mac os x")){
				if(!lastcha.equals("/"))
				gradientDIR=gradientDIR+"/";
				String gradientDIR_=gradientDIR;
				Prefs.set("gradientDIR_.String",gradientDIR_);
			}
			
			int winindex = OSTYPE.indexOf("windows");
			String filesepST= (String) File.separator;
			
			if(winindex!=-1){
				if(!lastcha.equals(filesepST)){
					
					gradientDIR=gradientDIR+"\\";
					IJ.log("win added, File.separator; "+File.separator+"   lastcha; "+lastcha);
					String gradientDIR_=gradientDIR;
					Prefs.set("gradientDIR_.String",gradientDIR_);
				}
			}
			
			ImagePlus hemiMIPmask;
			
			File fMIP = new File(gradientDIR+"MAX_hemi_to_JRC2018U_fineTune.png");
			if (!fMIP.exists()){
				IJ.log("The file cannot open; "+gradientDIR+"MAX_hemi_to_JRC2018U_fineTune.png");
				return impgradientMask;
				
			}else{
				
				hemiMIPmask = IJ.openImage(gradientDIR+"MAX_hemi_to_JRC2018U_fineTune.png");
				
			}
			
			ImageProcessor iphemiMIP = hemiMIPmask.getProcessor();
			
			ImagePlus MaskFlipIMP = impgradientMask.duplicate();
			ImageProcessor ipgradientFlipMask=MaskFlipIMP.getProcessor();
			
			ipgradientFlipMask.flipHorizontal();
			
			
			//		if(test==1){
			
			//			MaskFlipIMP.show();
			//			return impgradientMask; 
			//		}
			
			EightIMG = DeleteOutSideMask(EightIMG,iphemiMIP); // delete out side of emptyhemibrain region
			ipgradientFlipMask = DeleteOutSideMask(ipgradientFlipMask,iphemiMIP); // delete out side of emptyhemibrain region
			
			
			ic = new ImageConverter(impgradientMask);
			ic.convertToGray16();
			EightIMG = impgradientMask.getProcessor();
			
			ImageConverter ic2 = new ImageConverter(MaskFlipIMP);
			ic2.convertToGray16();
			ipgradientFlipMask = MaskFlipIMP.getProcessor();
			
			
			for(int ipix=0; ipix<sumpx; ipix++){// 255 binary mask creation, posi signal become 0 INV
				if(EightIMG.get(ipix)<1)
				EightIMG.set(ipix, 65535);
				else 
				EightIMG.set(ipix, 0);
				
				if(ipgradientFlipMask.get(ipix)<1)
				ipgradientFlipMask.set(ipix, 65535);
				else 
				ipgradientFlipMask.set(ipix, 0);
			}
			
			EightIMG=gradientslice(EightIMG); // make gradient mask
			ipgradientFlipMask=gradientslice(ipgradientFlipMask); // make Flip gradient mask
			
			//		if(test==1){
			//		impgradientMask.show();
			//		MaskFlipIMP.show();
			//			return impgradientMask; 
			//		}
			impgradientMask.hide();
			
			ImagePlus Value1maskIMP = impmask.duplicate();
			
			ic = new ImageConverter(Value1maskIMP);
			ic.convertToGray16();
			
			ImageProcessor ipValue1mask =Value1maskIMP.getProcessor();
			
			setsignal1(ipValue1mask,sumpx);
			
		//	if(test==1){
		//		Value1maskIMP.show();
		//		return Value1maskIMP; 
		//				}
			
			
			ImagePlus impFlipValue1mask =  Value1maskIMP.duplicate();
			ImageProcessor ipFlipValue1mask =impFlipValue1mask.getProcessor();
			ipFlipValue1mask.flipHorizontal();
			ipFlipValue1mask = DeleteOutSideMask(ipFlipValue1mask,iphemiMIP); // delete out side of emptyhemibrain region
			
			
			int [] info2= impstack.getDimensions();
			
			int WW2 = info[0];
			int HH2 = info[1];
			
			int PositiveStackSlice=stackslicenum;
			
			if(PositiveStackSlice>maxnumberF+50)
			PositiveStackSlice=maxnumberF+50;
			
			String [] namearray=new String [PositiveStackSlice];
			String [] totalnamearray=new String[PositiveStackSlice];
			double[] scorearray = new double[PositiveStackSlice];
			
			String [] gaparray=new String[PositiveStackSlice];
			
			double maxScore=0;
			long maxAreagap=0;
			ImageStack Stack2stack=impstack.getStack();
			ImageProcessor ipSLICE2, SLICE2F;
			
			if(rungradientonthefly==true){
				ImagePlus Stack2IMP =impstack.duplicate();
				
				ImageConverter ic3 = new ImageConverter(Stack2IMP);
				ic3.convertToGray8();
				
				Stack2stack = Stack2IMP.getStack();
				
				for(int istackscan=1; istackscan<=PositiveStackSlice; istackscan++){
					
					ImageProcessor EightStackIMG = Stack2stack.getProcessor(istackscan);
					
					for(int ipix2=0; ipix2<sumpx; ipix2++){// 255 binary mask creation
						if(EightStackIMG.get(ipix2)>1){
							EightStackIMG.set(ipix2, 255);
						}
					}
				}
				
				IJ.run(Stack2IMP,"Max Filter2D", "expansion=10 cpu=15 xyscaling=1");
				
				ic = new ImageConverter(Stack2IMP);
				ic.convertToGray16();
				
				Stack2stack = Stack2IMP.getStack();
				
				for(int istackscan2=1; istackscan2<=PositiveStackSlice; istackscan2++){
					ImageProcessor stackgradientIP = Stack2stack.getProcessor(istackscan2);
					
					for(int ipix=0; ipix<sumpx; ipix++){// 255 binary mask creation
						if(stackgradientIP.get(ipix)==0)
						stackgradientIP.set(ipix, 65535);
						else 
						stackgradientIP.set(ipix, 0);
					}
				}//for(int istackscan2=1; istackscan2<=150; istackscan2++){
				
				long timestart = System.currentTimeMillis();
				IJ.run(Stack2IMP,"Mask Outer Gradient Stack","");
				
				//	if(test==1){
				//		Stack2IMP.show();
				//		return Stack2IMP;
				//	}
				Stack2IMP.hide();
				Stack2stack = Stack2IMP.getStack();
				
				//Stack2IMP.close();
				
				long timeend = System.currentTimeMillis();
				
				long gapS = (timeend-timestart)/1000;
				IJ.log("Gradient creation; "+gapS+" second");
				
			}//if(rungradientonthefly==1){
			
			IJ.log("stackslicenum; "+stackslicenum+"  PositiveStackSlice; "+PositiveStackSlice);
			
			
			for(int isli=1; isli<PositiveStackSlice+1; isli++){
				ImageProcessor ipresult = originalresultstack.getProcessor(isli);
				ImagePlus SLICEtifimp = new ImagePlus ("SLICE.tif",ipresult);
				
				
				namearray[isli-1] = originalresultstack.getSliceLabel(isli);
				
				int spaceIndex=namearray[isli-1].indexOf(" ");
				if(spaceIndex!=-1){// replace slice label
					namearray[isli-1]=namearray[isli-1].replace(" ", "_");
					originalresultstack.setSliceLabel(namearray[isli-1],isli);
				}
				
				//	IJ.log(String.valueOf(isli));
				
				int undeIndex=namearray[isli-1].indexOf("_");
				
				scorearray[isli-1]=Double.parseDouble(namearray[isli-1].substring(0,undeIndex));
				
				if(maxScore<scorearray[isli-1])
				maxScore=scorearray[isli-1];
				
				//	if(test==1){
				
				//		return;
				//	}
				
				//multipy image creation/////////////
				ic = new ImageConverter(SLICEtifimp);
				ic.convertToGray16();
				
				ImageProcessor SLICEtifip = SLICEtifimp.getProcessor();
				
				setsignal1(SLICEtifip,sumpx);
			//	if(test==1 && isli==6){
			//		SLICEtifimp.show();
			//		return SLICEtifimp;
			//	}
				ImageProcessor ipgradientMask = impgradientMask.getProcessor();
				
				for(int ivx=0; ivx<sumpx; ivx++){
					
					int pix1 = SLICEtifip.get(ivx);
					int pix2 = ipgradientMask.get(ivx);
					
					SLICEtifip.set(ivx, pix1*pix2);
					
				}// multiply slice and gradient mask
				
				
				long MaskToSample=sumPXmeasure(SLICEtifimp);
				
		//		if(test==1 && isli==6){
		//						SLICEtifimp.show();
		//						return SLICEtifimp;
		//		}
				
				//		IJ.log("MaskToSample; "+MaskToSample);
				//		if(MaskToSample==0){
				//			IJ.log("SLICEtifimp is 0");
				//			return SLICEtifimp; 
				
				//		}
				SLICEtifimp.unlock();
				SLICEtifimp.close();
				
				
				
				long MaskToSampleFlip=0;
				//// flip ////////////////////////////////////
				if(flip==1){
					
					
					ImagePlus SLICEtifimpFlip = new ImagePlus ("SLICEflip.tif",ipresult);// original stack slice
					ic = new ImageConverter(SLICEtifimpFlip);
					ic.convertToGray16();
					
					SLICEtifip = SLICEtifimpFlip.getProcessor();
					setsignal1(SLICEtifip,sumpx);
					
					for(int ivx2=0; ivx2<sumpx; ivx2++){
						
						int pix1 = SLICEtifip.get(ivx2);
						int pix2 = ipgradientFlipMask.get(ivx2);
						
						SLICEtifip.set(ivx2, pix1*pix2);
						
					}// multiply slice and gradient mask
					
					//	if(test==1){
					//		SLICEtifimpFlip.show();
					//			return;
					//	}
					
					MaskToSampleFlip=sumPXmeasure(SLICEtifimpFlip);
					//		IJ.log("MaskToSampleFlip; "+MaskToSampleFlip);
					
					//		if(MaskToSampleFlip==0){
					//			IJ.log("SLICEtifimpFlip is 0");
					//			return SLICEtifimpFlip;
					//		}
					
					SLICEtifimpFlip.unlock();
					SLICEtifimpFlip.hide();
					SLICEtifimpFlip.close();
				}//	if(flip==1){
				
				
				ipSLICE2 = Stack2stack.getProcessor(isli);// already gradient stack
				String titleslice = Stack2stack.getSliceLabel(isli);
				
				impgradient = new ImagePlus (titleslice,ipSLICE2);
				
				if(rungradientonthefly==false){
					
					int undeindex=namearray[isli-1].indexOf("_");
					String filename=namearray[isli-1].substring(undeindex+1, namearray[isli-1].length());
					
					//		IJ.log("gradientDIR; "+gradientDIR+";   length; "+gradientDIR.length()+"   lastcha; "+lastcha+"   filename; "+filename);
					
					
					File f = new File(gradientDIR+filename);
					if (!f.exists()){
						IJ.log("The file cannot open; "+gradientDIR+filename);
						return impgradient;
						
					}else{
						
						impgradient = IJ.openImage(gradientDIR+filename);
						ipSLICE2 = impgradient.getProcessor();
						
					}
				}//if(rungradientonthefly==false){
				
				ImagePlus impSLICE2 = impgradient.duplicate();
				
				ImageProcessor ipforfunc2 = impSLICE2.getProcessor();
				
				for(int ivx2=0; ivx2<sumpx; ivx2++){
					int pix1 = ipforfunc2.get(ivx2);
					int pix2 = ipValue1mask.get(ivx2);
					
					ipforfunc2.set(ivx2, pix1*pix2);
					
				}// multiply slice and gradient mask
				
				long SampleToMask=sumPXmeasure(impSLICE2);
				
				//		IJ.log("SampleToMask; "+SampleToMask);
				
		//		if(test==1 && isli==6){
		//			impgradient.show();
		//			return impgradient;
		//		}
				
				if(IJ.escapePressed()){
					IJ.log("esc canceled");
					return impSLICE2;
				}
				
				impSLICE2.unlock();
				impSLICE2.hide();
				impSLICE2.close();
				long SampleToMaskflip;
				long normalval=(SampleToMask+MaskToSample)/2;
				long realval=normalval;
				
				if(flip==1){
					//// flip X//////////////////////////////////////
					ImageProcessor ipforfunc21;
					
					ImagePlus impSLICE2F = impgradient.duplicate();//single slice from em bodyID hit
					
					//	IJ.run(impSLICE2F,"Flip Horizontally","");// Flip gradient opened EM mask
					
					//			if(test==1){
					//				impgradient.show();
					//				impSLICE2F.show();
					//				return impgradient;
					//			}
					
					ipforfunc21 = impSLICE2F.getProcessor();
					
					for(int ivx2=0; ivx2<sumpx; ivx2++){
						
						int pix1 = ipforfunc21.get(ivx2);
						int pix2 = ipFlipValue1mask.get(ivx2);
						
						ipforfunc21.set(ivx2, pix1*pix2);
						
					}// multiply slice and gradient mask
					
					SampleToMaskflip=sumPXmeasure(impSLICE2F);
					
					//		IJ.log("SampleToMaskflip; "+SampleToMaskflip);
					
			//		if(test==1){
			//			impFlipValue1mask.show();
			//			impSLICE2F.show();
			//			return impSLICE2F;
			//		}
					
					//	if(SampleToMaskflip==0){
					//		impSLICE2F.show();
					//			IJ.log("impSLICE2F is 0");
					//			return impSLICE2F;
					//		}
					impSLICE2F.unlock();
					impSLICE2F.close();
					
					impFlipValue1mask.unlock();
					impFlipValue1mask.close();
					
					long flipval=(SampleToMaskflip+MaskToSampleFlip)/2;
					if(flipval<=normalval)
					realval=flipval;
				}//	if(flip==1){
				
				areaarray[isli-1]=realval;
				
				if(maxAreagap<areaarray[isli-1])
				maxAreagap=areaarray[isli-1];
				
				impgradient.unlock();
				//		impgradient.hide();
				impgradient.close();
			}//for(int isli=1; isli<=slices; isli++){
			
			impgradientMask.unlock();
			//		impgradientMask.hide();
			impgradientMask.close();
			
		//	IJ.log("Slice length; "+PositiveStackSlice);
			double [] normScorePercent=new double[PositiveStackSlice];
			/// normalize score ////////////////////
			for(int inorm=0; inorm<PositiveStackSlice; inorm++){
				
				double normAreaPercent=(double)areaarray[inorm]/(double)maxAreagap;
				normScorePercent[inorm]=scorearray[inorm]/maxScore;
				
				normAreaPercent=normAreaPercent*2;
				if(normAreaPercent>1)
				normAreaPercent=1;
				
				if(normAreaPercent<0.002)
				normAreaPercent=0.002;
				
				double doubleGap=(normScorePercent[inorm]/normAreaPercent)*100;
				
				//	IJ.log(inorm+1+"   normAreaPercent; "+normAreaPercent+"  normScorePercent[inorm]; "+normScorePercent[inorm]+"  doubleGap; "+doubleGap);
				
				String addST="_";
				if(doubleGap<100000 && doubleGap>9999.999999)
				addST=addST.replace("_","0");
				else if(doubleGap<10000 && doubleGap>999.999999)
				addST=addST.replace("_","00");
				else if(doubleGap<1000 && doubleGap>99.999999)
				addST=addST.replace("_","000");
				else if(doubleGap<100 && doubleGap>9.999999)
				addST=addST.replace("_","0000");
				else if(doubleGap<10)
				addST=addST.replace("_","00000");
				
				String finalpercent=String.format("%.10f",(normScorePercent[inorm]/normAreaPercent)*100);
				
				gaparray[inorm]=addST.concat(finalpercent);//,10
				
				String S1=gaparray[inorm].concat(" ");
				
				totalnamearray[inorm]=	S1.concat(namearray[inorm]);
				
				
				//		IJ.log(String.valueOf(inorm)+"  "+gaparray[inorm]);
			}
			
			//Array.show(totalnamearray);
			
			//Arrays.sort(gaparray);
			Arrays.sort(gaparray, Collections.reverseOrder());
			//Array.show(gaparray);
			
			int Finslice=PositiveStackSlice;
			if(Finslice>maxnumberF)
			Finslice=maxnumberF;
			
			ImageStack Stackfinal = new ImageStack (WW,HH);
			
			for(int inew=0; inew<Finslice; inew++){
				
				double Totalscore = Double.parseDouble(gaparray[inew]);
				String slicename="";
				
				for(int iscan=0; iscan<totalnamearray.length; iscan++){
					String [] arg2=totalnamearray[iscan].split(" ");
					
					//IJ.log("arg2[0]; "+arg2[0]+"   totalnamearray[iscan]; "+totalnamearray[iscan]);
					double arg2_0=Double.parseDouble(arg2[0]);
					
					//if(test==1){
					//		return;
					//	}
					
					if(arg2_0==Totalscore){
						
						//		IJ.log("arg2[1]; "+arg2[1]);
						slicename=arg2[1];
						arg2_0=0;
						totalnamearray[iscan]=String.valueOf(arg2[0])+" "+arg2[1];
						iscan=totalnamearray.length;
					}
				}
				
				for(int searchS=1; searchS<=PositiveStackSlice; searchS++){
					String slititle=namearray[searchS-1];
					
					//	IJ.log("slititle; "+slititle);
					
					String ADD0="0";
					if(inew<10)
					ADD0="00";
					else if(inew>99)
					ADD0="";
					
					//	IJ.log("slititle; "+slititle+"   slicename"+slicename);
					if(slititle.equals(slicename)){
						ImageProcessor hitslice = originalresultstack.getProcessor(searchS);//original search MIP stack
						Stackfinal.addSlice(ADD0+inew+"_"+gaparray[inew].substring(0,gaparray[inew].indexOf("."))+"_"+slititle, hitslice);
						
						searchS=PositiveStackSlice+1;
						
						//			IJ.log("slititle; "+slititle);
						
					}//if(slititle==slicename){
				}//for(searchS=1; seachS<nSlices; searchS++){
			}//for(int inew=0; inew<Finslice; inew++){
			
			Value1maskIMP.unlock();
			Value1maskIMP.hide();
			Value1maskIMP.close();
			MaskFlipIMP.unlock();
			MaskFlipIMP.hide();
			MaskFlipIMP.close();
			
			//		impstack.unlock();
			//		impstack.hide();
			//		impstack.close();
			
			ImagePlus newimp = new ImagePlus("Search_Result"+maskname, Stackfinal);
			newimp.show();
			long endT=System.currentTimeMillis();
			long gapT=endT-startT;
			
			IJ.log(gapT/1000+" sec");
			
		}//  if(PositiveStackSlice>1){
		return newimp;
	}//public class CDM_area_measure 
	
	ImageProcessor multiply (ImageProcessor ipmaskFlipgradient, ImageProcessor SLICEtifipf, int WWf, int HHf){
		
		int sumpx= WWf*HHf;
		
		ImagePlus impfunction = new ImagePlus ("funcimp", SLICEtifipf);
		ImagePlus impfunction2 = impfunction.duplicate();
		ImageProcessor ipfunction = impfunction2.getProcessor();
		
		for(int ivx=0; ivx<sumpx; ivx++){
			
			int pixneuron = SLICEtifipf.get(ivx);
			int pixgradient = ipmaskFlipgradient.get(ivx);
			
			ipfunction.set(ivx, pixgradient*pixneuron);
			
		}// multiply slice and gradient mask
		
		impfunction.unlock();
		impfunction.hide();
		impfunction.close();
		
		impfunction2.unlock();
		impfunction2.hide();
		impfunction2.close();
		
		return ipfunction;
	}//	public static ImageProcessor multiply (ImagePlus MaskFlipIMPf, ImagePlus SLICEtifimpf){
	
	public long sumPXmeasure (ImagePlus ipmf){
		final int width = ipmf.getWidth();
		final int height = ipmf.getHeight();
		
		int morethan=3;
		ImageProcessor ip2;
		
		ip2=ipmf.getProcessor();
		
		int sumpx = ip2.getPixelCount();
		
		long sumvalue=0;
		for(int i=0; i<sumpx; i++){
			
			int pix = ip2.get (i);	//input
			
			if(pix>morethan){
				sumvalue=sumvalue+pix;
			}
			
			
		}//	for(int i=1; i<sumpx; i++){
		
		return sumvalue;
	}
	
	ImageProcessor setsignal1 (ImageProcessor ipfunc, int sumpxfunc){
		for(int iff=0; iff<sumpxfunc; iff++){
			
			int pix = ipfunc.get (iff);	//input
			
			if(pix>2)
			ipfunc.set (iff, 1);//out put
			
		}//	for(int iff=1; iff<sumpx; iff++){
		return ipfunc;
	}
	
	ImageProcessor gradientslice (ImageProcessor ipgra){
		
		int nextmin=0;	int Stop=0; int Pout=0;
		
		ImagePlus impfunc = new ImagePlus ("funIMP",ipgra);
		
		int Fmaxvalue=255;
		if(impfunc.getType()==impfunc.GRAY8)
		Fmaxvalue=255;	
		else if(impfunc.getType()==impfunc.GRAY16)
		Fmaxvalue=65535;	
		
		int [] infof= impfunc.getDimensions();
		int width = infof[0];
		int height = infof[1];
		
		while(Stop==0){
			
			Stop=1;
			Pout=Pout+1; 
			
			
			//	IJ.log("run; "+ String.valueOf(try2)+"Fmaxvalue; "+String.valueOf(Fmaxvalue)+"  nextminF; "+String.valueOf(nextminF));
			
			for(int ix=0; ix<width; ix++){// x pixel shift
				//	IJ.log("ix; "+String.valueOf(ix));
				for(int iy=0; iy<height; iy++){
					
					int pix0=-1; int pix1=-1; int pix2=-1; int pix3=-1;
					
					int pix=ipgra.get(ix,iy);
					
					if(pix==Fmaxvalue){
						
						if(ix>0)
						pix0=ipgra.get(ix-1,iy);
						
						if(ix<width-1)
						pix1=ipgra.get(ix+1,iy);
						
						if(iy>0)
						pix2=ipgra.get(ix,iy-1);
						
						if(iy<height-1)
						pix3=ipgra.get(ix,iy+1);
						
						if(pix0==nextmin || pix1==nextmin || pix2==nextmin || pix3==nextmin){//|| edgepositive==1
							
							ipgra.set(ix,iy,Pout);
							Stop=0;
						}
					}//if(pix==Fmaxvalue){
					
				}//	for(int iy=0; iy<height; iy++){
			}//	for(int ix=0; ix<width; ix++){// x pixel shift
			nextmin=nextmin+1;
		}//	while(Stop==0){
		impfunc.close();
		return ipgra;
	}
	
	ImageProcessor DeleteOutSideMask (ImageProcessor ipneuron, ImageProcessor ipEMmask){
		
		ImagePlus impneuron = new ImagePlus ("neuronIMP",ipneuron);
		
		int [] infof= impneuron.getDimensions();
		int width = infof[0];
		int height = infof[1];
		int sumpxf=width*height;
		for(int idel=0; idel<sumpxf; idel++){
			
			int neuronpix=ipneuron.get(idel);
			int maskpix=ipEMmask.get(idel);	
			
			if(neuronpix>0){
				if(maskpix==0){
					ipneuron.set(idel, 0);
				}
			}
			
		}//for(idel=0; idel<sumpxfl idel++){
		return ipneuron;
	}
	
} //public class Two_windows_mask_search implements PlugInFilter{


