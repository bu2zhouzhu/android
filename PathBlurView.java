public class PathBlurView extends View {

    private Rect drawableRect;
    private Rect viewRect;

    private Bitmap mImageBitmap;

    private Canvas mMaskCanvas;
    private Paint mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap mMaskBitmap;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path mPath = new Path();

    public PathBlurView(Context context) {
        this(context, null);
    }

    public PathBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mMaskPaint.setMaskFilter(new BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL));
        mMaskPaint.setStyle(Paint.Style.STROKE);
        mMaskPaint.setStrokeWidth(20);
        mMaskPaint.setStrokeCap(Paint.Cap.ROUND);
        mMaskPaint.setColor(Color.BLUE);

        mMaskPaint.setAlpha(50);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            viewRect = new Rect(0, 0, w, h);
            mMaskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mMaskCanvas = new Canvas(mMaskBitmap);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mImageBitmap == null) {
            return;
        }
        canvas.drawBitmap(mImageBitmap, drawableRect, viewRect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(mMaskBitmap, drawableRect, viewRect, paint);
        paint.setXfermode(null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void touchStart(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
    }

    private void touchMove(float x, float y) {
        mPath.lineTo(x, y);
        mMaskCanvas.drawPath(mPath, mMaskPaint);
    }

    public void setBitmap(Bitmap bitmap) {
        mImageBitmap = bitmap;
        drawableRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        invalidate();
    }
}
