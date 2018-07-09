package lib.ui.chat;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * description: 监听键盘弹起关闭
 * created by kalu on 2018/6/13 17:21
 */
public final class EmojiLayout extends LinearLayout {

    // 表情按钮
    private final ImageView imageEmoji = new ImageView(getContext().getApplicationContext());
    // 发送按钮, 代替三种状态(弹起键盘, 收起键盘, 显示菜单)
    private final ImageView imageSend = new ImageView(getContext().getApplicationContext());
    // 输入框
    private final EditText editInput = new EditText(getContext().getApplicationContext());
    // 底部菜单
    private final LinearLayout menuLayout = new LinearLayout(getContext().getApplicationContext());
    // 菜单列表
    private final RecyclerView menuRecycler = new RecyclerView(getContext().getApplicationContext());

    private int maxHeight = 0, minHeight = 0, measuredHeight = 0;

    public EmojiLayout(Context context) {
        super(context);
    }

    public EmojiLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmojiLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (null == mOnInputChangeListener)
            return;

        final int newMeasuredHeight = getMeasuredHeight();

        if (minHeight == 0) {
            minHeight = newMeasuredHeight;
        }
        if (maxHeight == 0) {
            maxHeight = newMeasuredHeight;
        }

        maxHeight = Math.max(newMeasuredHeight, maxHeight);
        minHeight = Math.min(newMeasuredHeight, minHeight);
        if (minHeight == maxHeight)
            return;

