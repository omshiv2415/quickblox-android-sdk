package com.quickblox.sample.gcmchecker.main.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.quickblox.sample.gcmchecker.R;
import com.quickblox.sample.gcmchecker.main.Consts;
import com.quickblox.sample.gcmchecker.main.TestResultsAdapter;
import com.quickblox.sample.gcmchecker.main.models.ResultTests;

import java.util.ArrayList;

/**
 * Created by tereha on 24.09.15.
 */
public class ResultTestsActivity extends Activity{


    private ListView resultsTestsLV;
    private ArrayList<ResultTests> resultsSelectedServerserver;
    private TestResultsAdapter testResultsAdapter;
    private String selectedServerTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);

        initUI();
    }

    private void initUI() {
        selectedServerTitle = getIntent().getStringExtra(Consts.EXTRA_SERVER_TITLE);
        resultsSelectedServerserver = CheckerActivity.resultsMap.get(selectedServerTitle);
        testResultsAdapter = new TestResultsAdapter(this, resultsSelectedServerserver);
        resultsTestsLV = (ListView) findViewById(R.id.resultsTestsLV);
        resultsTestsLV.setAdapter(testResultsAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
