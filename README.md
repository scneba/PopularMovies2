### Popular Movies stage 2
This is the second project in the Android Developer Nanodegree. With this project, we used [AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html) to load data from [theMoviedb](http://www.themoviedb.org) database into a [GridView](https://developer.android.com/guide/topics/ui/layout/gridview.html) in the android application. These are the core functionalities of the application:

- Upon launch, present the user with an grid arrangement of movie posters.
- Allow your user to change sort order via a setting:
- The sort order can be by most popular, or by top rated
- Allow the user to tap on a movie poster and transition to a details screen with additional information such as:
 - original title
 - movie poster image thumbnail
 - A plot synopsis (called overview in the api)
 - user rating (called vote_average in the api)
 - release date

Additional functionality in stage 2
- Add more information to your movie details view:
- Allow users to view and play trailers ( either in the youtube app or a web browser).
- Allow users to read reviews of a selected movie.
- Allow users to mark a movie as a favorite in the details view by tapping a button(star). This is for a local movies collection that you will maintain and does not require an API request*.
- Modify the existing sorting criteria for the main view to include an additional pivot to show their favorites collection.


 ### License
Copyright [2017] [Shu Clasence Neba](https://www.linkedin.com/in/shu-clasence-neba-352615bb/)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.