package com.project.carousalrecycler.carousal_recycler;

public class ItemTransform {
    final float mScaleX;
    final float mScaleY;
    final float mTranslationX;
    final float mTranslationY;

    public ItemTransform(final float scaleX, final float scaleY, final float translationX, final float translationY) {
        mScaleX = scaleX;
        mScaleY = scaleY;
        mTranslationX = translationX;
        mTranslationY = translationY;
    }
}
