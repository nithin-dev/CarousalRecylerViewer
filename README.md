# Carousal Recyclerview with custom Dialog 

 A Custom RecyclerView with Swipeable Popup with custom directions on Java.

### Customized Carousal RecyclerView.

## Usage


Create `CustomCarouselLayoutManager`  class for zoom in zoom out recyclerview(horizontal and vertical layout supported).

import `CustomCarouselLayoutManager` to Main class

```
    final CustomCarouselLayoutManager layoutManager = new CustomCarouselLayoutManager(CustomCarouselLayoutManager.VERTICAL,true);
        layoutManager.setPostLayoutListener(new CustomZoomPostLayoutListener());
        layoutManager.isSmoothScrolling();
```
![20210514_173914](https://user-images.githubusercontent.com/71749797/118267834-3df0e600-b4da-11eb-9171-187774c09867.gif)



### Swipeable Custom Dialog.

## Usage




Create  and import `CustomSwipeDismissDialog`  class for customized swipeable dismiss dialog(TOP, BOTTOM, LEFT & RIGHT ).
```
    new CustomSwipeDismissDialog.Builder(context)
                    .setOnSwipeDismissListener((view, direction) -> Toast.makeText(context, "Swiped: " + direction, Toast.LENGTH_SHORT).show())

                    .setView(dialog)
                    .build()
                    .show();
```



Use `SwipeDismissDirectionfinder` enum to find the direction of the swiped dialog box.


```
       if (normalizedVelocityX > params.flingVelocity) {
                SwipeDismissDirectionfinder direction = (e2.getRawX() > e1.getRawX())
                        ? SwipeDismissDirectionfinder.RIGHT
                        : SwipeDismissDirectionfinder.LEFT;
                dismiss(direction);
                return true;
            } else if (normalizedVelocityY > params.flingVelocity) {
                SwipeDismissDirectionfinder direction = (e2.getRawY() > e1.getRawY())
                        ? SwipeDismissDirectionfinder.BOTTOM
                        : SwipeDismissDirectionfinder.TOP;
                dismiss(direction);
                return true;
            } else {
                return false;
            }
```

![140320210 (1)](https://user-images.githubusercontent.com/71749797/118280379-c925a800-b4e9-11eb-9293-44a4d4470d6e.gif)
### Author



Nithin Thomas.
