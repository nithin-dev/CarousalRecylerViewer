# Carousal Recyclerview with cutom Dialog and Retrofit

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
![passwordanalyser](https://user-images.githubusercontent.com/71749797/113116497-21029b00-922b-11eb-9f2a-d4f6a225a8c1.gif)
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
![passwordgenerator](https://user-images.githubusercontent.com/71749797/113116148-cc5f2000-922a-11eb-8daa-7740d29d0b9f.gif)
### Author



Nithin Thomas.
