package studio.roboto.hack24.localstorage;

import java.util.List;

public interface ISharedPrefsManager {

    public void setCurrentName(String newName);

    public String getCurrentName();

    public void addMyQuestionId(String questionId);

    public List<String> getMyQuestionIds();

    public void addAnsweredQuestionId(String questionId);

    public List<String> getAnsweredQuestionsIds();
}
