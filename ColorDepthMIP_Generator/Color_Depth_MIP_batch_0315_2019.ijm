//Wrote by Hideo Otsuna (HHMI Janelia Research Campus), Aug 4, 2015

run("Misc...", "divide=Infinity save");
autothre=0;//1 is FIJI'S threshold, 0 is DSLT thresholding
multiDSLT=1;// 1 is multi step DSLT for better thresholding sensitivity
measurement="No";// Yes will measure the shape of signals (for nc82 shape database creation)
printskip=0; // 1 will print out skipped file name
JFRCpath=0;
VncMaskpathF=0;
VncMaskpathM=0;
setBatchMode(true);
compCC=0;// 1 is compressed nrrd, 0 is not compressed nrrd
keepsubst=0; // 1 is keeoing sub folder structures
DSLTver="Line";//"normal";
SkipDuplication=true;

print("Choose a directory for aligned confocal files");
dir = getDirectory("Choose a directory for aligned confocal files");

savemethod=0;
print("Choose a directory for Color MIP SAVE");
dirCOLOR= getDirectory("Choose a directory for Color MIP SAVE");

savemethod=1;

print("Confocal Stack; "+dir);
print("MIP saving place; "+dirCOLOR);



filepath0=getDirectory("temp");//C:\Users\??\AppData\Local\Temp\...C:\DOCUME~1\ADMINI~1\LOCALS~1\Temp\
filepath=filepath0+"MIP_batch.txt";

print("filepath; "+filepath);

LF=10; TAB=9; swi=0; swi2=0; testline=0;
exi=File.exists(filepath);
List.clear();

MIPtype="Gen1_Gal4";
subfolder=false;
colorcoding=true;
CLAHE=true;
AutoBRV=true;
blockposition=1;
totalblock=1;
blockON=false;
desiredmean=190;
savestring="Save in same folder";
colorscale=true;
reverse0=false;
startMIP=0;
endMIP=1000;
usingLUT="PsychedelicRainBow2";
manualST="All channels MIP creation";
lowerweight=0.0;
lowthreM="Peak Histogram";
unsharp="NA";
expand=false;
secondjump=210;
UseSubfolderName=false;
CropYN=false;
JFRC3Dpath=0;
nc82nrrd=true;
tempMaskDir=0;
unsharp2=0;
easyADJ=true;
GammaON=false;

if(exi==1){
	s1 = File.openAsRawString(filepath);
	swin=0;
	swi2n=-1;
	
	n = lengthOf(s1);
	String.resetBuffer;
	for (testnum=0; testnum<n; testnum++) {
		enter = charCodeAt(s1, testnum);
		
		if(enter==10)
		testline=testline+1;//line number
	}
	
	String.resetBuffer;
	for (si=0; si<n; si++) {
		c = charCodeAt(s1, si);
		
		if(c==10){
			swi=swi+1;
			swin=swin+1;
			swi2n=swi-1;
		}
		
		if(swi==swin){
			if(swi2==swi2n){
				String.resetBuffer;
				swi2=swi;
			}
			if (c>=32 && c<=127)
			String.append(fromCharCode(c));
		}
		if(swi==0){
			MIPtype = String.buffer;
		}else if(swi==1 && swi<=testline){
			subfolder = String.buffer;
		}else if(swi==2 && swi<=testline){
			colorcoding = String.buffer;
		}else if(swi==3 && swi<=testline){
			CropYN = String.buffer;
		}else if(swi==4 && swi<=testline){
			AutoBRV = String.buffer;
		}else if(swi==5 && swi<=testline){
			totalblock = String.buffer;
		}else if(swi==6 && swi<=testline){
			blockposition = String.buffer;
		}else if(swi==7 && swi<=testline){
			blockON = String.buffer;
		}else if(swi==8 && swi<=testline){
			desiredmean = String.buffer;
		}else if(swi==9 && swi<=testline){
			savestring = String.buffer;
		}else if(swi==10 && swi<=testline){
			colorscale = String.buffer;
		}else if(swi==11 && swi<=testline){
			reverse0 = String.buffer;
		}else if(swi==12 && swi<=testline){
			startMIP = String.buffer;
		}else if(swi==13 && swi<=testline){
			endMIP = String.buffer;
		}else if(swi==14 && swi<=testline){
			usingLUT = String.buffer;
		}else if(swi==15 && swi<=testline){
			manualST = String.buffer;
		}else if(swi==16 && swi<=testline){
			lowerweight = String.buffer;
		}else if(swi==17 && swi<=testline){
			lowthreM = String.buffer;
		}else if(swi==18 && swi<=testline){
			unsharp = String.buffer;
		}else if(swi==19 && swi<=testline){
			expand = String.buffer;
		}else if(swi==20 && swi<=testline){
			secondjump = String.buffer;
		}else if(swi==21 && swi<=testline){
			UseSubfolderName = String.buffer;
		}else if(swi==22 && swi<=testline){
			JFRCpath = String.buffer;
		}else if(swi==23 && swi<=testline){
			VncMaskpathF = String.buffer;
		}else if(swi==24 && swi<=testline){
			JFRC3Dpath = String.buffer;
		}else if(swi==25 && swi<=testline){
			nc82nrrd = String.buffer;
		}else if(swi==26 && swi<=testline){
			DSLTver = String.buffer;
		}else if(swi==27 && swi<=testline){
			SkipDuplication = String.buffer;
		}else if(swi==28 && swi<=testline){
			tempMaskDir = String.buffer;
		}else if(swi==29 && swi<=testline){
			VncMaskpathM = String.buffer;
		}else if(swi==30 && swi<=testline){
			unsharp2 = String.buffer;
		}else if(swi==31 && swi<=testline){
			easyADJ = String.buffer;
		}else if(swi==32 && swi<=testline){
			GammaON = String.buffer;
		}  //swi==0 //swi==0
	}
	
	
}
run("Close All");

JaneliaVersion=0;

Dialog.create("Batch processing of the Color depth MIP creation");

if(JaneliaVersion==1)
Dialog.addCheckbox("Include sub-folder", subfolder);

Dialog.addCheckbox("Automatic Brightness adjustment", AutoBRV);

if(JaneliaVersion==1)
Dialog.addCheckbox("Crop Optic Lobe", CropYN);

Dialog.addCheckbox("Add color scale", colorscale);
Dialog.addCheckbox("Reversed color", reverse0);
Dialog.addCheckbox("Apply Gamma 1.4", GammaON);


Dialog.addCheckbox("VNC (Expand canvas for scale-bar)", expand);

if(JaneliaVersion==1)
Dialog.addCheckbox("MIP craetion from all .nrrd", nc82nrrd);
Dialog.addCheckbox("Skip MIP creation for already made in the save directory", SkipDuplication);

Dialog.addCheckbox("Skeleton MIP", unsharp2);

if(JaneliaVersion==1){
	itemFilter=newArray("NA", "Unsharp", "Max");
	Dialog.addRadioButtonGroup("Applying Filter", itemFilter, 1, 3, unsharp); 
}
if(JaneliaVersion==1){
	item4=newArray("Manual setting 1st time only", "Automatic", "All channels MIP creation");
	Dialog.addRadioButtonGroup("Nc82 channel discrimination method from a multi-channel stack", item4, 1, 3, manualST); 
}
item5=newArray("Normal", "Line");
Dialog.addRadioButtonGroup("DSLT version", item5, 1, 2, DSLTver); 

Dialog.addNumber("Starting MIP slice", startMIP);
Dialog.addNumber("Ending MIP slice, larger number will be the last slice", endMIP);

if(JaneliaVersion==1){
	Dialog.addNumber("Handling block", blockposition, 0, 0, " /Total block"); //0
	Dialog.addNumber("Total block number 1-10", totalblock, 0, 0, ""); //0
}
Dialog.show();

if(JaneliaVersion==1)
subfolder=Dialog.getCheckbox();// supporting sub folders

AutoBRV=Dialog.getCheckbox();//auto-brightness adjustment

if(JaneliaVersion==1)
CropYN=Dialog.getCheckbox();//crop

colorscale=Dialog.getCheckbox();//adding color depth scale bar
reverse0=Dialog.getCheckbox();//reverse color for front back inverted signal
GammaON=Dialog.getCheckbox();

expand=Dialog.getCheckbox();

if(JaneliaVersion==1)
nc82nrrd=Dialog.getCheckbox();
SkipDuplication=Dialog.getCheckbox();

unsharp2=Dialog.getCheckbox();

if(JaneliaVersion==1){
	unsharp= Dialog.getRadioButton();
	manualST= Dialog.getRadioButton();//manually neuron channel set only for 1 st time
}

usingLUT = "PsychedelicRainBow2"; //Dialog.getRadioButton();//LUT, "PsychedelicRainBow2" is for Color MIP mask search, "royal" is for better looiking
DSLTver = Dialog.getRadioButton();//background thresholding
startMIP=Dialog.getNumber();//MIP starting slice
endMIP=Dialog.getNumber();//MIP ending slice

if(JaneliaVersion==1){
	blockposition=Dialog.getNumber();
	totalblock=Dialog.getNumber();
}
blockON=false;

lowthreM="Peak Histogram";
savestring="Choose directory";

if(unsharp2==true)
unsharp="Max";

manual=0;
if(manualST=="Manual setting 1st time only")
manual=1;

if(manualST=="All channels MIP creation")
manual=2;

if(manualST=="Automatic")
manual=3;// Automatic signal amount measurement


if(subfolder==1){
	Dialog.create("Name setting for the stack from the subfolder");
	Dialog.addCheckbox("Use subfolder name as file name", UseSubfolderName);
	Dialog.show();
	UseSubfolderName=Dialog.getCheckbox();
}


if(AutoBRV==1){
	Dialog.create("Desired average value for Auto-Brightness");
	Dialog.addNumber("Desired average value for the Auto-Brightness adjustment /255  150-220", desiredmean);
	
	if(JaneliaVersion==1){
		Dialog.addNumber("the background thresholding strength 0-0.5", lowerweight);
		Dialog.addNumber("If the signal is too dim, use this average brightness 230-245.", secondjump);
	}
	if(CropYN){
		item10=newArray("Gen1_Gal4", "MCFO_MIP");
		Dialog.addRadioButtonGroup("Line Type for OpticLobe cropping", item10, 2, 2, MIPtype); 
	}
	
	Dialog.show();
	desiredmean=Dialog.getNumber();
	if(JaneliaVersion==1){
		lowerweight=Dialog.getNumber();
		secondjump=Dialog.getNumber();
	}
	
	if(CropYN)
	MIPtype=Dialog.getRadioButton();
}


listsave=getFileList(dirCOLOR);

blockposition=1;
totalblock=1;

if(blockposition>=1){
	if(totalblock>1){
		
		list = getFileList(dir);
		Array.sort(list);
		
		blocksize=(list.length/totalblock);
		blocksize=round(blocksize);
		startn=blocksize*(blockposition-1);
		endn=startn+blocksize;
		
		if(blockposition==totalblock)
		endn=list.length;
		
		blockON=true;
		print("Block mode; "+blockposition+" / "+totalblock);
	}
}

if(AutoBRV==1)
print("Desired mean; "+desiredmean);

if(blockposition<=1){
	if(totalblock<=1){// block mode OFF
		list = getFileList(dir);
		Array.sort(list);
		startn=0;
		endn=list.length;	
	}
}


myDir = 0; myDirT = 0; myDirCLAHE = 0; myDir2Co = 0;

firsttime=0;
firsttime1ch=0;
nc82=0;
neuronimg=0;
myDir2=0;
myDirCLAHE=0;
myDir2Co=0;
myDir=0;
//if(measurement=="Yes"){
numberGap=100000;//endn-startn+1;
Circulicity=newArray(numberGap);
Roundness=newArray(numberGap);
ratio=newArray(numberGap);
AR=newArray(numberGap);
areasizeM=newArray(numberGap);
perimLM=newArray(numberGap);
//}else{

defaultNoCH=0;
countFile=0;

