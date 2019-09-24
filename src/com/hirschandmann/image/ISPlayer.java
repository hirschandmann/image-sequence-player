package com.hirschandmann.image;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.data.JSONObject;

/**
 * Image Sequence Player
 * loads images in separate threads 
 * (use PApplet's requestImageMax to control how many image loading threads can be isStarted simultaneously)
 */
public class ISPlayer extends PImage implements PConstants,Runnable {
    
	// look up table of supported image formats
	private static HashMap<String,Boolean> formats;
	// parent sketch
	private PApplet parent;
	// thread
    private Thread playbackThread;
    // internal flag to resize this PImage instance based on the first loaded frame 
    private boolean resized;
    // playback flag
    private boolean isPlaying;
    // looping flag
    private boolean isLooping;
    // total number of farmes
    private int numFrames;
    // total number of pixels
    private int numPixels;
    // current frame index (0 to length-1 )
    private int currentFrame;
    // loaded PImage frames
    private PImage[] frames;
    // default playback rate (~30fps)
    private int delay = (int)(1000/30.0);//!rounded down
    // finished showing last frame flag
    private boolean finishedPlaying;
    // first frame loaded flag
    private boolean firstFrameLoaded;
    // sequence still loading flag
    private boolean loading;
    // current load progress
    private float loadProgress;
    
    // main sketch callbacks
    private Method onFirstFrameLoadedMethod;
    private Method onSequenceLoadedMethod;
    private Method onSequencePlayedMethod;
    // animation name (typically loaded image sequence folder name)
    private String name;
    // thread running flag
    private boolean isThreadRunning;
    // pretty-print library version
    public final static String VERSION = "##library.prettyVersion##";
    
    /**
     * instantiate empty image sequence player 
     * @param sketch
     */
    public ISPlayer(PApplet sketch){
        super(1, 1, ARGB);
        this.parent = sketch;
        
        setupFormatsLUT();
        
        this.onFirstFrameLoadedMethod = findCallback("firstFrameLoaded",ISPlayer.class);
        this.onSequenceLoadedMethod = findCallback("onSequenceLoaded",ISPlayer.class);
        this.onSequencePlayedMethod = findCallback("onSequencePlayed",ISPlayer.class);
    }

    /**
     * instantiate image sequence player and load supported images from folder
     * @param sketch
     * @param folderPath
     */
    public ISPlayer(PApplet sketch,String folderPath){
        this(sketch);
        init(folderPath);
    }
    
    /**
     * setup look-up table of supported formats
     */
    private void setupFormatsLUT(){
    	if(ISPlayer.formats == null){
    		ISPlayer.formats = new HashMap<String,Boolean>();
    		
    		ISPlayer.formats.put("jpeg",true);
    		ISPlayer.formats.put("jpg",true);
            ISPlayer.formats.put("png",true);
            ISPlayer.formats.put("tga",true);
            ISPlayer.formats.put("bmp",true);
            ISPlayer.formats.put("gif",true);
    	}
    }
    
    /**
     * Initialise an existing player with a different folder path 
     * @param folderPath
     */
    public void init(String folderPath){
    	File dir = new File(folderPath);
    	
        if(!dir.exists()) {
        	throw new Error("The location is not valid:\n"+dir.getAbsolutePath()+"\nCheck if the path exists and is a folder/directory.");
        }
        // remove previous frames (if any)
        clean();
        // auto rename based on folder name
        name = dir.getName();
        
        File[] files = dir.listFiles();
        ArrayList<String> paths = new ArrayList<>();
        
        assert files != null;
        // for each file
        for (File file : files) {
            String name = file.getName();
            int dotIndex = name.lastIndexOf(".");
            // ignore anything with extensions larger than 4 characters
            if(dotIndex < name.length() - 4){
            	System.err.println("invalid extension for file: " + name);
            	continue;
            }
            // extract extension
            String extension = name.substring(dotIndex+1);
            
            if(ISPlayer.formats.containsKey(extension)){
            	paths.add(file.getAbsolutePath());
            }
            
        }
        // sort by filename
        java.util.Collections.sort(paths);
        numFrames = paths.size();
        frames = new PImage[numFrames];
        // load images async
        for(int i = 0 ; i < numFrames; i++){
            frames[i] = parent.requestImage(paths.get(i));
        }
        
        loading = true;
        firstFrameLoaded = false;
        // start thread
        restartThread();
    }
    
