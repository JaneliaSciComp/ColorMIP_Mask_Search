import ij.*;
import ij.io.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.*;


public class ColorMIP_Mask_Search implements PlugInFilter
{
	ImagePlus imp, imp2;
	ImageProcessor ip1, nip1, ip2, ip3, ip4, ip5, ip6, ip33;
	int pix1=0, CheckPost,UniqueLineName=0,IsPosi;
	int pix3=0,Check=0,arrayPosition=0,dupdel=1,FinalAdded=1,enddup=0;;
	ImagePlus newimp, newimpOri;
	String linename,LineNo, LineNo2,preLineNo="A",FullName,LineName,arrayName,PostName;
	String args [] = new String[10],PreFullLineName,ScorePre,TopShortLinename;
	
	ExecutorService m_executor;
	
	boolean DUPlogon;

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
		IJ.register (ColorMIP_Mask_Search.class);
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
		gd.addNumericField("% of Positive PX Threshold 0-100 %", pixThres, 2);
		gd.addSlider("Pix Color Fluctuation, 1.18 per slice", 0, 20, pixflu);
				
		String[] dupnumstr = new String[11];
		for (int i = 0; i < dupnumstr.length; i++)
			dupnumstr[i] = Integer.toString(i);
		gd.addChoice("Duplicated line numbers; (only for GMR & VT), 0 = no check", dupnumstr, dupnumstr[dupline]); //Mask
		
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
		
		
		if(GCON==true)
		System.gc();
		
		if(logon==true){
			GenericDialog gd2 = new GenericDialog("log option");
			gd2.addCheckbox("Show NaN",logNan);
			
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
		start = System.nanoTime();

		//IJ.log(" masksize;"+String.valueOf(masksize));

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
				IJ.log("Virtual Stack (directory)");
				
				String dirtmp = vst.getDirectory();
				if (dirtmp.length()>0 && !(dirtmp.endsWith(Prefs.separator)||dirtmp.endsWith("/")))
					dirtmp += Prefs.separator;
				final String directory = dirtmp;
				final long size = width*height*3;
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
										}
										else if(fNumberSTint==1) {
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
			dupline=0;
			String linenameTmpo;
			
			for(int CreateLineArray=0; CreateLineArray<posislice; CreateLineArray++){
				linenameTmpo = srlabels.get(CreateLineArray);
				
				int GMRPosi=(linenameTmpo.indexOf("GMR"));
				int RPosi=(linenameTmpo.indexOf("R"));
				
				int VTPosi=(linenameTmpo.indexOf("VT"));
				if(RPosi!=-1 && GMRPosi==-1){// it is R

					int UnderS2=(linenameTmpo.indexOf("_", RPosi ));// end of line number
					
					LineNo=linenameTmpo.substring(RPosi, UnderS2);// GMR_01A02
				}else if(GMRPosi!=-1){// it is GMR 
					int UnderS1=(linenameTmpo.indexOf("_", GMRPosi+1));
					int UnderS2=(linenameTmpo.indexOf("_", UnderS1+1 ));// end of line number
					
					LineNo=linenameTmpo.substring(GMRPosi, UnderS2);// GMR_01A02
				}else if(VTPosi!=-1){//if VT
					int UnderS1=(linenameTmpo.indexOf("_", VTPosi+1));
					LineNo=linenameTmpo.substring(VTPosi, UnderS1);// VT00002
				}
				
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
															
															if(DUPlogon==true)
															IJ.log("   "+FinalAdded+"  Added Slice; "+FullLinenamePosi2);
															
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
			
			try {
				long size = width*height*3;
				int slnum = finallbs.size();
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
					}
				} else {
					for (int s = 0; s < slnum; s++) {
						String label = finallbs.get(s);
						SearchResult sr = srdict.get(label);
						if (sr != null) {
							OrigiStackfinal.addSlice(label, sr.m_iporg);
							if (ShowCo) dcStackfinal.addSlice(label, sr.m_ipcol);
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
				
				if(ShowCo==true){
					newimp = new ImagePlus("Co-localized.tif_"+pixThresdub2+" %_"+titles[Mask]+"", dcStackfinal);
					newimp.show();
				}
				
				newimpOri = new ImagePlus("Original_RGB.tif_"+pixThresdub2+" %_"+titles[Mask]+"", OrigiStackfinal);
				newimpOri.show();
			}
			
		}//if(posislice>0){

		end = System.nanoTime();
		IJ.log("time: "+((float)(end-start)/1000000.0)+"msec");
		
		if(posislice==0)
		IJ.log("No positive slice");

		imask.unlock();
		idata.unlock();
		
		//	IJ.log("Done; "+increment+" mean; "+mean3+" Totalmaxvalue; "+totalmax+" desiremean; "+desiremean);
		

		//	System.gc();
	} //public void run(ImageProcessor ip){
} //public class Two_windows_mask_search implements PlugInFilter{


