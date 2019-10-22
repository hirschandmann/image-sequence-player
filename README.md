## Image Sequence Player for Processing

**A Processing library to load, playback and display image sequences**

![preview](https://raw.githubusercontent.com/orgicus/image-sequence-player/master/preview.gif)

There already is a gifAnimation library that loads, plays back and exports .gif animation,
however there are cases where finer control is needed, such as:

- easily integrating image sequences into Processing sketches
- displaying animation sequences with more colours, but maintain transparency (e.g. animated png sequence with crisp colours)
- precisely controlling a timeline frame by frame where audio isn't needed (and video keyframes don't offer enough control)  

Contributions welcome.

### Installation:

### Method 1: via Contribution Manager

1. Go to **Processing > Sketch > Import Library... > Add Library...**
2. Type *Image Sequence Player* in the search field
3. Press **Install**

### Method 2: Manual install:

1. Download [ImageSequencePlayer.zip](https://github.com/hirschandmann/image-sequence-player/releases/download/latest/ImageSequencePlayer.zip)
2. Unzip into **Documents > Processing > libraries**
3. Restart Processing (if it was already running)

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

Developed by George Profenza at [Hirsch & Mann](http://hirschandmann.com) and used on multiple projects such as:

- [Imagine Festival Time Machine at Southbank Centre](https://www.hirschandmann.com/portfolio_page/time-machine/)![Imagine Fest Soutbank Centre 2014](https://www.hirschandmann.com/wp-content/uploads/2017/06/Hirschandmann_IMG_featured01_TIMEMACHINE.jpg)

- [Playable City Bristol](https://www.hirschandmann.com/portfolio_page/making-smiles-in-the-city/)![Playable City Bristol 2018](https://www.hirschandmann.com/wp-content/uploads/2018/08/city_website_0003_1150976_JonAitken_edit-001.jpg)(image credits: Jon Aitken)
- and [more](http://hirschandmann.com/work)
