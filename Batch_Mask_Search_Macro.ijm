// need to pre-open only 1 image as dataset stack

OverlapPercent=1;
colorFlux=1;

dir=getDirectory("Choose a Directory for 2D Mask.tif");
list=getFileList(dir);
Array.sort(list);

setBatchMode(true);

if(nImages>1){
	titlelist=getList("image.titles");
	
	for(i=0; i<titlelist.length; i++){
		selectWindow(titlelist[i]);
		if(nSlices>1)
		StackTitle=titlelist[i];
	}
}else
StackTitle=getTitle();

StackID=getImageID();
StackTitleSlice=nSlices();

if(StackTitleSlice==1)
StackTitleCplugin=StackTitle+"  ("+StackTitleSlice+") slice";
else
StackTitleCplugin=StackTitle+"  ("+StackTitleSlice+") slices";

myDir0 = dir+File.separator+"Search_results"+File.separator;
File.makeDirectory(myDir0);

for(i=0; i<list.length; i++){
	print("");
	
	tifindex=lastIndexOf(list[i],"tif");
	if(tifindex!=-1){
		
		open(dir+list[i]);
		originalMaskTitle=getTitle();
		originalMask=getImageID();
		
		dotindex=lastIndexOf(list[i],".");
		truname=substring(list[i],0,dotindex);
		
		makeRectangle(765, 0, 259, 85);
		setForegroundColor(0, 0, 0);
		run("Fill", "slice");
		
		originalMaskTitleSlice=nSlices();
		
		if(nSlices>1)
		exit("Mask has multiple slices");
		
		a=getTime();
		makeRectangle(765, 0, 259, 85);
		
			
//		print("mask=["+originalMaskTitle+"  (1) slice] 1.threshold=40 negative=none 2.threshold=50 data=["+StackTitle+"  ("+StackTitleSlice+") slices] 2.threshold=100 %="+OverlapPercent+" pix="+colorFlux+" duplicated=1 result=[Two windows] slice=[overlap value + line name] scoring=% clear");
		run("ColorMIP Mask Search", "mask=["+originalMaskTitle+"  (1) slice] 1.threshold=40 negative=none 2.threshold=50 data=["+StackTitle+"  ("+StackTitleSlice+") slices] 2.threshold=60 %="+OverlapPercent+" pix="+colorFlux+" duplicated=1 result=[Two windows] slice=[overlap value + line name] scoring=% clear");
		//run("ColorMIP Mask Search", "maskm="+originalMaskTitle+"  (1) slice 1.threshold=40 data="+StackTitle+"  ("+StackTitleSlice+") slices 2.threshold=100 %="+OverlapPercent+" pix="+colorFlux+" duplicated=2 result=[Two windows] slice=[overlap value + line name] scoring=% clear");
		//run("ColorMIP Mask Search", "maskm="+originalMaskTitle+" 1.threshold=30 data="+StackTitle+" 2.threshold=90 %="+OverlapPercent+" pix="+colorFlux+" duplicated=1 result=[Two windows] slice=[overlap value + line name] clear");
		
		b=getTime();
		Gap=b-a;
		Gap=Gap/1000/60;
		print(Gap+" min for the searching process  "+truname);
		
		selectWindow("Original_RGB.tif_"+OverlapPercent+".0 %_"+originalMaskTitle+"");
		PositiveStackSlice=nSlices();
		save(myDir0+truname+"_Result_"+PositiveStackSlice+".tif");
		close();
		
		print("After the duplication elimination; "+PositiveStackSlice+" positive slices");
		selectWindow(originalMaskTitle);
		close();
	}
}


"Done"

