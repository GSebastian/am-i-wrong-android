package studio.roboto.hack24.questions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tiagosantos.enchantedviewpager.EnchantedViewPager;

import studio.roboto.hack24.R;
import studio.roboto.hack24.firebase.FirebaseConnector;
import studio.roboto.hack24.firebase.models.Question;
import studio.roboto.hack24.localstorage.SharedPrefsManager;
import studio.roboto.hack24.questions.viewholder.YesNoCallback;

/**
 * Created by jordan on 18/03/17.
 */

public class QuestionElementFragment extends Fragment implements YesNoCallback, View.OnClickListener, TextWatcher {

    private int mPosition;
    private int totalScroll = 0;
    private Question mQuestion;
    private boolean isUnlocked = false;

    private EditText mEtComment;
    private ImageView mEtDivider;
    private ImageView mEtBackground;
    private Button mEtSend;
    private RelativeLayout mRlMain;
    private LinearLayoutManager mLayoutManager;
    private TextView mTvQuestionTitle;
    private RecyclerView mRvMain;
    private QuestionRVAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_question_element, container, false);

        if (getArgs(v)) {
            findViews(v);
            initViews();

            return v;
        } else {
            return inflater.inflate(R.layout.fragment_question_element_error, container, false);
        }
    }

    private boolean getArgs(View v) {
        if (getArguments() != null) {
            int position = getArguments().getInt("KEY", -1);
            if (position != -1) {
                mPosition = position;
                v.setTag(EnchantedViewPager.ENCHANTED_VIEWPAGER_POSITION + position);
            }
            String questionId = getArguments().getString("QUESTION_ID", null);
            if (questionId != null) {
                mQuestion = new Question(
                        getArguments().getString("QUESTION_TEXT", null),
                        getArguments().getLong("QUESTION_TIMESTAMP", 0L),
                        getArguments().getLong("QUESTION_YES", 0L),
                        getArguments().getLong("QUESTION_NO", 0L));
                mQuestion.id = questionId;
                return true;
            }
        }
        return false;
    }

    private void findViews(View v) {
        mTvQuestionTitle = (TextView) v.findViewById(R.id.tvQuestionTitle);
        mRvMain = (RecyclerView) v.findViewById(R.id.rvMain);
        mRlMain = (RelativeLayout) v.findViewById(R.id.rlMain);

        mEtComment = (EditText) v.findViewById(R.id.etContent);
        mEtDivider = (ImageView) v.findViewById(R.id.etDivider);
        mEtBackground = (ImageView) v.findViewById(R.id.etBackground);
        mEtSend = (Button) v.findViewById(R.id.tvSend);
    }

    private void initViews() {
        mAdapter = new QuestionRVAdapter(getContext(), SharedPrefsManager.sharedInstance.whatDidIAnswer(mQuestion.id), this, mQuestion.id, mQuestion);
        mLayoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return isUnlocked && super.canScrollVertically();
            }
        };
        mRvMain.setLayoutManager(mLayoutManager);
        mRvMain.setAdapter(mAdapter);
        mRvMain.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalScroll += dy;
                float alpha = 1.0f - (2f * ((float)totalScroll / (float)heightOfText()));
                mTvQuestionTitle.setAlpha(alpha);
            }
        });
        mTvQuestionTitle.setText(mQuestion.text);

        SharedPrefsManager.VOTED type = SharedPrefsManager.sharedInstance.whatDidIAnswer(mQuestion.id);
        if (type != SharedPrefsManager.VOTED.UNANSWERED) {
            clickedAnswer(type == SharedPrefsManager.VOTED.YES);
            mEtSend.setEnabled(false);
        }

        // Comment text
        mEtSend.setOnClickListener(this);
        mEtComment.addTextChangedListener(this);
    }

    public void clickedAnswer(boolean wasYes) {
        mAdapter.itemClicked(wasYes);
        SharedPrefsManager.sharedInstance.addAnsweredQuestionId(mQuestion.id, wasYes);
        isUnlocked = true;

        fadeIn(mEtBackground);
        fadeIn(mEtComment);
        fadeIn(mEtDivider);
        fadeIn(mEtSend);
    }
    private void fadeIn(View v) {
        v.setVisibility(View.VISIBLE);
        v.animate()
                .alpha(1.0f)
                .setDuration(400L)
                .start();
    }

    @Override
    public void onDestroy() {
        mAdapter.stop();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v == mEtSend) {
            SharedPrefsManager.VOTED vote = SharedPrefsManager.sharedInstance.whatDidIAnswer(mQuestion.id);
            if (vote != SharedPrefsManager.VOTED.UNANSWERED && mEtSend.getText().toString().trim().length() >= 1) {
                mEtSend.setEnabled(false);
                FirebaseConnector.addComment(mQuestion.id, mEtComment.getText().toString().trim(), vote == SharedPrefsManager.VOTED.YES);
                mEtComment.setText("");
                mLayoutManager.scrollToPosition(2);
            }
        }
    }

    //region Callbacks YesNoCallback
    @Override
    public void clickedYes() {
        clickedAnswer(true);
    }

    @Override
    public void clickedNo() {
        clickedAnswer(false);
    }

    @Override
    public int heightOfFrag() {
        return mRlMain.getHeight();
    }

    @Override
    public int heightOfText() {
        return mTvQuestionTitle.getHeight();
    }
    //endregion

    //region Callbacks TextWatcher
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mEtSend.setEnabled(s.length() != 0);
    }
    //endregion
}

