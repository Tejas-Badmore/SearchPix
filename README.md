# SearchPix
Search Engine for Images using Pixabay API
## Problem Statement
The home screen of the app has a search field and an options menu. The functionality of the app are as follows:
* Search:
When the user types a search term in the search field and presses done the application communicates with a any image search API (I have usedPixabay API) and displays the result images in a grid-layout below the search field.
The images must be shown as square views in the grid without any skewing.
* Options:
The number of columns in the grid can be changed to 2, 3 or 4 columns from the options menu (by default the grid is 2 column).
Changing the number of columns should NOT re-invoke the API, it must be handled at the UI level.
* Paging:
The grid layout has infinite scroll support i.e. if the user scrolls to the bottom of the page, then the app will re-contact the image search API to get more results and 
add them to the bottom of the grid.


## API used
1. Pixabay API
2. Gson API
3. Glide API
4. Retrofit2 API
5. Material API


## Fuctionality
1. Stores Search History
2. Stores Last Search fired
3. Infinite Scroller
4. Grid Column can change
5. Landscape and Potrait mode working
6. Search using Google Speech
7. High Resolution Photos when clicked

## Requirement
* Pixabay API: 
Create account to generate unique API key. 
Put that api key into "string.xml" file into "API_KEY" tag. And the project is ready for working.

## Demo
* Apk file is available for demo
