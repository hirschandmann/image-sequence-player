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
  noStroke();
  colorMode(HSB,360,100,100);
  background(47, 100, 100);
  // create instance and load all valid images from the data/PT_Teddy folder
  teddy = new ISPlayer(this,dataPath("PT_Teddy"));
  // update the animation at 24 fps
  teddy.setDelay(1000/24);
}

void draw() {
  int hue = frameCount % 360;
  fill(47,100,100,30);
  rect(0,0,width,height);
  // render image sequence
  int sections = (int)map(sin(frameCount * 0.01),-1.0,1.0,6,24);
  float angleIncrement = TWO_PI / sections; 
  int hueSection = 360 / sections;
  translate(width / 2, height / 2);
  noTint();
  image(teddy,-72,-96);
  for(int i = 0 ; i <= sections; i++){
    tint((hue + hueSection * (i + 1)) % 180,100,100);
    pushMatrix();
      rotate(angleIncrement * i);
      translate(90,0);
      pushMatrix();
      translate(mouseX - teddy.width / 2,mouseY - teddy.height / 2);
      rotate(atan2(mouseY-pmouseY,mouseX-pmouseX));
      image(teddy,0,0);
      popMatrix();
    popMatrix();
  }
}

// image sequence events
// all frames loaded
void onSequenceLoaded(ISPlayer player){
  println(player+" sequence fully loaded");
  player.loop();
}
