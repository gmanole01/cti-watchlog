package manolegeorge.watchlog.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.appbar.CollapsingToolbarLayout;

public class SquareCollapsingToolbarLayout extends CollapsingToolbarLayout {
	public SquareCollapsingToolbarLayout(Context context) {
		super(context);
	}

	public SquareCollapsingToolbarLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int width = getMeasuredWidth();
		setMeasuredDimension(width, width);
	}

}
