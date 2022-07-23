# Capstone MVP - RADIOWALK

RADIOWALK is a prototypal location-based augmented reality platform based around Internet radio stations.
Users of the app are invited to link radio broadcasts from across the web to in-app 'stations' they plant at their real-world locations.
Users that come within range of a station's real-world location will automatically pick up on its broadcasts through the app. 
RADIOWALK is a communal experience in which participants are encouraged, for best results, to walk around and experience the 
diverse sonic world that will be collectively assembled through these functions by users in their area.

The concept was inspired by the emerging artistic discipline of soundwalks - curated sonic experiences in which participants 
roam a geographic area, listening to music and sounds that have been tailored to the place's features.
RADIOWALK envisions a more social kind of soundwalk that literally brings people together to create unique, immersive auditory experiences.

Completed Features:
- Retrofit HTTP client is used to interface with Radio-Browsers.info database to retrieve radio (Planned Problem #1)
  - Request constructed from a base url and specific paths (specifically, to get all Stations vs. Stations with a user-entered tag) serialized into JSON by Gson
  - Response deserialized into custom StationInfo model of mostly key-value pairs
  - Handled by an adapter into a RecyclerView or into the back4app database via an intent callback back to the activity that handles Parse
- User, 'Station', and retrieved radio metadata maintained through Parse requests to a database hosted by back4app
  - After a brief launch screen, user can sign up or log in, with basic enforcement of properly formatted email addresses, matching passwords, etc.
  - Backend completed to support basic sharing and revoking of access to Private Stations between users, front-end interface coming soon
  - Querying/filtering algorithm (Planned Problem #2):
    - Continually queries back4app for Stations geo-located within radius of user's live-updating location, filters further based on certain user-input fields (whether Station is Public/Private/Either at the moment, more parameters soon)
    - (Coming soon, the user-input fields will include a tag (retrieved from a search bar) to compare to Radio-Browser tags data (fetched by Retrofit) associated with the radio link the Station is streaming)
    - Lastly, after populating this filtered data to Station UI elements on Map, the algorithm computes the single station that best optimizes the aforementioned parameters and proximity and chooses it to begin listening to
  - UI Design:
    - Numerous animations using rey5137's Material Libary (external) (ex. one button animation rearranges the line segments of it's + icon into a ✓ icon)
    - Maps seamlessly integrated across the main page, sandwiched between
      - Toolbar overhead with:
        - Dark/Light mode switch toggling styles/themes for text, widget, and even Map styles simultaneously (Complex Feature #3?)
        - ? button offering instructions AlertDialog on how to use the app
        - Logout button
      - SlidingUpPanel on bottom with:
        - radio favicon fetched with Retrofit client
        - volume bar with unique custom animation when dragging to change value
    - Station UI objects on map designed from color-coded, customized GMaps Markers and Circles
    - Alternating FloatingActionButtons in bottom right corner to add a station or modify an existing one based on user circumstance

Coming soon:
- Query/filter database stations from back4app by the metadata (particularly tags) of the radio-browser radio that was linked into the Station data model
- One-time operation to populate back4app with countless spread out Public Stations at provided by Retrofit HTTP handling of Places SDK's Nearby Search
- Front-end interface to add/remove/view associated users (by username) starting from the Station marker on the Map
- Link acounts to Facebook or Google, expand sharing functionality, otherwise make app more socially integrated
- Refactor newer code in MainActivity into Modal Controller style design

## Video Walkthrough

Here's what the app might look like for a new user:
<img src='NewUserDemo.mp4' title='Video Walkthrough' width='' alt='Video Walkthrough' />

Here's what the app might look like for an experienced user linked to Facebook and included in many private stations:
<img src='ExperiencedUserDemo.mp4' title='Video Walkthrough' width='' alt='Video Walkthrough' />

GIF created with [ezgif](https://ezgif.com/video-to-gif).

## Credits

List an 3rd party libraries, icons, graphics, or other assets you used in your app.

- [Android Async Http Client](http://loopj.com/android-async-http/) - networking library


## Notes


## License

    Copyright 2022 Jane Meenaghan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.