    /**
     * Initialize from a pre-loaded list of images
     * 
     * @param images - the list of images 
     * @param name - the name of the animation
     */
    public void init(PImage[] images, String name){
        clean();
        stop();
        
        this.name = name;
        frames = images.clone();
        numFrames = images.length;

        restartThread();
        
        loading = true;
        firstFrameLoaded = false;
    }
    
    private void restartThread(){
    	//check if already running and stop first
        if(playbackThread != null){
        	playbackThread.interrupt();
        	playbackThread = null;
        }
        // (re)start thread
        isThreadRunning = true;
        playbackThread = new Thread(this);
        playbackThread.setName("[ISPlayer - " + name + "]");
        playbackThread.start();
    }
    
    /**
     * check if all frames have fully loaded and dispatch events depending on the state
     */
    private void checkLoaded(){
    	
        int numFramesLoaded = 0;
        // for each frame
        for(int i = 0; i < numFrames; i++){
        	
        	// if there is a frame to check
            if(frames[i] != null){
            	
              // frames[i].isLoaded() sometimes returns true even before the image has fully initialized, using dimensions instead
              if(frames[i].width > 0 && frames[i].height > 0) {
                    numFramesLoaded++;
                    // dispatch first frame loaded if callback is defined
                    if(!firstFrameLoaded && i == 0){
                        firstFrameLoaded = true;
                        
                        if (onFirstFrameLoadedMethod != null) {
                			// try to call main sketch
                			try {
                				onFirstFrameLoadedMethod.invoke(parent, this);
                			}catch (Exception e) {
                				System.err.println("Error, disabling firstFrameLoaded()");
                				System.err.println(e.getLocalizedMessage());
                				onFirstFrameLoadedMethod = null;
                			}
                		}
                    }
              	}
            }
        }
        // have all frames fully loaded ? yes or no
        loading = (numFramesLoaded != numFrames);
        // how much have they loaded ?
        loadProgress = (float)numFramesLoaded / numFrames;
        // if loading is complete and there's a callback, notify parent sketch
        if(!loading && onSequenceLoadedMethod != null){
        	
        	// try to call main sketch
			try {
				onSequenceLoadedMethod.invoke(parent, this);
			}catch (Exception e) {
				System.err.println("Error, disabling onSequenceLoaded()");
				System.err.println(e.getLocalizedMessage());
				onSequenceLoadedMethod = null;
			}
			
        }
        
    }
    
    /**
     * handled by internal thread, don't call manually
     */
    public void run() {
    	
        while (isThreadRunning) {
        	// wait based on set frame rate
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {}
            // check if still loading
            if(loading){
            	checkLoaded();
            }
            // if playback is attempted with no frames, exit
            if(frames == null || frames.length == 0){
            	System.err.println("no frames loaded, use init() to run again with a different image sequence");
            	isThreadRunning = false;
            	stop();
            	return;
            }
            // to adjust width/height properties, reset this PImage to the first frame (assumes all frames have same dimensions)
            if(!resized){
                if(frames[0].width > 0 && frames[0].height > 0){
                    super.init(frames[0].width, frames[0].height, ARGB);
                    numPixels = width * height;
                    resized = true;
                }
            }
            // if playing back
            if (isPlaying) {
                // is this the last frame ?	
                finishedPlaying = (currentFrame == numFrames - 1);
                // if so
                if (finishedPlaying) {
                    // as long as there's a callback, try to call it
                	if(onSequencePlayedMethod != null){
                		
                		// try to call main sketch
            			try {
            				onSequencePlayedMethod.invoke(parent, this);
            			}catch (Exception e) {
            				System.err.println("Error, disabling onSequencePlayed()");
            				System.err.println(e.getLocalizedMessage());
            				onSequencePlayedMethod = null;
            			}
                		
                	}
                	
                	// reset play head if looping, otherwise stop
                    if (isLooping) {
                    	jump(0);
                    }else{
                    	stop();
                    }
                    
                } else {
                	// gotoAndStop(nextFrame);
                	jump(currentFrame + 1);
                }
            }
        }
    }
    
