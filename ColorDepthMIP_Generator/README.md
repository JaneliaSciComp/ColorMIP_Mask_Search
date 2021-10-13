# Color-MIP generation [![LinkToJanelia](../../images/jrc_logo_180x40.png)](https://www.janelia.org)
The algorithm created by Hideo Otsuna.  
FIJI plugins created by Hideo Otsuna and Takashi Kawase.  


## Before starting
 1. Download "Plugins_Color_MIP.zip" and decompress. Also download "Color_Depth_MIP_batch_0308_2021.ijm"
 2. Copy "Plugins_Color_MIP" folder into Fiji/Plugins/ folder (please do not change the folder name, the name of folder is using as a link to the contents of .tiff/nrrd files).
 3. Copy "Color_Depth_MIP_batch_0308_2021.ijm" into Fiji/Plugins/Macros/ folder.
 4. Copy "PsychedelicRainBow2.lut" into Fiji/luts/ folder.


## Startup
Select menu; Pugins/Macros/Color_Depth_MIP_batch_0308_2021

 1. Select directory that contains 3D stacks to be color depth MIP
 2. Select directory for saving MIPs
 
![ScreenShot0](../../images/CDM_generator.png)
### Parameters:
 - Automatic Brightness adjustment: Automatic brightness adjustment ON/OFF.
 - Add color scale: Adding the color scale bar on the right top. If you are creating the searching mask from the segmented 3D stack, this needs to be OFF.
 - Reversed color: The order of color is reversed with ON setting.
 - VNC (Expand canvas for scale-bar): If the sample is aligned to VNC, this setting needs to be ON.
 - Skip MIP creation if already present in the save directory: will skip MIP creation if the MIP file exists within the save directory
- DSLT version:

 Normal: Basically neuron fiber based brightness adjustment. However, this mode will account more background signals for the brightness adjustment

 Line: More sensitive for the neuron fiber based brightness adjustment.

 - Starting MIP slice: 0 is from beginning of 3D stack.
 - Ending MIP slice: 1000 will be until end of 3D stack.

