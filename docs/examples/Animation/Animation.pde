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
ISPlayer teddy;

void setup() {
  size(640, 360);
  noSmooth();
  imageMode(CENTER);
  // create instance and load all valid images from the data/PT_Teddy folder
  teddy = new ISPlayer(this,dataPath("PT_Teddy"));
  // update the animation at 24 fps
  teddy.setDelay(1000/24);
}

void draw() {
  // clear background with yellow
  background(255, 204, 0);
  // render image sequence
  image(teddy,mouseX,mouseY);
}

// image sequence events
// first frame loaded (e.g. can pause and render first frame while the rest load)
void firstFrameLoaded(ISPlayer player){
  println(player+" first frame loaded");
}

// all frames loaded
void onSequenceLoaded(ISPlayer player){
  println(player+" sequence fully loaded");
  player.loop();
}

// triggered when an sequence finished playing
void onSequencePlayed(ISPlayer player){
  println(player+" sequence played");
}
