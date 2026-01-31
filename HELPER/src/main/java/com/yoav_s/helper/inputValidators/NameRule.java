package com.yoav_s.helper.inputValidators;

import android.view.View;

public class NameRule extends TextRule{

    public NameRule(View view, RuleOperation operation, String message) {
        super(view, operation, message,
                2,      // minimum length
                20,     // maximum length
                false,  // include numbers
                true); //,   // starts with uppercase
                //null);  // let TextRule generate the regex
    }
}
