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
    
	// 
	private static HashMap<String,Boolean> formats;
	
	private PApplet parent;
    private Thread runner;
    
    private boolean resized;
    
    private boolean isPlaying;
    private boolean isLooping;
    
    private int numFrames;
    private int currentFrame;
    private PImage[] frames;
    
    private int lastUpdate;
    
    private int delay = (int)(1000/30.0);//!rounded down
    
    private boolean finishedPlaying,firstFrameLoaded,loading;
    private float loadProgress;
    
    private Method onFirstFrameLoadedMethod;
    private Method onSequenceLoadedMethod;
    private Method onSequencePlayedMethod;
    
    private String name;
    
    private boolean isThreadRunning;

    public final static String VERSION = "##library.prettyVersion##";

    public ISPlayer(PApplet sketch){
        super(1, 1, ARGB);
        this.parent = sketch;
        
        setupFormatsLUT();
        
        this.onFirstFrameLoadedMethod = findCallback("firstFrameLoaded",ISPlayer.class);
        this.onSequenceLoadedMethod = findCallback("onSequenceLoaded",ISPlayer.class);
        this.onSequencePlayedMethod = findCallback("onSequencePlayed",ISPlayer.class);
    }

    public ISPlayer(PApplet sketch,String folderPath){
        this(sketch);
        init(folderPath);
    }
    
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
        
        
        lastUpdate = parent.millis();
        
        loading = true;
        firstFrameLoaded = false;
        // start thread
        isThreadRunning = true;
        runner = new Thread(this);
        runner.setName("[ISPlayer - " + name + "]");
        runner.start();
    }
    
    /**
     * Initialize from a pre-loaded list of images
     * 
     * @param images - the list of images 
     * @param name - the name of the animation
     */
    public void init(PImage[] images, String name){
        clean();
        
        this.name = name;
        frames = images.clone();
        numFrames = images.length;

        //TODO: check if running twice on the same instance works
        isThreadRunning = true;
        runner = new Thread(this);
        runner.setName("[ISPlayer - " + name + "]");
        runner.start();
        
        lastUpdate = parent.millis();
        
        loading = true;
        firstFrameLoaded = false;
    }
    
    private void checkLoaded(){
    	
        int numFramesLoaded = 0;
        
        for(int i = 0; i < numFrames; i++){
        	
            if(frames[i] != null){
            	
              if(frames[i].isLoaded()){
            	  numFramesLoaded++;
              }
              
              if(frames[i].width > 0 && frames[i].height > 0) {
                    numFramesLoaded++;
                    
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
        
        loading = (numFramesLoaded != numFrames);
        
        loadProgress = (float)numFramesLoaded / numFrames;
        
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
    
    public void run() {
    	
        while (isThreadRunning) {
        	
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {}
            
            if(loading){
            	checkLoaded();
            }
            
            if(frames == null || frames.length == 0){
            	System.err.println("no frames loaded, use init() to run again with a different image sequence");
            	isThreadRunning = false;
            	stop();
            	return;
            }
            
            if(!resized){
                if(frames[0].width > 0 && frames[0].height > 0){
                    super.init(frames[0].width, frames[0].height, ARGB);
                    resized = true;
                }
            }
            
            if (isPlaying) {
//                if (parent.millis() - lastUpdate >= delay) {
                	
                    finishedPlaying = (currentFrame == numFrames - 1);
                    
                    if (finishedPlaying) {
                        
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
                    	
                    	// reset playhead if looping, otherwise stop
                        if (isLooping) {
                        	jump(0);
                        }else{
                        	stop();
                        }
                        
                    }else jump(currentFrame + 1);
//                }
            }
        }
    }
    
    /**
     * 
     */
    public void dispose() {
    	isThreadRunning = false;
        stop();
        runner = null;
    }
    
    /**
     * 
     */
    public void clean(){
        stop();
        for(int i = 0 ; i < numFrames; i++) frames[i] = null;
        numFrames = 0;
    }
    
    /**
     * 
     * @param where
     */
    public void jump(int where) {
        if (numFrames > where) {
            currentFrame = where;
            //TODO: is this needed ?
//            loadPixels();
            
            if(frames[currentFrame].width > 0 && frames[currentFrame].height > 0){
                
            	try{
                    System.arraycopy(frames[currentFrame].pixels, 0, pixels, 0, width * height);
                }catch(Exception e){
                    e.printStackTrace();
                }
                
                if(currentFrame == numFrames-1) {
                	loading = false;
                }
            }
            updatePixels();

            lastUpdate = parent.millis();
        }
    }
    
    /**
     * 
     */
    public void stop() {
        isPlaying = false;
        currentFrame = 0;
    }
    
    /**
     * 
     */
    public void play() {
        isPlaying = true;
    }
    
    /**
     * 
     */
    public void loop() {
        isPlaying = true;
        isLooping = true;
    }
    
    /**
     * 
     */
    public void loopOnly() {
        isLooping = true;
    }
    
    /**
     * 
     */
    public void noLoop() {
        isLooping = false;
    }
    
    /**
     * 
     */
    public void pause() {
        isPlaying = false;
    }
    
    /**
     * 
     * @param ms
     */
    public void setDelay(int ms){
        if(ms < 0) return;
        delay = ms;
    }

    public PImage[] getPImages() {
        return frames;
    }
    /**
     * 
     * @return
     */
    public boolean isPlaying() {
        return isPlaying;
    }
    
    /**
     * 
     * @return
     */
    public int currentFrame() {
        return currentFrame;
    }
    
    /**
     * 
     * @return
     */
    public int totalFrames(){
        return numFrames;
    }
    
    /**
     * 
     * @return
     */
    public boolean isLooping() {
        return isLooping;
    }
    
    /**
     * 
     * @return
     */
    public boolean hasFinishedPlaying(){
        return finishedPlaying;
    }
    
    /**
     * 
     * @return
     */
    public boolean isLoading(){
        return loading;
    }
    
    /**
     * 
     * @return
     */
    public float getLoadProgress(){
        return loadProgress;
    }
    
    public String toString(){
    	return "[ISPlayer name=" + name + "]";
    }
    
    /**
     * 
     * @return
     */
    public String getName(){
        return name;
    }
    
    /**
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
 	private Method findCallback(final String name,Class argumentType) {
 		try {
 	      return parent.getClass().getMethod(name, argumentType);
 	    } catch (Exception e) {
 	    	System.out.println("couldn't find " + name + " callback in sketch, ignoring data");
 	    	//e.printStackTrace();
 	    }
 	    return null;
 	 }
}