        if (newMeasuredHeight < maxHeight) {

            if (measuredHeight != newMeasuredHeight) {
                mOnInputChangeListener.onInputOpen(maxHeight, minHeight);
                measuredHeight = newMeasuredHeight;

                if(getChildCount() == 3){
                    menuRecycler.setTag(maxHeight - minHeight);
                }else{
                    editInput.requestFocus();
                    // 改变图片
                    imageSend.setTag(R.drawable.ic_chat_open);
                    imageSend.setImageResource(R.drawable.ic_chat_open);
                    // 菜单高度
                    menuRecycler.setTag(maxHeight - minHeight);
                    final LinearLayout.LayoutParams layoutParams = (LayoutParams) menuRecycler.getLayoutParams();
                    layoutParams.height = 0;
                    menuRecycler.getAdapter().notifyDataSetChanged();
                }
            }
        } else {

            if (measuredHeight != newMeasuredHeight) {
                mOnInputChangeListener.onInputShut(maxHeight, minHeight);
                measuredHeight = newMeasuredHeight;

                if(getChildCount() == 3){
                    LinearLayout.LayoutParams parmars = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, maxHeight - minHeight);
                    menuRecycler.setLayoutParams(parmars);
                    menuRecycler.setTag(maxHeight - minHeight);
                    addView(menuRecycler);
                    presenter?.initListMenu(this@PatientChatActivity, patient_menu_list)

                }else{
                    // 改变图片
                    open.tag = R.drawable.ic_chat_menu_down
                    open.setImageResource(R.drawable.ic_chat_menu_down)
                    // 清除焦点
                    edit.clearFocus()
                    // 菜单高度
                    if (null == patient_menu_list.tag) return
                            patient_menu_list.layoutParams.height = patient_menu_list.tag as Int

                    patient_menu_list.adapter.notifyDataSetChanged()
                    // presenter?.setControlMenu(this@PatientChatActivity, patient_menu_list)
                    LogUtil.e("onInputShut", "maxHeight = $maxHeight, minHeight = $minHeight" + ", tag = " + patient_menu_list.tag)
                }
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final DisplayMetrics metrics = getContext().getApplicationContext().getResources().getDisplayMetrics();
        final int menuHeight = (int) (50 * metrics.density);
        final int childHeight = (int) (30 * metrics.density);
        final int margin = (int) (10 * metrics.density);
        // step1
        final LayoutParams params1 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, menuHeight);
        params1.gravity = Gravity.BOTTOM;
        menuLayout.setPadding(margin, 0, margin, 0);
        menuLayout.setLayoutParams(params1);
        menuLayout.setBackgroundColor(Color.GRAY);
        menuLayout.setGravity(Gravity.CENTER_VERTICAL);
        addView(menuLayout);
        // step2
        final LayoutParams params2 = new LayoutParams(childHeight, childHeight);
        imageEmoji.setLayoutParams(params2);
        imageEmoji.setImageResource(R.drawable.ic_chat_emoji);
        menuLayout.addView(imageEmoji);
        // step3
        final LayoutParams params3 = new LayoutParams(0, childHeight);
        params3.setMargins(margin, 0, margin, 0);
        params3.weight = 1;
        editInput.setLayoutParams(params3);
        editInput.setBackgroundDrawable(null);
        editInput.setBackgroundColor(Color.RED);
        menuLayout.addView(editInput);
        // step4
        final LayoutParams params4 = new LayoutParams(childHeight, childHeight);
        imageSend.setLayoutParams(params4);
        imageSend.setImageResource(R.drawable.ic_chat_menu_up);
        menuLayout.addView(imageSend);
        // step5
        imageSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //  LogUtil.e("key_open", "键盘隐藏, 菜单隐藏, 显示弹起图标")
                if (null == imageSend.getTag() || imageSend.getTag() == R.drawable.ic_chat_menu_up) {
                    // 改变图片
                    imageSend.setTag(R.drawable.ic_chat_open);
                    imageSend.setImageResource(R.drawable.ic_chat_open);
                    // 弹起键盘
                    toogleKeybord();
                }
                // LogUtil.e("key_open", "键盘显示, 菜单显示, 显示发送图标")
                else if (imageSend.getTag() == R.drawable.ic_chat_send) {
                    Toast.makeText(getContext(), "发送", Toast.LENGTH_SHORT).show();
                }
                // LogUtil.e("key_open", "键盘显示, 菜单显示, 显示更多图标")
                else if (imageSend.getTag() == R.drawable.ic_chat_open) {
                    imageSend.setTag(R.drawable.ic_chat_menu_down);
                    imageSend.setImageResource(R.drawable.ic_chat_menu_down);
                    toogleKeybord();
                }
                // LogUtil.e("key_open", "键盘隐藏, 菜单显示, 显示收起图标")
                else if (imageSend.getTag() == R.drawable.ic_chat_menu_down) {
                    imageSend.setTag(R.drawable.ic_chat_menu_up);
                    imageSend.setImageResource(R.drawable.ic_chat_menu_up);
                    // 菜单高度
                    if (null == menuRecycler.getTag()) return;
                    final LinearLayout.LayoutParams layoutParams = (LayoutParams) menuRecycler.getLayoutParams();
                    layoutParams.height = 0;
                    menuRecycler.getAdapter().notifyDataSetChanged();
                }
            }
        });
        // step6
        imageEmoji.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "表情", Toast.LENGTH_SHORT).show();
            }
        });
        // step7
        editInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (null != s && !s.toString().isEmpty()) {
                    imageSend.setTag(R.drawable.ic_chat_send);
                    imageSend.setImageResource(R.drawable.ic_chat_send);
                } else {
                    imageSend.setTag(R.drawable.ic_chat_open);
                    imageSend.setImageResource(R.drawable.ic_chat_open);
                }
            }
        });
        // step8
        editInput.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imageSend.setTag(R.drawable.ic_chat_open);
                    imageSend.setImageResource(R.drawable.ic_chat_open);
                }
            }
        });
    }

    private final void toogleKeybord() {
        final InputMethodManager imm = (InputMethodManager) getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**********************************************************************************************/

    private OnInputChangeListener mOnInputChangeListener;

    public void setOnInputChangeListener(OnInputChangeListener mOnInputChangeListener) {
        this.mOnInputChangeListener = mOnInputChangeListener;
    }

    public interface OnInputChangeListener {

        void onInputOpen(int maxHeight, int minHeight);

        void onInputShut(int maxHeight, int minHeight);
    }
}