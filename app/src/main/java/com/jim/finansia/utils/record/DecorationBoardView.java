package com.jim.finansia.utils.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.jim.finansia.R;
import com.jim.finansia.database.BoardButton;
import com.jim.finansia.database.BoardButtonDao;
import com.jim.finansia.database.CreditDetials;
import com.jim.finansia.database.RootCategory;
import com.jim.finansia.utils.PocketAccounterGeneral;

import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class DecorationBoardView extends BaseBoardView {
    private Context context;
    private RectF workscpace;
    private float beginMargin, shadowSide, betweenButtonsMargin, whiteSide, gradientSide, oneDp;
    private float buttonSideRatio = 0.20427112f, betweenButtonsRatio = 0.0362117f;
    private Bitmap bitmap;
    private BitmapFactory.Options options;
    protected List<ABoardButton> buttons;
    protected boolean manualAlphaSetted = false;
    private final int DRAWING_PROCESS = 0, DRAWN = 1; //states
    private int drawState = DRAWN; // drawing state
    protected int alpha = 0xFF, fullAlpha = 0xFF; //alpha control fields
    private int frames = 20, elapsed = 0; //frames and elapsed time
    private long interim = 8; //sleep
    private int position = 0;
    private boolean longPressed = false, drawIcons = true;
    private int active, not_active, indicatorFrame;
    private String debtBorrowIcon = "icons_30";
    private List<BoardButton> boardButtons;
    private List<RootCategory> categories;
    private List<CreditDetials> credits;
    private boolean drawIndicator = true;

    public DecorationBoardView(Context context, int table) {
        super(context, table);
        this.context = context;
        init();
    }

    public DecorationBoardView(Context context, AttributeSet attrs, int table) {
        super(context, attrs, table);
        this.context = context;
        init();
    }

    public DecorationBoardView(Context context, AttributeSet attrs, int defStyleAttr, int table) {
        super(context, attrs, defStyleAttr, table);
        this.context = context;
        init();
    }

    public DecorationBoardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int table) {
        super(context, attrs, defStyleAttr, defStyleRes, table);
        this.context = context;
        init();
    }

    public void initCache(int cacheId, int bitmapResourceId, int side) {
        if (dataCache.getElements().get(cacheId) == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), bitmapResourceId, options);
            bitmap = Bitmap.createScaledBitmap(bitmap, side, side, false);
            dataCache.getElements().put(cacheId, bitmap);
        }
    }


    private void init() {
        active = ContextCompat.getColor(getContext(), R.color.active_circle);
        not_active = ContextCompat.getColor(getContext(), R.color.not_active_circle);
        indicatorFrame = ContextCompat.getColor(getContext(), R.color.indicators_frame);
        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int width = getResources().getDisplayMetrics().widthPixels;
        beginMargin = width * 0.0371402f;
        if (table == PocketAccounterGeneral.EXPENSE)
            workscpace = new RectF(beginMargin, beginMargin, width - beginMargin, width);
        else
            workscpace = new RectF(beginMargin, beginMargin, width - beginMargin, width*buttonSideRatio);
        betweenButtonsMargin = workscpace.width() * betweenButtonsRatio;
        whiteSide = width * buttonSideRatio;
        shadowSide = whiteSide / 0.7f;
        oneDp = getResources().getDimension(R.dimen.one_dp);
        gradientSide = whiteSide - oneDp;
        initForButtons();
        loadDrawingElements();
        initButtons();
    }

    private void initForButtons() {
        if (boardButtons == null)
            boardButtons = daoSession.getBoardButtonDao().loadAll();
        if (categories == null)
            categories = daoSession.getRootCategoryDao().loadAll();
        if (credits == null)
            credits = daoSession.getCreditDetialsDao().loadAll();
    }

    public void loadDrawingElements() {
        initCache(LEFT_TOP_SHAPE_SHADOW, R.drawable.shadow_left_top, (int) shadowSide);
        initCache(LEFT_TOP_SHAPE_WHITE, R.drawable.left_top_shape_white, (int) whiteSide);
        initCache(LEFT_TOP_SHAPE_GRADIENT, R.drawable.left_top_shape_gradient, (int) gradientSide);
        initCache(RIGHT_TOP_SHAPE_SHADOW, R.drawable.shadow_right_top, (int) shadowSide);
        initCache(RIGHT_TOP_SHAPE_WHITE, R.drawable.right_top_shape_white, (int) whiteSide);
        initCache(RIGHT_TOP_SHAPE_GRADIENT, R.drawable.right_top_shape_gradient, (int) gradientSide);
        initCache(LEFT_BOTTOM_SHAPE_SHADOW, R.drawable.shadow_left_bottom, (int) shadowSide);
        initCache(LEFT_BOTTOM_SHAPE_WHITE, R.drawable.left_bottom_shape_white, (int) whiteSide);
        initCache(LEFT_BOTTOM_SHAPE_GRADIENT, R.drawable.left_bottom_shape_gradient, (int) gradientSide);
        initCache(RIGHT_BOTTOM_SHAPE_SHADOW, R.drawable.shadow_right_bottom, (int) shadowSide);
        initCache(RIGHT_BOTTOM_SHAPE_WHITE, R.drawable.right_bottom_shape_white, (int) whiteSide);
        initCache(RIGHT_BOTTOM_SHAPE_GRADIENT, R.drawable.right_bottom_shape_gradient, (int) gradientSide);
        initCache(SIMPLE_SHADOW, R.drawable.shadow_circular, (int) shadowSide);
        initCache(SIMPLE_WHITE, R.drawable.circle_white, (int) whiteSide);
        initCache(SIMPLE_GRADIENT, R.drawable.circle_gradient, (int) gradientSide);
    }

    public void press(int position) {
        this.position = position;
        new Thread(new Runnable() {
            @Override
            public void run() {
                drawState = DRAWING_PROCESS;
                while (elapsed <= frames) {
                    try {
                        Thread.sleep(interim);
                        if (elapsed < frames / 2) {
                            alpha = (int) (fullAlpha * (1 - (float) elapsed / frames));
                        } else {
                            alpha = (int) (fullAlpha * (float) elapsed / frames);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        postInvalidate();
                        elapsed++;
                    }
                }
                elapsed = 0;
                drawState = DRAWN;
            }
        }).start();
    }

    public void longPress(int position) {
        this.position = position;
        longPressed = true;
        invalidate();
    }

    public void releasePress() {
        longPressed = false;
        invalidate();
    }

    protected void initButtons() {
        buttons = new ArrayList<>();
        List<BoardButton> allButtons = daoSession
                .getBoardButtonDao()
                .queryBuilder()
                .where(BoardButtonDao.Properties.Table.eq(table))
                .build()
                .list();
        int pageElementsCount = table == PocketAccounterGeneral.EXPENSE ? EXPENSE_BUTTONS_COUNT_PER_PAGE : INCOME_BUTTONS_COUNT_PER_PAGE;
        float left = 0.0f, top = 0.0f, right = 0.0f, bottom = 0.0f;
        for (int pos = 0; pos < pageElementsCount; pos++) {
            if (pos % 4 == 0) {
                left = workscpace.left;
                top = workscpace.top + (whiteSide + betweenButtonsMargin) * pos / 4;
                right = left + whiteSide;
                bottom = top + whiteSide;
            } else {
                left = workscpace.left + (whiteSide + betweenButtonsMargin) * (pos % 4);
                right = left + whiteSide;
            }
            RectF container = new RectF(left, top, right, bottom);
            Long categoryId = null;
            for (BoardButton boardButton : allButtons) {
                if (boardButton.getPos() == currentPage*pageElementsCount + pos) {
                    categoryId = boardButton.getId();
                    break;
                }
            }
            ABoardButton aBoardButton = new ABoardButton().builder()
                    .container(container)
                    .category(categoryId)
                    .table(table)
                    .position(currentPage*pageElementsCount+pos)
                    .build();
            buttons.add(aBoardButton);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawButtons(canvas);
        if (setCount > 1 && drawIndicator)
            drawIndicator(canvas);
    }

    public void setDrawIndicator(boolean drawIndicator) {
        this.drawIndicator = drawIndicator;
        invalidate();
    }

    private void drawButtons(Canvas canvas) {
        Paint shadowsPaint = new Paint();
        shadowsPaint.setAntiAlias(true);
        Paint whitePaint = new Paint();
        whitePaint.setAntiAlias(true);
        Bitmap shadow = null, white = null, gradient = null, icon = null;
        if(dataCache.getElements().size() == 0){
            loadDrawingElements();
        }
        for (ABoardButton button : buttons) {
            if (drawState == DRAWING_PROCESS && buttons.indexOf(button) == position) {
                shadowsPaint.setAlpha(alpha);
            } else if (longPressed && buttons.indexOf(button) == position){
                shadowsPaint.setAlpha(fullAlpha/2);
            }
            else if (manualAlphaSetted) {
                shadowsPaint.setAlpha(alpha);
                whitePaint.setAlpha(alpha);
            }
            else {
                shadowsPaint.setAlpha(fullAlpha);
                whitePaint.setAlpha(fullAlpha);
            }
            if (table == PocketAccounterGeneral.EXPENSE) {
                switch (button.getPosition()%EXPENSE_BUTTONS_COUNT_PER_PAGE) {
                    case 0:
                        shadow = dataCache.getElements().get(LEFT_TOP_SHAPE_SHADOW);
                        white = dataCache.getElements().get(LEFT_TOP_SHAPE_WHITE);
                        gradient = dataCache.getElements().get(LEFT_TOP_SHAPE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                    case 1:
                    case 2:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 13:
                    case 14:
                        shadow = dataCache.getElements().get(SIMPLE_SHADOW);
                        white = dataCache.getElements().get(SIMPLE_WHITE);
                        gradient = dataCache.getElements().get(SIMPLE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                    case 3:
                        shadow = dataCache.getElements().get(RIGHT_TOP_SHAPE_SHADOW);
                        white = dataCache.getElements().get(RIGHT_TOP_SHAPE_WHITE);
                        gradient = dataCache.getElements().get(RIGHT_TOP_SHAPE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                    case 12:
                        shadow = dataCache.getElements().get(LEFT_BOTTOM_SHAPE_SHADOW);
                        white = dataCache.getElements().get(LEFT_BOTTOM_SHAPE_WHITE);
                        gradient = dataCache.getElements().get(LEFT_BOTTOM_SHAPE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                    case 15:
                        shadow = dataCache.getElements().get(RIGHT_BOTTOM_SHAPE_SHADOW);
                        white = dataCache.getElements().get(RIGHT_BOTTOM_SHAPE_WHITE);
                        gradient = dataCache.getElements().get(RIGHT_BOTTOM_SHAPE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                }
            } else {
                switch (button.getPosition()%INCOME_BUTTONS_COUNT_PER_PAGE) {
                    case 0:
                        shadow = dataCache.getElements().get(LEFT_TOP_SHAPE_SHADOW);
                        white = dataCache.getElements().get(LEFT_TOP_SHAPE_WHITE);
                        gradient = dataCache.getElements().get(LEFT_TOP_SHAPE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                    case 1:
                    case 2:
                        shadow = dataCache.getElements().get(SIMPLE_SHADOW);
                        white = dataCache.getElements().get(SIMPLE_WHITE);
                        gradient = dataCache.getElements().get(SIMPLE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                    case 3:
                        shadow = dataCache.getElements().get(RIGHT_BOTTOM_SHAPE_SHADOW);
                        white = dataCache.getElements().get(RIGHT_BOTTOM_SHAPE_WHITE);
                        gradient = dataCache.getElements().get(RIGHT_BOTTOM_SHAPE_GRADIENT);
                        icon = dataCache.getBoardBitmapsCache().get(button.getButtonId());
                        break;
                }
            }

            canvas.drawBitmap(shadow, button.getContainer().centerX() - shadow.getWidth() / 2,
                    button.getContainer().centerY() - shadow.getHeight() / 2, shadowsPaint);
            canvas.drawBitmap(white, button.getContainer().left, button.getContainer().top, whitePaint);
            canvas.drawBitmap(gradient, button.getContainer().centerX() - gradient.getWidth() / 2,
                    button.getContainer().centerY() - gradient.getHeight() / 2, shadowsPaint);
            canvas.drawBitmap(icon, button.getContainer().centerX() - icon.getWidth() / 2,
                    button.getContainer().centerY() - icon.getHeight() / 2, shadowsPaint);
        }
    }

    private String getIcon(int type, String id) {
        String iconPath = "";
        switch (type) {
            case PocketAccounterGeneral.CATEGORY:
                for (RootCategory category : categories)
                    if (category.getId().equals(id))
                        iconPath = category.getIcon();
                break;
            case PocketAccounterGeneral.CREDIT:
                for (CreditDetials creditDetials : credits)
                    if (Long.toString(creditDetials.getMyCredit_id()).equals(id))
                        iconPath = creditDetials.getIcon_ID();
                break;
        }
        return iconPath;
    }

    private BoardButton getId(Long buttonId) {
        BoardButton result = null;
        for (BoardButton button : boardButtons) {
            if (button.getId() == buttonId) {
                result = button;
                break;
            }
        }
        return result;
    }

    private void drawIndicator(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(indicatorFrame);
        paint.setAlpha(0xFF/2);
        float margin = 5 * oneDp, radius = 2 * oneDp;
        PointF beginFocus = new PointF(workscpace.centerX(), getHeight());
        float length = 2 * radius * setCount + (setCount+1)*margin;
        float height = 6 * radius;
        RectF indicatorContainer = new RectF(beginFocus.x - length/2, beginFocus.y - height, beginFocus.x + length/2, beginFocus.y);
        Path path = new Path();
        path.moveTo(indicatorContainer.left, indicatorContainer.bottom);
        path.lineTo(indicatorContainer.right, indicatorContainer.bottom);
        path.lineTo(indicatorContainer.right, indicatorContainer.top + height);
        path.arcTo(new RectF(indicatorContainer.right - height, indicatorContainer.top,
                indicatorContainer.right, indicatorContainer.top + height), 0.0f, -90.0f);
        path.lineTo(indicatorContainer.left + height, indicatorContainer.top);
        path.arcTo(new RectF(indicatorContainer.left, indicatorContainer.top, indicatorContainer.left + height, indicatorContainer.top + height),
                270.0f, -90.0f);
        path.close();
        canvas.drawPath(path, paint);
        float circleY = indicatorContainer.centerY();
        float beginX = indicatorContainer.left + margin;
        for (int i = 0; i < setCount; i++) {
            if (i == currentPage) paint.setColor(active);
            else paint.setColor(not_active);
            canvas.drawCircle(beginX + radius + i *(2*radius + margin) , circleY, radius, paint);
        }
    }
    class ABoardButton {
        private RectF container;
        private Long buttonId;
        private int table, position;
        private float iconSize = 0.35f * whiteSide;
        private Bitmap icon;

        public ABoardButton builder() {
            return this;
        }

        public ABoardButton container(RectF container) {
            this.container = container;
            iconSize = container.width() / 2.4f;
            return this;
        }

        public ABoardButton category(Long buttonId) {
            this.buttonId = buttonId;
            String categoryId = null;
            BoardButton boardButton = getId(buttonId);

            if (getId(buttonId) != null) {
                categoryId = boardButton.getCategoryId();
            }
            if (categoryId == null) {
                if (dataCache.getBoardBitmapsCache().get(buttonId) == null) {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.no_category, options);
                    icon = Bitmap.createScaledBitmap(icon, (int) iconSize, (int) iconSize, false);
                    dataCache.getBoardBitmapsCache().put(buttonId, icon);
                }
            } else {
                String iconPath = null;
                switch (boardButton.getType()) {
                    case PocketAccounterGeneral.CATEGORY:
                        iconPath = getIcon(PocketAccounterGeneral.CATEGORY, categoryId);
                        break;
                    case PocketAccounterGeneral.CREDIT:
                        iconPath = getIcon(PocketAccounterGeneral.CREDIT, categoryId);
                        break;
                    case PocketAccounterGeneral.DEBT_BORROW:
                        iconPath = debtBorrowIcon;
                        break;
                    case PocketAccounterGeneral.FUNCTION:
                        String[] operationIds = getResources().getStringArray(R.array.operation_ids);
                        String[] operationIcons = getResources().getStringArray(R.array.operation_icons);
                        for (int i = 0; i < operationIds.length; i++) {
                            if (operationIds[i].equals(categoryId)) {
                                iconPath = operationIcons[i];
                                break;
                            }
                        }
                        break;
                    case PocketAccounterGeneral.PAGE:
                        String[] pageIds = getResources().getStringArray(R.array.page_ids);
                        String[] pageIcons = getResources().getStringArray(R.array.page_icons);
                        for (int i = 0; i < pageIds.length; i++) {
                            if (pageIds[i].equals(categoryId)) {
                                iconPath = pageIcons[i];
                                break;
                            }
                        }
                        break;
                }
                if (dataCache.getBoardBitmapsCache().get(buttonId) == null) {
                    int resId = getResources().getIdentifier(iconPath, "drawable", getContext().getPackageName());
                    icon = BitmapFactory.decodeResource(getResources(), resId, options);
                    icon = Bitmap.createScaledBitmap(icon, (int) iconSize, (int) iconSize, false);
                    dataCache.getBoardBitmapsCache().put(buttonId, icon);
                }
            }
            return this;
        }

        public ABoardButton table(int table) {
            this.table = table;
            return this;
        }

        public ABoardButton position(int position) {
            this.position = position;
            return this;
        }
        public ABoardButton build() {
            return this;
        }
        public int getTable() {
            return this.table;
        }
        public int getPosition() {
            return position;
        }
        public Long getButtonId() {
            return buttonId;
        }
        public RectF getContainer() {
            return container;
        }
    }
}