for (i=startn; i<endn; i++){
	
	if(i==startn){
		
		FilePathArray=newArray(JFRCpath, "JFRC2010_Mask.tif","DontOpen",tempMaskDir,"");//DontOpen
		fileOpen(FilePathArray);
		JFRCpath=FilePathArray[0];
		tempMaskDir=FilePathArray[3];
		print("JFRCpath; "+JFRCpath);
		
		
		FilePathArray=newArray(VncMaskpathF, "Mask_VNC_Female.tif","DontOpen",tempMaskDir,"");
		fileOpen(FilePathArray);
		VncMaskpathF=FilePathArray[0];
		print("VncMaskpathF; "+VncMaskpathF);
		
		FilePathArray=newArray(VncMaskpathM, "Mask_VNC_Male.tif","DontOpen",tempMaskDir,"");
		fileOpen(FilePathArray);
		VncMaskpathM=FilePathArray[0];
		print("VncMaskpathM; "+VncMaskpathM);
		
		//	setBatchMode(false);
		//		updateDisplay();
		//		a
		
		TDext=File.exists(tempMaskDir+"JFRC2010_3D_Mask.nrrd");
		if(TDext==1)
		JFRC3Dpath=tempMaskDir;
		
		FilePathArray=newArray(JFRC3Dpath, "JFRC2010_3D_Mask.nrrd","DontOpen",tempMaskDir,"");
		fileOpen(FilePathArray);
		JFRC3Dpath=FilePathArray[0];
		tempMaskDir=FilePathArray[3];
		print("JFRC3Dpath; "+JFRC3Dpath);
		
		File.saveString(MIPtype+"\n"+subfolder+"\n"+colorcoding+"\n"+CropYN+"\n"+AutoBRV+"\n"+totalblock+"\n"+blockposition+"\n"+blockON+"\n"+desiredmean+"\n"+savestring+"\n"+colorscale+"\n"+reverse0+"\n"+startMIP+"\n"+endMIP+"\n"+usingLUT+"\n"+manualST+"\n"+lowerweight+"\n"+lowthreM+"\n"+unsharp+"\n"+expand+"\n"+secondjump+"\n"+UseSubfolderName+"\n"+JFRCpath+"\n"+VncMaskpathF+"\n"+JFRC3Dpath+"\n"+nc82nrrd+"\n"+DSLTver+"\n"+SkipDuplication+"\n"+tempMaskDir+"\n"+VncMaskpathM+"\n"+unsharp2+"\n"+easyADJ+"\n"+GammaON, filepath);
	}//if(i==startn){
	
	countFile=countFile+1;
	
	if(countFile==40){
		list = getFileList(dir);
		Array.sort(list);
		if(blockposition==totalblock)
		endn=list.length;
		
		if(blockposition>=1){
			if(totalblock>1){
				
				list = getFileList(dir);
				Array.sort(list);
				
				blocksize=(list.length/totalblock);
				blocksize=round(blocksize);
				OriStartn=blocksize*(blockposition-1);
				endn=OriStartn+blocksize;
				
			}
		}
		countFile=0;
	}//if(countFile==20){
	
	arrayi=i-startn;
	progress=i/endn;
	showProgress(progress);
	path = dir+list[i];
	
	if(UseSubfolderName!=false && UseSubfolderName!=true)
	UseSubfolderName=false;
	
	mipbatch=newArray(list[i], path, UseSubfolderName, dirCOLOR, endn, i, dir, startn, firsttime, firsttime1ch,AutoBRV,MIPtype,desiredmean,savemethod,CropYN,neuronimg,nc82,myDir2,myDirCLAHE,myDir2Co,myDir,usingLUT,Circulicity[arrayi],Roundness[arrayi],ratio[arrayi],AR[arrayi],lowerweight,lowthreM,manual,0,0,0,defaultNoCH,startMIP,endMIP,unsharp,printskip,expand,multiDSLT,secondjump,JFRCpath,VncMaskpathF,JFRC3Dpath,SkipDuplication,VncMaskpathM);
	//	mipbatch[0]=list[i]; mipbatch[1]=path; mipbatch[2]=UseSubfolderName; mipbatch[3]=dirCOLOR; mipbatch[4]=endn; 
	//	mipbatch[5]=i; mipbatch[6]=dir; mipbatch[7]=; mipbatch[8]=; mipbatch[9]=;
	
	if (endsWith(list[i], "/")){//if "/"
		if(subfolder==1){
			
			if(keepsubst==1){
				myDir0 = dirCOLOR+File.separator+list[i];
				File.makeDirectory(myDir0);
				dirCOLOR=myDir0;
			}
			
			//		print(subfolder);
			listsub = getFileList(dir+list[i]);
			Array.sort(listsub);
			for (ii=0; ii<listsub.length; ii++){
				path2 = path+listsub[ii];
				
				if (endsWith(listsub[ii], "/")){//if "/"
					listsub2 = getFileList(path2);
					Array.sort(listsub2);
					
					for (iii=0; iii<listsub2.length; iii++){
						path3 = path2+listsub2[iii];
						
						if (endsWith(listsub2[iii], "/")){//if "/"
							listsub3 = getFileList(path3);
							Array.sort(listsub3);
							
							for (iiii=0; iiii<listsub3.length; iiii++){
								path4 = path3+listsub3[iiii];
								
								if (endsWith(listsub3[iiii], "/")){//if "/"
									listsub4 = getFileList(path4);
									Array.sort(listsub4);
									
									for (iiiii=0; iiiii<listsub4.length; iiiii++){
										path5 = path4+listsub4[iiiii];
										mipbatch=newArray(listsub4[iiiii], path5, UseSubfolderName, dirCOLOR, endn, i, dir, startn, firsttime,firsttime1ch,AutoBRV,MIPtype,desiredmean,savemethod,CropYN,neuronimg,nc82,myDir2,myDirCLAHE,myDir2Co,myDir,usingLUT,Circulicity[arrayi],Roundness[arrayi],ratio[arrayi], AR[arrayi],lowerweight,lowthreM,manual,0,0,0,defaultNoCH,startMIP,endMIP,unsharp,printskip,expand,multiDSLT,secondjump,JFRCpath,VncMaskpathF,JFRC3Dpath,SkipDuplication,VncMaskpathM);
										mipfunction(nc82nrrd,mipbatch,easyADJ,GammaON);
										firsttime=mipbatch[8];
										firsttime1ch=mipbatch[9];
										neuronimg=mipbatch[15];
										nc82=mipbatch[16];
										myDir2=mipbatch[17];
										myDirCLAHE=mipbatch[18];
										myDir2Co=mipbatch[19];
										myDir=mipbatch[20];
										Circulicity[arrayi]=mipbatch[22];
										Roundness[arrayi]=mipbatch[23];
										ratio[arrayi]=mipbatch[24];
										AR[arrayi]=mipbatch[25];
										areasizeM[arrayi]=mipbatch[29];
										perimLM[arrayi]=mipbatch[30];
										defaultNoCH=mipbatch[31];
										JFRCpath=mipbatch[40];
										//	VncMaskpath=mipbatch[41];
									}
								}//	if (endsWith(listsub3[iiii], "/"))
								
								mipbatch=newArray(listsub3[iiii], path4, UseSubfolderName, dirCOLOR, endn, i, dir, startn, firsttime, firsttime1ch,AutoBRV,MIPtype,desiredmean,savemethod,CropYN,neuronimg,nc82,myDir2,myDirCLAHE,myDir2Co,myDir,usingLUT,Circulicity[arrayi],Roundness[arrayi],ratio[arrayi],AR[arrayi],lowerweight,lowthreM,manual,0,0,0,defaultNoCH,startMIP,endMIP,unsharp,printskip,expand,multiDSLT,secondjump,JFRCpath,VncMaskpathF,JFRC3Dpath,SkipDuplication,VncMaskpathM);
								mipfunction(nc82nrrd,mipbatch,easyADJ,GammaON);
								firsttime=mipbatch[8];
								firsttime1ch=mipbatch[9];
								neuronimg=mipbatch[15];
								nc82=mipbatch[16];
								myDir2=mipbatch[17];
								myDirCLAHE=mipbatch[18];
								myDir2Co=mipbatch[19];
								myDir=mipbatch[20];
								Circulicity[arrayi]=mipbatch[22];
								Roundness[arrayi]=mipbatch[23];
								ratio[arrayi]=mipbatch[24];
								AR[arrayi]=mipbatch[25];
								areasizeM[arrayi]=mipbatch[29];
								perimLM[arrayi]=mipbatch[30];
								defaultNoCH=mipbatch[31];
								JFRCpath=mipbatch[40];
								//		VncMaskpath=mipbatch[41];
							}
						}//if (endsWith(listsub2[iii], "/")
						
						mipbatch=newArray(listsub2[iii], path3, UseSubfolderName, dirCOLOR, endn, i, dir, startn, firsttime, firsttime1ch, AutoBRV, MIPtype, desiredmean, savemethod, CropYN, neuronimg, nc82,myDir2,myDirCLAHE,myDir2Co, myDir, usingLUT,Circulicity[arrayi],Roundness[arrayi],ratio[arrayi],AR[arrayi],lowerweight,lowthreM,manual,0,0,0,defaultNoCH,startMIP,endMIP,unsharp,printskip,expand,multiDSLT,secondjump,JFRCpath,VncMaskpathF,JFRC3Dpath,SkipDuplication,VncMaskpathM);
						mipfunction(nc82nrrd,mipbatch,easyADJ,GammaON);
						firsttime=mipbatch[8];
						firsttime1ch=mipbatch[9];
						neuronimg=mipbatch[15];
						nc82=mipbatch[16];
						myDir2=mipbatch[17];
						myDirCLAHE=mipbatch[18];
						myDir2Co=mipbatch[19];
						myDir=mipbatch[20];
						Circulicity[arrayi]=mipbatch[22];
						Roundness[arrayi]=mipbatch[23];
						ratio[arrayi]=mipbatch[24];
						AR[arrayi]=mipbatch[25];
						areasizeM[arrayi]=mipbatch[29];
						perimLM[arrayi]=mipbatch[30];
						defaultNoCH=mipbatch[31];
						JFRCpath=mipbatch[40];
						//	VncMaskpath=mipbatch[41];
					}//for (iii=0; iii<listsub2.length; iii++)
				}//if (endsWith(listsub[ii], "/"))
				mipbatch=newArray(listsub[ii], path2, UseSubfolderName, dirCOLOR, endn, i, dir, startn, firsttime, firsttime1ch, AutoBRV, MIPtype, desiredmean, savemethod, CropYN, neuronimg, nc82,myDir2,myDirCLAHE,myDir2Co, myDir, usingLUT,Circulicity,Roundness[arrayi],ratio[arrayi],AR[arrayi],lowerweight,lowthreM,manual,0,0,0,defaultNoCH,startMIP,endMIP,unsharp,printskip,expand,multiDSLT,secondjump,JFRCpath,VncMaskpathF,JFRC3Dpath,SkipDuplication,VncMaskpathM);
				mipfunction(nc82nrrd,mipbatch,easyADJ,GammaON);
				firsttime=mipbatch[8];
				firsttime1ch=mipbatch[9];
				neuronimg=mipbatch[15];
				nc82=mipbatch[16];
				myDir2=mipbatch[17];
				myDirCLAHE=mipbatch[18];
				myDir2Co=mipbatch[19];
				myDir=mipbatch[20];
				Circulicity[arrayi]=mipbatch[22];
				Roundness[arrayi]=mipbatch[23];
				ratio[arrayi]=mipbatch[24];
				AR[arrayi]=mipbatch[25];
				areasizeM[arrayi]=mipbatch[29];
				perimLM[arrayi]=mipbatch[30];
				defaultNoCH=mipbatch[31];
				JFRCpath=mipbatch[40];
				//	VncMaskpath=mipbatch[41];
			}//for (ii=0; ii<listsub.length; ii++){
			
		}//if(subfolder==1){
	}else{//	if (endsWith(list[i], "/")){
		mipfunction(nc82nrrd,mipbatch,easyADJ,GammaON);
		firsttime=mipbatch[8];
		firsttime1ch=mipbatch[9];
		neuronimg=mipbatch[15];
		nc82=mipbatch[16];
		myDir2=mipbatch[17];
		myDirCLAHE=mipbatch[18];
		myDir2Co=mipbatch[19];
		myDir=mipbatch[20];
		Circulicity[arrayi]=mipbatch[22];
		Roundness[arrayi]=mipbatch[23];
		ratio[arrayi]=mipbatch[24];
		AR[arrayi]=mipbatch[25];
		areasizeM[arrayi]=mipbatch[29];
		perimLM[arrayi]=mipbatch[30];
		defaultNoCH=mipbatch[31];
		JFRCpath=mipbatch[40];
		//	VncMaskpath=mipbatch[41];
		//	print("AR"+AR[arrayi]+"arrayi; "+arrayi);
	}
	if(nImages>0){
		//	print("Image"+nImages);
		for(ni=1; ni<=nImages; ni++){
			close();
		}
	}
}//for (i=startn; i<endn; i++){

if(measurement=="Yes"){//shape measurement
	resultnum=nResults();
	IJ.deleteRows(1, resultnum);
	
	for(resultNum=0; resultNum<endn-startn; resultNum++){
		setResult("Circulicity", resultNum, Circulicity[resultNum]);
		setResult("Roundness", resultNum, Roundness[resultNum]);
		setResult("ratio(perim/size)", resultNum, ratio[resultNum]);
		setResult("AR", resultNum, AR[resultNum]);
		setResult("Size", resultNum, areasizeM[resultNum]);
		setResult("Perim", resultNum, perimLM[resultNum]);
	}
}

