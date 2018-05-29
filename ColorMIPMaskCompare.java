import java.util.ArrayList;

import ij.process.ImageProcessor;

public class ColorMIPMaskCompare {
	
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
	
	public static class Output {
		int matchingPixNum;
		double matchingPct;
		public Output (int pixnum, double pct) {
			matchingPixNum = pixnum;
			matchingPct = pct;
		}
    }
	
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

	public int calc_score(ImageProcessor src, int[] srcmaskposi, byte[] tar, int[] tarmaskposi, int th, double pixfludub, byte[] coloc_out) {

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

}
