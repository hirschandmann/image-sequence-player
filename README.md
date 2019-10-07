## Image Sequence Player for Processing

**A Processing library to load, playback and display image sequences**

![preview](https://raw.githubusercontent.com/orgicus/image-sequence-player/master/preview.gif)

There already is a gifAnimation library that loads, plays back and exports .gif animation,
however there are cases where finer control is needed, such as:

- easily integrating image sequences into Processing sketches
- displaying animation sequences with more colours, but maintain transparency (e.g. animated png sequence with crisp colours)
- precisely controlling a timeline frame by frame where audio isn't needed (and video keyframes don't offer enough control)  

Contributions welcome.

### Minimal Example:

```processing
// import library
import com.hirschandmann.image.*;
// create a reference to an image sequence
ISPlayer player;

void setup() {
  size(640, 360);
  // create instance and load all valid images from the data folder
  player = new ISPlayer(this,dataPath("name-of-image-folder-in-data"));
}

void draw() {
  // clear background with yellow
  background(255, 204, 0);
  // render image sequence, ISPlayer extends PImage, use it as such
  image(player,mouseX,mouseY);
}

// image sequence events
// all frames loaded
void onSequenceLoaded(ISPlayer player){
  println(player+" sequence fully loaded");
  player.loop();
}
```

## Credits

The library is heavily influenced by [Patrick Meister's gifAnimation library](https://github.com/extrapixel/gif-animation)

The examples include [James Patterson's animation](http://presstube.com/hello/) from Processing > Examples > Topics > Animation > AnimatedSprite

The libray has been developed at [Hirsch & Mann](http://hirschandmann.com/work) and used on multiple projects

![Imagine Fest Soutbank Centre 2014](https://www.hirschandmann.com/wp-content/uploads/2017/06/Hirschandmann_IMG_featured01_TIMEMACHINE.jpg)

![Playable City Bristol 2018](https://www.hirschandmann.com/wp-content/uploads/2018/08/city_website_0003_1150976_JonAitken_edit-001.jpg)