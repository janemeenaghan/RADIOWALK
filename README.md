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
- User can create or edit geo-located "Station" Parse Objects stored in a back4app database and rendered on a GoogleMap that follows user's live-updating location
- At any time when in range of station(s), live-streams audio from a selected station's radio source via the app's MediaPlayerController
  - Audio focus management with error management and pausing, playing, and stopping music if audio focus is lost/gained
  - Pauses, resumes, stops in responds to activity live cycle events and is properly released on destroy or critical MediaPlayer.MEDIA_ERROR_SERVER_DIED error 
  - Implemented via dynamic UI (play/pause button updates icon according to MediaPlayer Controller callback regarding playing state, play/pause button and radio favicon hide when no station is being played)
- Retrofit HTTP client constructed to interface with Radio-Browsers.info database to retrieve radio source when adding or editing a Station (Planned Problem #1)
  - Request constructed from a base url and specific paths, serialized into JSON by Gson
  - Response deserialized into custom StationInfo model of mostly key-value pairs,  packaged through activityResult back into the main code flow, formulated by the StationController into a Station ParseObject saved to back4app database
  - Handled by an adapter into a RecyclerView or into the back4app database via an intent callback back to the activity that handles Parse
  - Queried results rendered in a devoted activity with icons, names, tags, and like counts, 20 results per page, with Next and Prev buttons to navigate
  - Initially queries the most popular stations (by retrieved likes data), also allows users to instead filter by tags through SearchView input, both safeguarding against 'broken' results
- User, 'Station', and retrieved radio source data maintained through Parse requests to a database hosted by back4app
  - After a brief launch screen, user can sign up or log in, with basic enforcement of properly formatted email addresses, matching passwords, etc.
  - Backend completed to support basic sharing and revoking of access to Private Stations between users, front-end interface coming soon
  - Querying/filtering/selecting algorithm (Planned Problem #2) (Stretch Feature):
    a. Automatic, real-time querying of back4app database according to the user's live-updating location for nearby Station objects satisfying along 3 parameters:
       1) if geo-located within a defined detection radius of user's location
       2) if matching the user-inputted type preference (Private Stations, Public Stations, or Both)
       3) if containing the user-inputted search string (from a SearchView) in its tags data (fetched from its radio-browser source into back4app by Retrofit when the radio-browser source was selected) (only factors this if the user made an input to the SearchView)
    b. Selection from among these queried Stations for a station to according to a score computed according to:
       1) proximity to user
       2) popularity of station's radio source (according to likes data fetched from its radio-browser source into back4app by Retrofit at the time of source selection)
       3) flat preference weights given to the already currently selected station (as an inertia)
       4) randomness scaled by a user-inputted chaos factor to offer diversity in station selection at higher user input levels
- UI Design:
   - Queried/selected Stations rendered on a GoogleMap via color-coded custom markers and circles on map
   - Extensive styling and numerous animations accessed from rey5137's Material Libary (external library)
   - Maps seamlessly integrated across the main page
     - Toolbar overhead with:
       - Facebook Link Button
       - Dark/Light mode toggle button
       - ? button offering instructions AlertDialog on how to use the app
       - Logout button
     - Small panel underneath Toolbar with UI elements for controlling algorithm:
       - Public/Private/Both radio group
       - Chaos Meter
       - SearchView to filter nearby by tag
     - SlidingUpPanel from beneath maps displaying Station's stream information:
       - Radio source's favicon (fetched with Retrofit client into back4app)
       - Volume bar with unique custom animation when dragging to change value
       - Play/pause button
     - Station UI objects on map designed from color-coded, customized GMaps Markers and Circles
     - Add and Edit FloatingActionButtons in bottom right area to add or modify station
       - Add button click animation rearranges the line segments of it's + icon into a ✓ icon
     - Dark/light mode toggled by Toolbar button encompassing styles/themes for text, widget, and even Map styles simultaneously (Complex Feature #3?) (Stretch Feature)
- Social Integration / Accounts (Stretch Feature):
  - On Launch, after splash screen, user directed through signup/login flow UI if not already logged in
  - Parse signup/login flow with option of linking to a Facebook profile at any time
  - 'Continue through Facebook' Facebook-directed signup/login flow
  
More stretch features coming soon:
- One-time operation to populate back4app with countless spread out Public Stations provided by Retrofit HTTP handling of Places SDK's Nearby Search
- Social Integration:
  - Front-end interface implemented onto SlidePanelLayout's empty space when swiped up:
    - Add/remove/view associated users (by username) starting from the Station marker on the Map
    - (Also display other information about the current station)
  - Expand sharing functionality
  - Expand Facebook integration
  - Otherwise make app more socially integrated
- Include original plan of collaborative playlist streaming linked to stations
- Dark/Light mode: automatically set based on current time?

## Video Walkthrough

Here's what the app might look like for a new user:
<img src='NewUserDemo.gif' title='Video Walkthrough' width='' alt='Video Walkthrough' />

Here's what the app might look like for an experienced user linked to Facebook and included in many private stations:
<img src='ExperiencedUserDemo.gif' title='Video Walkthrough' width='' alt='Video Walkthrough' />

## Credits

List an 3rd party libraries, icons, graphics, or other assets you used in your app.
- Retrofit - networking library
- Material Design - UI icons, graphics, styles
- Parse - networking Library
- GoogleMaps, Places
- Radio Browser - external database (not IN my app, but retrieved/logged data from it)

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