    /**
     * stop playback and thread
     */
    public void dispose() {
    	isThreadRunning = false;
        stop();
        playbackThread = null;
    }
    
    /**
     * stop playback and remove all frames
     */
    public void clean(){
        stop();
        for(int i = 0 ; i < numFrames; i++) frames[i] = null;
        numFrames = 0;
    }
    
    /**
     * jump to a different frame (within the available number of frames this sequence holds)
     * @param where
     */
    public void jump(int where) {
        if (numFrames > where) {
            currentFrame = where;
            
            if(frames[currentFrame].width > 0 && frames[currentFrame].height > 0){
                
            	try{
                    System.arraycopy(frames[currentFrame].pixels, 0, pixels, 0, numPixels);
                }catch(Exception e){
                    e.printStackTrace();
                }
                
                if(currentFrame == numFrames-1) {
                	loading = false;
                }
            }
            updatePixels();

        }
    }
    
    /**
     * stop playback and reset to first frame
     */
    public void stop() {
        isPlaying = false;
        currentFrame = 0;
    }
    
    /**
     * play/resume
     */
    public void play() {
        isPlaying = true;
    }
    
    /**
     * resume playback and loop
     */
    public void loop() {
        isPlaying = true;
        isLooping = true;
    }
    
    /**
     * set loop flag to true, but don't resume playback yet
     */
    public void loopOnly() {
        isLooping = true;
    }
    
    /**
     * stop looping
     */
    public void noLoop() {
        isLooping = false;
    }
    
    /**
     * pause playback
     */
    public void pause() {
        isPlaying = false;
    }
    
    /**
     * change playback rate
     * @param ms
     */
    public void setDelay(int ms){
        if(ms < 0) return;
        delay = ms;
    }
    
    /**
     * returns the array of loaded images
     * @return
     */
    public PImage[] getPImages() {
        return frames;
    }
    /**
     * returns true if the playhead is automatically incremented
     * @return
     */
    public boolean isPlaying() {
        return isPlaying;
    }
    
    /**
     * returns the current frame (image index) the sequence is currently at
     * @return
     */
    public int currentFrame() {
        return currentFrame;
    }
    
    /**
     * returns the total number of frames (images) this sequence holds
     * @return
     */
    public int totalFrames(){
        return numFrames;
    }
    
    /**
     * returns true if the sequence is set to repeat from the first frame at the end of the last frame
     * @return
     */
    public boolean isLooping() {
        return isLooping;
    }
    
    /**
     * returns true if the last frame was reached
     * @return
     */
    public boolean hasFinishedPlaying(){
        return finishedPlaying;
    }
    
    /**
     * returns true as long as images have yet to fully load
     * @return
     */
    public boolean isLoading(){
        return loading;
    }
    
    /**
     * returns a normalized value (0.0 to 1.0)
     * where 0 = 0%, 0.5 = 50%, 1.0 = 100%
     * 
     * @return
     */
    public float getLoadProgress(){
        return loadProgress;
    }
    
    /**
     * String representation
     */
    public String toString(){
    	return "[ISPlayer name=" + name + "]";
    }
    
    /**
     * Get the name of the sequence (typically the folder name)
     * 
     * @return
     */
    public String getName(){
        return name;
    }
    
    /**
     * rename the sequence
     * 
     * @param newName
     */
    public void setName(String newName){
    	name = newName;
    }
    
    
    /**
	 * return the version of the Library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}
    
	// "kindly borrowed" from https://github.com/processing/processing/blob/master/java/libraries/serial/src/processing/serial/Serial.java
 	private Method findCallback(final String name,Class<ISPlayer> argumentType) {
 		try {
 	      return parent.getClass().getMethod(name, argumentType);
 	    } catch (Exception e) {
 	    	System.out.println("couldn't find " + name + " callback in sketch, ignoring data");
 	    	//e.printStackTrace();
 	    }
 	    return null;
 	 }
}