/////////Function//////////////////////////////////////////////////////////////////
function mipfunction(nc82nrrd,mipbatch,easyADJ,GammaON) { 
	
	KeiNrrdShrink=0;
	GradientDim=false;
	CLAHE=true;
	colorcoding=true;
	
	listP=mipbatch[0];
	path=mipbatch[1];
	UseSubfolderName=mipbatch[2];
	dirCOLOR=mipbatch[3];
	endn=mipbatch[4];
	i=mipbatch[5];
	dir=mipbatch[6];
	startn=mipbatch[7];
	firsttime=mipbatch[8];
	firsttime1ch=mipbatch[9];
	AutoBRV=mipbatch[10];
	MIPtype=mipbatch[11];
	desiredmean=mipbatch[12];
	savemethod=mipbatch[13];
	CropYN=mipbatch[14];
	neuronimg=mipbatch[15];
	nc82=mipbatch[16];
	myDir2=mipbatch[17];
	myDirCLAHE=mipbatch[18];
	myDir2Co=mipbatch[19];
	myDir=mipbatch[20];
	usingLUT=mipbatch[21];
	Circulicity=mipbatch[22];
	Roundness=mipbatch[23];
	ratio=mipbatch[24];
	AR=mipbatch[25];
	lowerweight=mipbatch[26];
	lowthreM=mipbatch[27];
	manual=mipbatch[28];
	areasizeM=mipbatch[29];
	perimLM=mipbatch[30];
	defaultNoCH=mipbatch[32];
	startMIP=mipbatch[33];
	endMIP=mipbatch[34];
	unsharp=mipbatch[35];
	printskip=mipbatch[36];
	expand=mipbatch[37];
	multiDSLT=mipbatch[38];
	secondjump=mipbatch[39];
	JFRCpath=mipbatch[40];
	VncMaskpathF=mipbatch[41];
	JFRC3Dpath=mipbatch[42];
	SkipDuplication=mipbatch[43];
	VncMaskpathM=mipbatch[44];
	
	dotIndex = -1;
	dotIndexAM = -1;
	dotIndextif = -1;
	dotIndexTIFF = -1;
	dotIndexLSM = -1;
	dotIndexV3 = -1;
	dotIndexMha= -1;
	dotIndexzip= -1;
	dotIndexVNC =-1;
	dotIndexV3raw = -1;
	files=files+1;
	
	dotIndexMha = lastIndexOf(listP, "mha");
	dotIndexV3 = lastIndexOf(listP, "c0.v3dpbd");
	dotIndexV3raw = lastIndexOf(listP, "v3draw");
	dotIndexLSM = lastIndexOf(listP, "lsm");
	dotIndexoib = lastIndexOf(listP, "oib");
	if(dotIndexoib==-1)
	dotIndexoib= lastIndexOf(listP, "oif");
	
	//dotIndexJBA = lastIndexOf(listP, "02.nrrd");//02 channel JaneliaWorkstation downloaded nrrd
	//if(dotIndexJBA==-1)
	dotIndexJBA = lastIndexOf(listP, "02_warp");
	if(dotIndexJBA==-1)
	dotIndexJBA = lastIndexOf(listP, "03_warp");//03 channel JaneliaWorkstation downloaded nrrd
	
	if(nc82nrrd==true)
	dotIndexJBA = lastIndexOf(listP, ".nrrd");
	
	//print("dotIndexJBA; "+dotIndexJBA);
	
	dotIndex = lastIndexOf(listP, ".v3dpbd");
	if(dotIndex==-1)
	dotIndex = lastIndexOf(listP, ".h5j");
	
	dotIndexCMTK = lastIndexOf(listP, "_02_warp");//02 channel CMTKresult nrrd
	if(dotIndexCMTK==-1)
	dotIndexCMTK = lastIndexOf(listP, "_03_warp");//03 channel CMTKresult nrrd
	if(dotIndexCMTK==-1)
	dotIndexCMTK = lastIndexOf(listP, "_04_warp");//03 channel CMTKresult nrrd
	
	//	if(dotIndexJBA==-1 && dotIndexCMTK==-1)
	//	dotIndexJBA = lastIndexOf(listP, ".nrrd");// all nrrd files
	
	
	//	print("dotIndexJBA; "+dotIndexJBA+"   dotIndexCMTK; "+dotIndexCMTK);
	//	print("");
	//	print(path);
	
	dotIndexAM = lastIndexOf(listP, ".am");
	dotIndextif = lastIndexOf(listP, "tif");
	dotIndexTIFF = lastIndexOf(listP, "TIFF");
	dotIndexzip = lastIndexOf(listP, ".zip");
	//dotIndexVNC = lastIndexOf(listP, "v_");
	
	
	//// Duplication check ////////////////////////////////////////////
	filepathcolor=0;
	JPGindex=lastIndexOf(listP, ".jpg");
	
	if(SkipDuplication==true){
		
		if(JPGindex==-1){
			for(save0=0; save0<listsave.length; save0++){
				
				namelist=listsave[save0];
				nnamelist = lengthOf(namelist);
				
				if(UseSubfolderName==false){
					dotposition=lastIndexOf(listP, ".");
					purenameOri=substring(listP, 0, dotposition);//original data
					
					QIindex=lastIndexOf(purenameOri,"_QI");
					if(QIindex!=-1){
						purenameOri=substring(purenameOri, 0, QIindex);
					}
				}else{
					FileSep1=lastIndexOf(path, "/");
					if(FileSep1!=-1)
					folderLast1=substring(path, 0, FileSep1);
					
					FileSep2=lastIndexOf(folderLast1, File.separator);
					purenameOri=substring(folderLast1, FileSep2+1, lengthOf(folderLast1));
				}
				lengthNameOri = lengthOf(purenameOri);
				if(lengthNameOri<nnamelist)
				namelist = substring(namelist, 0, lengthNameOri); // adjust to same length
				
				//	print("dotposition; "+dotposition+"   purenameOri; "+purenameOri+"  lengthNameOri; "+lengthNameOri+"   namelist; "+namelist+"  nnamelist; "+nnamelist);
				
				if(namelist==purenameOri){
					filepathcolor=1;
					save0=listsave.length;
				}
			}
		}//if(JPGindex==-1){
	}//if(SkipDuplication){
	//print("dotIndexVNC; "dotIndexVNC+"   dotIndextif; "+dotIndextif+"   dotIndexzip; "+dotIndexzip+"   dotIndexTIFF; "+dotIndexTIFF+"   dotIndex;"+dotIndex+"   dotIndexAM; "+dotIndexAM+"   dotIndexLSM; "+dotIndexLSM+"   dotIndexMha; "+dotIndexMha+"   dotIndexV3; "+dotIndexV3+"   dotIndexV3raw; "+dotIndexV3raw==-1 && dotIndexJBA==-1 && dotIndexCMTK==-1))
	
	if(filepathcolor==1){
		if(printskip==1)
		print("Skipped; "+i+"; 	 "+listP);
	}else if(dotIndexoib==-1 && dotIndexVNC==-1 && dotIndextif==-1 && dotIndexzip== -1 && dotIndexTIFF==-1 && dotIndex==-1 && dotIndexAM==-1 && dotIndexLSM==-1 && dotIndexMha==-1 && dotIndexV3==-1 && dotIndexV3raw==-1 && dotIndexJBA==-1 && dotIndexCMTK==-1){
		
		if(printskip==1)
		print("Skipped; "+i+"; 	 "+listP);
	}else{
		
		if(dotIndexoib>-1){// if not compressed
			previousPath=path;
			
			newpath = replace(path, " ", "_");
			File.rename(previousPath, newpath); // - Renames, or moves, a file or directory. Returns "1" (true) if successful. 
			path = newpath;
			
			run("Bio-Formats Importer", "open="+path+" autoscale color_mode=Default view=[Standard ImageJ] stack_order=Default");
			getDimensions(width, height, channels, slices, frames);
			run("Stack to Hyperstack...", "order=xyczt(default) channels="+channels+" slices="+slices+" frames=1 display=Color");
		}
		
		if(dotIndexJBA>-1 || dotIndexCMTK>-1 || dotIndextif>-1 || dotIndexTIFF>-1 || dotIndexLSM>-1 || dotIndexMha>-1 || dotIndexzip>-1 || dotIndexVNC>-1 || dotIndexV3raw>-1 || dotIndexV3>-1 || dotIndex>-1){
			//	filesize=File.length(path);
			//	if(filesize>10000000){// if more than 60MB
			print(listP+"	 ;	 "+i+" / "+endn+"  TRY");
			IJ.redirectErrorMessages();
			
			PathExt=File.exists(path);
			
			if(PathExt==1){
				open(path);// for tif, comp nrrd, lsm", am, v3dpbd, mha
				print(listP+"	 ;	 "+i+" / "+endn+"  opened");
			}else{
				print("File is not existing; "+path);
			}
			//	}else{
			//		print("file size is too small, "+filesize/10000000+" MB, less than 60MB.  "+listP+"	 ;	 "+i+" / "+endn);
			//	print(listP+"	 ;	 "+i+" / "+endn+"  too small");
			//	}
			
			dotIndex= lastIndexOf(listP,".");
			origiMIP= substring(listP, 0, dotIndex);
		}
		
		
		if(nImages>0){
			
			bitd=bitDepth();
			totalslice=nSlices();
			origi=getTitle();
			getDimensions(width, height, channels, slices, frames);
			getVoxelSize(VxWidth, VxHeight, VxDepth, VxUnit);
			
			
			if(KeiNrrdShrink==1){
				run("Size...", "width=802 height=601 constrain average interpolation=Bicubic");
				run("Translate...", "x=0 y=-19 interpolation=None");
				run("Canvas Size...", "width=1024 height=512 position=Center-Right zero");
			}
			
			
			if(channels>10){
				run("Properties...", "channels=1 slices="+channels+" frames=1 unit=microns pixel_width="+VxWidth+" pixel_height="+VxHeight+" voxel_depth="+VxDepth+"");
				
				getDimensions(width, height, channels, slices, frames);	
			}
			
			QIvalue="";
			
			print("UseSubfolderName; "+UseSubfolderName);
			if(UseSubfolderName==false){
				dotIndex = lastIndexOf(origi, ".");
				if (dotIndex!=-1);
				origiMIP = substring(origi, 0, dotIndex); // remove extension
				
				QIindex=lastIndexOf(origiMIP,"_QI");
				if(QIindex!=-1){
					origiMIP=substring(origiMIP, 0, QIindex);
					
					QInameEndIndex=lastIndexOf(origi,",");
					if(QInameEndIndex!=-1){
						QInameEnd=substring(origi,QIindex,QInameEndIndex);
						
						QIvalueStartIndex=lastIndexOf(QInameEnd,",");
						QIvalue=substring(QInameEnd,QIvalueStartIndex+1, lengthOf(QInameEnd));
						QIvalue="_QI"+QIvalue+"_";
					}else{
						
						warpIndex=lastIndexOf(origi,"_warp");
						
						if(warpIndex!=-1){
							QIvalue=substring(origi,warpIndex-3,dotIndex);
							
						}else{
							
							QIvalue=substring(origi,QIindex,dotIndex);
							QIvalue=QIvalue+"_";
						}	
					}
				}//if(QIindex!=-1){
				
			}else{
				
				FileSep1=lastIndexOf(path, "/");
				folderLast1=substring(path, 0, FileSep1);
				print("folderLast1;  "+folderLast1);
				
				FileSep2=lastIndexOf(folderLast1, File.separator);
				origiMIP=substring(folderLast1, FileSep2+1, lengthOf(folderLast1));
			}
			if(manual==1){//if(manualST=="Manual setting 1st time only")
				if(channels==2 || channels==3 || channels==4){
					if(defaultNoCH!=channels && defaultNoCH!=0){
						channels=0;
						AutoBRV=0;
						colorcoding=0;
						close();
						print("Ch number different skipped; "+i+"; 	 "+listP);// if ch2 and ch3 are mixed
					}
				}// if ch2 and ch3 are mixed
				
				if(channels==2 || channels==3 || channels==4 || bitd==24){
					if(firsttime==1 || firsttime==2 || firsttime==3){
						defaultNoCH=channels;
						run("Split Channels");
					}
					
					if(firsttime==0){//creating directory
						firsttime=1;
						
						if(colorcoding==1){
							if(savemethod==1)
							myDir2Co = dirCOLOR;
							
							if(AutoBRV==1){
								if(savemethod==0)
								myDir2Co = dir+File.separator+"Color_Depth_MIP_"+desiredmean+"_mean_adjusted"+File.separator;
							}//if(AutoBRV==1){
							
							if(AutoBRV==0){
								if(savemethod==0)
								myDir2Co = dir+File.separator+"Color_Depth_MIP"+File.separator;
							}//if(AutoBRV==0){
							
							if(savemethod==0)
							File.makeDirectory(myDir2Co);
						}//if(colorcoding==1){
						
						if(channels==2 || channels==3){
							setBatchMode(false);
							updateDisplay();
							run("Split Channels");
							if(channels==2)
							waitForUser("Choose neuron channel on Front, nc82 window for back");
							
							if(channels==3){
								waitForUser("Choose neuron channel on Front, nc82 window for 2nd, the other is back");
								imageNum=2;
							}
							
							setBatchMode(true);
						}else
						run("Split Channels");
						
						if(channels==2){
							if(neuronimg=="C1-"+origi){
								neuronimg="C1-";
								nc82="C2-";
							}else if(neuronimg=="C2-"+origi){
								neuronimg="C2-";
								nc82="C1-";
							}//if(neuronimg=="C2-"+origi){
						}else if(channels==3){
							if(neuronimg=="C1-"+origi){
								neuronimg="C1-";
								run("Put Behind [tab]");
								notneed=getTitle();
								if(notneed=="C2-"+origi){
									notneedST="C2-";
									nc82="C3-";
								}else{
									notneedST="C3-";
									nc82="C2-";
								}
							}else if(neuronimg=="C2-"+origi){
								neuronimg="C2-";
								run("Put Behind [tab]");
								notneed=getTitle();
								if(notneed=="C1-"+origi){
									notneedST="C1-";
									nc82="C3-";
								}else{
									notneedST="C3-";
									nc82="C1-";
								}
							}else if(neuronimg=="C3-"+origi){
								neuronimg="C3-";
								run("Put Behind [tab]");
								notneed=getTitle();
								if(notneed=="C1-"+origi){
									notneedST="C1-";
									nc82="C2-";
								}else{
									notneedST="C2-";
									nc82="C1-";
								}
							}//if(neuronimg=="C1-"+origi){
						}else if(channels==4){
							nc82="C4-";
							
						}
						//if(channels==2){
					}//if(firsttime==0){//creating directory
					
					//////manual == 1, manual channel discrimination///////////////	
					if(channels==2 || channels==3)
					neuronCH=neuronimg+origi;
					
					if(channels==3){
						selectWindow(notneedST+origi);//not need
						close();
						selectImage(nc82);
						close();
					}
					
					if(channels==4)
					titlelist=getList("image.titles");
					
					if(measurement=="Yes"){
						run("Z Project...", "start=1 stop="+nSlices+" projection=[Average Intensity]");
						run("8-bit");
						maxP=getTitle();
						
						//setAutoThreshold("Huang dark");
						setAutoThreshold("Intermodes dark");
						
						getThreshold(lower, upper);
						setThreshold(lower, upper);
						
						setOption("BlackBackground", true);
						run("Convert to Mask");
						
						run("Make Binary");
						run("Analyze Particles...", "size=100.00-Infinity circularity=0.00-1.00 show=Nothing display clear");
						maxsize=0; maxperim=0;
						updateResults();
						for(getresult=0; getresult<nResults; getresult++){
							areasize=getResult("Area", getresult);
							perimL=getResult("Perim.", getresult);
							//				print("perimL; "+perimL);
							
							if(areasize>=maxsize){
								maxsize=areasize;
								maxperim=perimL;
								Circulicity=getResult("Circ.", getresult);
								Roundness=getResult("Round", getresult);
								AR=getResult("AR", getresult);
								areasizeM=getResult("Area", getresult);
								perimLM=getResult("Perim.", getresult);
								//		print("AR; "+AR);
							}
						}
					}//if(measurement=="Yes"){
					
					if(channels==2 || channels==3)
					selectWindow(neuronCH);//Green signal
				}//if(channels==2 || channels==3 ){
				
				if(channels==1){
					if(colorcoding==1){//create directory
						if(savemethod==1)
						myDir2Co = dirCOLOR;
						
						if(savemethod==0){
							myDir2Co = dir+File.separator+"1ch_Color_Depth_MIP"+desiredmean+"_mean_adjusted"+File.separator;
							if(firsttime1ch==0){
								File.makeDirectory(myDir2Co);
								firsttime1ch=1;
							}
						}
					}//if(colorcoding==1){
					selectWindow(origi);
				}//if(channels==1){
				imageNum=nImages();
				if(channels==2)
				imageNum=imageNum-1;
			}//	if(manual==1){//if(manualST=="Manual setting 1st time only")
			
			if(manual==2){//	if(manual==2){ all channels mip creation
				if(channels>1 || bitd==24)
				run("Split Channels");
				titlelist=getList("image.titles");
				
				imageNum=titlelist.length;
				
				if(colorcoding==1)
				if(savemethod==1)
				myDir2Co = dirCOLOR;
			}//if(manual==2){//	if(manual==2){ all channels mip creation
			
			if(manual==3){// Automatic signal amount measurement & nc82 close
				
				run("Split Channels");
				
				run("Set Measurements...", "area centroid perimeter shape redirect=None decimal=2");
				
				sumCh=newArray(channels);
				
				//		for(imageN=0; imageN<channels; imageN++){
				//			Ch[imageN]=getTitle();
				//			run("Put Behind [tab]");
				//			print(Ch[imageN]);
				//		}
				Ch=getList("image.titles");
				
				for(iamgen=0; iamgen<channels; iamgen++){
					
					selectWindow(Ch[iamgen]);
					//	print(Ch[iamgen]);
					
					run("Z Project...", "start=1 stop="+nSlices+" projection=[Max Intensity]");
					run("8-bit");
					maxP=getTitle();
					
					//run("Histogram thresholding", "z-attenuation=1 how=2");
					setAutoThreshold("Intermodes dark");
					
					//	setAutoThreshold("Huang dark");
					
					//			setThreshold(0, 255);
					
					//Histval=getTitle();
					//Histval=round(Histval);
					
					getThreshold(lower, upper);
					setThreshold(lower, upper);
					
					setOption("BlackBackground", true);
					run("Convert to Mask");
					
					run("Make Binary");
					run("Analyze Particles...", "size=100.00-Infinity circularity=0.00-1.00 show=Nothing display clear");
					maxsize=0; maxperim=0;
					
					for(getresult=0; getresult<nResults; getresult++){
						areasize=getResult("Area", getresult);
						perimL=getResult("Perim.", getresult);
						
						if(areasize>=maxsize){
							maxsize=areasize;
							maxperim=perimL;
							Circulicity=getResult("Circ.", getresult);
							Roundness=getResult("Round", getresult);
							AR=getResult("AR", getresult);
							areasizeM=getResult("Area", getresult);
							perimLM=getResult("Perim.", getresult);
							//	print("AR; "+AR);
						}
					}
					
					run("Analyze Particles...", "size="+maxsize-100+"-Infinity circularity=0.00-1.00 show=Masks display clear");
					
					masknc82=getImageID();// opened images are original stack, "sumCh"+iamgen, masknc82
					masknc82title=getTitle();
					run("RGB Color");
					run("8-bit");
					
					print("masknc82title; "+masknc82title+"   Slices; "+nSlices);
					
					//creation of rectangle = background measure
					makeRectangle(0, 2, 100, 100);
					getStatistics(area, mean, min, max, std, histogram);
					
					if(mean>200){//convert to 16bit mask
						run("Invert LUT");
						run("RGB Color");
						run("8-bit");
						run("16-bit");
						run("Select All");
						run("Mask255 to 4095");
					}else{
						run("Select All");
						run("16-bit");
						run("Mask255 to 4095");
					}
					
					selectImage(maxP);
					close();
					
					selectWindow(Ch[iamgen]);
					run("Z Project...", "start=1 stop="+nSlices+" projection=[Sum Slices]");
					
					run("16-bit");
					sumP=getImageID();
					sumPST=getTitle();
					
					imageCalculator("AND create", ""+sumPST+"", ""+masknc82title+"");//AND operation, then measure signal amount only mask region//////////////
					getStatistics(area, amount, min, max, std, histogram);//	Amount of signal, larger is nc82
					close();//
					
					ratio=maxperim/maxsize;// smaller is nc82
					
					List.set("ImageSize"+iamgen, maxsize);
					List.set("ratio"+iamgen, ratio);
					List.set("Amount"+iamgen, amount);	//counting only signal region, not count background
					List.set("Circulicity"+iamgen, Circulicity);
					
					selectImage(masknc82);
					close();
					
					selectImage(sumP);
					close();
					
					ChRest=getList("image.titles");
					restNo=0;
					while(nImages>channels){
						selectWindow(ChRest[restNo]);
						sampleslice=nSlices();
						if(sampleslice==1)
						close();
						
						restNo=restNo+1;
					}
					
					print(iamgen+";  maxsize; "+maxsize+"  ratio; "+ratio+"  amount; "+amount+"  Circulicity; "+Circulicity);
				}//for(iamgen=0; iamgen<channels; iamgen++){
				
				defaultM=100; defaultsize=0; defaultamout=0; defaultcirc=0; amountgap=0; sizegap=0; nc82Amount=0; nc82Size=0; nc82Ratio=0; nc82Circu=0;
				
				for(chnum0=0; chnum0<channels; chnum0++){
					ratiocomp=List.get("ratio"+chnum0);
					sizecomp=List.get("ImageSize"+chnum0);
					sizecomp=round(sizecomp);
					amountcomp=List.get("Amount"+chnum0);
					amountcomp=round(amountcomp);
					
					CirculicityComp=List.get("Circulicity"+chnum0);
					
					if(ratiocomp<defaultM){//smallest ratio is nc82
						defaultM=ratiocomp;
						nc82Ratio=chnum0;
					}
					if(sizecomp > defaultsize){//highest number= nc82
						if(defaultsize!=0)
						sizegap=sizecomp/defaultsize;
						
						defaultsize=sizecomp;
						nc82Size=chnum0;
					}
					if(amountcomp>defaultamout){//highest number= nc82
						if(defaultamout!=0)
						amountgap=amountcomp/defaultamout;
						
						defaultamout=amountcomp;
						nc82Amount=chnum0;
					}
					if(CirculicityComp>defaultcirc){//highest number= nc82
						defaultcirc=CirculicityComp;
						nc82Circu=chnum0;
					}
				}//for(chnum0=0; chnum0<channels; chnum0++){
				print("nc82Amount; "+nc82Amount+"  nc82Size; "+nc82Size+"  nc82Ratio; "+nc82Ratio+"	  nc82Circu; "+nc82Circu);
				
				nc82Real=1000;
				
				if(nc82Amount==nc82Size && nc82Ratio==nc82Size && nc82Amount==nc82Ratio && nc82Circu==nc82Ratio && nc82Circu==nc82Amount){
					nc82Real=nc82Amount;
				}else{
					
					if(nc82Circu==nc82Ratio)
					nc82Real=nc82Circu;
					
					else if(nc82Amount==nc82Size)
					nc82Real=nc82Amount;
					
					else if(nc82Ratio==nc82Size)
					nc82Real=nc82Ratio;
					
					else if(nc82Amount==nc82Ratio)
					nc82Real=nc82Amount;
				}
				if(defaultcirc<0.1){//Circulicity thresholding
					if(nc82Circu!=nc82Real)
					nc82Real=nc82Circu;
					
					if(nc82Amount==nc82Size && nc82Ratio==nc82Size && nc82Amount==nc82Ratio)
					nc82Real=nc82Amount;
					
					if(sizegap>10)//if size difference is more than 10 time, bigger is nc82
					nc82Real=nc82Size;
					
				}
				
				if(nc82Real==1000){
					print("Could not detect nc82, just will close 1 image")
					nc82Real=0;
				}
				
				selectWindow(Ch[nc82Real]);//nc82
				close();
				
				if(channels==2)
				neuronCH=getTitle();
				
				//	setBatchMode(false);
				//	updateDisplay();
				//	a
				
				titlelist=getList("image.titles");
				imageNum=nImages();
				print("imageNum; "+imageNum);
				
				if(colorcoding==1)
				if(savemethod==1)
				myDir2Co = dirCOLOR;
			}//if(manual==3){
			
			for(MIPtry=1; MIPtry<=imageNum; MIPtry++){
				
				if(channels==3 || manual==3 || bitd==24){
					selectWindow(titlelist[MIPtry-1]);
					neuronCH=getTitle();
					NeuronID=getImageID();
					neuronimg="C"+MIPtry+"-";
					
					neuronCH=neuronimg+origi;
				}
				
				if(channels==4 || manual==2){
					selectWindow(titlelist[MIPtry-1]);
					neuronCH=titlelist[MIPtry-1];
					NeuronID=getImageID();
					neuronimg="C"+MIPtry+"-";
					
					print("All channel MIP mode");
				}
				
				if(channels!=0){
					
					stackSt=getTitle();
					selectWindow(stackSt);
					stack=getImageID();
					
					
					if(bitd==32 || unsharp=="Max"){
						setMinAndMax(0, 1);
						run("8-bit");
						bitd=8;
						unsharp="Max";//"NA", "Unsharp", "Max"
						DefMaxValue=1;
						AutoBRV=0;
					}
					
					if(bitd==24)
					Inimax=255;
					
					MedianSub=70;
					print("Channel number; "+channels);
					if(bitd==8){
						print("8bit file");
						desiredmean=200;
						lowerweight=0.3;
						secondjump=245;
						MedianSub=0;
						DefMaxValue=255;
						
						run("Z Project...", "projection=[Max Intensity]");
						run("Enhance Contrast", "saturated=0.3");
						getMinAndMax(min, Inimax);
						close();
						
						if(Inimax!=255){
							selectWindow(stackSt);
							setMinAndMax(0, Inimax);
							run("Apply LUT", "stack");
						}
						
						run("Max value");
						logsum=getInfo("log");
						maxStartindex=lastIndexOf(logsum,"Maxvalue");
						maxEndindex=lastIndexOf(logsum,"Minvalue");
						maxvalue=substring(logsum, maxStartindex+10, maxEndindex-2);
						maxvalue=round(maxvalue);
						
						print("3D stack brightness adjusted; maxvalue; "+maxvalue+"  Inimax; "+Inimax);
						
					}//if(bitd==8){
					
					if(bitd==16){
						run("Z Project...", "projection=[Max Intensity]");
						MIPtitle= getTitle();
						
						resetMinAndMax();
						getMinAndMax(Inimin, max);
						
						if(max>255 && max<4096)
						DefMaxValue=4095;
						else if (max>4095)
						DefMaxValue=65535;
						else if (max<256)
						DefMaxValue=255;
						
						if(max<=255){// 8 bit file in 16bit format
							desiredmean=205;
							lowerweight=0;// mip function subtraction
							
							MedianSub=100;
							print("desiremean 205  16bit file with 8bit data");
						}
						
						
						run("Enhance Contrast", "saturated=0.3");
						getMinAndMax(min, Inimax);
						
						RealInimax=Inimax;
						
						if(DefMaxValue==255){
							Inimax=round(Inimax*6);
							print("DefMaxValue = 255, 16 bit");
						}
						
						if(DefMaxValue==4095){
							if(Inimax<200 && Inimax>100)
							Inimax=Inimax*3.5;
							else if (Inimax>=200 && Inimax<300)
							Inimax=round(Inimax*3);
							else if (Inimax<100)
							Inimax=Inimax*4;
							else if (Inimax>=300 && Inimax<500)
							Inimax=Inimax*2;
						}
						
						if(DefMaxValue==65535){
							if(Inimax<3200 && Inimax>1600)
							Inimax=Inimax*3;
							else if (Inimax>=3200 && Inimax<4800)
							Inimax=round(Inimax*2.5);
							else if (Inimax<1600)
							Inimax=Inimax*4;
							else if (Inimax>=4800 && Inimax<8000)
							Inimax=Inimax*2;
						}
						
						
						selectWindow(MIPtitle);
						
						setMinAndMax(0, Inimax);
						run("Apply LUT");
						
						VNCMaskName="";
						if(getWidth==512){
							if(getHeight==1024 || getHeight==1100){
								
								if(getHeight==1024){
									FilePathArray=newArray(VncMaskpathF, "Mask_VNC_Female.tif","Open",tempMaskDir,MIPtitle);
									VNCMaskName="Mask_VNC_Female.tif";
								}if(getHeight==1100){
									FilePathArray=newArray(VncMaskpathM, "Mask_VNC_Male.tif","Open",tempMaskDir,MIPtitle);
									VNCMaskName="Mask_VNC_Male.tif";
								}
								fileOpen(FilePathArray);
									
								zerovalue=239907;
								
								selectWindow(MIPtitle);
							}
						}else if(getWidth==1024){
							if(getHeight==512){
								FilePathArray=newArray(0, "JFRC2010_Mask.tif","Open",tempMaskDir,MIPtitle);
								MaskName="JFRC2010_Mask.tif";
								fileOpen(FilePathArray);
							}
						}else{
							
							setMinAndMax(0, 65535);
							selectWindow(MIPtitle);
							
							makeRectangle(getWidth*0.1, getHeight*0.1, getWidth*0.8, getHeight*0.8);
							setForegroundColor(0, 0, 0);
							run("Fill", "slice");
						}//if(getWidth==512){
						
						total=0; // getHistogram is broke;
						for(ix=0; ix<getWidth; ix++){
							for(iy=0; iy<getHeight; iy++){
								pxv = getPixel(ix,iy);
								total= total+pxv;
								
								if(pxv==0 && 	VNCMaskName=="")
								zerovalue=zerovalue+1;
							}
						}//for(ix=0; ix<getWidth; ix++){
						
						
						
						//		zerovalue=counts[0];
						Inimin=round((total/((getHeight*getWidth)-zerovalue))*0.8);//239907 is female VNC size
						
						print("Initial Bri adjustment; Inimin; "+Inimin+"   max; "+Inimax+"   RealInimax; "+RealInimax+"   DefMaxValue; "+DefMaxValue);
						
						
						//		setBatchMode(false);
						//		updateDisplay();
						//		a
						
						while(isOpen(MIPtitle)){
							selectWindow(MIPtitle);
							close();
						}
						
						if(Inimin!=0 || Inimax!=65535){
							selectWindow(stackSt);
							
							if(easyADJ==true || AutoBRV==1){
								setMinAndMax(0, Inimax);
								run("Apply LUT", "stack");
							}
							
							//		setBatchMode(false);
							//		updateDisplay();
							//		a
							if(AutoBRV==1){
								setMinAndMax(Inimin, 65535);
								run("Apply LUT", "stack");
								
								run("Max value");
								logsum=getInfo("log");
								maxStartindex=lastIndexOf(logsum,"Maxvalue");
								maxEndindex=lastIndexOf(logsum,"Minvalue");
								maxvalue=substring(logsum, maxStartindex+10, maxEndindex-2);
								maxvalue=round(maxvalue);
								
								print("3D stack brightness adjusted; maxvalue; "+maxvalue);
							}
						}else
						maxvalue=65535;
						
					}//	if(bitd==16){
					
					
					//			setBatchMode(false);
					//			updateDisplay();
					//			a
					
					if(unsharp!="Max" && AutoBRV==1){
						if(lowerweight>0){
							if(getHeight==512 && getWidth==1024){
								if(MedianSub!=0){
									
									run("Z Project...", "projection=[Max Intensity]");
									MedianSub=70;
									getMinAndMax(min, max);
									if(max<=255){// 8 bit file in 16bit format
										desiredmean=200;
										lowerweight=0;
										
										MedianSub=100;
										print("16bit file with 8bit data");
									}
									close();
									
									
									if(isOpen("JFRC2010_3D_Mask.nrrd")==0);
									open(JFRC3Dpath);
									
									if(bitd==16 && max>255){
										histave=50;
										MedianSub=60;
									}else
									histave=1;
									
									
									run("Mask Median Subtraction", "mask=JFRC2010_3D_Mask.nrrd data="+stackSt+" %="+MedianSub+" subtract histogram="+histave+"");
									selectWindow("JFRC2010_3D_Mask.nrrd");
									close;
								}//	if(MedianSub!=0){	
							}else{
								OrigiSlice=nSlices();
								if(MedianSub!=0){
									if(lowerweight>0){
										run("Z Project...", "start=10 stop="+OrigiSlice-10+" projection=[Max Intensity]");
										rename("MIP_mask.tif");
										setAutoThreshold("Mean dark");
										setOption("BlackBackground", true);
										run("Convert to Mask");
										
										//		setBatchMode(false);
										//		updateDisplay();
										//		a
										
										run("Select All");
										run("Copy");
										
										if(bitd==16)
										histave=50;
										else
										histave=5;
										
										for(islicen=2; islicen<=OrigiSlice; islicen++){
											run("Add Slice");
											run("Paste");
											//		print("slice added "+islicen);
										}
										
										//			setBatchMode(false);
										//					updateDisplay();
										//					a
										
										print("1278 lowerweight*0.3; "+lowerweight*0.3);
										run("Mask Median Subtraction", "mask=MIP_mask.tif data="+stackSt+" %="+lowerweight*30+" subtract histogram="+histave+"");
										
										//					setBatchMode(false);
										//										updateDisplay();
										//										a
										
										selectWindow("MIP_mask.tif");
										close;
									}
								}
							}
						}//	if(lowerweight>0){
					}//if(unsharp!="Max"){
					//					setBatchMode(false);
					//					updateDisplay();
					//					a
				}//if(channels!=0){
				
				
				BasicMIP=newArray(bitd,0,stack,GradientDim,stackSt);
				basicoperation(BasicMIP);//rename MIP.tif
				
				
				MIP=getImageID();
				run("Canvas Size...", "width="+round(getWidth()*0.95)+" height="+round(getHeight()*0.95)+" position=Center zero");
				//	DefMaxValue=BasicMIP[1];//actual max value in stack
				sigsize=0;
				
				print("basicoperation done");
				
				//			setBatchMode(false);
				//				updateDisplay();
				//				a
				applyV=1;
				
				if(AutoBRV==1){//to get brightness value from MIP
					selectImage(MIP);
					briadj=newArray(desiredmean, 0, 0, 0,lowerweight,lowthreM,autothre,maxvalue,MIP,stack,multiDSLT,secondjump);
					autobradjustment(briadj,DSLTver,DefMaxValue);
					applyV=briadj[2];
					sigsize=briadj[1];
					sigsizethre=briadj[3];
					sigsizethre=parseFloat(sigsizethre);
					sigsize=parseFloat(sigsize);
					
					if(isOpen("test.tif")){
						selectWindow("test.tif");
						close();
					}
					print("Auto-bri finished");
				}//	if(AutoBRV==1){
				
				if(colorcoding==1 && AutoBRV==1){
					
					if(channels==1)
					selectWindow(origi);
					
					if(channels==2 || channels==3 || channels==4)
					selectWindow(neuronCH);
					
					if(unsharp=="Unsharp")
					run("Unsharp Mask...", "radius=1 mask=0.35 stack");
					else if(unsharp=="Max")
					run("Maximum...", "radius=1.5 stack");
					
					if(bitd==16){
						if(DefMaxValue==4095)
						RealapplyV= round((applyV/16)*(Inimax/DefMaxValue));// adjusting from 65535 to 4095
						else if (DefMaxValue==255)
						RealapplyV=((applyV/(16*16))*(Inimax/DefMaxValue));// adjusting from 65535 to 4095
						else if (DefMaxValue==65535)
						RealapplyV= round(applyV*(Inimax/DefMaxValue));// adjusting from 65535 to 4095
					}
				}//if(colorcoding==1){
				
				if(AutoBRV==1){//to get brightness value from MIP
					if(bitd==8 || bitd==24)
					RealapplyV= round(applyV*(Inimax/255));
					
					print("After +Inimax RealapplyV; "+RealapplyV+"   applyV; "+applyV);
				}
				
				
				if(sigsize<30){
					if(AutoBRV==1){
						brightnessapplyArray = newArray(applyV,RealapplyV,sigsize,sigsizethre,GammaON);
						brightnessapply(DefMaxValue,filepath,brightnessapplyArray, bitd,lowerweight,lowthreM,stack,JFRCpath,VncMaskpathF,secondjump,Inimax,VncMaskpathM);
						
						applyV=brightnessapplyArray[0];
						RealapplyV=brightnessapplyArray[1];
					}
				}else{
					setMinAndMax(0, 65535);
					run("8-bit");
				}//if(sigsize<30){
				if(reverse0==1){
					run("Reverse");
					run("Flip Horizontally", "stack");
				}
				
				if(usingLUT=="royal")
				stackconcatinate();
				
				
				if(AutoBRV==0){
					selectWindow(origi);
					applyV=255;
					if(bitd==16){
						
						if(easyADJ==false)
						setMinAndMax(0, DefMaxValue);
						
						run("8-bit");
					}
					
					print("line 1670 nImages; "+nImages);
				}//if(AutoBRV==0){
				
				if(AutoBRV==1){
					applyV = round(RealapplyV);
					print("Line 1518 RealapplyV; "+round(RealapplyV));
				}
				
				
				ColorCoder(slices, applyV, width, AutoBRV, bitd, CLAHE, colorscale, reverse0, colorcoding, usingLUT,DefMaxValue,startMIP,endMIP,expand,GammaON);
				
				if(AutoBRV==1){
					if(sigsize>9)
					DSLTst="_DSLT";
					else if(sigsize<10)
					DSLTst="_DSLT0";
					
					if(sigsizethre>9)
					threST="_thre";
					else if (sigsizethre<10)
					threST="_thre0";
					
					if(bitd==8){
						if(applyV<100)
						applyVST="_0";
						else
						applyVST="_";
					}else if(bitd==16){
						if(applyV<1000)
						applyVST="_0";
						else if (applyV>999)
						applyVST="_";
						else if(applyV<100)
						applyVST="_00";
					}
				}//if(AutoBRV==1){
				
				if(CropYN)
				CropOP(MIPtype,applyV,colorscale);
				
				
				TrueMaxValue=0;
				if(DefMaxValue<4096){
					
					TrueMaxValue=4095;
					if(DefMaxValue<256)
					TrueMaxValue=255;
					
				}else if(DefMaxValue>4095)
				TrueMaxValue=65535;
				
				if(imageNum==1){
					if(AutoBRV==1)
					save(myDir2Co+origiMIP+QIvalue+applyVST+applyV+DSLTst+sigsize+threST+sigsizethre+".tif");
					else
					save(myDir2Co+origiMIP+".tif");
					
				}else{
					if(AutoBRV==1)
					save(myDir2Co+origiMIP+"_CH"+MIPtry+QIvalue+applyVST+applyV+DSLTst+sigsize+threST+sigsizethre+".tif");
					else
					save(myDir2Co+origiMIP+".tif");
					
					print("AutoBRV; "+AutoBRV+"   MIP saved; "+myDir2Co+origiMIP+"_CH"+MIPtry+applyVST+applyV+DSLTst+sigsize+threST+sigsizethre+".tif");
				}
				
				close();
				
				if(isOpen("MIP.tif")){
					selectWindow("MIP.tif");
					close();
				}
				
				selectWindow("Original_Stack.tif");
				close();
				
				if(channels==4){
					OpenImage=nImages(); OpenTitlelist=getList("image.titles");
					for(iImage=0; iImage<OpenImage; iImage++){
						//		print("OpenImage; "+OpenTitlelist[iImage]);
						DontClose=0;
						for(sameornot=0; sameornot<titlelist.length; sameornot++){
							
							if(OpenTitlelist[iImage]==titlelist[sameornot])
							DontClose=1;
						}
						if(DontClose==0){
							selectWindow(OpenTitlelist[iImage]);
							close();
						}
					}
				}//if(channels>1){
			}//	for(MIPtry=1; MIPtry<=imageNum; MIPtry++){
			run("Close All");
			wait(100);
			call("java.lang.System.gc");
			
			mipbatch[8]=firsttime;
			mipbatch[9]=firsttime1ch;
			mipbatch[15]=neuronimg;
			mipbatch[16]=nc82;
			
			mipbatch[17]=myDir2;
			mipbatch[18]=myDirCLAHE;
			mipbatch[19]=myDir2Co;
			mipbatch[20]=myDir;
			
			mipbatch[22]=Circulicity;
			mipbatch[23]=Roundness;
			mipbatch[24]=ratio;
			mipbatch[25]=AR;
			mipbatch[29]=areasizeM;
			mipbatch[30]=perimLM;
			mipbatch[31]=defaultNoCH;
		}//	if(nImages>0){
	}//if(dotIndextif==-1 && dotI
} //function mipfunction(mipbatch) { 
///////////////////////////////////////////////////////////////
function autobradjustment(briadj,DSLTver,DefMaxValue){
	DOUBLEdslt=1;
	desiredmean=briadj[0];
	lowerweight=briadj[4];
	lowthreM=briadj[5];
	autothre=briadj[6];
	maxvalue=briadj[7];//65535
	MIP=briadj[8];
	stack=briadj[9];
	multiDSLT=briadj[10];
	secondjump=briadj[11];
	
	if(autothre==1)//Fiji Original thresholding
	run("Duplicate...", "title=test.tif");
	
	bitd=bitDepth();
	run("Properties...", "channels=1 slices=1 frames=1 unit=px pixel_width=1 pixel_height=1 voxel_depth=1");
	getDimensions(width2, height2, channels, slices, frames);
	totalpix=width2*height2;
	
	run("Select All");
	if(bitd==8){
		run("Copy");
	}
	
	if(bitd==16){
		setMinAndMax(0, maxvalue);
		run("Copy");
	}
	/////////////////////signal size measurement/////////////////////
	selectImage(MIP);
	run("Duplicate...", "title=test2.tif");
	setAutoThreshold("Triangle dark");
	getThreshold(lower, upper);
	setThreshold(lower, maxvalue);//is this only for 8bit??
	
	run("Convert to Mask", "method=Triangle background=Dark black");
	
	selectWindow("test2.tif");
	
	if(bitd==16)
	run("8-bit");
	
	run("Create Selection");
	getStatistics(areathre, mean, min, max, std, histogram);
	if(areathre!=totalpix){
		if(mean<200){
			selectWindow("test2.tif");
			run("Make Inverse");
		}
	}
	getStatistics(areathre, mean, min, max, std, histogram);
	close();//test2.tif
	
	
	if(areathre/totalpix>0.4){
		
		selectImage(MIP);
		run("Duplicate...", "title=test2.tif");
		setAutoThreshold("Moments dark");
		getThreshold(lower, upper);
		setThreshold(lower, maxvalue);
		
		run("Convert to Mask", "method=Moments background=Dark black");
		
		selectWindow("test2.tif");
		
		if(bitd==16)
		run("8-bit");
		
		run("Create Selection");
		getStatistics(areathre, mean, min, max, std, histogram);
		if(areathre!=totalpix){
			if(mean<200){
				selectWindow("test2.tif");
				run("Make Inverse");
			}
		}
		getStatistics(areathre, mean, min, max, std, histogram);
		close();//test2.tif
		
	}//if(area/totalpix>0.4){
	
	/////////////////////Fin signal size measurement/////////////////////
	
	selectImage(MIP);
	
	dsltarray=newArray(autothre, bitd, totalpix, desiredmean, 0,multiDSLT,DSLTver);
	DSLTfun(dsltarray);
	desiredmean=dsltarray[3];
	area2=dsltarray[4];
	//////////////////////
	
	selectImage(MIP);//MIP
	resetMinAndMax();
	getMinAndMax(min1, max);
	
	//setBatchMode(false);
	//					updateDisplay();
	//					a
	
	run("Mask Brightness Measure", "mask=test.tif data=MIP.tif desired="+desiredmean+"");
	selectImage(MIP);//MIP
	
	fff=getTitle();
	print("fff 1202; "+fff);
	applyvv=newArray(1,bitd,stack,MIP);
	applyVcalculation(applyvv);
	applyV=applyvv[0];
	
	selectImage(MIP);//MIP
	
	
	
	if(fff=="MIP.tif"){
		if(bitd==16)
		applyV=150;
		
		if(bitd==8)
		applyV=40;
		
	}
	
	rename("MIP.tif");//MIP
	
	selectWindow("test.tif");//new window from DSLT
	close();
	/////////////////2nd time DSLT for picking up dimmer neurons/////////////////////
	
	
	if(applyV>30 && desiredmean<secondjump && bitd==8 && DOUBLEdslt==1 && applyV<80){
		applyVpre=applyV;
		selectImage(MIP);
		
		setMinAndMax(0, applyV);
		
		run("Duplicate...", "title=MIPtest.tif");
		
		setMinAndMax(0, applyV);
		run("Apply LUT");
		maxcounts=0; maxi=0;
		
		histoArray = newArray(256);
		for(ix=0; ix<getWidth; ix++){
			for(iy=0; iy<getHeight; iy++){
				pxv = getPixel(ix,iy);
				histoArray[pxv]=histoArray[pxv]+1;
			}
		}
		//getHistogram(values, counts,  256); broken function
		for(i=0; i<100; i++){
			Val=histoArray[i];
			
			if(Val>maxcounts){
				maxcounts=histoArray[i];
				maxi=i;
			}
		}
		
		changelower=maxi*lowerweight;
		if(changelower<1)
		changelower=1;
		
		selectWindow("MIPtest.tif");
		close();
		
		selectImage(MIP);
		setMinAndMax(0, applyV);
		run("Apply LUT");
		
		//	setBatchMode(false);
		//						updateDisplay();
		//						a
		
		setMinAndMax(changelower, 255);
		run("Apply LUT");
		
		print("Double DSLT");
		//	run("Multibit thresholdtwo", "w/b=Set_black max=207 in=[In macro]");
		
		desiredmean=secondjump;//230 for GMR
		
		dsltarray=newArray(autothre, bitd, totalpix, desiredmean, 0, multiDSLT,DSLTver);
		DSLTfun(dsltarray);//will generate test.tif DSLT thresholded mask
		desiredmean=dsltarray[3];
		area2=dsltarray[4];
		
		selectImage(MIP);//MIP
		
		run("Mask Brightness Measure", "mask=test.tif data=MIP.tif desired="+desiredmean+"");
		
		selectImage(MIP);//MIP
		
		fff=getTitle();
		print("fff 1279; "+fff);
		
		applyvv=newArray(1,bitd,stack,MIP);
		applyVcalculation(applyvv);
		applyV=applyvv[0];
		
		if(applyVpre<applyV){
			applyV=applyVpre;
			print("previous applyV is brighter");
		}
		
		selectImage(MIP);//MIP
		rename("MIP.tif");//MIP
		close();
		
		selectWindow("test.tif");//new window from DSLT
		close();
	}//	if(applyV>50 && applyV<150 && bitd==8){
	
	while(isOpen("test.tif")){
		selectWindow("test.tif");
		close();
	}
	
	sigsize=area2/totalpix;
	if(sigsize==1)
	sigsize=0;
	
	sigsizethre=areathre/totalpix;
	
	print("Signal brightness; after 65535 applyV;	"+applyV+"	 Signal Size DSLT; 	"+sigsize+"	 Sig size threshold; 	"+sigsizethre);
	briadj[1]=(sigsize)*100;
	briadj[2]=applyV;
	briadj[3]=sigsizethre*100;
}//function autobradjustment

