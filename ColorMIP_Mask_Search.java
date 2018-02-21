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


public class ColorMIP_Mask_Search implements PlugInFilter
{
	ImagePlus imp, imp2;
	ImageProcessor ip1, nip1, ip2, ip3, ip4, ip5, ip6, ip33;
	int pix1=0, CheckPost,UniqueLineName=0,IsPosi;
	int pix3=0,Check=0,arrayPosition=0,dupdel=1,FinalAdded=1,enddup=0;;
	ImagePlus newimp, newimpOri;
	String linename,LineNo, LineNo2,preLineNo="A",FullName,LineName,arrayName,PostName;
	String args [] = new String[10],PreFullLineName,ScorePre,TopShortLinename;
	
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
		if(imp.getType()!=imp.COLOR_RGB){
			IJ.showMessage("Error", "Plugin requires RGB image");
			return 0;
		}
		return DOES_RGB;
		
		//	IJ.log(" noisemethod;"+String.valueOf(ff));
	}

	public int[] get_mskpos_array(ImageProcessor msk, int thresm){
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

	public int calc_score(ImageProcessor src, int[] srcmaskposi, byte[] tar, int[] tarmaskposi, int th, double pixfludub, byte[] coloc) {
		int masksize = srcmaskposi.length <= tarmaskposi.length ? srcmaskposi.length : tarmaskposi.length;
		int width = src.getWidth();
		int height = src.getHeight();
		ColorProcessor ipnew=  new ColorProcessor(width, height);
				
		//IJ.log(" linename;"+linename);
		int posi = 0;
		for(int masksig=0; masksig<masksize; masksig++){
			
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
					if(coloc!=null) {
						coloc[p] = tar[p];
						coloc[p+1] = tar[p+1];
						coloc[p+2] = tar[p+2];
					}
					posi++;
				}else if(pxGap==1000)
				IJ.log("There is 255 x2 value");
				
			}//if(red2>th || green2>th || blue2>th){
		}//for(int masksig=0; masksig<masksize; masksig++){

		return posi;
	}

	public int calc_score(ImageProcessor src, int[] srcmaskposi, ImageProcessor tar, int[] tarmaskposi, int th, double pixfludub, ImageProcessor coloc) {
		int masksize = srcmaskposi.length <= tarmaskposi.length ? srcmaskposi.length : tarmaskposi.length;
		int width = src.getWidth();
		int height = src.getHeight();
		ColorProcessor ipnew=  new ColorProcessor(width, height);
				
		//IJ.log(" linename;"+linename);
		int posi = 0;
		for(int masksig=0; masksig<masksize; masksig++){
			
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
					if(coloc!=null)
						coloc.set(tarmaskposi[masksig], pix2);// new RGB image
					posi++;
				}else if(pxGap==1000)
				IJ.log("There is 255 x2 value");
				
			}//if(red2>th || green2>th || blue2>th){
		}//for(int masksig=0; masksig<masksize; masksig++){

		return posi;
	}

	public double calc_score_px(int red1, int green1, int blue1, int red2, int green2, int blue2) {
		int RG1=0; int BG1=0; int GR1=0; int GB1=0; int RB1=0; int BR1=0;
		int RG2=0; int BG2=0; int GR2=0; int GB2=0; int RB2=0; int BR2=0;
		int MIPtwo=0; int max1=0; int max2=0;
		double rb1=0; double rg1=0; double gb1=0; double gr1=0; double br1=0; double bg1=0;
		double rb2=0; double rg2=0; double gb2=0; double gr2=0; double br2=0; double bg2=0;
		double pxGap=10000; 
		double BrBg=0.354862745; double BgGb=0.996078431; double GbGr=0.505882353; double GrRg=0.996078431; double RgRb=0.505882353;
		double BrGap=0; double BgGap=0; double GbGap=0; double GrGap=0; double RgGap=0; double RbGap=0;
				
		String checkborder="";
				
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
		boolean logon=(boolean)Prefs.get("logon.boolean",false);
		int Thresm=(int)Prefs.get("Thresm.int",50);
		int NegThresm=(int)Prefs.get("NegThresm.int",50);
		boolean logNan=(boolean)Prefs.get("logNan.boolean",false);
		int labelmethod=(int)Prefs.get("labelmethod.int",0);
		boolean DUPlogon=(boolean)Prefs.get("DUPlogon.boolean",false);
		boolean GCON=(boolean)Prefs.get("GCON.boolean",false);
		boolean ShowCo=(boolean)Prefs.get("ShowCo.boolean",true);
		int NumberSTint=(int)Prefs.get("NumberSTint.int",0);
		
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
		
		gd.addNumericField("Duplicated line numbers; (only for GMR & VT), 0 = no check", dupline, 0);
		
		gd.setInsets(0, 372, 0);
		gd.addCheckbox("Show Duplication log",DUPlogon);
		
		gd.setInsets(0, 362, 5);
		String []	ColorDis = {"Combine", "Two windows"};
		gd.addRadioButtonGroup("Result windows", ColorDis, 1, 2, ColorDis[colormethod]);
		
		gd.setInsets(0, 362, 5);
		String []	labelmethodST = {"overlap value", "overlap value + line name"};
		gd.addRadioButtonGroup("Slice sorting method; ", labelmethodST, 1, 2, labelmethodST[labelmethod]);
		
		gd.setInsets(0, 362, 5);
		String []	NumberST = {"%", "absolute value"};
		gd.addRadioButtonGroup("Scoring method; ", NumberST, 1, 2, NumberST[NumberSTint]);
		
		gd.setInsets(20, 372, 0);
		gd.addCheckbox("Show normal log",logon);
		
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
		dupline=(int)gd.getNextNumber();
		DUPlogon = gd.getNextBoolean();
		
		String thremethodSTR=(String)gd.getNextRadioButton();
		String labelmethodSTR=(String)gd.getNextRadioButton();
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
		Prefs.set("logon.boolean",logon);
		Prefs.set("labelmethod.int",labelmethod);
		Prefs.set("DUPlogon.boolean",DUPlogon);
		Prefs.set("dupline.int",dupline);
		Prefs.set("GCON.boolean",GCON);
		Prefs.set("ShowCo.boolean",ShowCo);
		Prefs.set("NumberSTint.int",NumberSTint);
		
		double pixfludub=pixflu/100;
		//	IJ.log(" pixfludub;"+String.valueOf(pixfludub));
		
		double pixThresdub= pixThres/100;///10000
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
		
		int sumpx = ip1.getPixelCount();
		int width = imask.getWidth();
		int height = imask.getHeight();
		
		if(IJ.escapePressed())
			return;
		
	
		IJ.showProgress(0.0);
		
		//	IJ.log("maxvalue; "+maxvalue2+"	 gap;	"+gap);
		
		
		ImageStack st3 = idata.getStack();
		int posislice = 0;
		
		double posipersent2 = 0;
		double pixThresdub2 = 0;
		
		String MIPtwoST="";
		
		int [] maskposi = get_mskpos_array(ip1, Thresm);
		int [] negmaskposi = nip1 != null ? get_mskpos_array(nip1, NegThresm) : null;
		int masksize = maskposi.length;
		int negmasksize = nip1 != null ? negmaskposi.length : 0;
		int [] maskposi_mirror = null;
		int [] negmaskposi_mirror = null;
		
		if (mirror_mask && maskposi != null) {
			maskposi_mirror = maskposi.clone();
			int x, y, z;
			int ypitch = width;
			int zpitch = height*width;
			for(int i = 0; i < masksize; i++) {
				int val = maskposi[i];
				x = val % ypitch;
				maskposi_mirror[i] = val + (width-1) - 2*x;
			}
		}
		if (mirror_negmask && negmaskposi != null) {
			negmaskposi_mirror = negmaskposi.clone();
			int x, y, z;
			int ypitch = width;
			int zpitch = height*width;
			for(int i = 0; i < negmasksize; i++) {
				int val = negmaskposi[i];
				x = val % ypitch;
				negmaskposi_mirror[i] = val + (width-1) - 2*x;
			}
		}
		
		int maskpos_st = negmaskposi != null ? Math.min(maskposi[0], negmaskposi[0])*3 : maskposi[0]*3;
		int maskpos_ed = negmaskposi != null ? Math.max(maskposi[masksize-1], negmaskposi[negmasksize-1])*3 : maskposi[masksize-1]*3;
		int stripsize = maskpos_ed-maskpos_st+3;

		long start, end;
		start = System.nanoTime();

		HashMap<String,Integer>smap = new HashMap<String,Integer>(1000);
		HashMap<String,Long>smapl = new HashMap<String,Long>(1000);

		//IJ.log(" masksize;"+String.valueOf(masksize));

		ArrayList<String> srlabels = new ArrayList<String>();
		ArrayList<String> finallbs = new ArrayList<String>();
		HashMap<String, SearchResult> srdict = new HashMap<String, SearchResult>(1000);
		
		
		try{
			if (st3.isVirtual()) {
				VirtualStack vst = (VirtualStack)st3;
				if (vst.getDirectory() == null) {
					FileInfo fi = idata.getOriginalFileInfo();
					if (fi.directory.length()>0 && !(fi.directory.endsWith(Prefs.separator)||fi.directory.endsWith("/")))
						fi.directory += Prefs.separator;
					String datapath = fi.directory + fi.fileName;
					RandomAccessFile f = new RandomAccessFile(datapath, "r");
					long size = fi.width*fi.height*fi.getBytesPerPixel();
					long loffset = fi.getOffset();
										
					for (int islice=1; islice<=slicenumber ; islice++){
						if(IJ.escapePressed())
							break;

						byte [] impxs = new byte[(int)size];
						byte [] colocs = null;
						if (ShowCo) colocs = new byte[(int)size];

						loffset = fi.getOffset() + (islice-1)*(size+fi.gapBetweenImages) + maskpos_st;
						f.seek(loffset);
						f.read(impxs, maskpos_st, stripsize);
						
						IJ.showStatus("Color MIP Mask_search; "+posislice+" / positive slices");
						IJ.showProgress((double)islice/(double)slicenumber);
						
						linename=st3.getSliceLabel(islice);
						
						int posi = calc_score(ip1, maskposi, impxs, maskposi, Thres, pixfludub, colocs);
						double posipersent= (double) posi/ (double) masksize;
						
						if (nip1 != null) {
							int nega = calc_score(nip1, negmaskposi, impxs, negmaskposi, Thres, pixfludub, null);
							double negapersent= (double) nega/ (double) negmasksize;
							posipersent -= negapersent;
							posi = (int)Math.round((double)posi*(1.0-negapersent));
						}

						int mirror_posi = 0;
						double mirror_posipersent = 0.0;
						if (mirror_mask && maskposi_mirror != null) {
							mirror_posi = calc_score(ip1, maskposi, impxs, maskposi_mirror, Thres, pixfludub, colocs);
							mirror_posipersent = (double) mirror_posi/ (double) masksize;
						}
						if (nip1 != null && mirror_negmask && negmaskposi_mirror != null) {
							int nega = calc_score(nip1, negmaskposi, impxs, negmaskposi_mirror, Thres, pixfludub, null);
							double negapersent= (double) nega/ (double) negmasksize;
							mirror_posipersent -= negapersent;
							mirror_posi = (int)Math.round((double)mirror_posi*(1.0-negapersent));
						}
						if (posi < mirror_posi) {
							posi = mirror_posi;
							posipersent = mirror_posipersent;
						}
						
						if(posipersent<=pixThresdub){
							if (logon==true && logNan==true)
							IJ.log("NaN");
						}else if(posipersent>pixThresdub){
							loffset = fi.getOffset() + (islice-1)*(size+fi.gapBetweenImages);
														
							double posipersent3=posipersent*100;
							double pixThresdub3=pixThresdub*100;
							
							posipersent3 = posipersent3*100;
							posipersent3 = Math.round(posipersent3);
							posipersent2 = posipersent3 /100;
						
							pixThresdub3 = pixThresdub3*100;
							pixThresdub3 = Math.round(pixThresdub3);
							pixThresdub2 = pixThresdub3 /100;
							
							if(logon==true && logNan==true)// sort by name
								IJ.log("Positive linename; 	"+linename+" 	"+String.valueOf(posipersent2));
							
							String title="";
							if(NumberSTint==0){
								String numstr = getZeroFilledNumString(posipersent2, 3, 2);
								title = (labelmethod==0 || labelmethod==1) ? numstr+"_"+linename : linename+"_"+numstr;
							}
							else if(NumberSTint==1){
								String posiST=getZeroFilledNumString(posi, 4);
								title = (labelmethod==0 || labelmethod==1) ? posiST+"_"+linename : linename+"_"+posiST;
							}
							srlabels.add(title);
							srdict.put(title, new SearchResult(title, islice, loffset, impxs, colocs, null, null));
							
							posislice=posislice+1;
						}//if(posipersent>pixThresdub){
					
					}//for (int islice=1; islice<=slicenumber ; islice++){
					f.close();
					
				} else {
					String directory = vst.getDirectory();
					if (directory.length()>0 && !(directory.endsWith(Prefs.separator)||directory.endsWith("/")))
						directory += Prefs.separator;
					long size = width*height*3;
										
					for (int islice=1; islice<=slicenumber ; islice++){
						if(IJ.escapePressed())
							break;
						byte [] impxs = new byte[(int)size];
						byte [] colocs = null;
						if (ShowCo) colocs = new byte[(int)size];
						String datapath = directory + vst.getFileName(islice);

						try {
							RandomAccessFile f = new RandomAccessFile(datapath, "r");
							TiffDecoder tfd = new TiffDecoder(directory, vst.getFileName(islice));
							if (tfd == null) continue;
							FileInfo[] fi_list = tfd.getTiffInfo();
							if (fi_list == null) continue;
							long loffset = fi_list[0].getOffset();
	
							f.seek(loffset+(long)maskpos_st);
							f.read(impxs, maskpos_st, stripsize);
							
							IJ.showStatus("Color MIP Mask_search; "+posislice+" / positive slices");
							IJ.showProgress((double)islice/(double)slicenumber);
							
							linename=st3.getSliceLabel(islice);
							
							int posi = calc_score(ip1, maskposi, impxs, maskposi, Thres, pixfludub, colocs);
							double posipersent= (double) posi/ (double) masksize;
							if (nip1 != null) {
								int nega = calc_score(nip1, negmaskposi, impxs, negmaskposi, Thres, pixfludub, null);
								double negapersent= (double) nega/ (double) negmasksize;
								posipersent -= negapersent;
								posi = (int)Math.round((double)posi*(1.0-negapersent));
							}

							int mirror_posi = 0;
							double mirror_posipersent = 0.0;
							if (mirror_mask && maskposi_mirror != null) {
								mirror_posi = calc_score(ip1, maskposi, impxs, maskposi_mirror, Thres, pixfludub, colocs);
								mirror_posipersent = (double) mirror_posi/ (double) masksize;
							}
							if (nip1 != null && mirror_negmask && negmaskposi_mirror != null) {
								int nega = calc_score(nip1, negmaskposi, impxs, negmaskposi_mirror, Thres, pixfludub, null);
								double negapersent= (double) nega/ (double) negmasksize;
								mirror_posipersent -= negapersent;
								mirror_posi = (int)Math.round((double)mirror_posi*(1.0-negapersent));
							}
							if (posi < mirror_posi) {
								posi = mirror_posi;
								posipersent = mirror_posipersent;
							}
							
							if(posipersent<=pixThresdub){
								if (logon==true && logNan==true)
								IJ.log("NaN");
							}else if(posipersent>pixThresdub){
								double posipersent3=posipersent*100;
								double pixThresdub3=pixThresdub*100;
								
								posipersent3 = posipersent3*100;
								posipersent3 = Math.round(posipersent3);
								posipersent2 = posipersent3 /100;
							
								pixThresdub3 = pixThresdub3*100;
								pixThresdub3 = Math.round(pixThresdub3);
								pixThresdub2 = pixThresdub3 /100;
							
								if(logon==true && logNan==true)// sort by name
									IJ.log("Positive linename; 	"+linename+" 	"+String.valueOf(posipersent2));
								
								String title="";
								if(NumberSTint==0){
									String numstr = getZeroFilledNumString(posipersent2, 3, 2);
									title = (labelmethod==0 || labelmethod==1) ? numstr+"_"+linename : linename+"_"+numstr;
								}
								else if(NumberSTint==1){
									String posiST=getZeroFilledNumString(posi, 4);
									title = (labelmethod==0 || labelmethod==1) ? posiST+"_"+linename : linename+"_"+posiST;
								}
								srlabels.add(title);
								srdict.put(title, new SearchResult(title, islice, loffset, impxs, colocs, null, null));
								
								posislice=posislice+1;
							}//if(posipersent>pixThresdub){
							f.close();
						} catch (IOException e) {
							continue;
						}
					}//for (int islice=1; islice<=slicenumber ; islice++){
				}
		
			} else {
				for (int islice=1; islice<=slicenumber ; islice++){
					if(IJ.escapePressed())
						break;
		
					IJ.showStatus("Color MIP Mask_search; "+posislice+" / positive slices");
					IJ.showProgress((double)islice/(double)slicenumber);
					
					ColorProcessor ipnew = new ColorProcessor(width, height);
					ip3 = st3.getProcessor(islice);// data
					linename=st3.getSliceLabel(islice);
					
					int posi = calc_score(ip1, maskposi, ip3, maskposi, Thres, pixfludub, ShowCo ? ipnew : null);
					double posipersent= (double) posi/ (double) masksize;
					if (nip1 != null) {
						int nega = calc_score(nip1, negmaskposi, ip3, negmaskposi, Thres, pixfludub, null);
						double negapersent= (double) nega/ (double) negmasksize;
						posipersent -= negapersent;
						posi = (int)Math.round((double)posi*(1.0-negapersent));
					}

					int mirror_posi = 0;
					double mirror_posipersent = 0.0;
					if (mirror_mask && maskposi_mirror != null) {
						mirror_posi = calc_score(ip1, maskposi, ip3, maskposi_mirror, Thres, pixfludub, ShowCo ? ipnew : null);
						mirror_posipersent = (double) mirror_posi/ (double) masksize;
					}
					if (nip1 != null && mirror_negmask && negmaskposi_mirror != null) {
						int nega = calc_score(nip1, negmaskposi, ip3, negmaskposi_mirror, Thres, pixfludub, null);
						double negapersent= (double) nega/ (double) negmasksize;
						mirror_posipersent -= negapersent;
						mirror_posi = (int)Math.round((double)mirror_posi*(1.0-negapersent));
					}
					if (posi < mirror_posi) {
						posi = mirror_posi;
						posipersent = mirror_posipersent;
					}
					
					if(posipersent<=pixThresdub){
						if (logon==true && logNan==true)
						IJ.log("NaN");
					}else if(posipersent>pixThresdub){
						double posipersent3=posipersent*100;
						double pixThresdub3=pixThresdub*100;
						
						posipersent3 = posipersent3*100;
						posipersent3 = Math.round(posipersent3);
						posipersent2 = posipersent3 /100;
						
						pixThresdub3 = pixThresdub3*100;
						pixThresdub3 = Math.round(pixThresdub3);
						pixThresdub2 = pixThresdub3 /100;
						
						if(logon==true && logNan==true)// sort by name
						IJ.log("Positive linename; 	"+linename+" 	"+String.valueOf(posipersent2));
						
						String title="";
						if(NumberSTint==0){
							String numstr = getZeroFilledNumString(posipersent2, 3, 2);
							title = (labelmethod==0 || labelmethod==1) ? numstr+"_"+linename : linename+"_"+numstr;
						}
						else if(NumberSTint==1) {
							String posiST=getZeroFilledNumString(posi, 4);
							title = (labelmethod==0 || labelmethod==1) ? posiST+"_"+linename : linename+"_"+posiST;
						}
						srlabels.add(title);
						srdict.put(title, new SearchResult(title, islice, 0L, null, null, ip3, ipnew));
						
						posislice=posislice+1;
					}//if(posipersent>pixThresdub){
					
				}//for (int islice=1; islice<=slicenumber ; islice++){
			}
		} catch(IOException e) {
			e.printStackTrace();
		}	
		
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
				int VTPosi=(linenameTmpo.indexOf("VT"));
				
				if(GMRPosi!=-1){// it is GMR 
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



























