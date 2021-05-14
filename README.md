# Carousal Recyclerview with Custom Dialog 

 A Customized RecyclerView with Swipeable Popup with custom directions using Java.

### Customized Carousal RecyclerView.

## Usage


Create `CustomCarouselLayoutManager`  class for zoom in zoom out recyclerview(horizontal and vertical layout supported).

import `CustomCarouselLayoutManager` to Main class

```
    final CustomCarouselLayoutManager layoutManager = new CustomCarouselLayoutManager(CustomCarouselLayoutManager.VERTICAL,true);
        layoutManager.setPostLayoutListener(new CustomZoomPostLayoutListener());
        layoutManager.isSmoothScrolling();
```
![14052012 (1)](https://user-images.githubusercontent.com/71749797/118281040-6b459000-b4ea-11eb-8a6c-d47f406788cc.gif)



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