function DSLTfun(dsltarray){
	
	autothre=dsltarray[0];
	bitd=dsltarray[1];
	totalpix=dsltarray[2];
	desiredmean=dsltarray[3];
	multiDSLT=dsltarray[5];
	DSLTver=dsltarray[6];
	
	if(autothre==0){//DSLT
		
		//	updateDisplay();
		//	setBatchMode(false);
		//	a
		
		run("Anisotropic Diffusion 2D", "number=6 smoothings=7 keep=20 a1=0.50 a2=0.90 dt=20 edge=2");// threads=1
		print("Anisotropic diffusion ON");
		//		updateDisplay();
		//		setBatchMode(false);
		//		a
		
		if(bitd==8){
			if(DSLTver=="Line")
			run("DSLT3D LINE2 Multi", "radius_r_max=15 radius_r_min=3 radius_r_step=3 rotation=8 weight=5 filter=MEAN close=None noise=5px parallel=3");
			else
			run("DSLT ", "radius_r_max=15 radius_r_min=1 radius_r_step=4 rotation=8 weight=5 filter=MEAN close=None noise=7px");
		}
		if(bitd==16){
			
			//		updateDisplay();
			//		setBatchMode(false);
			//		a
			resetMinAndMax();
			getMinAndMax(min,max);
			
			run("A4095 normalizer", "subtraction=0 max="+max+" start=1 end=1");
			print("A4095 normalizer max; "+max);
			
			//			updateDisplay();
			//			setBatchMode(false);
			//			a
			
			if(DSLTver=="Line")
			run("DSLT3D LINE2 Multi", "radius_r_max=15 radius_r_min=2 radius_r_step=3 rotation=8 weight=140 filter=GAUSSIAN close=None noise=5px parallel=3");
			else
			run("DSLT ", "radius_r_max=15 radius_r_min=2 radius_r_step=3 rotation=8 weight=140 filter=MEAN close=None noise=5px");
			
			FirstDSLT=getTitle();
			run("Remove Outliers...", "radius=1 threshold=90 which=Dark");
			
			run("DSLT ", "radius_r_max=5 radius_r_min=1 radius_r_step=2 rotation=8 weight=1 filter=GAUSSIAN close=None less=9");
			
			SecondDSLT=getTitle();
			
			selectWindow(FirstDSLT);
			close();
			
			selectWindow(SecondDSLT);
			run("Remove Outliers...", "radius=1 threshold=90 which=Dark");
			run("16-bit");
			run("Mask255 to 4095");
		}
		
		rename("test.tif");//new window from DSLT
	}//if(autothre==0){//DSLT
	
	
	selectWindow("test.tif");
	
	//setBatchMode(false);
	//	updateDisplay();
	//	a
	
	run("Duplicate...", "title=test2.tif");
	selectWindow("test2.tif");
	
	if(bitd==16)
	run("8-bit");
	
	run("Canvas Size...", "width="+round(getWidth()*0.95)+" height="+round(getHeight()*0.95)+" position=Center zero");
	
	run("Create Selection");
	getStatistics(area1, mean, min, max, std, histogram);
	
	
	if(area1!=totalpix){
		if(mean<200){
			selectWindow("test2.tif");
			run("Make Inverse");
			getStatistics(area1, mean, min, max, std, histogram);
		}
	}
	
	close();//test2.tif
	
	//	print("Area 1412;  "+area+"   mean; "+mean);
	
	presize=area1/totalpix;
	
	if(area1==totalpix){
		presize=0.0001;
		print("Equal");
	}
	print("Area 1st time;  "+area1+"   mean; "+mean+"  totalpix; "+totalpix+"   presize; "+presize*100+" %   bitd; "+bitd);
	realArea=area1;
	
	//	setBatchMode(false);
	//		updateDisplay();
	//		a
	
	multiDSLT=0;
	if(multiDSLT==1){//|| bitd==16
		if(presize<0.3){// set DSLT more sensitive, too dim images, less than 5%
			selectWindow("test.tif");//new window from DSLT
			close();
			
			if(isOpen("test.tif")){
				selectWindow("test.tif");
				close();
			}
			
			selectWindow("MIP.tif");//MIP
			run("Anisotropic Diffusion 2D", "number=6 smoothings=7 keep=20 a1=0.50 a2=0.90 dt=20 edge=2");// threads=1
			//				setBatchMode(false);
			//	updateDisplay();
			//		a
			
			if(bitd==8){
				if(DSLTver=="Line")
				run("DSLT3D LINE2 Multi", "radius_r_max=15 radius_r_min=2 radius_r_step=3 rotation=8 weight=1 filter=MEAN close=None noise=5px parallel=3");
				else
				run("DSLT ", "radius_r_max=15 radius_r_min=1 radius_r_step=4 rotation=8 weight=2 filter=MEAN close=None noise=7px");
			}
			if(bitd==16){
				getMinAndMax(min,max);
				if(max!=65536){
					setMinAndMax(min, max);
					run("Apply LUT");
					max=65535;
				}
				
				run("A4095 normalizer", "subtraction=0 max="+max+" start=1 end=1");
				
				print("A4095 2nd normalizer max; "+max);
				
				if(DSLTver=="Line")
				run("DSLT3D LINE2 Multi", "radius_r_max=15 radius_r_min=2 radius_r_step=3 rotation=8 weight=35 filter=MEAN close=None noise=5px parallel=3");
				else
				run("DSLT ", "radius_r_max=15 radius_r_min=1 radius_r_step=4 rotation=8 weight=30 filter=MEAN close=None noise=5px");
			}
			run("Create Selection");
			getStatistics(area2, mean, min, max, std, histogram);
			if(area2!=totalpix){
				if(mean<200){
					run("Make Inverse");
					print("Inverted 1430");
					getStatistics(area2, mean, min, max, std, histogram);
				}
			}
			
			if(bitd==16){
				run("16-bit");
				run("Mask255 to 4095");
			}//if(bitd==16){
			
			
			rename("test.tif");//new window from DSLT
			run("Select All");
			print("2nd measured size;"+area2);
			realArea=area2;
			
			sizediff=(area2/totalpix)/presize;
			print("2nd_sizediff; 	"+sizediff);
			if(bitd==16){
				if(sizediff>1.3){
					repeatnum=(sizediff-1)*10;
					oriss=1;
					
					for(rep=1; rep<repeatnum+1; rep++){
						oriss=oriss+oriss*0.11;
					}
					weight=oriss/5;
					desiredmean=desiredmean+(desiredmean/4)*weight;
					desiredmean=round(desiredmean);
					
					if(desiredmean>secondjump || isNaN(desiredmean))
					desiredmean=secondjump;
					
					print("desiredmean; 	"+desiredmean+"	 sizediff; "+sizediff+"	 weight *25%;"+(desiredmean/4)*weight);
				}
			}else if(bitd==8){
				if(sizediff>2){
					repeatnum=(sizediff-1);//*10
					oriss=1;
					
					for(rep=1; rep<=repeatnum+1; rep++){
						oriss=oriss+oriss*0.08;
					}
					weight=oriss/7;
					desiredmean=desiredmean+(desiredmean/7)*weight;
					desiredmean=round(desiredmean);
					
					if(desiredmean>204)
					desiredmean=secondjump;
					
					print("desiredmean; 	"+desiredmean+"	 sizediff; "+sizediff+"	 weight *25%;"+(desiredmean/4)*weight);
				}
			}
		}//if(area2/totalpix<0.01){
	}//	if(multiDSLT==1){
	
	dsltarray[3]=desiredmean;
	dsltarray[4]=realArea;
}//function DSLTfun

