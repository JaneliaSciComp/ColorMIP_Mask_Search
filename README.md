# Color-MIP Mask Search [![LinkToJanelia](../images/jrc_logo_180x40.png)](https://www.janelia.org)
The algorithm created by Hideo Otsuna.  
FIJI plugins created by Hideo Otsuna and Takashi Kawase.  
This manual is modified based on the instruction developed by Justine X Chia (UCSF).  


## Before starting
 1. Launch and update FIJI.
 2. Drag and drop `ColorMIP_Mask_Search.jar` on FIJI then quit (the plugin installation).  
 (you can get the .jar file from [here](https://github.com/JaneliaSciComp/ColorMIP_Mask_Search/blob/master/Color_MIP_Mask_Search.jar)) 

## Startup
Drag the folder (containing Gal4 color depth MIPs) into fiji, use virtual stack option. 

## Create mask of neuron of interest
 1. Duplicate a single slice that containing the neuron from the Gal4 image (⌘⇧ D).
 2. Trace area of interest on a duplicated slice (use polygon tool and try to be as accurate as possible).
 3. Edit > Clear Outside.

## Search stacks with mask
Plugins > ColorMIP Mask search  
![ScreenShot0](../images/scr0.png)
### considerations/ tips:
 - Show log // -> show NaN for log may be useful for a first pass, just to keep track of all slices.
 - Threshold for data: 30-60 is a good place to start for searching Gal4.
 - If background is too high, increase threshold (max value is 255).  
 - If too many hits, can try increasing % of Positive PX Threshold 5-10%.
 - The search can stop by pushing escape.

## Synchronize windows
To make sure the position between the mask (the neuron) and hits (Gal4 lines), synchronizing the wingdows is useful function.
 1. Analyze > Tools > Synchronize Windows.  
 2. Select the two windows to synchronize.  
<!-- dummy -->
![ScreenShot1](../images/scr1.png)  
<br />
A red x (cross-hair) will now appear at the same position in both images.
![ScreenShot2](../images/scr3.jpg)

## Create a list of candidate lines
`realtime_Result_ctrl_click_substack.ijm` is useful for quickly making a list of lines and the substack while scrolling through the stack of potential hits. 
 1. Open `realtime_Result_ctrl_click_substack.ijm`.
 2. Click window with colorMIP search result stacks.
 3. Run macro (only accepts one open window). Then Result window will be open.
 4. Shift + click on the result stack will add the image name into the Result table.
 5. ctrl + click will create a substack with the Result table from the result stack.
 
 ## Demo movie
 [![Demomovie](../images/MIPsearchDemo.jpg)](https://youtu.be/JVZs19yvEqY)
