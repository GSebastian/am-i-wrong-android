package studio.roboto.hack24.questions;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import studio.roboto.hack24.firebase.FirebaseConnector;
import studio.roboto.hack24.firebase.models.Question;
import studio.roboto.hack24.localstorage.SharedPrefsManager;

/**
 * Created by jordan on 18/03/17.
 */

public  abstract class QuestionFragmentPagerAdapter extends FragmentStatePagerAdapter implements ChildEventListener {

    private Context mContext;
    private List<Question> questions = new ArrayList<>();

    private DatabaseReference mQuestionRef;

    private OnQuestionAddedListener mOnQuestionAddedListener;

    public QuestionFragmentPagerAdapter(FragmentManager manager, Context context) {
        super(manager);
        this.mContext = context;
        this.mQuestionRef = getRef();
        this.mQuestionRef.addChildEventListener(this);
    }

    @Override
    public int getCount() {
        return (showNew() ? 1 : 0) + questions.size();
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0 && showNew()) {
            NewQuestionFragment newQuestionFragment = new NewQuestionFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("KEY", position);
            newQuestionFragment.setArguments(bundle);

            newQuestionFragment.setOnQuestionAddedListener(mOnQuestionAddedListener);

            return newQuestionFragment;
        } else {
            position -= (showNew() ? 1 : 0);

            QuestionElementFragment frag = new QuestionElementFragment();
            Bundle b = new Bundle();
            b.putInt("KEY", position + (showNew() ? 1 : 0));
            b.putString("QUESTION_ID", questions.get(position).id);
            b.putString("QUESTION_TEXT", questions.get(position).text);
            b.putLong("QUESTION_TIMESTAMP", questions.get(position).timestamp);
            b.putLong("QUESTION_YES", questions.get(position).yes);
            b.putLong("QUESTION_NO", questions.get(position).no);
            frag.setArguments(b);

            return frag;
        }
    }

    public void stop() {
        mQuestionRef.removeEventListener(this);
    }

	public void setOnQuestionAddedListener(OnQuestionAddedListener onQuestionAddedListener) {
        this.mOnQuestionAddedListener = onQuestionAddedListener;
    }


    public abstract boolean shouldAdd(Question question);

    public abstract boolean showNew();

    public abstract DatabaseReference getRef();

    //region Callbacks ChildEventListener
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
            Question question = dataSnapshot.getValue(Question.class);
            question.id = dataSnapshot.getKey();
            if (shouldAdd(question)) {
                questions.add(question);
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
    //endregion
}