function applyVcalculation(applyvv){
	bitd=applyvv[1];
	stack=applyvv[2];
	MIP=applyvv[3];
	
	selectImage(MIP);//MIP
	applyV=getTitle();
	
	if(applyV=="MIP.tif")
	applyV=200;
	
	applyV=round(applyV);
	run("Select All");
	getMinAndMax(min, max);
	
	//print("applyV max; "+max+"   bitd; "+bitd+"   applyV; "+applyV);
	
	if(bitd==8){
		applyV=255-applyV;
		
		if(applyV==0)
		applyV=255;
		else if(applyV<10)
		applyV=10;
	}else if(bitd==16){
		
		if(max<=4095)
		applyV=4095-applyV;
		
		if(max>4095)
		applyV=65535-applyV;
		
		if(applyV==0)
		applyV=max;
		else if(applyV<100)
		applyV=100;
	}
	applyvv[0]=applyV;
}

function stackconcatinate(){
	
	getDimensions(width2, height2, channels2, slices, frames);
	addingslices=slices/10;
	addingslices=round(addingslices);
	
	for(GG=1; GG<=addingslices; GG++){
		setSlice(nSlices);
		run("Add Slice");
	}
	run("Reverse");
	for(GG=1; GG<=addingslices; GG++){
		setSlice(nSlices);
		run("Add Slice");
	}
	run("Reverse");
}

