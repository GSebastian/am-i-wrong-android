package studio.roboto.hack24.questions;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.google.firebase.database.DatabaseReference;
import com.tiagosantos.enchantedviewpager.EnchantedViewPager;

import studio.roboto.hack24.firebase.FirebaseConnector;
import studio.roboto.hack24.firebase.models.Question;
import studio.roboto.hack24.localstorage.SharedPrefsManager;
import studio.roboto.hack24.mycontent.QuestionsIPostedRVAdapter;

/**
 * Created by jordan on 18/03/17.
 */

public class QuestionAnswersPA extends QuestionFragmentPagerAdapter {

    private ChangeNotifier mNotifier;

    public QuestionAnswersPA(FragmentManager manager,
                             Context context,
                             EnchantedViewPager enchantedViewPager, ChangeNotifier notifier) {
        super(manager, context, enchantedViewPager);
        this.mNotifier = notifier;

    }

    @Override
    public boolean shouldAdd(Question question) {
        return !SharedPrefsManager.sharedInstance.isQuestionRemoved(question.id) &&
                SharedPrefsManager.sharedInstance.haveIAnsweredQuestion(question.id);
    }

    @Override
    public void added(Question question) {
        mNotifier.countChanged(getCount());
    }

    @Override
    public boolean reverseOrder() {
        return true;
    }

    @Override
    public boolean showNew() {
        return false;
    }

    @Override
    public DatabaseReference getRef() {
        return FirebaseConnector.getQuestions();
    }

    public interface ChangeNotifier {
        void countChanged(int count);
    }
}
