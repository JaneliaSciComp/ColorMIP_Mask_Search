# Color-MIP Mask Search [![LinkToJanelia](../images/jrc_logo_180x40.png)](https://www.janelia.org)
The algorithm created by Hideo Otsuna.  
FIJI plugins created by Hideo Otsuna and Takashi Kawase.  
This documentation wrote by Justine X Chia (UCSF).  


## Before starting
 1. launch and update FIJI
 2. drag and drop `ColorMIP_Mask_Search.jar` on FIJI then quit  
 (you can get the .jar file from [here](https://github.com/JaneliaSciComp/ColorMIP_Mask_Search/releases)) 

## Startup
drag folder (eg. containing Gal4 color depth MIPs) into fiji, use virtual stack  
_the examples are searching a neuron from the Gal4 color depth MIP image dataset_

## Create mask of neuron of interest
 1. duplicate a single slice that containing the neuron (⌘⇧ D)
 2. trace area of interest on duplicated slice (use polygon tool and try to be as accurate as possible)
 3. Edit > Clear Outside  

## Search stacks with mask
Plugins > ColorMIP Mask search  
![ScreenShot0](../images/scr0.png)
### considerations/ tips:
 - Show log // -> show NaN for log may be useful for a first pass, just to keep track of all slices
 - threshold for data: 30-60 is a good place to start for searching Gal4
 - if background is too high, decrease threshold (max value is 255)  
 - if too many hits, can try increasing % of Positive PX Threshold 5-10%
 - if you run out of memory, you can stop the search early by pushing esc

## Synchronize windows
masks (the neuron) and hits (Gal4 lines) are output into a separate stacks. synchronizing the stacks is useful.
 1. Analyze > Tools > Synchronize Windows  
 2. Select the two windows to synchronize  
<!-- dummy -->
![ScreenShot1](../images/scr1.png)  
<br />
A red x (cross-hair) will now appear at the same position in both stacks; scrolling through one stack will automatically advance the other stack  
![ScreenShot2](../images/scr2.png)

## Create a list of candidate lines
`realtime_Result_ctrl_click_substack.ijm` is useful for quickly making a list of lines while scrolling through the stack of potential hits 
 1. open `realtime_Result_ctrl_click_substack.ijm`
 2. click window with colorMIP stacks (e.g. `Original_RGB_5.- %reformatted`)
 3. run macro (only accepts one open window)