function brightnessapply(DefMaxValue,filepath,brightnessapplyArray,bitd,lowerweight,lowthreM,stack,JFRCpath,VncMaskpathF,secondjump,Inimax,VncMaskpathM){
	stacktoApply=getTitle();
	
	applyV = brightnessapplyArray[0];
	RealapplyV=brightnessapplyArray[1];
	sigsize=brightnessapplyArray[2];
	sigsizethre=brightnessapplyArray[3];	
	GammaON=brightnessapplyArray[4];	
	
	print("brightnessapply start, RealapplyV; "+RealapplyV+"   applyV; "+applyV+"  lowerweight; "+lowerweight+"   sigsize; "+sigsize+"  sigsizethre; "+sigsizethre);
	
	BackgroundMaskArray = newArray("","");
	brightnessNeed=0;
	changelower=0;
	
	if(bitd==8){
		if(applyV<255){
			setMinAndMax(0, applyV);
			
			if(applyV<secondjump){
				run("Z Project...", "projection=[Max Intensity]");
				MIPapply=getTitle();
				
				setMinAndMax(0, applyV);
				if(applyV!=255)
				run("Apply LUT");
				
				if(getHeight==512 || getHeight==592){
					if(getWidth==1024 || getWidth==1184){
						tissue="Brain";
						BackgroundMask (BackgroundMaskArray,tissue,JFRCpath,VncMaskpathF,MIPapply,bitd,VncMaskpathM,GammaON);
						brightnessNeed=BackgroundMaskArray[0];
					}
				}else if (getHeight==1024 || getHeight==1100 ){// VNC
					if(getWidth==512){
						
						tissue="VNC";
						BackgroundMask (BackgroundMaskArray,tissue,JFRCpath,VncMaskpathF,MIPapply,bitd,VncMaskpathM,GammaON);
						brightnessNeed=BackgroundMaskArray[0];
					}
				}else if(getHeight>getWidth){
					
					newImage("MaskMIP.tif", "8-bit black", getWidth, getHeight, 1);
					setForegroundColor(255, 255, 255);
					
					makeRectangle(229, 105, 828, 2023);
					makeRectangle(round(getWidth*(229/1292)), round(getHeight*(105/2583)), round(getWidth*(828/1292)), round(getHeight*(2023/2583)));
					run("Make Inverse");
					run("Fill", "slice");
					
					imageCalculator("Max", MIPapply,"MaskMIP.tif");
					
					selectWindow("MaskMIP.tif");
					close();
					
					selectWindow(MIPapply);
					
				}//	if(getHeight==512 && getWidth==1024){
				
				if(lowthreM=="Peak Histogram"){//lowthre measurement
					maxcounts=0; maxi=0;
					getHistogram(values, counts,  256);
					for(i3=1; i3<200; i3++){
						
						sumave=0;
						for(ix=0; ix<getWidth; ix++){
							for(iy=0; iy<getHeight; iy++){
								pxv = getPixel(ix,iy);
								
								if(pxv>=i3)
								if(pxv<i3+5)
								sumave= sumave+pxv;
							}
						}//for(ix=0; ix<getWidth; ix++){
						
						aveave=sumave/5;
						
						if(aveave>maxcounts){
							
							maxcounts=aveave;
							maxi=i3+2;
							print("GrayValue; "+i3+"  "+aveave+"  maxi; "+maxi);
						}
					}//for(i3=0; i3<200; i3++){
					if(maxi!=2)
					changelower=maxi*0.6;//lowerweight
					else
					changelower=0;
					
				}else if(lowthreM=="Auto-threshold"){
					setAutoThreshold("Huang dark");
					getThreshold(lower, upper);
					resetThreshold();
					changelower=lower*0.6;
				}
				
				changelower=round(changelower);
				//		if(changelower>100)
				//		changelower=100;
				
				selectWindow(MIPapply);
				
				//	setBatchMode(false);
				//	updateDisplay();
				//	a
				
				close();
				
				selectWindow(stacktoApply);
				setMinAndMax(0, applyV);//brightness adjustment
				
				if(applyV!=255)
				run("Apply LUT", "stack");
				
				print("2269 ok  "+changelower);
				if(changelower>0){
					changelower=round(changelower);
					
					setMinAndMax(changelower, 255);//lowthre cut
					run("Apply LUT", "stack");
				}else
				changelower=0;
				
				print("  lower threshold; 	"+changelower);
			}
		}
	}//if(bitd==8){
	
	if(bitd==16){
		
		applyV2=applyV;
		if(applyV==65535)
		applyV2=65534;
		
		selectImage(stack);
		
		run("Z Project...", "projection=[Max Intensity]");
		MIP2=getImageID();
		getMinAndMax(min, max);
		setMinAndMax(min, max);
		
		print("Line 1225 min; "+min+"   max; "+max);
		
		if(min!=0 && max!=65535)
		run("Apply LUT");
		
		run("Duplicate...", "title=MIPDUP.tif");
		MIPDUPid = getImageID();
		MIPthresholding=getTitle();
		
		print("Line 1252;  MIPthresholding; "+MIPthresholding);
		//		setBatchMode(false);
		//		updateDisplay();
		///		a
		
		print("getHeight; "+getHeight);
		
		if(getHeight==512 || getHeight==592){
			if(getWidth==1024 || getWidth==1184){
				tissue="Brain";
				BackgroundMask (BackgroundMaskArray,tissue,JFRCpath,VncMaskpathF,MIPthresholding,bitd,VncMaskpathM,GammaON);
				brightnessNeed= BackgroundMaskArray[0];
			}
		}else if (getHeight==1024 || getHeight==1100){
			if(getWidth==512){
				tissue="VNC";
				BackgroundMask (BackgroundMaskArray,tissue,JFRCpath,VncMaskpathF,MIPthresholding,bitd,VncMaskpathM,GammaON);
				brightnessNeed= BackgroundMaskArray[0];
			}
		}else{//	if(getHeight==512 && getWidth==1024){
			
			tissue="UNKNOWN";
			newImage("MaskMIP.tif", "16-bit", getWidth, getHeight, 1);
			setForegroundColor(255, 255, 255);
			makeRectangle(round(getWidth*(150/1500)), round(getHeight*(100/833)), round(getWidth*(1200/1500)), round(getHeight*(600/833)));			//		run("Make Inverse");
			run("Fill", "slice");
			setMinAndMax(0, 255);
			run("Apply LUT");
			
			run("Three D Ave");
			MaskBri = getTitle();
			MaskBri= parseFloat(MaskBri);//Chaneg string to number
			MaskBri = round(MaskBri);
			rename("MaskMIP.tif");
			
			selectImage(MIPDUPid);
			rename(MIPthresholding);
			
			imageCalculator("Min", MIPthresholding,"MaskMIP.tif");
			
			//	setBatchMode(false);
			if(GammaON==true){
				
				run("Gamma ", "gamma=1.40 in=InMacro cpu=6");
				gamma = getTitle();
				
				selectWindow(MIPthresholding);
				close();
				
				selectWindow(gamma);
				rename(MIPthresholding);
				MIPDUPid=getImageID();
				print("GAMMA 1.4 applied to 2D");
			}
			
			run("Three D Ave");
			//		setBatchMode(false);
			//		updateDisplay();
			//		a
			
			//	run("Mask Brightness Measure", "mask=MaskMIP.tif data="+MIPthresholding+" desired=150");
			
			brightnessNeed = getTitle();
			brightnessNeed= parseFloat(brightnessNeed);//Chaneg string to number
			brightnessNeed = round(brightnessNeed);
			brightnessNeed= round(brightnessNeed*(65535/MaskBri));
			
			selectImage(MIPDUPid);
			rename(MIPthresholding);
			
			print("brightnessNeed; "+brightnessNeed);
			
			
			//			setBatchMode(false);
			//				updateDisplay();
			//				a
			
			
			selectWindow("MaskMIP.tif");
			close();
			
			selectWindow(MIPthresholding);
			MIPDUPid=getImageID();// masked image
		}
		
		if(DefMaxValue==4095 && tissue=="VNC")
		MinSigSize = 0;
		else if (DefMaxValue==65535 && tissue=="VNC")
		MinSigSize = 0;
		else if (DefMaxValue==255 && tissue=="VNC")
		MinSigSize = 0;
		
		if(tissue=="Brain" || tissue=="UNKNOWN"){
			if(DefMaxValue==4095)
			MinSigSize = 0;
			else if (DefMaxValue==65535)
			MinSigSize = 0;
			else if (DefMaxValue==255)
			MinSigSize = 0;
		}
		
		print("MIPthresholding; "+MIPthresholding);
		//// lower thresholding //////////////////////////////////	
		maxi=0;			 		countregion=10000;
		
		if(lowthreM=="Peak Histogram"){
			selectWindow(MIPthresholding);
			MIPDUPid = getImageID();
			
			//	setBatchMode(false);
			//					updateDisplay();
			//					a
			
			maxcounts=0; medianNum=400; 
			//	getHistogram(values, counts,  65536);
			
			counts=newArray(65536);
			
			for(ix=0; ix<getWidth; ix++){
				for(iy=0; iy<getHeight; iy++){
					pxv = getPixel(ix,iy);
					
					counts[pxv]=counts[pxv]+1;
				}
			}//for(ix=0; ix<getWidth; ix++){
			
			////Average value
			
			brightnessNeed=round(brightnessNeed);
			
			if(brightnessNeed>35000)
			countregion=16000;
			else
			countregion=brightnessNeed;
			
			for(i3=5; i3<countregion-medianNum; i3++){
				
				sumVal20=0; 
				
				for(aveval=i3; aveval<i3+medianNum; aveval++){
					Val20=counts[aveval]*aveval;
					sumVal20=sumVal20+Val20;
				}
				AveVal20=sumVal20/medianNum;
				
				if(AveVal20>maxcounts){
					maxcounts=AveVal20;
					maxi=i3+(medianNum/2);
					Final_i3 = i3;
					//	print("i3; "+i3);
				}
			}//		for(i3=5; i3<countregion-medianNum; i3++){
			
			maxiZero=0;
			if(maxi==0){
				maxi=brightnessNeed;
				maxiZero=1;
			}
			
			print("Avebrightness; "+brightnessNeed+"  maxi"+maxi+"   countregion; "+countregion+"  sigsize; "+sigsize+"  MinSigSize;"+MinSigSize);
			
			if(brightnessNeed>40000){
				
				applyV2Ori=applyV2;//61539
				applyV2=applyV2*(brightnessNeed/40000);
				
				if(applyV2>65534)
				applyV2=65534;
				
				print("Too bright, applyV2 decreased from "+applyV2Ori+" to "+applyV2);
			}
			if(sigsize>=MinSigSize){
				//				setBatchMode(false);
				//				updateDisplay();
				//				a
				
				selectWindow("MIPDUP.tif");
				selectImage(MIPDUPid);
				close();
				numtry=0;
				if (maxi<1000 && brightnessNeed<2000){
					
					print("Decreasing maxi; original maxi = "+maxi);
					minus2=-50;
					Orimaxi=maxi; avebrightness=0;
					
					while(maxi<500 && avebrightness<2000){
						
						minus2=minus2-50;
						selectImage(MIP2);//MIP
						run("Duplicate...", "title=MIPDUP2.tif");
						MIPthresholding=getTitle();
						
						print("minus2; "+minus2+"  applyV2; "+applyV2+"  maxi; "+maxi);
						
						if(applyV2+minus2>80)
						setMinAndMax(min, applyV2+minus2);
						else{
							//Orimaxi
							break;
						}
						
						run("Apply LUT");
						
						if(getHeight==512 || getHeight==592){
							if(getWidth==1024 || getWidth==1184){
								tissue="Brain";
								BackgroundMask (BackgroundMaskArray,tissue,JFRCpath,VncMaskpathF,MIPthresholding,bitd,VncMaskpathM,GammaON);
							}
						}else if (getHeight==1024 || getHeight==1100){
							if(getWidth==512){
								tissue="VNC";
								BackgroundMask (BackgroundMaskArray,tissue,JFRCpath,VncMaskpathF,MIPthresholding,bitd,VncMaskpathM,GammaON);
							}
						}else{//	if(getHeight==512 && getWidth==1024){
							
							newImage("MaskMIP.tif", "16-bit", getWidth, getHeight, 1);
							setForegroundColor(255, 255, 255);
							makeRectangle(round(getWidth*(162/1499)), round(getHeight*(131/833)), round(getWidth*(1150/1499)), round(getHeight*(508/833)));
							run("Make Inverse");
							run("Fill", "slice");
							
							
							//		setBatchMode(false);
							//			updateDisplay();
							//			a
							
							imageCalculator("Max", MIPthresholding,"MaskMIP.tif");
							
							selectWindow("MaskMIP.tif");
							close();
							
							selectWindow(MIPthresholding);
						}
						
						
						maxcounts=0;	 medianNum=400; 
						getHistogram(values, counts, 65535);
						
						////Average value
						total=0;
						
						for(ix=0; ix<getWidth; ix++){
							for(iy=0; iy<getHeight; iy++){
								pxv = getPixel(ix,iy);
								total= total+pxv;
								
							}
						}//for(ix=0; ix<getWidth; ix++){
						
						//for(iave=1; iave<65535; iave++){
						//	total=total+(counts[iave]*iave);
						//					}
						
						avebrightness=round(total/(getHeight*getWidth));
						
						print("avebrightness; "+avebrightness);
						
						
						//		if(avebrightness>2000){
						//			setBatchMode(false);
						//							updateDisplay();
						//							a
						//		}
						
						for(i32=5; i32<countregion-medianNum; i32++){
							
							sumVal20=0; 
							
							for(aveval=i32; aveval<i32+medianNum; aveval++){
								Val20=counts[aveval];
								sumVal20=sumVal20+(Val20*aveval);
							}
							AveVal20=sumVal20/medianNum;
							
							if(AveVal20>maxcounts){
								maxcounts=AveVal20;
								maxi=round(i32+(medianNum/2));
								//			print("maxi; "+maxi+"   minus2; "+minus2);
							}
						}//		for(i3=5; i3<countregion-medianNum; i3++){
						
						if(isOpen(MIPthresholding)){
							selectWindow(MIPthresholding);
							
							//		setBatchMode(false);
							//						updateDisplay();
							//					a
							
							close();
						}
						numtry=numtry+1;
					}//while(maxi>7000){
					
					if(maxi<3000){
						applyV2 = applyV2+minus2;
						RealapplyV= round(applyV2*(Inimax/4095));
					}
					
					print("Adjusted maxi; = "+maxi+"   minus2; "+minus2+"   RealapplyV; "+RealapplyV);
					changelower=maxi*0.5;
					
					minus = minus2;
					
				}else if (maxi>9000)
				changelower=maxi*0.9;
				else if (maxi>3500 && maxi<=6000)
				changelower=maxi*0.6;
				else if (maxi>6000 && maxi<=9000)
				changelower=maxi*0.7;
				else if (maxi>2500 && maxi<=3500)
				changelower=maxi*0.2;
				else if (maxi<=2500)
				changelower=0;
				
				
				//	if(applyV>1000 && maxi>9000)
				//		changelower=0;
			}//if(sigsize>=1){
			if(sigsize<MinSigSize){
				applyV2 = 65535;
				
				if(applyV2>65535)
				applyV2=65535;
				
				if(DefMaxValue==4095)
				RealapplyV= round((applyV2/16)*(Inimax/DefMaxValue));// adjusting from 65535 to 4095
				else if (DefMaxValue==255)
				RealapplyV=((applyV2/(16*16))*(Inimax/DefMaxValue));// adjusting from 65535 to 4095
				else if (DefMaxValue==65535)
				RealapplyV= round(applyV2*(Inimax/DefMaxValue));// adjusting from 65535 to 4095
				
				print("sigsize too small, 2x lower brightness; sigsize; "+sigsize+"   RealapplyV; "+RealapplyV+"  applyV2; "+applyV2);
				
				selectWindow(stacktoApply);
				selectImage(stack);
				
				run("Z Project...", "projection=[Max Intensity]");
				MIPaveMeasure=getImageID();
				
				run("Three D Ave");
				print("end");
				logsum=getInfo("log");
				close();
				aveIndex = lastIndexOf(logsum, "AVEsamp;");
				endIndex = lastIndexOf(logsum, "end");
				
				avesamp = substring(logsum, aveIndex+9, endIndex-1);
				
				print("avesamp; "+avesamp);
				avesamp = parseFloat(avesamp);//Chaneg string to number
				
				maxi=round(avesamp);
				
				//changelower=maxi*0.4;
				//if(maxiZero==1)
				changelower=maxi*0.9;
			}
			
			print("lower threshold; 	"+changelower+"   maxi; "+maxi);
			
			
			//		setBatchMode(false);
			//		updateDisplay();
			//		a
			
		}//if(lowthreM=="Peak Histogram"){
		
		selectWindow(stacktoApply);
		selectImage(stack);
		
		if(lowthreM=="Auto-threshold"){
			
			setAutoThreshold("Huang dark");
			getThreshold(lower, upper);
			resetThreshold();
			
			
			changelower=lower-lower/4;
			
			if(changelower>250)
			changelower=150;
		}//if(lowthreM=="Auto-threshold")
		
		//		if(lowerweight==0)
		//		changelower=0;
		//	close();
		selectWindow(stacktoApply);
		selectImage(stack);
		
		
		if(GammaON==true){
			
			run("Gamma ", "gamma=1.40 3d in=InMacro cpu=6");
			gamma = getTitle();
			
			selectWindow(stacktoApply);
			close();
			
			selectWindow(gamma);
			rename(stacktoApply);
			stack = getImageID();
			print("GAMMA 1.4 applied to 3D stack");
		}
		
		if(sigsize>=MinSigSize){
			setMinAndMax(0, applyV2);//subtraction
			run("Apply LUT", "stack");
		}
		
		applyV=applyV2;
		
		run("Max value");
		logsum=getInfo("log");
		maxStartindex=lastIndexOf(logsum,"Maxvalue");
		maxEndindex=lastIndexOf(logsum,"Minvalue");
		maxvalue=substring(logsum, maxStartindex+10, maxEndindex-2);
		maxvalue=round(maxvalue);
		
		print("stack maxvalue; "+maxvalue);
		
		//		setBatchMode(false);
		//				updateDisplay();
		//		a
		
		changelower=round(changelower);
		if(changelower!=0){
			setMinAndMax(changelower, maxvalue);//subtraction
			run("Apply LUT", "stack");
		}
		
		
		
		setMinAndMax(0, 65535);
		run("8-bit");
		
		brightnessapplyArray[0] = applyV;
		brightnessapplyArray[1] = RealapplyV;
	}//if(bitd==16){
}//function brightnessapply(applyV, bitd){

