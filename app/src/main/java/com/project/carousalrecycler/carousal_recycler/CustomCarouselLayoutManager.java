package com.project.carousalrecycler.carousal_recycler;

import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomCarouselLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider  {

    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;
    public static final int VERTICAL = OrientationHelper.VERTICAL;

    public static final int INVALID_POSITION = -1;
    public static final int MAX_VISIBLE_ITEMS = 3;

    private static final boolean CIRCLE_LAYOUT = false;

    private boolean mDecoratedChildSizeInvalid;
    private Integer mDecoratedChildWidth;
    private Integer mDecoratedChildHeight;

    private final int mOrientation;
    private boolean mCircleLayout;

    private int mPendingScrollPosition;

    public final CustomCarouselLayoutManager.LayoutHelper mLayoutHelper = new CustomCarouselLayoutManager.LayoutHelper(MAX_VISIBLE_ITEMS);

    private CustomCarouselLayoutManager.PostLayoutListener mViewPostLayout;

    private final List<CustomCarouselLayoutManager.OnCenterItemSelectionListener> mOnCenterItemSelectionListeners = new ArrayList<>();
    private int mCenterItemPosition = INVALID_POSITION;
    private int mItemsCount;

    @Nullable
    private CustomCarouselLayoutManager.CarouselSavedState mPendingCarouselSavedState;

    @SuppressWarnings("unused")
    public CustomCarouselLayoutManager(final int orientation, final boolean circleLayout) {
        if (HORIZONTAL != orientation && VERTICAL != orientation) {
            throw new IllegalArgumentException("orientation should be HORIZONTAL or VERTICAL");
        }
        mOrientation = orientation;
        mCircleLayout = circleLayout;
        mPendingScrollPosition = INVALID_POSITION;
    }
    @SuppressWarnings("unused")
    public void setPostLayoutListener(@Nullable final CustomCarouselLayoutManager.PostLayoutListener postLayoutListener) {
        mViewPostLayout = postLayoutListener;
        requestLayout();
    }
    protected int getOffsetCenterView() {
        return Math.round(getCurrentScrollPosition()) * getScrollItemSize() - mLayoutHelper.mScrollOffset;
    }
    public int getOrientation() {
        return mOrientation;
    }

    @CallSuper
    @SuppressWarnings("unused")
    public void setMaxVisibleItems(final int maxVisibleItems) {
        if (0 > maxVisibleItems) {
            throw new IllegalArgumentException("maxVisibleItems can't be less then 0");
        }
        mLayoutHelper.mMaxVisibleItems = maxVisibleItems;
        requestLayout();
    }

    @SuppressWarnings("unused")
    public int getMaxVisibleItems() {
        return mLayoutHelper.mMaxVisibleItems;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollHorizontally() {
        return 0 != getChildCount() && HORIZONTAL == mOrientation;
    }

    @Override
    public boolean canScrollVertically() {
        return 0 != getChildCount() && VERTICAL == mOrientation;
    }


    @SuppressWarnings("RefusedBequest")
    @Override
    public void scrollToPosition(final int position) {
        if (0 > position) {
            throw new IllegalArgumentException("position can't be less then 0. position is : " + position);
        }
        mPendingScrollPosition = position;
        requestLayout();
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    public void smoothScrollToPosition(@NonNull final RecyclerView recyclerView, @NonNull final RecyclerView.State state, final int position) {
        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public int calculateDyToMakeVisible(final View view, final int snapPreference) {
                if (!canScrollVertically()) {
                    return 0;
                }

                return getOffsetForCurrentView(view);
            }

            @Override
            public int calculateDxToMakeVisible(final View view, final int snapPreference) {
                if (!canScrollHorizontally()) {
                    return 0;
                }
                return getOffsetForCurrentView(view);
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    @Override
    @Nullable
    public PointF computeScrollVectorForPosition(final int targetPosition) {
        if (0 == getChildCount()) {
            return null;
        }
        final float directionDistance = getScrollDirection(targetPosition);
        final int direction = (int) -Math.signum(directionDistance);

        if (HORIZONTAL == mOrientation) {
            return new PointF(direction, 0);
        } else {
            return new PointF(0, direction);
        }
    }

    private float getScrollDirection(final int targetPosition) {
        final float currentScrollPosition = makeScrollPositionInRange0ToCount(getCurrentScrollPosition(), mItemsCount);

        if (mCircleLayout) {
            final float t1 = currentScrollPosition - targetPosition;
            final float t2 = Math.abs(t1) - mItemsCount;
            if (Math.abs(t1) > Math.abs(t2)) {
                return Math.signum(t1) * t2;
            } else {
                return t1;
            }
        } else {
            return currentScrollPosition - targetPosition;
        }
    }

    @Override
    public int scrollVerticallyBy(final int dy, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (HORIZONTAL == mOrientation) {
            return 0;
        }
        return scrollBy(dy, recycler, state);
    }

    @Override
    public int scrollHorizontallyBy(final int dx, final RecyclerView.Recycler recycler, final RecyclerView.State state) {
        if (VERTICAL == mOrientation) {
            return 0;
        }
        return scrollBy(dx, recycler, state);
    }

    @CallSuper
    protected int scrollBy(final int diff, @NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (null == mDecoratedChildWidth || null == mDecoratedChildHeight) {
            return 0;
        }
        if (0 == getChildCount() || 0 == diff) {
            return 0;
        }
        final int resultScroll;
        if (mCircleLayout) {
            resultScroll = diff;

            mLayoutHelper.mScrollOffset += resultScroll;

            final int maxOffset = getScrollItemSize() * mItemsCount;
            while (0 > mLayoutHelper.mScrollOffset) {
                mLayoutHelper.mScrollOffset += maxOffset;
            }
            while (mLayoutHelper.mScrollOffset > maxOffset) {
                mLayoutHelper.mScrollOffset -= maxOffset;
            }

            mLayoutHelper.mScrollOffset -= resultScroll;
        } else {
            final int maxOffset = getMaxScrollOffset();

            if (0 > mLayoutHelper.mScrollOffset + diff) {
                resultScroll = -mLayoutHelper.mScrollOffset; //to make it 0
            } else if (mLayoutHelper.mScrollOffset + diff > maxOffset) {
                resultScroll = maxOffset - mLayoutHelper.mScrollOffset; //to make it maxOffset
            } else {
                resultScroll = diff;
            }
        }
        if (0 != resultScroll) {
            mLayoutHelper.mScrollOffset += resultScroll;
            fillData(recycler, state);
        }
        return resultScroll;
    }

    @Override
    public void onMeasure(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state, final int widthSpec, final int heightSpec) {
        mDecoratedChildSizeInvalid = true;

        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onAdapterChanged(final RecyclerView.Adapter oldAdapter, final RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);

        removeAllViews();
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    @CallSuper
    public void onLayoutChildren(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        if (0 == state.getItemCount()) {
            removeAndRecycleAllViews(recycler);
            selectItemCenterPosition(INVALID_POSITION);
            return;
        }

        detachAndScrapAttachedViews(recycler);

        if (null == mDecoratedChildWidth || mDecoratedChildSizeInvalid) {
            final List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();

            final boolean shouldRecycle;
            final View view;
            if (scrapList.isEmpty()) {
                shouldRecycle = true;
                final int itemsCount = state.getItemCount();
                view = recycler.getViewForPosition(
                        mPendingScrollPosition == INVALID_POSITION ?
                                0 :
                                Math.max(0, Math.min(itemsCount - 1, mPendingScrollPosition))
                );
                addView(view);
            } else {
                shouldRecycle = false;
                view = scrapList.get(0).itemView;
            }
            measureChildWithMargins(view, 0, 0);

            final int decoratedChildWidth = getDecoratedMeasuredWidth(view);
            final int decoratedChildHeight = getDecoratedMeasuredHeight(view);
            if (shouldRecycle) {
                detachAndScrapView(view, recycler);
            }

            if (null != mDecoratedChildWidth && (mDecoratedChildWidth != decoratedChildWidth || mDecoratedChildHeight != decoratedChildHeight)) {
                if (INVALID_POSITION == mPendingScrollPosition && null == mPendingCarouselSavedState) {
                    mPendingScrollPosition = mCenterItemPosition;
                }
            }

            mDecoratedChildWidth = decoratedChildWidth;
            mDecoratedChildHeight = decoratedChildHeight;
            mDecoratedChildSizeInvalid = false;
        }

        if (INVALID_POSITION != mPendingScrollPosition) {
            final int itemsCount = state.getItemCount();
            mPendingScrollPosition = 0 == itemsCount ? INVALID_POSITION : Math.max(0, Math.min(itemsCount - 1, mPendingScrollPosition));
        }
        if (INVALID_POSITION != mPendingScrollPosition) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mPendingScrollPosition, state);
            mPendingScrollPosition = INVALID_POSITION;
            mPendingCarouselSavedState = null;
        } else if (null != mPendingCarouselSavedState) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mPendingCarouselSavedState.mCenterItemPosition, state);
            mPendingCarouselSavedState = null;
        } else if (state.didStructureChange() && INVALID_POSITION != mCenterItemPosition) {
            mLayoutHelper.mScrollOffset = calculateScrollForSelectingPosition(mCenterItemPosition, state);
        }

        fillData(recycler, state);
    }

    private int calculateScrollForSelectingPosition(final int itemPosition, final RecyclerView.State state) {
        if (itemPosition == INVALID_POSITION) {
            return 0;
        }

        final int fixedItemPosition = itemPosition < state.getItemCount() ? itemPosition : state.getItemCount() - 1;
        return fixedItemPosition * (VERTICAL == mOrientation ? mDecoratedChildHeight : mDecoratedChildWidth);
    }

    private void fillData(@NonNull final RecyclerView.Recycler recycler, @NonNull final RecyclerView.State state) {
        final float currentScrollPosition = getCurrentScrollPosition();

        generateLayoutOrder(currentScrollPosition, state);
        detachAndScrapAttachedViews(recycler);
        recyclerOldViews(recycler);

        final int width = getWidthNoPadding();
        final int height = getHeightNoPadding();
        if (VERTICAL == mOrientation) {
            fillDataVertical(recycler, width, height);
        } else {
            fillDataHorizontal(recycler, width, height);
        }

        recycler.clear();

        detectOnItemSelectionChanged(currentScrollPosition, state);
    }

    private void detectOnItemSelectionChanged(final float currentScrollPosition, final RecyclerView.State state) {
        final float absCurrentScrollPosition = makeScrollPositionInRange0ToCount(currentScrollPosition, state.getItemCount());
        final int centerItem = Math.round(absCurrentScrollPosition);

        if (mCenterItemPosition != centerItem) {
            mCenterItemPosition = centerItem;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    selectItemCenterPosition(centerItem);
                }
            });
        }
    }

    private void selectItemCenterPosition(final int centerItem) {
        for (final CustomCarouselLayoutManager.OnCenterItemSelectionListener onCenterItemSelectionListener : mOnCenterItemSelectionListeners) {
            onCenterItemSelectionListener.onCenterItemChanged(centerItem);
        }
    }

    private void fillDataVertical(final RecyclerView.Recycler recycler, final int width, final int height) {
        final int start = (width - mDecoratedChildWidth) / 2;
        final int end = start + mDecoratedChildWidth;

        final int centerViewTop = (height - mDecoratedChildHeight) / 2;

        for (int i = 0, count = mLayoutHelper.mLayoutOrder.length; i < count; ++i) {
            final CustomCarouselLayoutManager.LayoutOrder layoutOrder = mLayoutHelper.mLayoutOrder[i];
            final int offset = getCardOffsetByPositionDiff(layoutOrder.mItemPositionDiff);
            final int top = centerViewTop + offset;
            final int bottom = top + mDecoratedChildHeight;
            fillChildItem(start, top, end, bottom, layoutOrder, recycler, i);
        }
    }

    private void fillDataHorizontal(final RecyclerView.Recycler recycler, final int width, final int height) {
        final int top = (height - mDecoratedChildHeight) / 2;
        final int bottom = top + mDecoratedChildHeight;

        final int centerViewStart = (width - mDecoratedChildWidth) / 2;

        for (int i = 0, count = mLayoutHelper.mLayoutOrder.length; i < count; ++i) {
            final CustomCarouselLayoutManager.LayoutOrder layoutOrder = mLayoutHelper.mLayoutOrder[i];
            final int offset = getCardOffsetByPositionDiff(layoutOrder.mItemPositionDiff);
            final int start = centerViewStart + offset;
            final int end = start + mDecoratedChildWidth;
            fillChildItem(start, top, end, bottom, layoutOrder, recycler, i);
        }
    }


    @SuppressWarnings("MethodWithTooManyParameters")
    private void fillChildItem(final int start, final int top, final int end, final int bottom, @NonNull final CustomCarouselLayoutManager.LayoutOrder layoutOrder, @NonNull final RecyclerView.Recycler recycler, final int i) {
        final View view = bindChild(layoutOrder.mItemAdapterPosition, recycler);
        ViewCompat.setElevation(view, i);
        ItemTransform transformation = null;
        if (null != mViewPostLayout) {
            transformation = mViewPostLayout.transformChild(view, layoutOrder.mItemPositionDiff, mOrientation, layoutOrder.mItemAdapterPosition);
        }
        if (null == transformation) {
            view.layout(start, top, end, bottom);
        } else {
            view.layout(Math.round(start + transformation.mTranslationX), Math.round(top + transformation.mTranslationY),
                    Math.round(end + transformation.mTranslationX), Math.round(bottom + transformation.mTranslationY));

            view.setScaleX(transformation.mScaleX);
            view.setScaleY(transformation.mScaleY);
        }
    }
    private float getCurrentScrollPosition() {
        final int fullScrollSize = getMaxScrollOffset();
        if (0 == fullScrollSize) {
            return 0;
        }
        return 1.0f * mLayoutHelper.mScrollOffset / getScrollItemSize();
    }
    private int getMaxScrollOffset() {
        return getScrollItemSize() * (mItemsCount - 1);
    }
    
    private void generateLayoutOrder(final float currentScrollPosition, @NonNull final RecyclerView.State state) {
        mItemsCount = state.getItemCount();
        final float absCurrentScrollPosition = makeScrollPositionInRange0ToCount(currentScrollPosition, mItemsCount);
        final int centerItem = Math.round(absCurrentScrollPosition);

        if (mCircleLayout && 1 < mItemsCount) {
            final int layoutCount = Math.min(mLayoutHelper.mMaxVisibleItems * 2 + 1, mItemsCount);

            mLayoutHelper.initLayoutOrder(layoutCount);

            final int countLayoutHalf = layoutCount / 2;
            for (int i = 1; i <= countLayoutHalf; ++i) {
                final int position = Math.round(absCurrentScrollPosition - i + mItemsCount) % mItemsCount;
                mLayoutHelper.setLayoutOrder(countLayoutHalf - i, position, centerItem - absCurrentScrollPosition - i);
            }
            for (int i = layoutCount - 1; i >= countLayoutHalf + 1; --i) {
                final int position = Math.round(absCurrentScrollPosition - i + layoutCount) % mItemsCount;
                mLayoutHelper.setLayoutOrder(i - 1, position, centerItem - absCurrentScrollPosition + layoutCount - i);
            }
            mLayoutHelper.setLayoutOrder(layoutCount - 1, centerItem, centerItem - absCurrentScrollPosition);

        } else {
            final int firstVisible = Math.max(centerItem - mLayoutHelper.mMaxVisibleItems, 0);
            final int lastVisible = Math.min(centerItem + mLayoutHelper.mMaxVisibleItems, mItemsCount - 1);
            final int layoutCount = lastVisible - firstVisible + 1;

            mLayoutHelper.initLayoutOrder(layoutCount);

            for (int i = firstVisible; i <= lastVisible; ++i) {
                if (i == centerItem) {
                    mLayoutHelper.setLayoutOrder(layoutCount - 1, i, i - absCurrentScrollPosition);
                } else if (i < centerItem) {
                    mLayoutHelper.setLayoutOrder(i - firstVisible, i, i - absCurrentScrollPosition);
                } else {
                    mLayoutHelper.setLayoutOrder(layoutCount - (i - centerItem) - 1, i, i - absCurrentScrollPosition);
                }
            }
        }
    }

    public int getWidthNoPadding() {
        return getWidth() - getPaddingStart() - getPaddingEnd();
    }

    public int getHeightNoPadding() {
        return getHeight() - getPaddingEnd() - getPaddingStart();
    }

    private View bindChild(final int position, @NonNull final RecyclerView.Recycler recycler) {
        final View view = recycler.getViewForPosition(position);

        addView(view);
        measureChildWithMargins(view, 0, 0);

        return view;
    }

    private void recyclerOldViews(final RecyclerView.Recycler recycler) {
        for (RecyclerView.ViewHolder viewHolder : new ArrayList<>(recycler.getScrapList())) {
            int adapterPosition = viewHolder.getAdapterPosition();
            boolean found = false;
            for (CustomCarouselLayoutManager.LayoutOrder layoutOrder : mLayoutHelper.mLayoutOrder) {
                if (layoutOrder.mItemAdapterPosition == adapterPosition) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                recycler.recycleView(viewHolder.itemView);
            }
        }
    }
    
    protected int getCardOffsetByPositionDiff(final float itemPositionDiff) {
        final double smoothPosition = convertItemPositionDiffToSmoothPositionDiff(itemPositionDiff);

        final int dimenDiff;
        if (VERTICAL == mOrientation) {
            dimenDiff = (getHeightNoPadding() - mDecoratedChildHeight) / 2;
        } else {
            dimenDiff = (getWidthNoPadding() - mDecoratedChildWidth) / 2;
        }
        return (int) Math.round(Math.signum(itemPositionDiff) * dimenDiff * smoothPosition);
    }
    @SuppressWarnings({"MagicNumber", "InstanceMethodNamingConvention"})
    protected double convertItemPositionDiffToSmoothPositionDiff(final float itemPositionDiff) {
        final float absIemPositionDiff = Math.abs(itemPositionDiff);

        if (absIemPositionDiff > StrictMath.pow(1.0f / mLayoutHelper.mMaxVisibleItems, 1.0f / 3)) {
            return StrictMath.pow(absIemPositionDiff / mLayoutHelper.mMaxVisibleItems, 1 / 2.0f);
        } else {
            return StrictMath.pow(absIemPositionDiff, 2.0f);
        }
    }

   
    protected int getScrollItemSize() {
        if (VERTICAL == mOrientation) {
            return mDecoratedChildHeight;
        } else {
            return mDecoratedChildWidth;
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        if (null != mPendingCarouselSavedState) {
            return new CustomCarouselLayoutManager.CarouselSavedState(mPendingCarouselSavedState);
        }
        final CustomCarouselLayoutManager.CarouselSavedState savedState = new CustomCarouselLayoutManager.CarouselSavedState(super.onSaveInstanceState());
        savedState.mCenterItemPosition = mCenterItemPosition;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof CustomCarouselLayoutManager.CarouselSavedState) {
            mPendingCarouselSavedState = (CustomCarouselLayoutManager.CarouselSavedState) state;

            super.onRestoreInstanceState(mPendingCarouselSavedState.mSuperState);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    protected int getOffsetForCurrentView(@NonNull final View view) {
        final int targetPosition = getPosition(view);
        final float directionDistance = getScrollDirection(targetPosition);

        return Math.round(directionDistance * getScrollItemSize());
    }
    
    private static float makeScrollPositionInRange0ToCount(final float currentScrollPosition, final int count) {
        float absCurrentScrollPosition = currentScrollPosition;
        while (0 > absCurrentScrollPosition) {
            absCurrentScrollPosition += count;
        }
        while (Math.round(absCurrentScrollPosition) >= count) {
            absCurrentScrollPosition -= count;
        }
        return absCurrentScrollPosition;
    }

    public interface OnCenterItemSelectionListener {
        void onCenterItemChanged(final int adapterPosition);
    }

    @SuppressWarnings("InterfaceNeverImplemented")
    public abstract static class PostLayoutListener {

        public ItemTransform transformChild(
                @NonNull final View child,
                final float itemPositionToCenterDiff,
                final int orientation,
                final int itemPositionInAdapter
        ) {
            return transformChild(child, itemPositionToCenterDiff, orientation);
        }

        public ItemTransform transformChild(
                @NonNull final View child,
                final float itemPositionToCenterDiff,
                final int orientation
        ) {
            throw new IllegalStateException("at least one transformChild should be implemented");
        }
    }
    
    private static class LayoutHelper {

        private int mMaxVisibleItems;

        private int mScrollOffset;

        private CustomCarouselLayoutManager.LayoutOrder[] mLayoutOrder;

        private final List<WeakReference<CustomCarouselLayoutManager.LayoutOrder>> mReusedItems = new ArrayList<>();

        LayoutHelper(final int maxVisibleItems) {
            mMaxVisibleItems = maxVisibleItems;
        }
        void initLayoutOrder(final int layoutCount) {
            if (null == mLayoutOrder || mLayoutOrder.length != layoutCount) {
                if (null != mLayoutOrder) {
                    recycleItems(mLayoutOrder);
                }
                mLayoutOrder = new CustomCarouselLayoutManager.LayoutOrder[layoutCount];
                fillLayoutOrder();
            }
        }
        
        void setLayoutOrder(final int arrayPosition, final int itemAdapterPosition, final float itemPositionDiff) {
            final CustomCarouselLayoutManager.LayoutOrder item = mLayoutOrder[arrayPosition];
            item.mItemAdapterPosition = itemAdapterPosition;
            item.mItemPositionDiff = itemPositionDiff;
        }

        @SuppressWarnings("VariableArgumentMethod")
        private void recycleItems(@NonNull final CustomCarouselLayoutManager.LayoutOrder... layoutOrders) {
            for (final CustomCarouselLayoutManager.LayoutOrder layoutOrder : layoutOrders) {
                mReusedItems.add(new WeakReference<>(layoutOrder));
            }
        }

        private void fillLayoutOrder() {
            for (int i = 0, length = mLayoutOrder.length; i < length; ++i) {
                if (null == mLayoutOrder[i]) {
                    mLayoutOrder[i] = createLayoutOrder();
                }
            }
        }
        private CustomCarouselLayoutManager.LayoutOrder createLayoutOrder() {
            final Iterator<WeakReference<CustomCarouselLayoutManager.LayoutOrder>> iterator = mReusedItems.iterator();
            while (iterator.hasNext()) {
                final WeakReference<CustomCarouselLayoutManager.LayoutOrder> layoutOrderWeakReference = iterator.next();
                final CustomCarouselLayoutManager.LayoutOrder layoutOrder = layoutOrderWeakReference.get();
                iterator.remove();
                if (null != layoutOrder) {
                    return layoutOrder;
                }
            }
            return new CustomCarouselLayoutManager.LayoutOrder();
        }
    }
    private static class LayoutOrder {
        
        private int mItemAdapterPosition;
        private float mItemPositionDiff;
    }

    protected static class CarouselSavedState implements Parcelable {

        private final Parcelable mSuperState;
        private int mCenterItemPosition;

        protected CarouselSavedState(@Nullable final Parcelable superState) {
            mSuperState = superState;
        }

        private CarouselSavedState(@NonNull final Parcel in) {
            mSuperState = in.readParcelable(Parcelable.class.getClassLoader());
            mCenterItemPosition = in.readInt();
        }

        protected CarouselSavedState(@NonNull final CustomCarouselLayoutManager.CarouselSavedState other) {
            mSuperState = other.mSuperState;
            mCenterItemPosition = other.mCenterItemPosition;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel parcel, final int i) {
            parcel.writeParcelable(mSuperState, i);
            parcel.writeInt(mCenterItemPosition);
        }

        public static final Parcelable.Creator<CustomCarouselLayoutManager.CarouselSavedState> CREATOR
                = new Parcelable.Creator<CustomCarouselLayoutManager.CarouselSavedState>() {
            @Override
            public CustomCarouselLayoutManager.CarouselSavedState createFromParcel(final Parcel parcel) {
                return new CustomCarouselLayoutManager.CarouselSavedState(parcel);
            }

            @Override
            public CustomCarouselLayoutManager.CarouselSavedState[] newArray(final int i) {
                return new CustomCarouselLayoutManager.CarouselSavedState[i];
            }
        };
    }   
}
