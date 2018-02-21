# Color-MIP Mask Search
The algorithm is created by Hideo Otsuna.  
FIJI plugins created by Hideo Otsuna and Takashi Kawase.  
This documentation wrote by Justine X Chia.  

## Before starting
 1. update FIJI then quit
 2. in the Applications folder, get package contents of FIJI
 3. add Color_MIP_plugins folder and ColorMIP_Mask_Search.class to the plugins folder
 4. add PsychedelicRainBow2.lut to the lut folder

## Startup
drag folder (eg. containing AD) into fiji, use virtual stack  
_nb: the examples are from searching AD from the VT lines for neurons to mask, but DBD works as well_

## Create mask of neuron of interest
 1. duplicate the slice containing the neuron (⌘⇧ D)
 2. trace area of interest on duplicated slice (use polygon tool and try to be as accurate as possible)
 3. Edit > Selection > Make Inverse  
 (if this is the first time, select black area with dropper)
 4. fill the background black (⌘ F)

## Search stacks with mask
Plugins > ColorMIP Mask search  
### considerations/ tips:
 - Show log // -> show NaN for log may be useful for a first pass, just to keep track of all slices
 - threshold for data: 30-50 is a good place to start for VT, 50 for GMR
 - if background is too high, decrease threshold (max value is 255)  
 - if too many hits, can try increasing % of Positive PX Threshold 0-100% (eg. to 10%) – I was running out of memory on my laptop
 - if you run out of memory, you can stop the search early by pushing esc

## Synchronize windows
masks (e.g. AD) and hits (e.g. DBD) are output into a separate stacks. synchronizing the stacks is useful.  
 1. Analyze > Tools > Synchronize Windows  
 2. Select the two windows to synchronize  
<!-- dummy -->
A red x (cross-hair) will now appear at the same position in both stacks; scrolling through one stack will automatically advance the other stack  

## Create a list of candidate lines
`realtime_Result_ctrl_click_substack.ijm` is useful for quickly making a list of lines while scrolling through the stack of potential hits 
 1. open `realtime_Result_ctrl_click_substack.ijm`
 2. click window with colorMIP stacks (e.g. `Original_RGB_5.- %reformatted`)
 3. run macro (only accepts one open window)