function basicoperation(BasicMIP){
	//	run("Mean Thresholding", "-=30 thresholding=Subtraction");//new plugins
	bitd=BasicMIP[0];
	stack=BasicMIP[2];
	GradientDim=BasicMIP[3];
	stackSt=BasicMIP[4];
	
	
	if(GradientDim==true && bitd==8){
		
		selectWindow(stackSt);
		run("16-bit");
		
		LF=10; TAB=9; swi=0; swi2=0; testline=0;
		filepath0=getDirectory("temp");
		filepath2=filepath0+"Gradient.txt";
		exi2=File.exists(filepath2);
		GradientPath=0;
		
		if(exi2==1){
			print("exi2==1");
			s1 = File.openAsRawString(filepath2);
			swin=0;
			swi2n=-1;
			
			n = lengthOf(s1);
			String.resetBuffer;
			for (testnum=0; testnum<n; testnum++) {
				enter = charCodeAt(s1, testnum);
				
				if(enter==10)
				testline=testline+1;//line number
			}
			
			String.resetBuffer;
			for (si=0; si<n; si++) {
				c = charCodeAt(s1, si);
				
				if(c==10){
					swi=swi+1;
					swin=swin+1;
					swi2n=swi-1;
				}
				
				if(swi==swin){
					if(swi2==swi2n){
						String.resetBuffer;
						swi2=swi;
					}
					if (c>=32 && c<=127)
					String.append(fromCharCode(c));
				}
				if(swi==0){
					GradientPath = String.buffer;
				}
			}
			print("GradientPath; "+GradientPath);
		}//if(exi2==1){
		
		tempmaskEXI=File.exists(GradientPath);
		if(tempmaskEXI==1){
			open(GradientPath);
		}else{
			print("Choose a Directory for Gradient.tif");
			Gradient=getDirectory("Choose a Directory for Gradient.tif");
			GradientPath=Gradient+"Gradient.tif";
			open(GradientPath);
		}
		
		GradientID=getImageID();
		
		File.saveString(GradientPath+"\n", filepath2);
		//	print("Image cal pre");
		imageCalculator("Multiply stack", ""+stackSt+"", "Gradient.tif");
		//	print("Image cal done");
		selectWindow("Gradient.tif");
		close();
		
		selectWindow(stackSt);
		run("Z Project...", "projection=[Max Intensity]");
		getMinAndMax(min, max);
		close();
		
		selectImage(stack);
		setMinAndMax(0, max);
		run("8-bit");
		max=255;
	}else{
		
		if(bitd==16){
			run("Z Project...", "projection=[Max Intensity]");
			getMinAndMax(min, max);
			close();
			
			selectImage(stack);
			setMinAndMax(0, max);
		}
		if(bitd==8)
		max=255;
	}
	run("Z Project...", "start=15 stop="+nSlices+" projection=[Max Intensity]");
	rename("MIP.tif");
	if(bitd==16)
	resetMinAndMax();
	
	//updateDisplay();
	//setBatchMode(false);
	//a
	
	BasicMIP[1]=max;
}

