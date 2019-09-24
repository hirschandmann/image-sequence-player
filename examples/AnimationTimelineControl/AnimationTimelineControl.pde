// Copyright (C) 2019 Hirsch & Mann
// 
// This file is part of Image Sequence Player Examples.
// 
// Image Sequence Player is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Image Sequence Player Examples is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// <http://www.gnu.org/licenses/>.
// 
// Crediting, submitting showcases or contributing is very much appreciated :)
// ===========================================================================

// Hirsch & Mann
// https://www.hirschandmann.com

// import library
import com.hirschandmann.image.*;
// create a reference to an image sequence
int rows = 3;
int cols = 6;
int numPlayers = rows * cols;
ISPlayer[] players = new ISPlayer[numPlayers];

void setup() {
  size(640, 360);
  noSmooth();
  imageMode(CENTER);
  // manually, synchronously load frames
  PImage[] frames = loadImages(dataPath("PT_Teddy"),"gif");
  // for each player
  for(int i = 0 ; i < numPlayers; i++){
    // create an instance
    players[i] = new ISPlayer(this);
    // pass the frames for independent control
    players[i].init(frames,"teddy-" + nf(i+1,2));
    // playback at 24fps
    players[i].setDelay(1000/24);
  }
}

// return a list of loaded images for a given folder path and image extension 
PImage[] loadImages(String path,String extension){
  String[] paths = listPaths(path, "files", "extension=" + extension);
  
  int numFrames = paths.length;
  PImage[] frames = new PImage[numFrames];
  
  for(int i = 0 ; i < numFrames; i++){
    frames[i] = loadImage(paths[i]);
  }
  
  return frames;
}

void draw() {
  // clear background with yellow
  background(255, 204, 0);
  // for each player, control timeline and display
  for(int i = 0 ; i < numPlayers; i++){
    int frameOffset = ((i + 1) * 2);
    
    if(mousePressed){
      // mouse based frame control
      int frame = (int)map(constrain(mouseX,0,width),0,width,0,players[i].totalFrames());
      players[i].jump((frame + frameOffset) % players[i].totalFrames());
    }else{
      // time autoplay
      players[i].jump((frameCount + frameOffset) % players[i].totalFrames());
    }

    // render image sequence
    int row = i % cols;
    int col = i / cols;
    image(players[i],100 + row * 100,col * 200);
  }
  // instructions
  text("drag mouse to control frames",5,15);
}

// image sequence events
// first frame loaded (e.g. can pause and render first frame while the rest load)
void firstFrameLoaded(ISPlayer player){
  println(player+" first frame loaded");
}

// all frames loaded
void onSequenceLoaded(ISPlayer player){
  println(player+" sequence fully loaded");
}

// triggered when an sequence finished playing
void onSequencePlayed(ISPlayer player){
  println(player+" sequence played");
}
