# Podcasts

Yet another podcast app. My wife and I both really enjoy using Google Podcasts because of its 
simplicity and basically complete feature set but sadly it is getting shut down on Apr 2, 2024. 
We tried several other apps but didn't find the same appeal in any of them which is why I decided 
to replicate the UI and functionality here.

[Get it on Google Play Store](https://play.google.com/store/apps/details?id=com.ramitsuri.podcasts.android)

## Features
- Offline first 
- Allows import from Google Podcasts
- Queue management
- Sleep timer
- Speed change 
- Trim silence 
- More to come...

## Built using 
- Podcast Index
- Kotlin multiplatform (Android only for now)
- Jetpack compose
- Media 3

## Screenshots
<p align="center">
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/1.png" width="25%"  alt="1.png"/>
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/2.png" width="25%"  alt="2.png"/>
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/3.png" width="25%"  alt="3.png"/>
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/4.png" width="25%"  alt="4.png"/>
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/5.png" width="25%"  alt="5.png"/>
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/6.png" width="25%"  alt="6.png"/>
</p>

<p align="center">
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/1-dark.png" width="25%"  alt="1-dark.png"/>
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/2-dark.png" width="25%"  alt="2-dark.png"/>
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/3-dark.png" width="25%"  alt="3-dark.png"/>
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/4-dark.png" width="25%"  alt="4-dark.png"/>
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/5-dark.png" width="25%"  alt="5-dark.png"/>
<img src="https://github.com/ramitsuri/podcasts/blob/main/images/6-dark.png" width="25%"  alt="6-dark.png"/>
</p>

> [!NOTE]
> For building the app, create a `secret.properties` file in the root of the project and paste
> your Podacst Index key and secret like
> ```
> PODCAST_INDEX_KEY=<key_value>
> PODCAST_INDEX_SECRET=<secret_value>
> ```