function ColorCoder(slicesOri, applyV, width, AutoBRV, bitd, CLAHE, GFrameColorScaleCheck, reverse0, colorcoding, usingLUT,DefMaxValue,startMIP,endMIP,expand,GammaON) {//"Time-Lapse Color Coder" 
	
	if(usingLUT=="royal")
	var Glut = "royal";	//default LUT
	
	if(usingLUT=="PsychedelicRainBow2")
	var Glut = "PsychedelicRainBow2";	//default LUT
	
	var Gstartf = 1;
	
	getDimensions(width, height, channels, slices, frames);
	rename("Original_Stack.tif");
	
	//	setBatchMode(false);
	if(GammaON==true){
		
		run("Gamma ", "gamma=1.40 3d in=InMacro cpu=6");
		gamma = getTitle();
		
		selectWindow("Original_Stack.tif");
		close();
		
		selectWindow(gamma);
		rename("Original_Stack.tif");
		print("GAMMA 1.4 applied");
	}
	
	
	if(frames>slices)
	slices=frames;
	
	newImage("lut_table.tif", "8-bit black", slices, 1, 1);
	for(xxx=0; xxx<slices; xxx++){
		per=xxx/slices;
		colv=255*per;
		colv=round(colv);
		setPixel(xxx, 0, colv);
	}
	
	run(Glut);
	run("RGB Color");
	
	selectWindow("Original_Stack.tif");
	//print("1992 pre MIP");
	run("Z Code Stack HO", "data=Original_Stack.tif 1px=lut_table.tif");
	
	selectWindow("Depth_color_RGB.tif");
	//print("1996 post MIP");
	if(endMIP>nSlices)
	endMIP=nSlices;
	
	if(usingLUT=="royal"){
		addingslices=slicesOri/10;
		addingslices=round(addingslices);
		startMIP=addingslices+startMIP;
		endMIP=addingslices+endMIP;
		
		if(endMIP>nSlices)
		endMIP=nSlices;
		
		run("Z Project...", "start="+startMIP+" stop="+endMIP+" projection=[Max Intensity] all");
	}
	
	if(usingLUT=="PsychedelicRainBow2")
	run("MIP right color", "start="+startMIP+" end="+endMIP+"");
	
	max=getTitle();
	
	selectWindow("Depth_color_RGB.tif");
	close();
	
	selectWindow("lut_table.tif");
	close();
	
	selectWindow(max);
	rename("color.tif");
	if (GFrameColorScaleCheck==1){
		CreateScale(Glut, Gstartf, slicesOri, reverse0);
		
		selectWindow("color time scale");
		run("Select All");
		run("Copy");
		close();
	}
	
	selectWindow("color.tif");
	run("Properties...", "channels=1 slices=1 frames=1 unit=pixel pixel_width=1.0000 pixel_height=1.0000 voxel_depth=0 global");
	if(CLAHE==1 && usingLUT=="royal" )
	run("Enhance Local Contrast (CLAHE)", "blocksize=127 histogram=256 maximum=1.5 mask=*None*");
	
	if (GFrameColorScaleCheck==1){
		
		if(expand==1)
		run("Canvas Size...", "width="+width+" height="+height+90+" position=Bottom-Left zero");
		makeRectangle(width-257, 1, 256, 48);
		run("Paste");
		
		if(AutoBRV==1){
			setFont("Arial", 20, " antialiased");
			setColor("white");
			if(applyV>999 && applyV<10000){
				
				if(bitd==16 && DefMaxValue>4095)
				drawString("Max: 0"+applyV+" /65535", width-210, 78);
				
				if(bitd==16 && DefMaxValue<=4095)
				drawString("Max: "+applyV+" /4095", width-180, 78);
				
			}else if(applyV>99 && applyV<1000){
				if(bitd==8)
				drawString("Max: "+applyV+" /255", width-180, 78);
				
				if(bitd==16 && DefMaxValue>4095)
				drawString("Max: 00"+applyV+" /65535", width-210, 78);
				
				if(bitd==16 && DefMaxValue<=4095)
				drawString("Max: 0"+applyV+" /4095", width-180, 78);
				
			}else if(applyV<100){
				if(bitd==8)
				drawString("Max: 0"+applyV+" /255", width-180, 78);
				if(bitd==16 && DefMaxValue<=4095)
				drawString("Max: 00"+applyV+" /4095", width-180, 78);
				if(bitd==16 && DefMaxValue>4095)
				drawString("Max: 000"+applyV+" /65535", width-210, 78);
				
			}else if(applyV>9999){
				drawString("Max: "+applyV+" /65535", width-210, 78);
			}
			setMetadata("Label", applyV+"	 DSLT; 	"+sigsize+"	Thre; 	"+sigsizethre);
		}//if(AutoBRV==1){
	}//if (GFrameColorScaleCheck==1){
	run("Select All");
	
}//function ColorCoder(slicesOri, applyV, width, AutoBRV, bitd) {//"Time-Lapse Color Coder" 

function CreateScale(lutstr, beginf, endf, reverse0){
	ww = 256;
	hh = 32;
	newImage("color time scale", "8-bit White", ww, hh, 1);
	if(reverse0==0){
		for (j = 0; j < hh; j++) {
			for (i = 0; i < ww; i++) {
				setPixel(i, j, i);
			}
		}
	}//	if(reverse0==0){
	
	if(reverse0==1){
		valw=ww;
		for (j = 0; j < hh; j++) {
			for (i = 0; i < ww; i++) {
				setPixel(i, j, valw);
				valw=ww-i;
			}
		}
	}//	if(reverse0==1){
	
	if(usingLUT=="royal"){
		makeRectangle(25, 0, 204, 32);
		run("Crop");
	}
	
	run(lutstr);
	run("RGB Color");
	op = "width=" + ww + " height=" + (hh + 16) + " position=Top-Center zero";
	run("Canvas Size...", op);
	setFont("SansSerif", 12, "antiliased");
	run("Colors...", "foreground=white background=black selection=yellow");
	drawString("Slices", round(ww / 2) - 12, hh + 16);
	
	if(usingLUT=="PsychedelicRainBow2"){
		drawString(leftPad(beginf, 3), 10, hh + 16);
		drawString(leftPad(endf, 3), ww - 30, hh + 16);
	}else{
		drawString(leftPad(beginf, 3), 24, hh + 16);
		drawString(leftPad(endf, 3), ww - 50, hh + 16);
	}
}

function CropOP (MIPtype,applyV,colorscale){
	setPasteMode("Max");
	setForegroundColor(0, 0, 0);
	setFont("Arial", 22);
	
	
	if(MIPtype=="MCFO_MIP"){
		makeRectangle(0, 0, 239, 60);//Line
		run("Copy");
		makeRectangle(195, 455, 239, 60);
		run("Paste");
		
		makeRectangle(6, 58, 129, 26);//AD DBD
		run("Copy");
		
		makeRectangle(606, 482, 129, 26);
		run("Paste");
		
		makeRectangle(197, 0, 617, 512);
		run("Crop");
		setForegroundColor(0, 0, 0);
		makeRectangle(567, 0, 50, 46);
		run("Fill", "slice");
		
		makeRectangle(0, 0, 42, 55);
		run("Fill", "slice");
		
		setFont("Arial", 22); 
		
		setForegroundColor(0, 0, 0);
		makeRectangle(542, 455, 70, 49);
		run("Fill", "slice");
		
		BriValue=round(applyV);
		
		print("BriValue; "+BriValue+"   MIPtype; "+MIPtype);
		if(BriValue>255 && BriValue<4096){
			MaxVal=4095;
			
			if(BriValue<1000)
			BriValue="0"+BriValue;
			
			if(colorscale){
				setForegroundColor(255, 255, 255);
				drawString(BriValue, 543, 483);
				drawString(MaxVal, 543, 509);
				
				setLineWidth(2);
				drawLine(543, 482, 595, 482);
			}//if(colorscale)
			
		}else if(BriValue<256){
			MaxVal=255;
			
			if(BriValue<100)
			BriValue="0"+BriValue;
			
			if(colorscale){
				setForegroundColor(255, 255, 255);
				drawString(BriValue, 543, 483);
				drawString(MaxVal, 543, 509);
				
				setLineWidth(2);
				
				drawLine(543, 482, 581, 482);
			}///if(colorscale){
			//		setBatchMode(false);
			//		updateDisplay();
			//		a
			
		}else if(BriValue>4095){
			MaxVal=65535;
			
			if(BriValue<10000){
				if(BriValue<1000)
				BriValue="00"+BriValue;
				else
				BriValue="0"+BriValue;
			}
			
			if(colorscale){
				setForegroundColor(255, 255, 255);
				drawString(BriValue, 543, 483);
				drawString(MaxVal, 543, 509);
				
				setLineWidth(2);
				drawLine(543, 482, 609, 482);
			}
		}
		
		//	if(DrawName!="NA")
		//	drawString(DrawName, 574, 30);
		
		
	}else if(MIPtype=="Gen1_Gal4"){
		makeRectangle(3, 0, 227, 60);
		run("Copy");
		makeRectangle(195, 455, 227, 60);
		run("Paste");
		
		makeRectangle(833, 49, 149, 43);
		run("Copy");
		
		makeRectangle(308, 482, 149, 43); 
		run("Paste");
		
		makeRectangle(197, 0, 617, 512);
		run("Crop");
		
		makeRectangle(0, 0, 38, 35);
		setForegroundColor(0, 0, 0);
		run("Fill", "slice");
		makeRectangle(567, 0, 50, 46);
		run("Fill", "slice");
	}
	
	setForegroundColor(255, 255, 255);
	setPasteMode("Copy");
}

function BackgroundMask (BackgroundMaskArray,tissue,JFRCpath,VncMaskpathF,MIPapply,bitd,VncMaskpathM,GammaON){
	
	brightnessNeed=BackgroundMaskArray[0];
	
	print("tissue; "+tissue);
	
	MaskName=0;
	if(tissue=="Brain"){
		
		if(getHeight==512){
			MaskName="JFRC2010_Mask.tif";
			BrightnessAdjustMaskValue = 65535/46447;
		}
		if(getHeight==592){
			MaskName="JFRC2013_20x_Mask.tif";
			BrightnessAdjustMaskValue = 65535/40972;
		}
		openPath=JFRCpath;
	}else if (tissue=="VNC"){
		
		if(getHeight==1024){
			MaskName="Mask_VNC_Female.tif";
			BrightnessAdjustMaskValue = 65535/29988;
			openPath=VncMaskpathF;
		}
		
		if(getHeight==1100){
			MaskName="Mask_VNC_Male.tif";
			BrightnessAdjustMaskValue = 65535/29700;
			openPath=VncMaskpathM;
		}
		
	}
	
	print("Mask path; "+openPath);
	maskExist=File.exists(openPath);
	if(maskExist==1){
		print("Used a Mask for background subtraction.");
		open(openPath);
		
		objective="40x";
		if(objective=="40x" &&	MaskName=="JFRC2010_Mask.tif"){
			makePolygon(813,90,672,431,840,503,1022,488,1020,113);
			setForegroundColor(0, 0, 0);
			run("Fill", "slice");
			makePolygon(177,56,355,453,227,507,1,505,12,89);
			run("Fill", "slice");
			BrightnessAdjustMaskValue = 65535/25266;
		}
		
		if(bitd==8)
		run("8-bit");
		
		selectWindow(MIPapply);
		MIPid = getImageID();
		
		
		//	setBatchMode(false);
		if(GammaON==true){
			
			run("Gamma ", "gamma=1.40 in=InMacro cpu=6");
			gamma = getTitle();
			
			selectWindow(MIPapply);
			close();
			
			selectWindow(gamma);
			rename(MIPapply);
			MIPid = getImageID();
			print("GAMMA 1.4 applied to 2D");
		}//	if(GammaON==true){
		
		if(MaskName!=0){
			imageCalculator("Min", MIPapply,MaskName);
			//	run("Three D Ave");
			
			selectImage(MIPid);
			selectWindow(MIPapply);
			
			run("Mask MIP Brightness Measure", "mask="+MaskName+" data="+MIPapply+"");
			brightnessNeed = call("Mask_MIP_Brightness_Measure.getResult");
			
			//	brightnessNeed = getTitle();
			//		brightnessNeed= parseFloat(brightnessNeed);//Chaneg string to number
			//		brightnessNeed = round(brightnessNeed);
			
			brightnessNeed= round(brightnessNeed*BrightnessAdjustMaskValue);
			rename(MIPapply);
			
			print("brightnessNeed; 3405  "+brightnessNeed);
			
			//		setBatchMode(false);
			//		updateDisplay();
			//		a
			
			
			selectWindow(MaskName);
			close();
		}//if(MaskName!=0){
	}
	
	BackgroundMaskArray[0]=brightnessNeed;
	
	selectWindow(MIPapply);
}


function fileOpen(FilePathArray){
	FilePath=FilePathArray[0];
	MIPname=FilePathArray[1];
	OpenorNot=FilePathArray[2];
	tempMaskDir=FilePathArray[3];
	MIPtitle=FilePathArray[4];
	
	//	print(MIPname+"; "+FilePath);
	if(isOpen(MIPname)){
		selectWindow(MIPname);
		tempMaskDir=getDirectory("image");
		FilePath=tempMaskDir+MIPname;
	}else{
		if(FilePath==0){
			
			FilePath=getDirectory("plugins")+MIPname;
			tempmaskEXI=File.exists(FilePath);
			if(tempmaskEXI!=1){
				FilePath=tempMaskDir+MIPname;
				tempmaskEXI=File.exists(FilePath);
			}if(tempmaskEXI!=1)
			FilePath=getDirectory("plugins")+"Plugins_Color_MIP"+File.separator+MIPname;
			
			tempmaskEXI=File.exists(FilePath);
			
			if(tempmaskEXI==1){
				if(OpenorNot!="DontOpen")
				open(FilePath);
			}else{
				print("Choose a Directory for "+MIPname+"");
				tempMaskDir=getDirectory("Choose a Directory for "+MIPname+"");
				FilePath=tempMaskDir+MIPname;
				if(OpenorNot!="DontOpen")
				open(FilePath);
			}
		}else{
			tempmaskEXI=File.exists(FilePath);
			if(tempmaskEXI!=1)
			FilePath=tempMaskDir+MIPname;
			
			tempmaskEXI=File.exists(FilePath);
			
			if(tempmaskEXI==1){
				if(OpenorNot!="DontOpen"){
					open(FilePath);
				}
			}else{
				print("Choose a Directory for "+MIPname+"");
				tempMaskDir=getDirectory("Choose a Directory for "+MIPname+"");
				FilePath=tempMaskDir+MIPname;
				if(OpenorNot!="DontOpen")
				open(FilePath);
			}
		}
	}//if(isOpen("JFRC2013_63x_Tanya.nrrd")){
	
	if(OpenorNot=="Open"){
		imageCalculator("Subtract", MIPtitle,MIPname);
		
		while(isOpen(MIPname)){
			selectWindow(MIPname);
			close();
		}
	}
	
	FilePathArray[0]=FilePath;
	FilePathArray[3]=tempMaskDir;
}

function leftPad(n, width) {
	s = "" + n;
	while (lengthOf(s) < width)
	s = "0" + s;
	return s;
}

"done"
