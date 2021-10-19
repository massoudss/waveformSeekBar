
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
# WaveformSeekBar
Android Waveform SeekBar library

<img src="./files/preview.png" width="300">
<img src="./files/preview.gif" width="300">


## How to add

Add below lines in your root build.gradle at the end of repositories

``` groovy
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
Add the dependency to your app build.gradle file

``` groovy
dependencies {
    implementation  'com.github.massoudss:waveformSeekBar:4.0.1'

    // Amplitude will allow you to call setSampleFrom() with files, URLs and resources
    // Important: Only works with api level 21 and higher
    implementation 'com.github.lincollincol:amplituda:2.1.0' // or newer version
}
```

And then sync your gradle.


## How to use
You can simply use this View like other Views in android,
just add ``WaveformSeekBar`` in your java/kotlin code or xml.
### XML
``` xml
<com.masoudss.lib.WaveformSeekBar
    app:wave_progress="33"
    app:wave_max_progress="100"
    app:wave_width="5dp"
    app:wave_gap="2dp"
    app:wave_min_height="5dp"
    app:wave_corner_radius="2dp"
    app:wave_background_color="@color/colorAccent"
    app:wave_progress_color="@color/colorPrimary"
    app:wave_gravity="center"
    android:id="@+id/waveformSeekBar"
    android:layout_width="300dp"
    android:layout_height="50dp"/>
```

### Set samples
``` kotlin
// Custom samples
waveformSeekBar.setSampleFrom(intArrayOf( /* samples */ ))

/* Functions below require Amplituda library dependency */
// Local audio file
waveformSeekBar.setSampleFrom(File("/storage/emulated/0/Music/song.mp3"))

// Path to local file
waveformSeekBar.setSampleFrom("/storage/emulated/0/Music/song.mp3")

// Url
waveformSeekBar.setSampleFrom("https://audio-url-example.com/song.mp3")

// Resource (res/raw)
waveformSeekBar.setSampleFrom(R.raw.song)
```

### Progress Listener
``` kotlin
waveformSeekBar.onProgressChanged = object : SeekBarOnProgressChanged {
    override fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Int, fromUser: Boolean) {
        // do your stuff here
    }
}
```

### Customization
#### Kotlin
``` kotlin
val waveformSeekBar = WaveformSeekBar(yourContext)
waveformSeekBar.progress = 33
waveformSeekBar.maxProgress = 100
waveformSeekBar.waveWidth = Utils.dp(this,5)
waveformSeekBar.waveGap = Utils.dp(this,2)
waveformSeekBar.waveMinHeight = Utils.dp(this,5)
waveformSeekBar.waveCornerRadius = Utils.dp(this,2)
waveformSeekBar.waveGravity = WaveGravity.CENTER
waveformSeekBar.waveBackgroundColor = ContextCompat.getColor(this,R.color.colorAccent)
waveformSeekBar.waveProgressColor = ContextCompat.getColor(this,R.color.colorPrimary)
```

#### Java
``` java
WaveformSeekBar waveformSeekBar = new WaveformSeekBar(yourContext);
waveformSeekBar.setProgress(33);
waveformSeekBar.setMaxProgress(100);
waveformSeekBar.setWaveWidth(Utils.dp(this,5));
waveformSeekBar.setWaveGap(Utils.dp(this,2));
waveformSeekBar.setWaveMinHeight(Utils.dp(this,5));
waveformSeekBar.setWaveCornerRadius(Utils.dp(this,2));
waveformSeekBar.setWaveGravity(WaveGravity.CENTER);
waveformSeekBar.setWaveBackgroundColor(ContextCompat.getColor(this,R.color.white));
waveformSeekBar.setWaveProgressColor(ContextCompat.getColor(this,R.color.blue));
```

### View Properties

You can customize WaveformSeekBar, all of this attributes can change via xml or code (runtime)

|Attribute|Type|Kotlin|Description|
|:---:|:---:|:---:|:---:|
|wave_progress|Float|`progress`|SeekBar progress value, default value is `0F`|
|wave_max_progress|Float|`maxProgress`|SeekBar max progress value, default value is `100F`|
|wave_visible_progress|Float|`visibleProgress`|How much part of the progress should be shown, default value is `0F` meaning everything is shown and progress indicator moves, if value > `0F` the bars move and progress indicator stays in the center|
|wave_width|Dimension|`waveWidth`|Width of each wave, default value is `5dp`|
|wave_gap|Dimension|`waveGap`|Gap width between waves, default value is `2dp`|
|wave_min_height|Dimension|`waveMinHeight`|Minimum height of each wave, default value is equal to `waveWidth`|
|wave_corner_radius|Dimension|`waveCornerRadius`|Corner raduis of each wave, default value is `2dp`|
|wave_gravity|Enum|`waveGravity`|Waves Gravity, default is `WaveGravity.CENTER`|
|wave_background_color|Color|`waveBackgroundColor`|UnReached Waves color, default color is `Color.LTGRAY`|
|wave_progress_color|Color|`waveProgressColor`|Reached Waves color, default color is `Color.WHITE`|
| - |IntArray|`sample`|Sample data for drawing waves, default is `null`|

### Reduce size
Add ``` android:extractNativeLibs="false" ``` to application in the Manifest.xml

``` xml
<application
      . . .
    android:extractNativeLibs="false"
      . . . >
    <activity . . ./>
</application>
```

## License
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
