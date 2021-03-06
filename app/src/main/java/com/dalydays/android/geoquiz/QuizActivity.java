package com.dalydays.android.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {

    private Button mTrueButton;
    private Button mFalseButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private Button mCheatButton;
    private TextView mQuestionTextView;
    private TextView mCheatsRemainingTextView;

    private final Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
            new Question(R.string.question_australia, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_oceans, true)
    };

    private final boolean[] isQuestionAnswered = new boolean[mQuestionBank.length];
    private boolean[] mIsCheater = new boolean[mQuestionBank.length];
    private float score = 0;
    private int mCurrentIndex = 0;
    private int mCheatTokens = 3;

    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_IS_CHEATER = "is_cheater";
    private static final int REQUEST_CODE_CHEAT = 0;
    private static final String KEY_CHEAT_TOKENS = "cheat_tokens";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            if (CheatActivity.wasAnswerShown(data)) {
                mIsCheater[mCurrentIndex] = true;
                mCheatTokens--;
                updateCheatsRemaining();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");

        // restore saved index
        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX);
            mIsCheater = savedInstanceState.getBooleanArray(KEY_IS_CHEATER);
            mCheatTokens = savedInstanceState.getInt(KEY_CHEAT_TOKENS);
        }

        setContentView(R.layout.activity_quiz);

        mTrueButton = findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
            }
        });

        mFalseButton = findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
            }
        });

        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // reset cheater status
                changeQuestion(1);
            }
        });

        mPrevButton = findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeQuestion(-1);
            }
        });

        mCheatButton = findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start CheatActivity
                Intent intent = CheatActivity.newIntent(QuizActivity.this, mQuestionBank[mCurrentIndex].isAnswerTrue());
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        mQuestionTextView = findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeQuestion(1);
            }
        });
        updateQuestion();

        mCheatsRemainingTextView = findViewById(R.id.cheats_remaining_text_view);
        updateCheatsRemaining();
    }

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);

        toggleAnswerButtons();
    }

    private void changeQuestion(int increment) {
        mCurrentIndex = (mCurrentIndex + increment) % mQuestionBank.length;
        if (mCurrentIndex < 0) {
            mCurrentIndex += mQuestionBank.length;
        }
        updateQuestion();
    }

    private void checkAnswer(boolean userPressedTrue) {
        // mark this question as answered and disable the true/false buttons
        isQuestionAnswered[mCurrentIndex] = true;
        toggleAnswerButtons();

        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        int messageResId;

        if (mIsCheater[mCurrentIndex]) {
            messageResId = R.string.judgment_toast;
        }
        else {
            if (userPressedTrue == answerIsTrue) {
                score++;
                messageResId = R.string.correct_toast;
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();

        // Check if all questions are answered, then show a Toast with the percentage score
        // count the remaining questions to answer
        int remainingQuestionCount = 0;
        for (boolean b : isQuestionAnswered) {
            if (!b) {
                remainingQuestionCount++;
            }
        }
        // if no questions remain, toast the score
        if (remainingQuestionCount == 0) {
            int percentScore = (int) ((score / mQuestionBank.length) * 100);
            Toast.makeText(this, getString(R.string.percent_score, percentScore), Toast.LENGTH_LONG).show();
        }
    }

    private void toggleAnswerButtons() {
        // disable answer buttons if this question was already answered, otherwise re-enable them
        if (isQuestionAnswered[mCurrentIndex]) {
            mTrueButton.setEnabled(false);
            mFalseButton.setEnabled(false);
        }
        else {
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
        }
    }

    private void updateCheatsRemaining() {
        mCheatsRemainingTextView.setText(mCheatTokens + " Cheats Remaining");
        if (mCheatTokens <= 0) {
            mCheatButton.setEnabled(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState(Bundle) called");
        outState.putInt(KEY_INDEX, mCurrentIndex);
        outState.putBooleanArray(KEY_IS_CHEATER, mIsCheater);
        outState.putInt(KEY_CHEAT_TOKENS, mCheatTokens);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }
}
